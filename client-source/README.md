# Socket Client — клиент

Клиент для Minecraft 1.21.4 на Fabric: информация, память, удобство ввода, визуал и
производительность. Код проекта открыт под MIT; сторонние компоненты сохраняют свои лицензии.

## Сборка

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew build          # jar в build/libs/
./gradlew compileJava    # быстрая проверка компиляции
```

Нужен JDK 21. Запуск сразу в мир: `node ../loader-source/scripts/devlaunch.js "New World"`.

## Модули (37)

| Категория | Модули |
|---|---|
| Визуал (Render, 13) | Adornments, Bloom, Cape, ChinaHat, CustomFog, EnchantGlow, FakePlayer, FullBright, ItemPhysic, Removals, Saturation, SkyShader |
| Камера и анимации (7) | AspectRatio, ChunkAnimator, Freelook, MotionBlur, SwingAnimation, ViewModel, Zoom |
| Интерфейс (Hud, 7) | ChatHelper, DeathCoords, Interface, ItemHighlight, ItemInfo, RadialMenu, StreamerMode |
| Разное (Misc, 10) | AuctionHelper, AutoResell, AutoSwap, ElytraSwap, FastPlace, InventorySort, ItemScroller, Optimization, SoundReducer, Sounds, Sprint |

Список собран по аннотациям `@ModuleRegister` в `src/main/java/socket/module`.

> Часть модулей автоматизирует действия (`AutoResell` сам повторяет `/ah` и перевыставляет лоты,
> `AutoSwap`, `ElytraSwap`, `FastPlace`) и на некоторых серверах может нарушать правила.
> Проверяйте правила сервера перед использованием.

## Структура кода

Пакет `socket` (бывший `aethereal`), миксины лежат отдельно в `platform.inject`.

| Пакет | Что внутри |
|---|---|
| `socket.core` | Точка входа `Socket`, контейнер процессоров `Processor`, шина событий |
| `socket.module.*` | Модули по категориям (`render`, `misc`, `player`, `movement`, `combat`) |
| `socket.config` | Процессоры конфигов и тем, реестр модулей `ModuleProcessor` |
| `socket.ui` | Экраны, виджеты HUD, элементы и шейдеры интерфейса |
| `socket.render` | 2D/3D-отрисовка, пакетный рендер `BatchProcessor`, анимации |
| `socket.handler` | Обработчики событий игры (инвентарь, ввод, TPS) |
| `socket.event` | Классы событий |
| `socket.discord` | Discord RPC |
| `socket.lib.*` | Встроенные копии сторонних библиотек (javassist, jsoup, log4j, json) |
| `platform.inject` | Миксины, аксессоры и инвокеры Minecraft |

### Как добраться до нужного объекта

Все процессоры доступны через синглтон:

```java
Socket.getInstance().getProcessors().themes()      // ThemeProcessor
Socket.getInstance().getProcessors().modules()     // ModuleProcessor (реестр модулей)
Socket.getInstance().getProcessors().modules().zoom()   // конкретный модуль
Socket.getInstance().getProcessors().handlers()    // HandlerProcessor
```

Геттеры `Processor`: `friends`, `discord`, `accounts`, `draw2D`, `draw3D`, `batch`,
`notifications`, `resourcePacks`, `themes`, `cosmetics`, `drag`, `modules`, `handlers`.
Геттеры модулей в `ModuleProcessor` названы по классу (`sprint()`, `zoom()`, `streamerMode()`…).

В старых классах встречаются
короткие имена вроде `a()`, `b()`, `c()`. Они переименовываются постепенно.

## Что сделано

- Вырезаны читерские модули, команды чата, стафф-детект и декоративный визуал
  (подробности — в истории git и `tools/strip_cheats.py`).
- Удалены остатки автоматизации: каталог `AutoBuyEntry`, экран `AssistantScreen`,
  `UseableHandler`, фильтры предметов и проверка `BotFilter`.
- Пакеты `aethereal` переименованы в `socket`.
- HUD-виджеты `TargetWidget` и `EnvironmentWidget` берут цель из прицела
  (игрок в пределах досягаемости); в открытом чате `TargetWidget` показывает вас самих,
  чтобы виджет можно было перетащить.

## Что осталось сделать

1. Перенести GUI из Zenith вместо текущего `socket/ui`.
2. Продолжить переименование коротких имён методов (`a()`, `b()`…) в старых классах.
3. Проверить клиент в игре: сборка компилируется и remap-ится, запуск в мир — через
   `devlaunch.js`.

## HUD

Общий визуальный язык виджетов («приборная панель») собран в `socket/ui/widget/HudSkin.java`:
плоские матовые плашки со скруглением 3, волосяная рамка, блик по верхней кромке, акцентная
линия снизу с затуханием вправо, метка канала слева в шапке, волосяные разделители, сплошные
шкалы с яркой ведущей кромкой. Базовые примитивы вызываются из `Widget`, поэтому стиль
наследуют все виджеты.
