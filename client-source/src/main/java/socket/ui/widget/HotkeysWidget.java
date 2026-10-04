package socket.ui.widget;

import socket.config.ThemeInfo;
import socket.core.Socket;
import socket.core.GlobalEvent;
import socket.core.Interface;
import socket.core.Module;
import socket.event.DrawEvent;
import socket.render.ColorUtil;
import socket.render.EasingList;
import socket.render.Fonts;
import socket.ui.element.DragInfo;
import socket.util.KeyUtil;
import socket.util.MathUtil;
import net.minecraft.client.gui.screen.ChatScreen;

public class HotkeysWidget extends Widget implements Interface {
    /** Отступ строк от шины: между ними проходит кабель. */
    private static final float INDENT = 5.0f;

    public HotkeysWidget() {
        super(new DragInfo("Клавиши", 0.0f, 0.0f, 0.0f, 0.0f));
        j().setWidget(this);
    }

    @Override
    public void a(DrawEvent event) {
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float x = j().getClampedX();
        float y = j().getClampedY();
        float targetWidth = 14.5f + Fonts.e.a("Hot-keys", this.e) + 5.0f + 2.0f;
        float contentY = y + this.d + 3.0f;
        float rightWidth = Fonts.a.a("Q", 6.5f);
        boolean active = false;
        for (Module module : Socket.getInstance().getProcessors().modules().e()) {
            if (module.p() != -1 && module.f().c() > 0.0f) {
                active = true;
                targetWidth = Math.max(targetWidth, INDENT + 19.0f + Fonts.e.a(module.j(), 6.5f) + 8.0f + Fonts.e.a(KeyUtil.b(module.p()), 6.5f) + 4.0f + rightWidth + 5.0f + 2.0f);
            }
        }
        float width = MathUtil.c(j().getWidth(), targetWidth, 0.5f);
        j().setWidth(width);
        a(event, "Q", "Hot-keys", width, a());
        for (Module module2 : Socket.getInstance().getProcessors().modules().e()) {
            module2.f().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
            float animation = module2.p() != -1 ? module2.f().c() * a() : 0.0f;
            if (animation > 0.0f) {
                float offsetX = (-8.0f) * (1.0f - animation);
                float offsetY = -(1.0f - animation);
                float drawY = contentY + offsetY;
                float rowX = x + offsetX + INDENT;
                float rowWidth = width - INDENT;
                float bindWidth = Fonts.e.a(KeyUtil.b(module2.p()), 6.5f);
                float rightIconX = ((((x + offsetX) + width) - 5.0f) - rightWidth) - 1.0f;
                float textY = (drawY + ((11.5f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
                HudSkin.cable(event, x + offsetX + 2.375f, (y + this.d) - 3.0f, drawY, 11.5f, animation);
                a(event, rowX, drawY, rowWidth, 11.5f, false, animation);
                a(event, rowX + 14.0f, drawY, 11.5f, animation);
                Fonts.a.a(event.h(), module2.l().a(), rowX + 4.0f, (drawY + ((11.5f - Fonts.a.a(6.5f)) / 2.0f)) - 0.15f, 6.5f, ColorUtil.applyAlphaToColor(Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor(), animation));
                Fonts.e.a(event.h(), module2.j(), rowX + 18.0f, textY, 6.5f, ColorUtil.applyAlphaToColor(Socket.getInstance().getProcessors().themes().a(ThemeInfo.TEXT).toIntColor(), animation));
                Fonts.e.a(event.h(), KeyUtil.b(module2.p()), (rightIconX - 4.0f) - bindWidth, textY, 6.5f, ColorUtil.applyAlphaToColor(Socket.getInstance().getProcessors().themes().a(ThemeInfo.TEXT).toIntColor(), 0.55f * animation));
                Fonts.a.a(event.h(), "C", rightIconX, drawY + ((11.5f - Fonts.a.a(6.5f)) / 2.0f) + 0.15f, 6.5f, ColorUtil.applyAlphaToColor(Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor(), animation));
                contentY += 13.5f * animation;
            }
        }
        j().setHeight(active ? (contentY - y) - 2.0f : this.d);
        super.a(event);
    }

    @Override
    public void a(GlobalEvent event) {
        boolean visible = mc.currentScreen instanceof ChatScreen;
        for (Module module : Socket.getInstance().getProcessors().modules().e()) {
            if (module.p() != -1 && module.f().c() > 0.0f) {
                visible = true;
                break;
            }
        }
        d().a(visible);
        super.a(event);
    }
}
