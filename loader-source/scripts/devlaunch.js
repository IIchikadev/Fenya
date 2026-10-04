'use strict';
// Разовый запуск клиента сразу в мир — чтобы посмотреть HUD без ручной навигации по меню.
const path = require('path');
const { spawn } = require('child_process');
const root = 'C:\\Users\\fedor\\OneDrive\\Документы\\socket dev\\socket-loader\\src\\core';
const { paths, ensureDir, MC_VERSION, readSettings } = require(path.join(root, 'paths'));
const vanilla = require(path.join(root, 'vanilla'));
const fabric = require(path.join(root, 'fabric'));
const { findJava, offlineUuid } = require(path.join(root, 'launch'));

function artifactKey(name) {
  const [group, artifact] = String(name).split(':');
  return `${group}:${artifact}`;
}

function classpath(versionJson, profile) {
  const entries = [];
  const seen = new Set();
  for (const library of profile.libraries) {
    seen.add(artifactKey(library.name));
    entries.push(path.join(paths.libraries, fabric.mavenPath(library.name).split('/').join(path.sep)));
  }
  for (const library of versionJson.libraries) {
    if (!vanilla.libraryAllowed(library) || vanilla.isNative(library)) continue;
    if (seen.has(artifactKey(library.name))) continue;
    const task = vanilla.libraryTargets(library);
    if (task) {
      seen.add(artifactKey(library.name));
      entries.push(task.target);
    }
  }
  entries.push(paths.clientJar());
  return [...new Set(entries)].join(';');
}

(async () => {
  const settings = readSettings();
  const java = findJava();
  if (!java) throw new Error('Java 21 не найдена');
  const versionJson = await vanilla.fetchVersionJson();
  const profile = await fabric.fetchFabricProfile();
  ensureDir(paths.instance);
  const template = {
    auth_player_name: settings.nickname,
    version_name: MC_VERSION,
    game_directory: paths.instance,
    assets_root: paths.assets,
    assets_index_name: versionJson.assetIndex.id,
    auth_uuid: offlineUuid(settings.nickname),
    auth_access_token: '0',
    clientid: '0',
    auth_xuid: '0',
    user_type: 'legacy',
    version_type: 'Socket',
    resolution_width: '1280',
    resolution_height: '720',
  };
  const gameArgs = [];
  for (const item of (versionJson.arguments && versionJson.arguments.game) || []) {
    if (typeof item !== 'string') continue;
    gameArgs.push(item.replace(/\$\{(\w+)\}/g, (m, k) => (k in template ? String(template[k]) : m)));
  }
  gameArgs.push('--quickPlaySingleplayer', process.argv[2] || 'New World');
  const args = [
    `-Xmx${settings.memory}M`,
    `-Xms1024M`,
    '-Dfile.encoding=UTF-8',
    `-Djava.library.path=${paths.natives}`,
    '-Dminecraft.launcher.brand=socket-loader',
    '-cp',
    classpath(versionJson, profile),
    profile.mainClass,
    ...gameArgs,
  ];
  const child = spawn(java, args, { cwd: paths.instance, detached: false });
  child.stdout.on('data', c => process.stdout.write(c.toString()));
  child.stderr.on('data', c => process.stdout.write(c.toString()));
  child.on('exit', code => console.log('[exit]', code));
})();
