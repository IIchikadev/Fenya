'use strict';
const fs = require('fs');
const path = require('path');
const { paths, ensureDir } = require('./paths');
const { getJson, download } = require('./download');

// Minecraft 1.21.4 versions; keep Iris and Sodium as a matched pair.
const VERSIONS = ['Ca054sTe', 'c3YkZvne', 'pgb0JjoN'];
const PACK = 'MakeUp-UltraFast-9.5g.zip';

async function install(onProgress) {
  for (const id of VERSIONS) {
    const version = await getJson(`https://api.modrinth.com/v2/version/${id}`);
    const file = version.files.find(candidate => candidate.primary) || version.files[0];
    const directory = file.filename.endsWith('.zip')
      ? path.join(paths.instance, 'shaderpacks') : paths.mods;
    ensureDir(directory);
    if (onProgress) onProgress({ text: `Устанавливаю ${version.name}`, percent: 94 });
    await download(file.url, path.join(directory, file.filename), file.hashes.sha1);
  }
  configure();
}

function configure() {
  ensureDir(path.join(paths.instance, 'shaderpacks'));
  ensureDir(path.join(paths.instance, 'config'));
  fs.copyFileSync(path.join(__dirname, 'MakeUp-LICENSE.txt'),
    path.join(paths.instance, 'shaderpacks', 'MakeUp-LICENSE.txt'));
  const config = path.join(paths.instance, 'config', 'iris.properties');
  // Preserve the user's selection after the first install.
  if (!fs.existsSync(config)) {
    fs.writeFileSync(config, `shaderPack=${PACK}\nenableShaders=true\n`, 'utf8');
  } else {
    const previous = fs.readFileSync(config, 'utf8');
    const migrated = previous.replace(/^shaderPack=miniature-shader-2\.19\.zip\r?$/m, `shaderPack=${PACK}`);
    if (migrated !== previous) fs.writeFileSync(config, migrated, 'utf8');
  }
  const options = path.join(paths.instance, 'shaderpacks', `${PACK}.txt`);
  if (!fs.existsSync(options)) {
    fs.copyFileSync(path.join(__dirname, 'makeup-low.properties'), options);
  }
  const moduleConfig = path.join(paths.instance, 'configs', 'default.json');
  ensureDir(path.dirname(moduleConfig));
  const modules = fs.existsSync(moduleConfig)
    ? JSON.parse(fs.readFileSync(moduleConfig, 'utf8')) : { modules: [] };
  if (Array.isArray(modules.modules)) {
    const previous = modules.modules.find(module => module.name === 'Miniature Shader');
    if (previous) previous.name = 'Shaders';
    if (!modules.modules.some(module => module.name === 'Shaders')) {
      modules.modules.push({ name: 'Shaders', activated: true, bind: -1, settings: {} });
    }
    fs.writeFileSync(moduleConfig, JSON.stringify(modules, null, 2), 'utf8');
  }
}

function isInstalled() {
  return fs.existsSync(path.join(paths.mods, 'iris-fabric-1.8.8+mc1.21.4.jar'))
    && fs.existsSync(path.join(paths.mods, 'sodium-fabric-0.6.13+mc1.21.4.jar'))
    && fs.existsSync(path.join(paths.instance, 'shaderpacks', PACK));
}

module.exports = { install, configure, isInstalled, PACK };
