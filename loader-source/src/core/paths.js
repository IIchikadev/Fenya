'use strict';
const os = require('os');
const path = require('path');
const fs = require('fs');

const MC_VERSION = '1.21.4';
const FABRIC_LOADER = '0.18.4';
const FABRIC_API_VERSION = '0.119.4+1.21.4';

function appRoot() {
  const base = process.env.APPDATA || path.join(os.homedir(), 'AppData', 'Roaming');
  return path.join(base, '.socketclient');
}

const paths = {
  root: appRoot(),
  get instance() {
    return path.join(this.root, 'instance');
  },
  get mods() {
    return path.join(this.instance, 'mods');
  },
  get versions() {
    return path.join(this.root, 'versions');
  },
  get libraries() {
    return path.join(this.root, 'libraries');
  },
  get assets() {
    return path.join(this.root, 'assets');
  },
  get natives() {
    return path.join(this.root, 'natives', MC_VERSION);
  },
  get settings() {
    return path.join(this.root, 'loader-settings.json');
  },
  clientJar() {
    return path.join(this.versions, MC_VERSION, `${MC_VERSION}.jar`);
  },
  versionJson() {
    return path.join(this.versions, MC_VERSION, `${MC_VERSION}.json`);
  },
  fabricJson() {
    return path.join(this.versions, MC_VERSION, `fabric-${FABRIC_LOADER}.json`);
  },
};

function ensureDir(target) {
  fs.mkdirSync(target, { recursive: true });
  return target;
}

const defaultSettings = {
  nickname: 'Player',
  memory: 4096,
  javaPath: '',
  closeOnLaunch: false,
  fullscreen: false,
};

function readSettings() {
  try {
    const raw = fs.readFileSync(paths.settings, 'utf8');
    return Object.assign({}, defaultSettings, JSON.parse(raw));
  } catch {
    return Object.assign({}, defaultSettings);
  }
}

function writeSettings(patch) {
  const merged = Object.assign(readSettings(), patch || {});
  ensureDir(paths.root);
  fs.writeFileSync(paths.settings, JSON.stringify(merged, null, 2), 'utf8');
  return merged;
}

module.exports = { MC_VERSION, FABRIC_LOADER, FABRIC_API_VERSION, paths, ensureDir, readSettings, writeSettings, defaultSettings };
