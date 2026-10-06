'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { paths, writeSettings, readSettings, ensureDir } = require('./paths');
function directory() {
  const candidates = [path.join(process.resourcesPath || '', 'socket-bundle'), path.resolve(__dirname, '../../bundle')];
  return candidates.find(dir => fs.existsSync(path.join(dir, 'manifest.json')));
}
function available() { return Boolean(directory()); }
function manifest() { return JSON.parse(fs.readFileSync(path.join(directory(), 'manifest.json'), 'utf8')); }
function digest(file) { return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'); }
function checked(entry) {
  const root = directory();
  const file = path.resolve(root, entry.file);
  if (!file.startsWith(path.resolve(root) + path.sep) || digest(file) !== entry.sha256) throw new Error('Повреждён комплект Socket Loader: ' + entry.file);
  return file;
}
function version() { return available() ? manifest().version : ''; }
function coreJar() { return available() ? checked(manifest().files.find(entry => entry.kind === 'core')) : null; }
function initialize() {
  if (available() && (!readSettings().coreVersion || require('./updates').compareVersions(readSettings().coreVersion, version()) < 0)) {
    writeSettings({ coreVersion: version(), coreAsset: '' });
  }
}
function installDependencies() {
  if (!available()) return;
  for (const entry of manifest().files.filter(entry => entry.kind !== 'core')) {
    const source = checked(entry);
    const target = path.join(paths.instance, entry.file);
    ensureDir(path.dirname(target));
    if (!fs.existsSync(target) || digest(target) !== entry.sha256) {
      fs.copyFileSync(source, target + '.part');
      fs.renameSync(target + '.part', target);
    }
  }
  require('./shaders').configure();
}
module.exports = { available, version, coreJar, initialize, installDependencies, digest };
