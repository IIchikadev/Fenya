'use strict';
const api = window.loader;
const el = id => document.getElementById(id);
let state = null;

/* ---------- окно и вкладки ---------- */
document.querySelectorAll('[data-window]').forEach(button => {
  button.addEventListener('click', () => api.windowAction(button.dataset.window));
});

document.querySelectorAll('.tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(other => other.classList.toggle('active', other === tab));
    document.querySelectorAll('.view').forEach(view => view.classList.toggle('active', view.id === `view-${tab.dataset.view}`));
    if (tab.dataset.view === 'mods') renderMods();
  });
});

/* ---------- состояние ---------- */
async function loadState() {
  state = await api.getState();
  el('nickname').value = state.settings.nickname;
  el('memory').value = state.settings.memory;
  el('memory-value').textContent = `${state.settings.memory} МБ`;
  el('close-on-launch').checked = state.settings.closeOnLaunch;
  el('fullscreen').checked = state.settings.fullscreen;
  el('chip-version').textContent = `${state.version} · fabric ${state.loader}`;
  el('java-chip').textContent = state.java ? `java: ${shortPath(state.java)}` : 'java: автоустановка';
  el('core-chip').textContent = state.coreVersion ? `ядро: ${state.coreVersion}` : (state.coreInstalled ? 'ядро: установлено' : 'ядро: нет');
  el('auto-update').checked = state.settings.autoUpdateCore;
  el('allow-prerelease').checked = state.settings.allowPrerelease;
  el('java-path').textContent = state.java || 'установится автоматически';
  el('paths-info').textContent = `${state.paths.instance}\nмоды: ${state.paths.mods}`;
  setProgress(state.installed ? 100 : 0, state.installed ? 'игра установлена' : 'игра ещё не установлена');
  el('play-label').textContent = state.installed ? 'Играть' : 'Установить и играть';
}

function shortPath(value) {
  const parts = value.split('\\');
  return parts.length > 3 ? `…\\${parts.slice(-3).join('\\')}` : value;
}

function setProgress(percent, text) {
  el('progress-fill').style.width = `${Math.max(0, Math.min(100, percent))}%`;
  el('progress-percent').textContent = `${Math.round(percent)}%`;
  if (text) el('progress-text').textContent = text;
}

/* ---------- настройки ---------- */
el('nickname').addEventListener('change', event => {
  const value = event.target.value.trim() || 'Player';
  event.target.value = value;
  api.saveSettings({ nickname: value });
});
el('memory').addEventListener('input', event => {
  el('memory-value').textContent = `${event.target.value} МБ`;
});
el('memory').addEventListener('change', event => api.saveSettings({ memory: Number(event.target.value) }));
el('close-on-launch').addEventListener('change', event => api.saveSettings({ closeOnLaunch: event.target.checked }));
el('fullscreen').addEventListener('change', event => api.saveSettings({ fullscreen: event.target.checked }));
el('pick-java').addEventListener('click', async () => {
  const picked = await api.pickJava();
  if (picked) loadState();
});
el('reinstall').addEventListener('click', async () => {
  el('play').disabled = true;
  try {
    await api.runInstall();
    await loadState();
  } catch (error) {
    setProgress(0, `ошибка: ${error.message}`);
  }
  el('play').disabled = false;
});

/* ---------- запуск ---------- */
el('play').addEventListener('click', async () => {
  el('play').disabled = true;
  el('play-label').textContent = 'Готовим…';
  try {
    await api.launch();
    el('play-label').textContent = 'Игра запущена';
    setProgress(100, 'игра запущена');
  } catch (error) {
    setProgress(0, `ошибка: ${error.message}`);
    el('play').disabled = false;
    el('play-label').textContent = 'Играть';
  }
});

api.onInstallProgress(progress => setProgress(progress.percent, progress.text));
api.onGameLog(line => {
  const log = el('log');
  log.textContent = (log.textContent + line).slice(-8000);
  log.scrollTop = log.scrollHeight;
});
api.onGameState(payload => {
  if (!payload.running) {
    el('play').disabled = false;
    el('play-label').textContent = 'Играть';
    setProgress(100, payload.code === 0 ? 'игра закрыта' : `игра закрыта, код ${payload.code}`);
  }
});

/* ---------- ядро и обновления ---------- */
let updateInfo = null;

function setCoreButtons(disabled) {
  ['core-install', 'core-check', 'update-now'].forEach(id => { el(id).disabled = disabled; });
}

function renderUpdates(info) {
  updateInfo = info;
  const select = el('core-versions');
  const status = el('core-status');
  const releases = info && !info.error ? info.releases || [] : [];
  el('update-banner').hidden = !(info && info.available);
  if (!info || info.error || releases.length === 0) {
    status.textContent = info && info.error
      ? `не удалось получить релизы: ${info.error}`
      : 'в релизах пока нет файла ядра socket-client-*.jar';
    select.innerHTML = '<option>нет версий</option>';
    select.disabled = true;
    el('core-install').disabled = true;
    return;
  }
  select.innerHTML = releases.map(release => `<option value="${escapeHtml(release.tag)}">${escapeHtml(release.tag)}`
    + `${release.prerelease ? ' · пре-релиз' : ''}${release.tag === info.current ? ' · установлена' : ''}</option>`).join('');
  select.value = releases.some(release => release.tag === info.current) ? info.current : releases[0].tag;
  select.disabled = false;
  el('core-install').disabled = false;
  status.textContent = (info.current ? `установлена ${info.current}` : 'ядро из релизов ещё не установлено')
    + (info.latest ? ` · последняя ${info.latest.tag}` : '');
  if (info.available) {
    el('update-text').textContent = `Доступно ядро ${info.latest.tag}${info.current ? ` (у вас ${info.current})` : ''}`;
  }
}

async function checkUpdates() {
  el('core-status').textContent = 'проверяем релизы…';
  renderUpdates(await api.updates.check());
}

async function installCoreVersion(tag) {
  setCoreButtons(true);
  try {
    await api.updates.install(tag);
    await loadState();
    await checkUpdates();
  } catch (error) {
    setProgress(0, `ошибка: ${error.message}`);
  }
  setCoreButtons(false);
}

el('core-install').addEventListener('click', () => {
  const tag = el('core-versions').value;
  // выбор не последней версии — это закрепление: автообновление его бы перезаписало
  if (updateInfo && updateInfo.latest && tag !== updateInfo.latest.tag) {
    el('auto-update').checked = false;
    api.saveSettings({ autoUpdateCore: false });
  }
  installCoreVersion(tag);
});
el('update-now').addEventListener('click', () => {
  if (updateInfo && updateInfo.latest) installCoreVersion(updateInfo.latest.tag);
});
el('core-check').addEventListener('click', checkUpdates);
el('auto-update').addEventListener('change', event => api.saveSettings({ autoUpdateCore: event.target.checked }));
el('allow-prerelease').addEventListener('change', async event => {
  await api.saveSettings({ allowPrerelease: event.target.checked });
  checkUpdates();
});
api.onUpdateState(info => {
  renderUpdates(info);
  if (info && info.installed) loadState();
});

/* ---------- моды ---------- */
function formatSize(bytes) {
  return bytes > 1048576 ? `${(bytes / 1048576).toFixed(1)} МБ` : `${Math.max(1, Math.round(bytes / 1024))} КБ`;
}

async function renderMods() {
  const list = await api.mods.list();
  const container = el('mods-list');
  if (list.length === 0) {
    container.innerHTML = '<div class="empty">Пока пусто. Перетащите .jar сюда — ядро клиента появится после установки игры.</div>';
    return;
  }
  container.innerHTML = '';
  for (const mod of list) {
    const row = document.createElement('div');
    row.className = `mod${mod.enabled ? '' : ' off'}${mod.core ? ' core' : ''}`;
    row.innerHTML = `
      <div class="mod-icon">${(mod.name || '?').slice(0, 1).toUpperCase()}</div>
      <div>
        <div class="mod-title">${escapeHtml(mod.name)}${mod.core ? ' <span class="chip mono">ядро</span>' : ''}</div>
        <div class="mod-meta">${mod.version ? `v${escapeHtml(mod.version)} · ` : ''}${formatSize(mod.size)} · ${escapeHtml(mod.file)}</div>
      </div>
      <div class="mod-actions">
        <button class="icon-btn" data-toggle="${escapeHtml(mod.file)}">${mod.enabled ? 'вкл' : 'выкл'}</button>
        <button class="icon-btn danger" data-remove="${escapeHtml(mod.file)}">×</button>
      </div>`;
    container.appendChild(row);
  }
  container.querySelectorAll('[data-toggle]').forEach(button => {
    button.addEventListener('click', async () => {
      await api.mods.toggle(button.dataset.toggle);
      renderMods();
      loadState();
    });
  });
  container.querySelectorAll('[data-remove]').forEach(button => {
    button.addEventListener('click', async () => {
      await api.mods.remove(button.dataset.remove);
      renderMods();
      loadState();
    });
  });
}

el('mods-add').addEventListener('click', async () => {
  await api.mods.pick();
  renderMods();
});
el('mods-folder').addEventListener('click', () => api.mods.openFolder());
el('mods-core').addEventListener('click', async () => {
  await api.mods.installCore();
  renderMods();
  loadState();
});

function escapeHtml(value) {
  return String(value == null ? '' : value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
}

/* перетаскивание файлов в любое место окна */
const dropzone = el('dropzone');
['dragenter', 'dragover'].forEach(type => {
  window.addEventListener(type, event => {
    event.preventDefault();
    dropzone.classList.add('hot');
  });
});
['dragleave', 'drop'].forEach(type => {
  window.addEventListener(type, event => {
    event.preventDefault();
    if (type === 'dragleave' && event.relatedTarget) return;
    dropzone.classList.remove('hot');
  });
});
window.addEventListener('drop', async event => {
  event.preventDefault();
  const files = [...(event.dataTransfer ? event.dataTransfer.files : [])]
    .map(file => api.pathForFile(file))
    .filter(file => file && /\.jar$/i.test(file));
  if (files.length === 0) return;
  await api.mods.add(files);
  document.querySelector('.tab[data-view="mods"]').click();
  renderMods();
});

loadState();
