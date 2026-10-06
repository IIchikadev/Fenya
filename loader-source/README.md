# Socket Loader

Лоадер Socket Client: сам скачивает Minecraft 1.21.4, Fabric Loader, Fabric API,
ставит ядро клиента и управляет модами. Оформление: графит, лайм и циан.

## Запуск в разработке

```bash
npm install
npm start
```

## Сборка одного переносимого EXE

```bash
npm run dist        # dist/Socket loader.exe
```

Сначала соберите `../client-source` с JDK 21. Комплект создаётся автоматически:
ядро берётся из `../client-source/build/libs`, зависимости скачиваются по закреплённым
хешам из `bundle-lock.json`. Обычная сборка скачивает Electron автоматически.
`SOCKET_CORE_JAR` позволяет выбрать ядро, `SOCKET_ELECTRON_DIST` — локальный кеш Electron.
Java 21 устанавливается пользователю автоматически при первом запуске игры.

Код лоадера открыт под MIT: см. `LICENSE` и `../THIRD-PARTY-NOTICES.md`.

## Что где лежит

Инстанс игры создаётся в `%APPDATA%\.socketclient`:

```
.socketclient\
├─ instance\        игровая папка (mods, config, saves, options.txt)
│  └─ mods\         ядро клиента, Fabric API и пользовательские моды
├─ versions\1.21.4\ клиентский jar и version.json
├─ libraries\       библиотеки Mojang и Fabric
├─ assets\          индексы и объекты ассетов
├─ natives\1.21.4\  распакованные .dll
└─ loader-settings.json
```

## Структура кода

| Файл | Назначение |
|---|---|
| `src/main.js` | окно, IPC, поиск собранного ядра клиента |
| `src/preload.js` | мост в рендерер (`window.loader`) |
| `src/core/paths.js` | пути инстанса, настройки, версии MC/Fabric |
| `src/core/download.js` | докачка с проверкой sha1, пул параллельных загрузок |
| `src/core/vanilla.js` | манифест Mojang, библиотеки, нативы, ассеты |
| `src/core/fabric.js` | профиль Fabric, его библиотеки, Fabric API с Modrinth |
| `src/core/install.js` | полная установка инстанса с прогрессом |
| `src/core/launch.js` | поиск Java 21, classpath, аргументы, запуск |
| `src/core/mods.js` | список, добавление, вкл/выкл (`.disabled`), удаление |
| `src/ui/*` | интерфейс: играть / моды / настройки |

## Моды

- перетащить `.jar` в любое место окна — файл копируется в `instance\mods`;
- кнопка «вкл/выкл» переименовывает мод в `*.jar.disabled`, не удаляя его;
- ядро клиента помечено чипом «ядро», кнопка «Обновить ядро» перекладывает свежий
  `socket-client-*.jar` из сборки клиента.

## Аккаунт

Только офлайн-ник: UUID считается как у ванильного сервера — UUID v3 от
`OfflinePlayer:<ник>`, токен `0`.
