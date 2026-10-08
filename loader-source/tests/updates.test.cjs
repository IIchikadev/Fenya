const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const AdmZip = require('adm-zip');
process.env.APPDATA = fs.mkdtempSync(path.join(require('os').tmpdir(), 'socket-update-qa-'));
const { paths, readSettings, writeSettings } = require('../src/core/paths');
const updates = require('../src/core/updates');
const mods = require('../src/core/mods');
function jar(version, id='socket') { const z=new AdmZip(); z.addFile('fabric.mod.json',Buffer.from(JSON.stringify({id,version,name:id}))); return z.toBuffer(); }
function packageFor(version, corrupt=false, minecraft='1.21.4') {
 const data=jar(version); const core=`socket-client-${version}.jar`;
 const z=new AdmZip();z.addFile(core,data);z.addFile('update.json',Buffer.from(JSON.stringify({protocol:1,version,minecraft,core,sha256:corrupt?'0'.repeat(64):crypto.createHash('sha256').update(data).digest('hex')})));return z.toBuffer();
}
function release(version,data) { return {tag:version,asset:{name:'socket-update.zip',url:`https://github.com/IIchikadev/Socket/releases/download/${version}/socket-update.zip`,sha256:crypto.createHash('sha256').update(data).digest('hex')}}; }
test('updates validate, preserve mods, roll back failures and retain settings', async () => {
 assert.equal(readSettings().autoUpdateCore,true);
 fs.mkdirSync(paths.mods,{recursive:true});
 fs.writeFileSync(path.join(paths.mods,'socket-client-1.2.0.jar'),jar('1.2.0'));
 const other=jar('1.0','other');fs.writeFileSync(path.join(paths.mods,'socket-addon.jar'),other);
 writeSettings({coreVersion:'1.2.0',coreAsset:''});
 const data=packageFor('1.2.1');global.fetch=async ()=>new Response(data);
 await updates.installRelease(release('1.2.1',data));
 assert.equal(readSettings().coreVersion,'1.2.1');assert.ok(updates.selectedJar());
 assert.equal(mods.readModMeta(updates.selectedJar()).version,'1.2.1');
 assert.ok(fs.readFileSync(path.join(paths.mods,'socket-addon.jar')).equals(other));
 assert.equal(fs.readdirSync(paths.mods).filter(n=>mods.readModMeta(path.join(paths.mods,n))?.id==='socket').length,1);
 for(const bad of [packageFor('1.2.2',true),packageFor('1.2.2',false,'1.21.11')]) {
  global.fetch=async()=>new Response(bad);
  await assert.rejects(updates.installRelease(release('1.2.2',bad)));
  assert.equal(readSettings().coreVersion,'1.2.1');assert.equal(mods.readModMeta(mods.coreFile().file).version,'1.2.1');
  fs.rmSync(path.join(paths.root,'cores/1.2.2/socket-update.zip'),{force:true});
 }
 const newer=packageFor('1.2.2');global.fetch=async()=>new Response(newer);
 const rename=fs.renameSync;
 fs.renameSync=(a,b)=>{if(a===path.join(paths.mods,'socket-core.part'))throw new Error('simulated locked target');return rename(a,b);};
 try { await assert.rejects(updates.installRelease(release('1.2.2',newer)),/locked target/); }
 finally { fs.renameSync=rename; }
 assert.equal(readSettings().coreVersion,'1.2.1');assert.equal(mods.readModMeta(mods.coreFile().file).version,'1.2.1');
 global.fetch=async()=>{throw new Error('offline');};await assert.rejects(updates.checkForUpdate(),/offline/);
 assert.equal(readSettings().coreVersion,'1.2.1');
 assert.ok(updates.compareVersions('1.2.1','1.2.0')>0);
 assert.ok(updates.compareVersions('1.2.1','1.2.1-menu-effects')>0);
});
