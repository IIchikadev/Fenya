package socket.ui.widget;

import socket.config.ThemeInfo;
import socket.config.ThemeProcessor;
import socket.core.Socket;
import socket.event.DrawEvent;
import socket.render.ColorUtil;
import org.joml.Vector4f;

/**
 * Визуальный язык HUD «приборная панель».
 *
 * Плоские матовые плашки с волосяной обводкой, тонкий блик по верхней кромке и
 * акцентная подчёркивающая линия снизу. Вместо боковой шины — короткая метка канала
 * слева в шапке, вместо точечных разделителей — волосяные линии, шкалы сплошные
 * с яркой ведущей кромкой. Читается как телеметрия, а не как украшение.
 */
public final class HudSkin {
    /** Ширина акцентной метки канала в шапке. */
    public static final float BUS = 2.0f;
    /** Скругление у всех плашек одинаковое и небольшое — приборный вид. */
    private static final float CORNER = 3.0f;

    private HudSkin() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static ThemeProcessor theme() {
        return Socket.getInstance().getProcessors().themes();
    }

    public static int accent(float alpha) {
        return ColorUtil.applyAlphaToColor(theme().a(ThemeInfo.PRIMARY).toIntColor(), Math.min(1.0f, alpha));
    }

    public static int accentCold(float alpha) {
        int primary = theme().a(ThemeInfo.PRIMARY).toIntColor();
        return ColorUtil.applyAlphaToColor(ColorUtil.lerpColor(primary, ColorUtil.a(60, 231, 255), 0.45f),
                Math.min(1.0f, alpha));
    }

    public static int text(float alpha) {
        return ColorUtil.applyAlphaToColor(theme().a(ThemeInfo.TEXT).toIntColor(), Math.min(1.0f, alpha));
    }

    public static Vector4f rack() {
        return new Vector4f(CORNER, CORNER, CORNER, CORNER);
    }

    public static Vector4f inner() {
        return new Vector4f(CORNER - 1.0f, CORNER - 1.0f, CORNER - 1.0f, CORNER - 1.0f);
    }

    /** Основная плашка: матовое стекло, волосяная рамка, блик сверху, акцент снизу. */
    public static void rackPanel(DrawEvent event, float x, float y, float width, float height, float glow, float animation) {
        if (animation <= 0.0f) {
            return;
        }
        int background = backdrop(animation);
        event.getDraw2DProcessor().b(event.h(), x, y, width, height, CORNER, background, animation);
        // блик по верхней кромке — плашка перестаёт выглядеть плоской заливкой
        event.getDraw2DProcessor().a(event.h(), x + 1.0f, y + 0.5f, width - 2.0f, 0.75f, 0.375f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.16f * animation));
        event.getDraw2DProcessor().a(event.h(), x, y, width, height, rack(), 0.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.09f * animation));
        underline(event, x, y, width, height, animation, glow);
        bus(event, x, y, height, animation);
    }

    /** Вложенная поверхность: то же стекло, но без акцентов. */
    public static void innerPanel(DrawEvent event, float x, float y, float width, float height, float animation) {
        if (animation <= 0.0f) {
            return;
        }
        event.getDraw2DProcessor().b(event.h(), x, y, width, height, CORNER - 1.0f, backdrop(animation), animation);
        event.getDraw2DProcessor().a(event.h(), x, y, width, height, inner(), 0.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.06f * animation));
    }

    private static int backdrop(float animation) {
        ThemeProcessor theme = theme();
        theme.a(ThemeInfo.BACKGROUND_HUD).setAlpha(170);
        int mixed = ColorUtil.lerpColor(theme.a(ThemeInfo.BACKGROUND_HUD).toIntColor(),
                theme.a(ThemeInfo.PRIMARY).toIntColor(), theme.a(ThemeInfo.PRIMARY).getAlphaFloat() / 8.0f);
        return ColorUtil.applyAlphaToColor(mixed, theme.a(ThemeInfo.BACKGROUND_HUD).getAlphaFloat() * animation);
    }

    /** Акцентная линия по нижней кромке: слева ярче, к правому краю затухает. */
    private static void underline(DrawEvent event, float x, float y, float width, float height, float animation, float glow) {
        float length = Math.max(0.0f, width - 6.0f);
        if (length <= 0.0f) {
            return;
        }
        float lineY = (y + height) - 1.25f;
        int bright = accent(0.75f * animation);
        int faded = accent(0.06f * animation);
        event.getDraw2DProcessor().a(event.h(), x + 3.0f, lineY, length, 0.75f, new Vector4f(0.375f, 0.375f, 0.375f, 0.375f),
                bright, faded, bright, faded);
        if (glow > 0.01f) {
            event.getDraw2DProcessor().a(event.h(), x + 3.0f, lineY, length * 0.35f, 0.75f, 0.375f,
                    accentCold(0.5f * glow * animation));
        }
    }

    /** Метка канала: короткий вертикальный штрих в шапке слева. */
    public static void bus(DrawEvent event, float x, float y, float height, float animation) {
        float mark = Math.min(6.5f, Math.max(3.0f, height * 0.45f));
        event.getDraw2DProcessor().a(event.h(), x + 2.0f, y + ((Math.min(height, 12.5f) - mark) / 2.0f), BUS, mark,
                BUS / 2.0f, accent(0.95f * animation));
    }

    /** Перемычка между плашками, стоящими друг под другом. */
    public static void bridge(DrawEvent event, float x, float y, float height, float animation) {
        if (height <= 0.0f) {
            return;
        }
        event.getDraw2DProcessor().a(event.h(), x + 2.5f, y, 1.0f, height, 0.5f, accent(0.35f * animation));
    }

    /** Вертикальный волосяной разделитель. */
    public static void dots(DrawEvent event, float x, float y, float height, float animation) {
        float length = height * 0.5f;
        event.getDraw2DProcessor().a(event.h(), x, y + ((height - length) / 2.0f), 0.75f, length, 0.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.2f * animation));
    }

    /** Горизонтальный волосяной разделитель строк настроек. */
    public static void dotsRow(DrawEvent event, float x, float y, float width, float animation) {
        event.getDraw2DProcessor().a(event.h(), x + 2.0f, y, Math.max(0.0f, width - 4.0f), 0.75f, 0.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.14f * animation));
    }

    /** Подложка под иконку: без рамки, только мягкое затемнение. */
    public static void socket(DrawEvent event, float x, float y, float size, float animation) {
        event.getDraw2DProcessor().a(event.h(), x, y, size, size, 2.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.07f * animation));
    }

    /** Отвод к дочерней строке: аккуратный уголок, как в дереве файлов. */
    public static void cable(DrawEvent event, float busX, float busY, float rowY, float rowHeight, float animation) {
        float centerY = rowY + (rowHeight / 2.0f);
        float height = Math.max(0.0f, centerY - busY);
        event.getDraw2DProcessor().a(event.h(), busX, busY, 0.75f, height, 0.0f, accent(0.22f * animation));
        event.getDraw2DProcessor().a(event.h(), busX, centerY - 0.375f, 3.0f, 0.75f, 0.0f, accent(0.3f * animation));
    }

    /** Шкала: сплошная заливка с яркой ведущей кромкой. */
    public static void meter(DrawEvent event, float x, float y, float width, float height, float progress, int color, float animation) {
        event.getDraw2DProcessor().a(event.h(), x, y, width, height, height / 2.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.08f * animation));
        float filled = Math.max(0.0f, Math.min(1.0f, progress)) * width;
        if (filled <= 0.0f) {
            return;
        }
        event.getDraw2DProcessor().a(event.h(), x, y, filled, height, height / 2.0f,
                ColorUtil.applyAlphaToColor(color, 0.55f * animation));
        float edge = Math.min(2.0f, filled);
        event.getDraw2DProcessor().a(event.h(), (x + filled) - edge, y, edge, height, height / 2.0f,
                ColorUtil.applyAlphaToColor(color, 0.95f * animation));
    }
}
