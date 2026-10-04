package socket.module.render;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.GammaEvent;
import socket.event.TickEvent;
import socket.setting.ModeSetting;
import socket.setting.SliderSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@ModuleRegister(name = "Full Bright", description = "Полностью освещает мир через гамму или ночное зрение", category = Category.Render)
public class FullBright extends Module {
    private final ModeSetting b = new ModeSetting("Режим видения", "Гамма", "Гамма", "Ночное зрение");
    private final SliderSetting c = new SliderSetting("Уровень гаммы", 4.0f, 1.0f, 8.0f, 0.5f).a(() -> {
        return Boolean.valueOf(this.b.l("Гамма"));
    });

    public FullBright() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.l("Ночное зрение")) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 240, 1, false, false, false));
        } else {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventTarget
    public void a(GammaEvent event) {
        if (this.b.l("Гамма")) {
            event.setGamma(this.c.c().floatValue());
        }
    }
}
