# Socket Client / Socket loader

Исходники проекта Socket Client: клиент для Minecraft 1.21.4 (Fabric), лоадер на Electron.
Готовый **Socket loader.exe** лежит в
[Releases](https://github.com/IIchikadev/Socket/releases).

Игроку нужен только этот EXE: открыть, ввести ник и нажать «Установить и играть».
Java 21, Minecraft, Fabric, актуальное ядро, моды и шейдер установятся автоматически.
Поддерживается Windows 10/11 x64. Для первой установки нужен интернет.
Данные сохраняются в `%APPDATA%\.socketclient`; личные настройки автора не включены.
С версии 1.2.1 лоадер сам доставляет обновления ядра: скачивать новый EXE для
каждого обновления клиента не нужно. [Описание автообновлений](AUTO-UPDATES.md).
Текущий вход работает по нику, без авторизации Microsoft.

Собственный код клиента и лоадера открыт под [MIT](LICENSE).
Сторонние компоненты и материалы перечислены в [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).

| Папка | Содержимое |
|---|---|
| `client-source/` | Код клиента (Java 21, Gradle, Fabric Loom) |
| `loader-source/` | Код лоадера (Electron): скачивает Minecraft, Fabric, Fabric API и ставит ядро |

## Сборка

Клиент (нужен JDK 21):

```bash
cd client-source
./gradlew build          # jar появится в build/libs/
```

Лоадер (нужен Node.js):

```bash
cd loader-source
npm ci
npm start                # запуск в разработке
npm run dist             # один файл: dist/Socket loader.exe
```

Артефакты сборки (`build/`, `.gradle/`, `run/`, `node_modules/`, `dist/`, `*.exe`, `*.jar`)
в репозиторий не коммитятся — см. `.gitignore`.

Для сборки EXE нужны Windows x64, JDK 21 и Node.js 22.12+.
Сначала соберите клиент. `npm run dist` создаёт комплект из ядра в
`client-source/build/libs` и закреплённых зависимостей Modrinth с проверкой хешей.
Локальная папка игры для сборки не нужна. Папка `bundle` генерируется автоматически.
