'use strict';
const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const { paths, ensureDir } = require('./paths');

const CORE_PREFIX = 'socket-client';

function isJar(name) {
  return /\.jar(\.disabled)?$/i.test(name);
}

/** Читает fabric.mod.json внутри жара, чтобы показать имя и версию мода. */
function readModMeta(file) {
  try {
    const zip = new AdmZip(file);
    const entry = zip.getEntry('fabric.mod.json');
    if (!entry) return null;
    const meta = JSON.parse(zip.readAsText(entry).replace(/\u0000/g, ''));
    return {
      id: meta.id || null,
      name: meta.name || meta.id || null,
      version: meta.version || null,
      description: meta.description || null,
    };
  } catch {
    return null;
  }
}

function list() {
  ensureDir(paths.mods);
  return fs
    .readdirSync(paths.mods)
    .filter(isJar)
    .map(fileName => {
      const file = path.join(paths.mods, fileName);
      const stat = fs.statSync(file);
      const meta = readModMeta(file);
      const base = fileName.replace(/\.disabled$/i, '');
      return {
        file: fileName,
        name: (meta && meta.name) || base.replace(/\.jar$/i, ''),
        version: meta && meta.version,
        description: meta && meta.description,
        enabled: !/\.disabled$/i.test(fileName),
        core: base.toLowerCase().startsWith(CORE_PREFIX),
        size: stat.size,
        modified: stat.mtimeMs,
      };
    })
    .sort((first, second) => Number(second.core) - Number(first.core) || first.name.localeCompare(second.name, 'ru'));
}

function add(sourceFiles) {
  ensureDir(paths.mods);
  const added = [];
  for (const source of sourceFiles) {
    if (!isJar(source)) continue;
    const target = path.join(paths.mods, path.basename(source));
    fs.copyFileSync(source, target);
    added.push(path.basename(source));
  }
  return added;
}

function toggle(fileName) {
  const current = path.join(paths.mods, fileName);
  if (!fs.existsSync(current)) return null;
  const next = /\.disabled$/i.test(fileName)
    ? current.replace(/\.disabled$/i, '')
    : `${current}.disabled`;
  fs.renameSync(current, next);
  return path.basename(next);
}

function remove(fileName) {
  const target = path.join(paths.mods, fileName);
  if (fs.existsSync(target)) fs.rmSync(target);
  return fileName;
}

/** Ставит собранный жар клиента в инстанс, заменяя прежнюю версию. */
function installCore(jarPath) {
  if (!jarPath || !fs.existsSync(jarPath)) throw new Error('Socket core missing');
  const data = fs.readFileSync(jarPath);
  const meta = readModMeta(jarPath);
  if (!meta || meta.id !== 'socket') throw new Error('Invalid Socket core');
  ensureDir(paths.mods);
  const temporary = path.join(paths.mods, 'socket-core.part');
  fs.writeFileSync(temporary, data);
  const target = path.join(paths.mods, path.basename(jarPath));
  const backup = path.join(paths.root, 'backups', 'core-' + Date.now());
  const moved = [];
  try {
    for (const existing of fs.readdirSync(paths.mods)) {
      const file = path.join(paths.mods, existing);
      if (isJar(existing) && readModMeta(file)?.id === 'socket') {
        ensureDir(backup);
        fs.renameSync(file, path.join(backup, existing));
        moved.push(existing);
      }
    }
    fs.renameSync(temporary, target);
  } catch (error) {
    for (const existing of moved) fs.renameSync(path.join(backup, existing), path.join(paths.mods, existing));
    if (fs.existsSync(temporary)) fs.rmSync(temporary);
    throw error;
  }
  return target;
}

/** Возвращает установленное ядро с размером и датой — чтобы сравнить со сборкой. */
function coreFile() {
  ensureDir(paths.mods);
  const name = fs.readdirSync(paths.mods).find(file => file.toLowerCase().startsWith(CORE_PREFIX));
  if (!name) return null;
  const file = path.join(paths.mods, name);
  const stat = fs.statSync(file);
  return { file, name, size: stat.size, modified: stat.mtimeMs, enabled: !/\.disabled$/i.test(name) };
}

/**
 * Подкладывает свежую сборку, если установленное ядро отличается от найденного жара.
 * Вызывается перед запуском игры, чтобы не играть на прошлой сборке.
 */
function syncCore(jarPath) {
  if (!jarPath || !fs.existsSync(jarPath)) return { updated: false, reason: 'сборка не найдена' };
  const built = fs.statSync(jarPath);
  const installed = coreFile();
  if (installed && installed.enabled && installed.name === path.basename(jarPath)
      && installed.size === built.size && fs.readFileSync(installed.file).equals(fs.readFileSync(jarPath))) {
    return { updated: false, reason: 'ядро актуально' };
  }
  installCore(jarPath);
  return { updated: true, reason: installed ? 'ядро обновлено из сборки' : 'ядро установлено' };
}

function hasCore() {
  ensureDir(paths.mods);
  return fs.readdirSync(paths.mods).some(file => file.toLowerCase().startsWith(CORE_PREFIX) && !/\.disabled$/i.test(file));
}

module.exports = { list, add, toggle, remove, installCore, syncCore, coreFile, hasCore, readModMeta };
