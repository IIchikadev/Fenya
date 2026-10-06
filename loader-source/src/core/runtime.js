'use strict';
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const AdmZip = require('adm-zip');
const { paths, readSettings, ensureDir } = require('./paths');
const { getJson, download } = require('./download');
const managed = () => path.join(paths.root, 'runtime', 'java-21', 'bin', 'javaw.exe');
let cached = null;
function valid(file) {
  if (!file || !fs.existsSync(file)) return false;
  try {
    const java = path.join(path.dirname(file), 'java.exe');
    const result = spawnSync(java, ['-version'], { encoding: 'utf8', timeout: 10000, windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] });
    return result.status === 0 && /version "21[.\"]/.test(String(result.stderr) + String(result.stdout));
  } catch (error) { return /version "21[.\"]/.test(String(error.stderr || '')); }
}
function findJava() {
  if (cached && fs.existsSync(cached)) return cached;
  for (const file of [managed(), readSettings().javaPath]) if (valid(file)) return (cached = file);
  return null;
}
let pending;
async function ensureJava(onStage) {
  const existing = findJava();
  if (existing) return existing;
  if (process.platform !== 'win32' || process.arch !== 'x64') throw new Error('Этот лоадер предназначен для Windows x64');
  if (pending) return pending;
  pending = (async () => {
    if (onStage) onStage({ text: 'Скачиваю Java 21 автоматически', percent: 1 });
    const archive = { link: 'https://download.visualstudio.microsoft.com/download/pr/f1e5f23f-9d50-4b9f-8ed3-80522ae82bb5/71e8e5f0f13419cc726e470d25e0a0d0/microsoft-jdk-21.0.12.1-windows-x64.zip', checksum: '192441a9d27da813bada974bb88b4cf64d37a9589ed37f204374d411ca5ce07f' };
    const zipFile = path.join(paths.root, 'runtime', 'java-21.zip');
    await download(archive.link, zipFile, archive.checksum, 'sha256');
    const destination = path.join(paths.root, 'runtime', 'java-21');
    ensureDir(destination);
    const zip = new AdmZip(zipFile);
    for (const entry of zip.getEntries()) {
      if (entry.isDirectory) continue;
      const relative = entry.entryName.replace(/^[^/]+\//, '');
      const target = path.resolve(destination, relative);
      if (!target.startsWith(path.resolve(destination) + path.sep)) throw new Error('Небезопасный архив Java');
      ensureDir(path.dirname(target));
      fs.writeFileSync(target, entry.getData());
    }
    if (!valid(managed())) throw new Error('Не удалось проверить Java 21');
    fs.unlinkSync(zipFile);
    return (cached = managed());
  })();
  try { return await pending; } finally { pending = null; }
}
module.exports = { findJava, ensureJava, valid };
