"""Вырезает читерские модули из SocketClient (база Delta).

Оставляем только визуал, HUD и безобидные утилиты — под то, что обещает сайт.
Скрипт правит ModuleProcessor (поля, вызов setup, геттеры) и удаляет файлы модулей.
Запуск: python tools/strip_cheats.py [--dry]
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/socket"
MP = SRC / "config/ModuleProcessor.java"

# Модули, которые остаются в клиенте.
KEEP = {
    # render / визуал
    "AspectRatio", "ChinaHat", "Crosshair", "FullBright", "HandsShader",
    "Interface", "ItemPhysic", "Removals", "ShulkerPreview", "SwingAnimation",
    "ViewModel", "Animations", "Ambience",
    # motion (безобидное)
    "Sprint",
    # player / инфо
    "ThirdPerson", "SoundReducer", "DeathCoords",
    # misc
    "StreamerMode", "Sounds", "ChatHelper",
    # инвентарь: перекладывает только сферы и талисманы
    "AutoSwap",
}

MODULE_DIRS = [SRC / "module" / c for c in ("combat", "misc", "movement", "player", "render")]


def module_classes():
    found = {}
    for d in MODULE_DIRS:
        for f in sorted(d.glob("*.java")):
            found[f.stem] = f
    return found


def main():
    dry = "--dry" in sys.argv
    mods = module_classes()
    unknown = KEEP - set(mods) - {"Animations", "Ambience"}
    if unknown:
        print("!! в KEEP есть неизвестные классы:", sorted(unknown))

    drop = {name: path for name, path in mods.items() if name not in KEEP}
    print(f"модулей всего: {len(mods)}, остаётся: {len(mods) - len(drop)}, удаляем: {len(drop)}")

    text = MP.read_text(encoding="utf-8")

    # 1. поля вида: private final Aura B = new Aura();
    field_re = re.compile(r"^    private final (\w+) (\w+) = new \1\(\);\n", re.M)
    dropped_fields = []
    kept_fields = []

    def field_sub(m):
        cls, field = m.group(1), m.group(2)
        if cls in drop:
            dropped_fields.append(field)
            return ""
        kept_fields.append(field)
        return m.group(0)

    text = field_re.sub(field_sub, text)
    print(f"поля: убрано {len(dropped_fields)}, оставлено {len(kept_fields)}")

    # 2. геттеры вида: public Aura B() { return this.B; }
    getter_re = re.compile(
        r"\n    public (\w+) (\w+)\(\) \{\n        return this\.\2;\n    \}\n", re.M
    )

    def getter_sub(m):
        return "" if m.group(1) in drop else m.group(0)

    text, n = getter_re.subn(getter_sub, text)
    print(f"геттеры обработаны, блоков просмотрено: {n}")

    # 3. вызов a(...) в setup(): выкидываем this.<удалённое поле>
    setup_re = re.compile(r"(        a\()(.*?)(\);\n)", re.S)
    m = setup_re.search(text)
    if not m:
        sys.exit("не нашёл вызов a(...) в setup()")
    args = [a.strip() for a in m.group(2).split(",")]
    kept_args = [a for a in args if a.replace("this.", "") not in dropped_fields]
    print(f"аргументы setup: было {len(args)}, стало {len(kept_args)}")

    lines, cur = [], "        a("
    for i, a in enumerate(kept_args):
        piece = a + (", " if i < len(kept_args) - 1 else "")
        if len(cur) + len(piece) > 116:
            lines.append(cur)
            cur = "                " + piece
        else:
            cur += piece
    lines.append(cur + ");")
    text = text[: m.start()] + "\n".join(lines) + "\n" + text[m.end():]

    if dry:
        print("dry-run, ничего не записано")
        return

    MP.write_text(text, encoding="utf-8")
    for name, path in sorted(drop.items()):
        path.unlink()
    print(f"удалено файлов: {len(drop)}")


if __name__ == "__main__":
    main()
