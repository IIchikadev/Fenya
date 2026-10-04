'use strict';
const fs = require('fs');
const path = require('path');
const { paths, ensureDir, MC_VERSION, FABRIC_LOADER } = require('./paths');
const { downloadAll } = require('./download');
const vanilla = require('./vanilla');
const fabric = require('./fabric');
const mods = require('./mods');

/** Полная установка инстанса: ванилла → Fabric → нативы → ассеты → Fabric API → ядро клиента. */
async function install(onStage, coreJarPath) {
  const stage = (text, percent) => onStage && onStage({ text, percent });
  ensureDir(paths.instance);
  ensureDir(paths.mods);

  stage('Читаю манифест Mojang', 2);
  const versionJson = await vanilla.fetchVersionJson();

  stage('Читаю профиль Fabric', 5);
  const profile = await fabric.fetchFabricProfile();

  const vanillaTasks = vanilla.collectVanillaTasks(versionJson);
  const fabricTasks = fabric.collectFabricTasks(profile);
  const tasks = [...vanillaTasks, ...fabricTasks];
  await downloadAll(tasks, (done, total, name) => {
    stage(`Клиент и библиотеки: ${name}`, 5 + Math.round((done / total) * 35));
  });

  stage('Распаковываю нативные библиотеки', 42);
  await vanilla.extractNatives(versionJson);

  await vanilla.installAssets(versionJson, (done, total) => {
    stage(`Ассеты игры: ${done} из ${total}`, 45 + Math.round((done / total) * 45));
  });

  stage('Ставлю Fabric API', 92);
  await fabric.installFabricApi();

  if (coreJarPath && fs.existsSync(coreJarPath)) {
    stage('Ставлю ядро Socket Client', 96);
    mods.installCore(coreJarPath);
  }

  writeMarker();
  stage('Готово', 100);
  return { version: MC_VERSION, loader: FABRIC_LOADER };
}

function markerPath() {
  return path.join(paths.root, `installed-${MC_VERSION}-${FABRIC_LOADER}.json`);
}

function writeMarker() {
  ensureDir(paths.root);
  fs.writeFileSync(markerPath(), JSON.stringify({ installedAt: Date.now(), version: MC_VERSION, loader: FABRIC_LOADER }), 'utf8');
}

function isInstalled() {
  return fs.existsSync(markerPath()) && fs.existsSync(paths.clientJar());
}

module.exports = { install, isInstalled };
