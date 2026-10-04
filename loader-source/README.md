# Socket Loader

Лоадер Socket Client: сам скачивает Minecraft 1.21.4, Fabric Loader, Fabric API,
ставит ядро клиента и управляет модами. Стилистика — с лендинга (графит + лайм + циан).

## Запуск в разработке

```bash
npm install
npm start
```

## Сборка установщика

```bash
npm run dist        # electron-builder → NSIS-инсталлятор в dist/
```

Чтобы в собранный установщик попало ядро клиента, положите `socket-client-*.jar`
в `resources/core/` при упаковке (или соберите клиент рядом: `../SocketClient/build/libs`).

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
