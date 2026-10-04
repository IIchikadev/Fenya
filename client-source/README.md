# Socket Client — рабочая база

Вспомогательный (не читерский) клиент: информация, память, удобство ввода и
производительность. Собран из донорской кодовой базы (Fabric 1.21.4) с вырезанными
читерскими модулями. Декоративный визуал вырезан отдельным заходом — остались только
украшения модели игрока (Adornments, Cape, ChinaHat, EnchantGlow) и косметика.

## Сборка

```bash
export JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.12.101-hotspot"
./gradlew build          # jar в build/libs/
./gradlew compileJava    # быстрая проверка компиляции
```

JDK 21 (Microsoft OpenJDK) стоит в `C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot`.
Запуск сразу в мир: `node ../socket-loader/scripts/devlaunch.js "New World"`.

## Что осталось из модулей (29)

| Категория | Модули |
|---|---|
| Визуал (7) | Adornments, Cape, ChinaHat, EnchantGlow, ItemPhysic, FullBright, Removals |
| Камера (7) | Zoom, Freelook, MotionBlur, AspectRatio, ViewModel, SwingAnimation, Animations |
| Интерфейс (7) | Interface, ItemInfo, ItemHighlight, DeathCoords, RadialMenu, ChatHelper, StreamerMode |
| Разное (8) | InventorySort, ItemScroller, ElytraSwap, AutoSwap, Optimization, Sprint, Sounds, SoundReducer |

Категории выровнены по размеру (7/7/7/8). Прежняя «Анимации» переименована в «Камера»:
там собраны зум, freelook, смаз, соотношение сторон и настройки рук.


## Что вырезано

- **Combat целиком** — Aura, TriggerBot, AimAssistant, AntiBot, AutoTotem, AutoArmor,
  MaceHelper, Velocity, HitBoxes и остальные 24 модуля, вместе с `AuraHandler`,
  `AimHandler`, `PvEHandler`, `ANFindHandler` и `RotationProcessor` (спуф поворотов).
- **Movement-читы** — Fly, Scaffold, WallClimb, AirStuck, NoSlowDown, SafeWalk,
  FreeCamera, WaterJump, FastBreak, NoDelay и др.
- **ESP/X-Ray** — EntityESP, BlockESP, ShaderESP, SoundESP, WardenESP, SeeInvisibles,
  XRay, Predictions, Pointers, EntityBox.
- **Автоматизация игры и рынка** — AutoBuy, Collector, ServerAssistant, MineAssistant,
  AucReissue, AutoFish, AutoEat, ChestStealer, Nuker, фермы, `FunPay`-интеграция
  (`ChatPoller`, `OrderPoller`), экран `StationScreen`, команда `/ah`.
- **Прочее** — CaptchaSolver, PortalBypass, FastLoad, NoServerPack, NoCommands,
  ItemScroller, OpenWalls, макро-команды `/vclip`, `/hclip`, `/blockesp`, `/warden`.
- **Чат-команды целиком** — весь пакет клиентских команд с префиксом `.`
  (`.way`, `.gps`, `.layout`, `.rct`, `.macros`, `.friend`, `.staff`, `.config`,
  `.bind`, `.cc`), диспетчер `CommandProcessor`, подсказки `ChatInputSuggestorMixin`
  и хук `sendChatMessage`. Вместе с ними ушли макросы (`aethereal.macro`) и
  сохранение раскладок (`layouts` в конфиге). В пакете `aethereal.command` остались
  только `CommandExecutor` и `CommandException` — это RPC Discord, к чату отношения
  не имеют.
- **Стафф-детект** — виджет `StaffWidget`, `aethereal.staff`, пункт «Стафф»
  в настройках `Interface` и неиспользуемый шрифт значков `assets/socket/font/prefixes.json`.
- **Декоративный визуал** (при повороте в сторону вспомогательного клиента) — Trails,
  WorldParticles, HitParticles, HitBubbles, JumpCircle, KillEffect, TotemParticles,
  HitEffects, Bloom, WorldTint, WeatherEffects, HandsShader, BlockBreakEffects, Ambience,
  Crosshair, Nametags, ScoreboardVisual, ChatVisual, вместе с `ParticleEngine`,
  `RingEngine`, `VisualUtil`, `ChatBackgroundMixin` и пакетом `aethereal.ambience`.
  Резервная копия исходников до чистки — `../_backup/`.

Хуки миксинов, которые обслуживали удалённое, тоже сняты (см. историю правок в
`platform/inject/mixin`). Скрипт первичной чистки — `tools/strip_cheats.py`.

## Что осталось сделать

1. Перенести GUI из Zenith (`zenith/zov/client/screens`, `zenith/ui`) вместо текущего
   `aethereal/ui`.
2. Переименовать пакеты `aethereal` → `socket`. Брендинг ресурсов и Gradle уже
   переведён (`gradle.properties`, `fabric.mod.json`, `assets/socket/`).
3. HUD-виджеты `TargetWidget` и `EnvironmentWidget` остались без источника цели
   (раньше брали её из Aura/TriggerBot) — либо переписать на цель под прицелом,
   либо убрать.
4. Проверить клиент в игре: сборка компилируется и remap-ится, запуск в мир — через
   `../socket-loader/scripts/devlaunch.js`.

## HUD

Общий визуальный язык виджетов («приборная панель») собран в `aethereal/ui/widget/HudSkin.java`:
плоские матовые плашки со скруглением 3, волосяная рамка, блик по верхней кромке, акцентная
линия снизу с затуханием вправо, метка канала слева в шапке, волосяные разделители, сплошные
шкалы с яркой ведущей кромкой. Базовые примитивы вызываются из `Widget`, поэтому стиль
наследуют все виджеты.
