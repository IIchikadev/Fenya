# Fenya — Socket Client

Исходники проекта Socket Client: клиент для Minecraft 1.21.4 (Fabric), лоадер на Electron и лендинг.
Готовые сборки (`socket-client-1.0.0.jar`, установщик и portable-версия лоадера) лежат в
[Releases](https://github.com/IIchikadev/Fenya/releases).

| Папка | Содержимое |
|---|---|
| `client-source/` | Код клиента (Java 21, Gradle, Fabric Loom) |
| `loader-source/` | Код лоадера (Electron): скачивает Minecraft, Fabric, Fabric API и ставит ядро |
| `site/` | Лендинг, открывается как `site/index.html` без сборки |

## Сборка

Клиент (нужен JDK 21):

```bash
cd client-source
./gradlew build          # jar появится в build/libs/
```

Лоадер (нужен Node.js):

```bash
cd loader-source
npm install
npm start                # запуск в разработке
npm run dist             # установщик и portable в dist/
```

Артефакты сборки (`build/`, `.gradle/`, `run/`, `node_modules/`, `dist/`, `*.exe`, `*.jar`)
в репозиторий не коммитятся — см. `.gitignore`.
