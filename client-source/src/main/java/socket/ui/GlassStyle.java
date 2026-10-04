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

    /** Блик и светлая рамка поверх уже нарисованной плашки. */
    public static void sheen(Draw2DProcessor draw, MatrixStack matrices, float x, float y, float width, float height,
                             float radius, float animation) {
        Glass glass = module();
        if (glass == null || !glass.highlight() || animation <= 0.0f) {
            return;
        }
        int top = ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.16f * animation);
        int bottom = ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.0f);
        draw.a(matrices, x, y, width, height, new Vector4f(radius, radius, radius, radius), top, top, bottom, bottom);
        draw.a(matrices, x, y, width, height, radius, 0.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.32f * animation));
    }
}
