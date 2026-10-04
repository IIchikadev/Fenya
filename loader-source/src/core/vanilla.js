'use strict';
const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const { paths, ensureDir, MC_VERSION } = require('./paths');
const { getJson, download, downloadAll } = require('./download');

const MANIFEST = 'https://launchermeta.mojang.com/mc/game/version_manifest_v2.json';
const RESOURCES = 'https://resources.download.minecraft.net';

function ruleAllows(rules) {
  if (!rules || rules.length === 0) return true;
  let allowed = false;
  for (const rule of rules) {
    let matches = true;
    if (rule.os && rule.os.name) matches = rule.os.name === 'windows';
    if (rule.os && rule.os.arch) matches = matches && rule.os.arch === (process.arch === 'ia32' ? 'x86' : 'x64');
    if (matches) allowed = rule.action === 'allow';
  }
  return allowed;
}

function libraryAllowed(library) {
  return ruleAllows(library.rules);
}

function isNative(library) {
  return typeof library.name === 'string' && library.name.includes(':natives-');
}

function nativeMatchesWindows(library) {
  return library.name.includes('natives-windows');
}

function libraryTargets(library) {
  const artifact = library.downloads && library.downloads.artifact;
  if (!artifact || !artifact.path) return null;
  return {
    url: artifact.url,
    target: path.join(paths.libraries, artifact.path.split('/').join(path.sep)),
    sha1: artifact.sha1,
    name: library.name,
  };
}

/** Загружает и кеширует version.json ванильной 1.21.4. */
async function fetchVersionJson() {
  if (fs.existsSync(paths.versionJson())) {
    return JSON.parse(fs.readFileSync(paths.versionJson(), 'utf8'));
  }
  const manifest = await getJson(MANIFEST);
  const entry = manifest.versions.find(version => version.id === MC_VERSION);
  if (!entry) throw new Error(`Версия ${MC_VERSION} не найдена в манифесте Mojang`);
  const versionJson = await getJson(entry.url);
  ensureDir(path.dirname(paths.versionJson()));
  fs.writeFileSync(paths.versionJson(), JSON.stringify(versionJson), 'utf8');
  return versionJson;
}

/** Список всех файлов ванильной части: клиент, библиотеки, нативы. */
function collectVanillaTasks(versionJson) {
  const tasks = [];
  tasks.push({
    url: versionJson.downloads.client.url,
    target: paths.clientJar(),
    sha1: versionJson.downloads.client.sha1,
    name: `minecraft-${MC_VERSION}.jar`,
  });
  for (const library of versionJson.libraries) {
    if (!libraryAllowed(library)) continue;
    if (isNative(library) && !nativeMatchesWindows(library)) continue;
    const task = libraryTargets(library);
    if (task) tasks.push(task);
  }
  return tasks;
}

async function extractNatives(versionJson) {
  const target = ensureDir(paths.natives);
  for (const library of versionJson.libraries) {
    if (!libraryAllowed(library) || !isNative(library) || !nativeMatchesWindows(library)) continue;
    const task = libraryTargets(library);
    if (!task || !fs.existsSync(task.target)) continue;
    const zip = new AdmZip(task.target);
    for (const entry of zip.getEntries()) {
      if (entry.isDirectory) continue;
      const name = path.basename(entry.entryName);
      if (!/\.(dll|so|dylib)$/i.test(name)) continue;
      const file = path.join(target, name);
      if (!fs.existsSync(file)) fs.writeFileSync(file, entry.getData());
    }
  }
  return target;
}

/** Скачивает индекс ассетов и сами объекты. */
async function installAssets(versionJson, onProgress) {
  const index = versionJson.assetIndex;
  const indexFile = path.join(paths.assets, 'indexes', `${index.id}.json`);
  await download(index.url, indexFile, index.sha1);
  const objects = JSON.parse(fs.readFileSync(indexFile, 'utf8')).objects;
  const tasks = Object.values(objects).map(object => ({
    url: `${RESOURCES}/${object.hash.slice(0, 2)}/${object.hash}`,
    target: path.join(paths.assets, 'objects', object.hash.slice(0, 2), object.hash),
    sha1: object.hash,
    name: 'assets',
  }));
  await downloadAll(tasks, onProgress, 16);
  return index.id;
}

module.exports = { fetchVersionJson, collectVanillaTasks, extractNatives, installAssets, libraryAllowed, isNative, nativeMatchesWindows, libraryTargets };
