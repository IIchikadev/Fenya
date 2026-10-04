'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { spawn } = require('child_process');
const { paths, ensureDir, MC_VERSION, readSettings } = require('./paths');
const vanilla = require('./vanilla');
const fabric = require('./fabric');

/** UUID офлайн-игрока — как в ванильном сервере: UUID v3 от "OfflinePlayer:<ник>". */
function offlineUuid(nickname) {
  const hash = crypto.createHash('md5').update(`OfflinePlayer:${nickname}`, 'utf8').digest();
  hash[6] = (hash[6] & 0x0f) | 0x30;
  hash[8] = (hash[8] & 0x3f) | 0x80;
  const hex = hash.toString('hex');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function candidateJavaDirs() {
  const roots = ['C:\\Program Files\\Microsoft', 'C:\\Program Files\\Java', 'C:\\Program Files\\Eclipse Adoptium', 'C:\\Program Files\\Zulu'];
  const found = [];
  for (const root of roots) {
    if (!fs.existsSync(root)) continue;
    for (const entry of fs.readdirSync(root)) {
      if (!/jdk|jre/i.test(entry)) continue;
      const javaw = path.join(root, entry, 'bin', 'javaw.exe');
      if (fs.existsSync(javaw)) found.push({ path: javaw, version: entry });
    }
  }
  return found;
}

/** Ищет Java 21+: сначала из настроек, потом JAVA_HOME, потом стандартные каталоги. */
function findJava() {
  const settings = readSettings();
  if (settings.javaPath && fs.existsSync(settings.javaPath)) return settings.javaPath;
  if (process.env.JAVA_HOME) {
    const javaw = path.join(process.env.JAVA_HOME, 'bin', 'javaw.exe');
    if (fs.existsSync(javaw)) return javaw;
  }
  const candidates = candidateJavaDirs();
  const modern = candidates.filter(candidate => /(-|\b)(2[1-9]|[3-9]\d)/.test(candidate.version));
  const chosen = modern[0] || candidates[0];
  return chosen ? chosen.path : null;
}

function artifactKey(name) {
  const [group, artifact] = String(name).split(':');
  return `${group}:${artifact}`;
}

function classpath(versionJson, profile) {
  const entries = [];
  const seen = new Set();
  // библиотеки Fabric идут первыми и перекрывают ванильные (иначе конфликт версий ASM)
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

function gameArguments(versionJson, settings, assetsIndex) {
  const template = {
    auth_player_name: settings.nickname,
    version_name: MC_VERSION,
    game_directory: paths.instance,
    assets_root: paths.assets,
    assets_index_name: assetsIndex,
    auth_uuid: offlineUuid(settings.nickname),
    auth_access_token: '0',
    clientid: '0',
    auth_xuid: '0',
    user_type: 'legacy',
    version_type: 'Socket',
    resolution_width: '1280',
    resolution_height: '720',
  };
  const raw = (versionJson.arguments && versionJson.arguments.game) || [];
  const result = [];
  for (const item of raw) {
    if (typeof item !== 'string') continue;
    result.push(item.replace(/\$\{(\w+)\}/g, (match, key) => (key in template ? String(template[key]) : match)));
  }
  return result;
}

async function launch(onLog) {
  const settings = readSettings();
  const java = findJava();
  if (!java) throw new Error('Не найдена Java 21. Укажите путь к javaw.exe в настройках.');
  const versionJson = await vanilla.fetchVersionJson();
  const profile = await fabric.fetchFabricProfile();
  ensureDir(paths.instance);
  ensureDir(paths.mods);
  const args = [
    `-Xmx${settings.memory}M`,
    `-Xms${Math.max(512, Math.floor(settings.memory / 4))}M`,
    '-XX:+UnlockExperimentalVMOptions',
    '-XX:+UseG1GC',
    '-XX:G1NewSizePercent=20',
    '-XX:MaxGCPauseMillis=50',
    '-Dfile.encoding=UTF-8',
    `-Djava.library.path=${paths.natives}`,
    `-Dminecraft.launcher.brand=socket-loader`,
    '-cp',
    classpath(versionJson, profile),
    profile.mainClass,
    ...gameArguments(versionJson, settings, versionJson.assetIndex.id),
  ];
  if (settings.fullscreen) args.push('--fullscreen');
  const child = spawn(java, args, { cwd: paths.instance, detached: false });
  child.stdout.on('data', chunk => onLog && onLog(chunk.toString()));
  child.stderr.on('data', chunk => onLog && onLog(chunk.toString()));
  return child;
}

module.exports = { launch, findJava, candidateJavaDirs, offlineUuid };
