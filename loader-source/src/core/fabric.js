'use strict';
const fs = require('fs');
const path = require('path');
const { paths, ensureDir, MC_VERSION, FABRIC_LOADER, FABRIC_API_VERSION } = require('./paths');
const { getJson, download } = require('./download');

const FABRIC_META = 'https://meta.fabricmc.net/v2/versions/loader';
const MAVEN_FABRIC = 'https://maven.fabricmc.net/';
const MAVEN_CENTRAL = 'https://repo1.maven.org/maven2/';
const MODRINTH_API = 'https://api.modrinth.com/v2';

/** name вида group:artifact:version → относительный путь maven. */
function mavenPath(name) {
  const [group, artifact, version] = name.split(':');
  return `${group.split('.').join('/')}/${artifact}/${version}/${artifact}-${version}.jar`;
}

async function fetchFabricProfile() {
  if (fs.existsSync(paths.fabricJson())) {
    return JSON.parse(fs.readFileSync(paths.fabricJson(), 'utf8'));
  }
  const profile = await getJson(`${FABRIC_META}/${MC_VERSION}/${FABRIC_LOADER}/profile/json`);
  ensureDir(path.dirname(paths.fabricJson()));
  fs.writeFileSync(paths.fabricJson(), JSON.stringify(profile), 'utf8');
  return profile;
}

/** Библиотеки Fabric: у них нет downloads, только name + url репозитория. */
function collectFabricTasks(profile) {
  return profile.libraries.map(library => {
    const relative = mavenPath(library.name);
    const base = library.url || (library.name.startsWith('net.fabricmc') ? MAVEN_FABRIC : MAVEN_CENTRAL);
    return {
      url: base.endsWith('/') ? base + relative : `${base}/${relative}`,
      target: path.join(paths.libraries, relative.split('/').join(path.sep)),
      sha1: library.sha1,
      name: library.name,
    };
  });
}

/** Fabric API нужен клиенту как зависимость — берём с Modrinth. */
async function installFabricApi() {
  const target = path.join(paths.mods, `fabric-api-${FABRIC_API_VERSION}.jar`);
  if (fs.existsSync(target)) return target;
  const versions = await getJson(`${MODRINTH_API}/project/fabric-api/version?game_versions=["${MC_VERSION}"]&loaders=["fabric"]`);
  if (!Array.isArray(versions) || versions.length === 0) {
    throw new Error('Не удалось найти Fabric API для 1.21.4 на Modrinth');
  }
  const exact = versions.find(version => version.version_number === FABRIC_API_VERSION) || versions[0];
  const file = exact.files.find(candidate => candidate.primary) || exact.files[0];
  ensureDir(paths.mods);
  await download(file.url, target, file.hashes && file.hashes.sha1);
  return target;
}

module.exports = { fetchFabricProfile, collectFabricTasks, installFabricApi, mavenPath };
