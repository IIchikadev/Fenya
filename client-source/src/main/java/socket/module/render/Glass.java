package socket.module.render;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.setting.BooleanSetting;
import socket.setting.SliderSetting;

/**
 * Стеклянный режим интерфейса. Сам ничего не рисует: плашки HUD и панели меню спрашивают
 * {@link socket.ui.GlassStyle}, нужно ли сделать заливку прозрачнее и добавить блик.
 */
@ModuleRegister(name = "Glass", description = "Стеклянный интерфейс: прозрачные размытые панели с бликом", category = Category.Render)
public class Glass extends Module {
    private final SliderSetting density = new SliderSetting("Плотность стекла, %", 15.0f, 0.0f, 100.0f, 5.0f);
    private final BooleanSetting highlight = new BooleanSetting("Блик и светлая рамка", true);
    private final BooleanSetting hud = new BooleanSetting("HUD", true);
    private final BooleanSetting menu = new BooleanSetting("Меню", true);

    public Glass() {
        a(this.density, this.highlight, this.hud, this.menu);
    }

    /** Какую долю обычной непрозрачности оставить заливке, от 0 до 1. */
    public float density() {
        return this.density.c().floatValue() / 100.0f;
    }

    public boolean highlight() {
        return this.highlight.c().booleanValue();
    }

    public boolean hud() {
        return this.hud.c().booleanValue();
    }

    public boolean menu() {
        return this.menu.c().booleanValue();
    }
}
