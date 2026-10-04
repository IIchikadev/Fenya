'use strict';
const { contextBridge, ipcRenderer, webUtils } = require('electron');

contextBridge.exposeInMainWorld('loader', {
  windowAction: action => ipcRenderer.invoke('window:action', action),
  getState: () => ipcRenderer.invoke('state:get'),
  saveSettings: patch => ipcRenderer.invoke('settings:save', patch),
  runInstall: () => ipcRenderer.invoke('install:run'),
  launch: () => ipcRenderer.invoke('game:launch'),
  mods: {
    list: () => ipcRenderer.invoke('mods:list'),
    add: files => ipcRenderer.invoke('mods:add', files),
    toggle: file => ipcRenderer.invoke('mods:toggle', file),
    remove: file => ipcRenderer.invoke('mods:remove', file),
    installCore: () => ipcRenderer.invoke('mods:installCore'),
    openFolder: () => ipcRenderer.invoke('mods:openFolder'),
    pick: () => ipcRenderer.invoke('dialog:pickMods'),
  },
  pickJava: () => ipcRenderer.invoke('dialog:pickJava'),
  updates: {
    check: () => ipcRenderer.invoke('updates:check'),
    install: tag => ipcRenderer.invoke('updates:install', tag),
  },
  onUpdateState: handler => ipcRenderer.on('updates:state', (event, payload) => handler(payload)),
  // путь перетащенного файла в Electron 32+ доступен только через webUtils
  pathForFile: file => {
    try {
      return webUtils.getPathForFile(file);
    } catch {
      return file.path || null;
    }
  },
  onInstallProgress: handler => ipcRenderer.on('install:progress', (event, payload) => handler(payload)),
  onGameLog: handler => ipcRenderer.on('game:log', (event, payload) => handler(payload)),
  onGameState: handler => ipcRenderer.on('game:state', (event, payload) => handler(payload)),
});
