package socket.ui;

import socket.core.Socket;
import socket.module.render.Glass;
import socket.render.ColorUtil;
import socket.render.Draw2DProcessor;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector4f;

/**
 * Общие правила стеклянного режима ({@link Glass}). Фон под панелями и так размыт шейдером,
 * поэтому «стекло» — это более прозрачная заливка, матовый блик сверху вниз и светлая рамка.
 */
public final class GlassStyle {
    private static boolean menuText;

    public static void setMenuText(boolean enabled) {
        menuText = enabled;
    }

    public static boolean menuText() {
        return menuText;
    }

    public static int textColor(int color) {
        int red = (color >>> 16) & 255;
        int green = (color >>> 8) & 255;
        int blue = color & 255;
        if (Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)) < 50) {
            return (color & 0xFF000000) | 0x00FFFFFF;
        }
        return color;
    }

    private GlassStyle() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Glass module() {
        Socket socket = Socket.getInstance();
        if (socket == null || socket.getProcessors() == null || socket.getProcessors().modules() == null) {
            return null;
        }
        return socket.getProcessors().modules().glass();
    }

    /** Стекло включено для плашек HUD. */
    public static boolean hud() {
        Glass glass = module();
        return glass != null && glass.m() && glass.hud();
    }

    /** Стекло включено для панелей меню. */
    public static boolean menu() {
        Glass glass = module();
        return glass != null && glass.m() && glass.menu();
    }

    /** Делаем заливку прозрачнее, сохраняя её цвет. */
    public static int tint(int color) {
        Glass glass = module();
        if (glass == null) {
            return color;
        }
        int alpha = (color >>> 24) & 0xFF;
        return ColorUtil.combineColorWithAlpha(color, Math.round(alpha * glass.density()));
    }

    /** Нейтральный тёмный оттенок меню сохраняет контраст даже на светлой теме. */
    public static int menuTint(int color) {
        Glass glass = module();
        if (glass == null) {
            return color;
        }
        return ColorUtil.applyAlphaToColor(0x171A20, 0.28f + glass.density() * 0.42f);
    }

    /** Прозрачность размытого слоя: он не должен перекрывать мир под стеклом. */
    public static float blurAlpha(float animation) {
        Glass glass = module();
        return glass == null ? animation : animation * (0.12f + glass.density() * 0.25f);
    }

    /** Блик и светлая рамка поверх уже нарисованной плашки. */
    public static void sheen(Draw2DProcessor draw, MatrixStack matrices, float x, float y, float width, float height,
                             float radius, float animation) {
        Glass glass = module();
        if (glass == null || !glass.highlight() || animation <= 0.0f) {
            return;
        }
        int top = ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.035f * animation);
        int bottom = ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.0f);
        draw.a(matrices, x, y, width, height, new Vector4f(radius, radius, radius, radius), top, top, bottom, bottom);
        draw.a(matrices, x, y, width, height, radius, 0.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.18f * animation));
    }
}
