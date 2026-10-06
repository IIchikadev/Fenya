'use strict';
const { app, BrowserWindow, Menu, ipcMain, dialog, shell } = require('electron');
const fs = require('fs');
const path = require('path');
const { paths, readSettings, writeSettings, MC_VERSION, FABRIC_LOADER } = require('./core/paths');
const install = require('./core/install');
const launcher = require('./core/launch');
const mods = require('./core/mods');
const updates = require('./core/updates');

let window = null;
let gameProcess = null;
let preparing = false;
const bundle = require('./core/bundle');

function createWindow() {
  if (app.isPackaged) Menu.setApplicationMenu(null);
  window = new BrowserWindow({
    width: 1120,
    height: 720,
    minWidth: 960,
    minHeight: 640,
    frame: false,
    icon: path.join(__dirname, 'ui', 'assets', 'icon.ico'),
    title: 'Socket Client',
    backgroundColor: '#08090b',
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  window.loadFile(path.join(__dirname, 'ui', 'index.html'));
  if (app.isPackaged) {
    // в релизе не пускаем в инструменты разработчика
    window.webContents.on('before-input-event', (event, input) => {
      const devtools = input.key === 'F12'
        || (input.control && input.shift && ['I', 'J', 'C'].includes(String(input.key).toUpperCase()));
      if (devtools) event.preventDefault();
    });
    window.webContents.on('devtools-opened', () => window.webContents.closeDevTools());
  }
  window.once('ready-to-show', () => {
    window.show();
    checkCoreUpdate();
  });
  window.on('closed', () => {
    window = null;
  });
}

function send(channel, payload) {
  if (window && !window.isDestroyed()) window.webContents.send(channel, payload);
}

/** Ищет собранный жар клиента рядом с лоадером — чтобы ставить ядро без ручного выбора. */
function findCoreJar() {
  const bundled = bundle.coreJar();
  // версия, выбранная или скачанная из релизов, важнее локальной сборки
  const selected = updates.selectedJar();
  if (selected && (!bundled || updates.compareVersions(readSettings().coreVersion, bundle.version()) > 0)) return selected;
  if (bundled) return bundled;
  const candidates = [
    path.join(app.getAppPath(), '..', 'SocketClient', 'build', 'libs'),
    path.join(app.getAppPath(), 'resources', 'core'),
    path.join(process.resourcesPath || '', 'core'),
  ];
  for (const directory of candidates) {
    if (!fs.existsSync(directory)) continue;
    const jar = fs
      .readdirSync(directory)
      .filter(file => /^socket-client.*\.jar$/i.test(file) && !/sources/i.test(file))
      .map(file => path.join(directory, file))
      .sort((first, second) => fs.statSync(second).mtimeMs - fs.statSync(first).mtimeMs)
      .shift();
    if (jar) return jar;
  }
  return null;
}

/** Проверка обновлений ядра при запуске: при автообновлении ставим сразу, иначе сообщаем в окно. */
async function checkCoreUpdate() {
  try {
    const result = await updates.checkForUpdate();
    if (result.available && readSettings().autoUpdateCore && !preparing && !gameProcess) {
      await updates.installRelease(result.latest, progress => send('install:progress', progress));
      send('updates:state', Object.assign({}, result, { available: false, current: result.latest.tag, installed: true }));
      return;
    }
    send('updates:state', result);
  } catch (error) {
    send('updates:state', { error: error.message });
  }
}

app.whenReady().then(() => {
  bundle.initialize();
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

ipcMain.handle('window:action', (event, action) => {
  if (!window) return;
  if (action === 'minimize') window.minimize();
  if (action === 'close') window.close();
  if (action === 'maximize') (window.isMaximized() ? window.unmaximize() : window.maximize());
});

ipcMain.handle('state:get', () => ({
  settings: readSettings(),
  installed: install.isInstalled(),
  version: MC_VERSION,
  loader: FABRIC_LOADER,
  java: launcher.findJava(),
  javaList: launcher.candidateJavaDirs(),
  coreJar: findCoreJar(),
  coreInstalled: mods.hasCore(),
  coreVersion: readSettings().coreVersion || null,
  paths: { root: paths.root, instance: paths.instance, mods: paths.mods },
}));

ipcMain.handle('settings:save', (event, patch) => writeSettings(patch));

ipcMain.handle('install:run', async () => {
  if (preparing || gameProcess) throw new Error('Игра или установка уже запущена');
  preparing = true;
  try {
  await install.install(progress => send('install:progress', progress), findCoreJar());
  return { installed: install.isInstalled(), coreInstalled: mods.hasCore() };
  } finally { preparing = false; }
});

ipcMain.handle('game:launch', async () => {
  if (gameProcess && gameProcess.exitCode === null) throw new Error('Игра уже запущена');
  if (preparing) throw new Error('Preparation already running');
  preparing = true;
  try {
  bundle.installDependencies();
  await require('./core/runtime').ensureJava(progress => send('install:progress', progress));
  const shaders = require('./core/shaders');
  if (!shaders.isInstalled()) await shaders.install(progress => send('install:progress', progress));
  if (!install.isInstalled()) {
    await install.install(progress => send('install:progress', progress), findCoreJar());
  }
  // ядро могло быть пересобрано после установки — подкладываем свежий жар
  const sync = mods.syncCore(findCoreJar());
  if (sync.updated) send('game:log', `[loader] ${sync.reason}`);
  gameProcess = await launcher.launch(line => send('game:log', line));
  send('game:state', { running: true });
  gameProcess.on('exit', code => {
    gameProcess = null;
    send('game:state', { running: false, code });
  });
  const settings = readSettings();
  if (settings.closeOnLaunch) setTimeout(() => app.quit(), 8000);
  return { running: true };
  } finally { preparing = false; }
});

ipcMain.handle('updates:check', async () => {
  try {
    return await updates.checkForUpdate();
  } catch (error) {
    return { error: error.message };
  }
});

ipcMain.handle('updates:install', async (event, tag) => {
  if (preparing || gameProcess) throw new Error('Дождитесь завершения игры или установки');
  const { releases } = await updates.checkForUpdate();
  const release = releases.find(item => item.tag === tag);
  if (!release) throw new Error(`версия ${tag} не найдена в релизах`);
  await updates.installRelease(release, progress => send('install:progress', progress));
  return { coreVersion: release.tag };
});

ipcMain.handle('mods:list', () => mods.list());
ipcMain.handle('mods:add', (event, files) => mods.add(files || []));
ipcMain.handle('mods:toggle', (event, file) => mods.toggle(file));
ipcMain.handle('mods:remove', (event, file) => mods.remove(file));
ipcMain.handle('mods:installCore', () => mods.installCore(findCoreJar()));
ipcMain.handle('mods:openFolder', () => shell.openPath(paths.mods));

ipcMain.handle('dialog:pickMods', async () => {
  const result = await dialog.showOpenDialog(window, {
    title: 'Выберите моды',
    filters: [{ name: 'Fabric-моды', extensions: ['jar'] }],
    properties: ['openFile', 'multiSelections'],
  });
  return result.canceled ? [] : mods.add(result.filePaths);
});

ipcMain.handle('dialog:pickJava', async () => {
  const result = await dialog.showOpenDialog(window, {
    title: 'Выберите javaw.exe',
    filters: [{ name: 'Java', extensions: ['exe'] }],
    properties: ['openFile'],
  });
  if (result.canceled || result.filePaths.length === 0) return null;
  writeSettings({ javaPath: result.filePaths[0] });
  return result.filePaths[0];
});
