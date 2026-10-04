package aethereal.module.render;

import aethereal.config.ThemeInfo;
import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.core.Socket;
import aethereal.render.ColorUtil;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;

import java.awt.Color;

/**
 * Перекрашивает ванильный блик зачарования в цвет клиента.
 * Сам блик остаётся тем же — меняется только его оттенок и яркость,
 * поэтому эффект виден и в руке, и в инвентаре, и на выпавших предметах.
 */
@ModuleRegister(name = "Enchant Glow", description = "Свой цвет свечения зачарованных предметов", category = Category.Render)
public class EnchantGlow extends Module {
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Тема клиента", "Тема клиента", "Свой цвет", "Радужный");
    private final ColorSetting color;
    private final SliderSetting brightness = new SliderSetting("Яркость", 1.2f, 0.2f, 2.5f, 0.1f);
    private final SliderSetting speed = new SliderSetting("Скорость перелива", 4.0f, 1.0f, 12.0f, 0.5f);

    public EnchantGlow() {
        this.color = new ColorSetting("Свой цвет", Integer.valueOf(ColorUtil.a(60, 231, 255)))
                .a(() -> Boolean.valueOf(this.colorMode.l("Свой цвет")));
        this.speed.a(() -> Boolean.valueOf(this.colorMode.l("Радужный")));
        a(this.colorMode, this.color, this.brightness, this.speed);
    }

    /** Итоговый оттенок блика на текущем кадре. */
    public int q() {
        if (this.colorMode.l("Радужный")) {
            float cycle = ((System.currentTimeMillis() % 10000L) / 10000.0f) * this.speed.c().floatValue();
            return Color.HSBtoRGB(cycle % 1.0f, 0.65f, 1.0f) | (-16777216);
        }
        if (this.colorMode.l("Свой цвет")) {
            return this.color.c().intValue();
        }
        return Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor();
    }

    public float r() {
        return this.brightness.c().floatValue();
    }
}
