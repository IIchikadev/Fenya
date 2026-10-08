'use strict';
const fs = require('fs');
const path = require('path');
const { paths, readSettings, writeSettings } = require('./paths');
const { getJson, download } = require('./download');
const mods = require('./mods');
const crypto = require('crypto');
const AdmZip = require('adm-zip');
const { MC_VERSION } = require('./paths');

/** Репозиторий, в релизах которого лежит собранное ядро socket-client-*.jar. */
const REPO = 'IIchikadev/Socket';
const CORE_ASSET = /^socket-client-.*\.jar$/i;
const UPDATE_ASSET = /^socket-update\.zip$/i;

function coresDir() {
  return path.join(paths.root, 'cores');
}

function localJar(tag, assetName) {
  if (path.basename(assetName) !== assetName) throw new Error('Invalid update filename');
  return path.join(coresDir(), tag.replace(/[^\w.+-]/g, '_'), assetName);
}

/** Сравнение версий вида v1.2.10 / 1.2.0-beta: числа по порядку, без суффикса старше, чем с ним. */
function compareVersions(left, right) {
  const parse = value => {
    const [main, suffix] = String(value).replace(/^v/i, '').split('-', 2);
    return { numbers: main.split('.').map(part => parseInt(part, 10) || 0), suffix: suffix || '' };
  };
  const a = parse(left);
  const b = parse(right);
  for (let i = 0; i < Math.max(a.numbers.length, b.numbers.length); i++) {
    const diff = (a.numbers[i] || 0) - (b.numbers[i] || 0);
    if (diff !== 0) return diff;
  }
  if (a.suffix === b.suffix) return 0;
  if (!a.suffix) return 1;
  if (!b.suffix) return -1;
  return a.suffix < b.suffix ? -1 : 1;
}

/** Релизы, в которых есть jar ядра, от новых к старым. */
async function listReleases() {
  const releases = await getJson(`https://api.github.com/repos/${REPO}/releases?per_page=30`, 12000);
  return releases
    .filter(release => !release.draft)
    .map(release => {
      const assets = release.assets || [];
      const asset = assets.find(item => UPDATE_ASSET.test(item.name)) || assets.find(item => CORE_ASSET.test(item.name) && !/sources/i.test(item.name));
      if (!asset) return null;
      return {
        tag: release.tag_name,
        name: release.name || release.tag_name,
        prerelease: Boolean(release.prerelease),
        published: release.published_at,
        notes: release.body || '',
        asset: { name: asset.name, url: asset.browser_download_url, size: asset.size, sha256: (asset.digest || '').replace(/^sha256:/, '') },
      };
    })
    .filter(Boolean)
    .sort((first, second) => compareVersions(second.tag, first.tag));
}

/** Скачанный jar выбранной версии, если он есть на диске. */
function selectedJar() {
  const settings = readSettings();
  if (!settings.coreVersion || !settings.coreAsset) return null;
  const jar = localJar(settings.coreVersion, settings.coreAsset);
  return fs.existsSync(jar) ? jar : null;
}

/** Качает ядро релиза, кладёт его в моды и запоминает как текущую версию. */
async function installRelease(release, onProgress) {
  const report = (percent, text) => onProgress && onProgress({ percent, text });
  report(5, `скачиваем ядро ${release.tag}…`);
  let target = localJar(release.tag, release.asset.name);
  if (!release.asset.url.startsWith(`https://github.com/${REPO}/releases/download/`)) throw new Error('Invalid update source');
  await download(release.asset.url, target, release.asset.sha256 || undefined, 'sha256');
  if (UPDATE_ASSET.test(release.asset.name)) {
    const zip = new AdmZip(target);
    const entry = zip.getEntry('update.json');
    if (!entry || entry.header.size > 65536) throw new Error('Update manifest missing');
    const manifest = JSON.parse(entry.getData().toString('utf8'));
    if (manifest.protocol !== 1 || manifest.minecraft !== MC_VERSION || manifest.version !== release.tag) throw new Error('Это обновление требует другую версию лоадера');
    if (!/^socket-client-[\w.+-]+\.jar$/.test(manifest.core) || !/^[a-f0-9]{64}$/.test(manifest.sha256)) throw new Error('Invalid core manifest');
    const core = zip.getEntry(manifest.core);
    if (!core || core.header.size > 150 * 1024 * 1024) throw new Error('Invalid core size');
    const bytes = core.getData();
    if (crypto.createHash('sha256').update(bytes).digest('hex') !== manifest.sha256) throw new Error('Update checksum mismatch');
    const metaEntry = new AdmZip(bytes).getEntry('fabric.mod.json');
    const meta = metaEntry && JSON.parse(metaEntry.getData().toString('utf8'));
    if (!meta || meta.id !== 'socket' || meta.version !== release.tag) throw new Error('Wrong client version in update');
    target = localJar(release.tag, manifest.core);
    fs.writeFileSync(target + '.part', bytes);
    fs.renameSync(target + '.part', target);
  }
  report(80, `устанавливаем ядро ${release.tag}…`);
  mods.installCore(target);
  writeSettings({ coreVersion: release.tag, coreAsset: path.basename(target) });
  report(100, `ядро ${release.tag} установлено`);
  return target;
}

/** Есть ли версия новее установленной (предрелизы — только если разрешены). */
async function checkForUpdate() {
  const settings = readSettings();
  const releases = await listReleases();
  const candidates = releases.filter(release => settings.allowPrerelease || !release.prerelease);
  const latest = candidates[0] || null;
  const current = settings.coreVersion || null;
  const available = Boolean(latest) && (!current || compareVersions(latest.tag, current) > 0);
  return { current, latest, available, releases };
}

module.exports = { REPO, compareVersions, listReleases, selectedJar, installRelease, checkForUpdate };
