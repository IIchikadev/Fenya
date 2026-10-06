'use strict';
// Recreate the distributable bundle from built sources and pinned Modrinth files.
const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const { getJson, download } = require('../src/core/download');
const { digest } = require('../src/core/bundle');
const root = path.resolve(__dirname, '..');
async function prepare() {
  const output = path.join(root, 'bundle');
  const libs = path.resolve(root, '../client-source/build/libs');
  const core = process.env.SOCKET_CORE_JAR || (fs.existsSync(libs) && fs.readdirSync(libs)
    .filter(name => /^socket-client.*\.jar$/i.test(name) && !/sources|dev/i.test(name))
    .map(name => path.join(libs, name)).sort((a,b) => fs.statSync(b).mtimeMs - fs.statSync(a).mtimeMs)[0]);
  if (!core || !fs.existsSync(core)) throw new Error('Build client-source with JDK 21 first, or set SOCKET_CORE_JAR.');
  const zip = new AdmZip(core);
  const meta = JSON.parse(zip.readAsText('fabric.mod.json'));
  if (meta.id !== 'socket') throw new Error('Expected Socket Client core.');
  const files = [];
  const coreName = `core/socket-client-${meta.version}.jar`;
  fs.mkdirSync(path.join(output, 'core'), { recursive:true });
  fs.copyFileSync(core, path.join(output, coreName));
  for (const name of fs.readdirSync(path.join(output, 'core'))) {
    if (/^socket-client.*\.jar$/i.test(name) && name !== path.basename(coreName)) fs.unlinkSync(path.join(output, 'core', name));
  }
  files.push({file:coreName, kind:'core', sha256:digest(core)});
  const lock = JSON.parse(fs.readFileSync(path.join(root, 'bundle-lock.json'), 'utf8'));
  for (const entry of lock.dependencies) {
    const target = path.join(output, entry.file);
    if (!fs.existsSync(target) || digest(target) !== entry.sha256) {
      const version = await getJson(`https://api.modrinth.com/v2/version_file/${entry.sha512}?algorithm=sha512`);
      const file = version.files.find(candidate => candidate.hashes.sha512 === entry.sha512);
      if (!file) throw new Error('Pinned dependency unavailable: ' + entry.file);
      await download(file.url, target, entry.sha256, 'sha256');
    }
    files.push({file:entry.file,kind:'dependency',sha256:entry.sha256});
  }
  const licenseName = 'shaderpacks/MakeUp-LICENSE.txt';
  fs.mkdirSync(path.join(output, 'shaderpacks'), {recursive:true});
  fs.copyFileSync(path.join(root, 'src/core/MakeUp-LICENSE.txt'), path.join(output,licenseName));
  files.push({file:licenseName,kind:'dependency',sha256:digest(path.join(output,licenseName))});
  fs.mkdirSync(path.join(output,'licenses'), {recursive:true});
  for (const name of ['LICENSE', 'THIRD-PARTY-NOTICES.md']) {
    const target = path.join(output,'licenses',name);
    fs.copyFileSync(path.resolve(root,'..',name),target);
    files.push({file:'licenses/'+name,kind:'dependency',sha256:digest(target)});
  }
  fs.writeFileSync(path.join(output,'manifest.json'), JSON.stringify({version:require('../package.json').version,files},null,2));
  console.log(`Bundle ready: ${files.length} verified files, core ${meta.version}`);
}
prepare().catch(error => { console.error(error.message); process.exitCode=1; });
