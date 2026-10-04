'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha1(file) {
  return new Promise((resolve, reject) => {
    const hash = crypto.createHash('sha1');
    const stream = fs.createReadStream(file);
    stream.on('data', chunk => hash.update(chunk));
    stream.on('error', reject);
    stream.on('end', () => resolve(hash.digest('hex')));
  });
}

async function isValid(file, expectedSha1) {
  if (!fs.existsSync(file)) return false;
  if (!expectedSha1) return fs.statSync(file).size > 0;
  try {
    return (await sha1(file)) === expectedSha1;
  } catch {
    return false;
  }
}

async function getJson(url) {
  const response = await fetch(url, { headers: { 'User-Agent': 'SocketLoader/1.0' } });
  if (!response.ok) throw new Error(`${response.status} ${response.statusText} — ${url}`);
  return response.json();
}

/** Скачивает файл, если его нет или хеш не сходится. Возвращает true, если качали. */
async function download(url, target, expectedSha1) {
  if (await isValid(target, expectedSha1)) return false;
  fs.mkdirSync(path.dirname(target), { recursive: true });
  const response = await fetch(url, { headers: { 'User-Agent': 'SocketLoader/1.0' } });
  if (!response.ok) throw new Error(`${response.status} ${response.statusText} — ${url}`);
  // читаем целиком: файлы игры небольшие, а потоковая запись через undici иногда падает на backpressure
  const buffer = Buffer.from(await response.arrayBuffer());
  const temp = `${target}.part`;
  fs.writeFileSync(temp, buffer);
  fs.renameSync(temp, target);
  return true;
}

/** Качает список задач с ограничением параллелизма и колбэком прогресса. */
async function downloadAll(tasks, onProgress, concurrency = 8) {
  let done = 0;
  const total = tasks.length;
  const queue = tasks.slice();
  async function worker() {
    for (;;) {
      const task = queue.shift();
      if (!task) return;
      await download(task.url, task.target, task.sha1);
      done++;
      if (onProgress) onProgress(done, total, task.name || path.basename(task.target));
    }
  }
  const workers = [];
  for (let i = 0; i < Math.min(concurrency, Math.max(1, total)); i++) workers.push(worker());
  await Promise.all(workers);
}

module.exports = { sha1, isValid, getJson, download, downloadAll };
