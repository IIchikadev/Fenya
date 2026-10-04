'use strict';
/**
 * Сборка релиза: JS обфусцируется в папку obf/, оттуда его пакует electron-builder.
 * Внутри .exe остаётся только нечитаемый код, исходники в сборку не попадают.
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const obfuscator = require('javascript-obfuscator');

const root = path.join(__dirname, '..');
const source = path.join(root, 'src');
const output = path.join(root, 'obf');

const options = {
  compact: true,
  controlFlowFlattening: true,
  controlFlowFlatteningThreshold: 0.75,
  deadCodeInjection: true,
  deadCodeInjectionThreshold: 0.2,
  identifierNamesGenerator: 'hexadecimal',
  numbersToExpressions: true,
  simplify: true,
  splitStrings: true,
  splitStringsChunkLength: 8,
  stringArray: true,
  stringArrayEncoding: ['base64'],
  stringArrayThreshold: 0.9,
  selfDefending: true,
  disableConsoleOutput: true,
  target: 'node',
};

function walk(directory, visit) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) walk(full, visit);
    else visit(full);
  }
}

function build() {
  fs.rmSync(output, { recursive: true, force: true });
  let obfuscated = 0;
  let copied = 0;
  walk(source, file => {
    const relative = path.relative(source, file);
    const target = path.join(output, relative);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    if (file.endsWith('.js')) {
      const code = fs.readFileSync(file, 'utf8');
      const browser = relative.startsWith(`ui${path.sep}`);
      const result = obfuscator.obfuscate(code, Object.assign({}, options, { target: browser ? 'browser' : 'node' }));
      fs.writeFileSync(target, result.getObfuscatedCode(), 'utf8');
      obfuscated++;
    } else {
      fs.copyFileSync(file, target);
      copied++;
    }
  });
  console.log(`обфусцировано файлов: ${obfuscated}, скопировано: ${copied}`);
}

function pack() {
  const manifestPath = path.join(root, 'package.json');
  const original = fs.readFileSync(manifestPath, 'utf8');
  const manifest = JSON.parse(original);
  manifest.main = 'obf/main.js';
  manifest.build.files = ['obf/**/*'];
  manifest.build.asar = true;
  fs.writeFileSync(manifestPath, `${JSON.stringify(manifest, null, 2)}\n`, 'utf8');
  try {
    // путь проекта содержит пробелы и кириллицу, поэтому запускаем через npx без ручной сборки командной строки
    const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';
    execFileSync(npx, ['--yes', 'electron-builder', '--win', '--x64'], { cwd: root, stdio: 'inherit', shell: false });
  } finally {
    fs.writeFileSync(manifestPath, original, 'utf8');
  }
}

build();
if (process.argv.includes('--pack')) pack();
