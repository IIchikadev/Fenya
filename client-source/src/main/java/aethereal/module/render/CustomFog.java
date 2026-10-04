package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.AmbienceEvent;
import aethereal.setting.*;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Fog;

@ModuleRegister(name = "CustomFog", description = "Цвет и дистанция тумана", category = Category.Render)
public class CustomFog extends Module {
    private final BooleanSetting range = new BooleanSetting("Своя дистанция", true);
    private final SliderSetting distance = new SliderSetting("Конец тумана", 128, 1, 1024, 1);
    private final ColorSetting color = new ColorSetting("Цвет тумана", 0xff8fb7ff);
    private final BooleanSetting underwater = new BooleanSetting("Не трогать под водой", true);
    public CustomFog() { a(range, distance, color, underwater); }
    @EventTarget
    public void fog(AmbienceEvent.b event) {
        if (underwater.c() && event.getCamera().getSubmersionType() != CameraSubmersionType.NONE) return;
        Fog f = event.d();
        int c = color.c();
        float alpha = (c >>> 24) / 255f;
        event.a(range.c() ? 0 : f.start(), range.c() ? distance.c() : f.end(), f.shape(),
                f.red() + (((c >> 16 & 255) / 255f) - f.red()) * alpha,
                f.green() + (((c >> 8 & 255) / 255f) - f.green()) * alpha,
                f.blue() + (((c & 255) / 255f) - f.blue()) * alpha, f.alpha());
    }
}
