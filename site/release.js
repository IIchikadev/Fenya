/* Кнопки скачивания берут файл из последнего релиза на GitHub.
   Если API недоступно или в релизе нет установщика — ведём на страницу релизов. */
(() => {
  const REPO = "IIchikadev/Socket";
  const FALLBACK = `https://github.com/${REPO}/releases/latest`;
  const buttons = [...document.querySelectorAll("[data-download]")];
  const labels = [...document.querySelectorAll("[data-release]")];
  buttons.forEach((a) => (a.href = FALLBACK));
  document.querySelectorAll("[data-changelog]").forEach((a) => (a.href = `https://github.com/${REPO}/releases`));
  if (!buttons.length && !labels.length) return;

  const pick = (assets) =>
    assets.find((a) => /^socket[ ._-]*loader\.exe$/i.test(a.name)) ||
    assets.find((a) => /setup.*\.exe$/i.test(a.name)) ||
    assets.find((a) => /portable.*\.exe$/i.test(a.name)) ||
    assets.find((a) => /\.exe$/i.test(a.name));
  const size = (bytes) => (bytes >= 1048576 ? `${Math.round(bytes / 1048576)} МБ` : `${Math.max(1, Math.round(bytes / 1024))} КБ`);

  fetch(`https://api.github.com/repos/${REPO}/releases/latest`, { headers: { Accept: "application/vnd.github+json" } })
    .then((r) => (r.ok ? r.json() : Promise.reject(new Error(r.status))))
    .then((release) => {
      const asset = pick(release.assets || []);
      const version = release.name || release.tag_name;
      if (asset) {
        buttons.forEach((a) => {
          a.href = asset.browser_download_url;
          a.title = asset.name;
        });
      }
      labels.forEach((el) => {
        el.textContent = asset ? `Версия ${version} · ${size(asset.size)}` : `Версия ${version}`;
      });
    })
    .catch(() => {
      /* оставляем ссылку на страницу релизов */
    });
})();
