package socket.ui.widget;

import socket.config.ThemeInfo;
import socket.core.GlobalEvent;
import socket.core.Interface;
import socket.core.Socket;
import socket.event.DrawEvent;
import socket.render.ColorUtil;
import socket.render.EasingList;
import socket.render.Fonts;
import socket.setting.BooleanSetting;
import socket.ui.element.DragInfo;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;

/** Визуализация нажатий: WASD, пробел, кнопки мыши и Shift. */
public class KeyStrokesWidget extends Widget implements Interface {
    private static final float CELL = 15.0f;
    private static final float GAP = 1.5f;

    private final BooleanSetting mouse = new BooleanSetting("Кнопки мыши", true);
    private final BooleanSetting space = new BooleanSetting("Пробел", true);
    private final BooleanSetting sneak = new BooleanSetting("Shift", false);
    private final float[] presses = new float[8];

    public KeyStrokesWidget() {
        super(new DragInfo("Клавиатура", 0.0f, 0.0f, 0.0f, 0.0f));
        j().setWidget(this);
        j().setDragStatus(1);
        a(this.mouse, this.space, this.sneak);
    }

    @Override
    public void a(GlobalEvent event) {
        d().a(true);
        super.a(event);
    }

    @Override
    public void a(DrawEvent event) {
        if (!event.b() || mc.player == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float width = (CELL * 3.0f) + (GAP * 2.0f);
        int rows = 2;
        if (this.space.c().booleanValue()) {
            rows++;
        }
        if (this.mouse.c().booleanValue()) {
            rows++;
        }
        if (this.sneak.c().booleanValue()) {
            rows++;
        }
        j().setWidth(width);
        j().setHeight((CELL * rows) + (GAP * (rows - 1)));
        float x = j().getClampedX();
        float y = j().getClampedY();
        float animation = a();
        if (animation <= 0.0f) {
            super.a(event);
            return;
        }
        a(event, x + CELL + GAP, y, CELL, "W", mc.options.forwardKey, 0, animation);
        float second = y + CELL + GAP;
        a(event, x, second, CELL, "A", mc.options.leftKey, 1, animation);
        a(event, x + CELL + GAP, second, CELL, "S", mc.options.backKey, 2, animation);
        a(event, x + (CELL + GAP) * 2.0f, second, CELL, "D", mc.options.rightKey, 3, animation);
        float cursor = second + CELL + GAP;
        if (this.space.c().booleanValue()) {
            a(event, x, cursor, width, "—", mc.options.jumpKey, 4, animation);
            cursor += CELL + GAP;
        }
        if (this.sneak.c().booleanValue()) {
            a(event, x, cursor, width, "SHIFT", mc.options.sneakKey, 5, animation);
            cursor += CELL + GAP;
        }
        if (this.mouse.c().booleanValue()) {
            float half = (width - GAP) / 2.0f;
            a(event, x, cursor, half, "ЛКМ", mc.options.attackKey, 6, animation);
            a(event, x + half + GAP, cursor, half, "ПКМ", mc.options.useKey, 7, animation);
        }
        super.a(event);
    }

    private void a(DrawEvent event, float x, float y, float width, String label, KeyBinding key, int index, float animation) {
        boolean pressed = key != null && key.isPressed();
        this.presses[index] += ((pressed ? 1.0f : 0.0f) - this.presses[index]) * 0.35f;
        float press = this.presses[index];
        HudSkin.innerPanel(event, x, y, width, CELL, animation);
        int accent = Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
        if (press > 0.01f) {
            event.getDraw2DProcessor().a(event.h(), x, y, width, CELL, 2.0f,
                    ColorUtil.applyAlphaToColor(accent, 0.35f * press * animation));
        }
        int textColor = ColorUtil.lerpColor(
                Socket.getInstance().getProcessors().themes().a(ThemeInfo.TEXT).toIntColor(), accent, press);
        float size = 6.5f;
        Fonts.e.a(event.h(), label, x + ((width - Fonts.e.a(label, size)) / 2.0f),
                (y + ((CELL - Fonts.e.a(size)) / 2.0f)) - 0.5f, size,
                ColorUtil.applyAlphaToColor(textColor, animation));
    }
}
