package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BindSetting;
import aethereal.setting.BooleanSetting;
import aethereal.ui.screen.RadialMenuScreen;

@ModuleRegister(name = "Radial Menu", description = "Круговое меню быстрого доступа к модулям", category = Category.Hud)
public class RadialMenu extends Module {
    private final BooleanSetting b = new BooleanSetting("Закрывать при отпускании", true);

    public RadialMenu() {
        BindSetting bind = new BindSetting("Кнопка открытия", 82, 0).a(() -> {
            if (mc.currentScreen == null) {
                mc.setScreen(new RadialMenuScreen());
            } else if (mc.currentScreen instanceof RadialMenuScreen) {
                mc.setScreen(null);
            }
        }).b(() -> {
            if (this.b.c().booleanValue() && (mc.currentScreen instanceof RadialMenuScreen)) {
                mc.setScreen(null);
            }
        });
        a(bind, this.b);
    }
}
