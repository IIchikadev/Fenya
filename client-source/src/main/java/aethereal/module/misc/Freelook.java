package aethereal.module.misc;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.LookEvent;
import aethereal.event.RotationEvent;
import aethereal.event.TickEvent;
import aethereal.setting.BindSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Осмотр по сторонам без поворота модели: камера крутится сама,
 * направление движения игрока при этом не меняется.
 */
@ModuleRegister(name = "Freelook", description = "Осмотр камерой без поворота игрока", category = Category.Animations)
public class Freelook extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "По зажатию", "По зажатию", "По нажатию");
    private final SliderSetting sensitivity = new SliderSetting("Чувствительность", 0.15f, 0.05f, 0.4f, 0.01f);

    private boolean active;
    private Perspective previous;
    private float yaw;
    private float pitch;

    public Freelook() {
        BindSetting bind = new BindSetting("Клавиша", Integer.valueOf(GLFW.GLFW_KEY_V), 0).a(() -> {
            if (this.mode.l("По зажатию")) {
                toggle(true);
            } else {
                toggle(!this.active);
            }
        }).b(() -> {
            if (this.active && this.mode.l("По зажатию")) {
                toggle(false);
            }
        });
        a(bind, this.mode, this.sensitivity);
    }

    @Override
    public void c() {
        super.c();
        if (this.active) {
            toggle(false);
        }
    }

    private void toggle(boolean enable) {
        if (enable == this.active || mc.player == null) {
            return;
        }
        if (enable) {
            this.yaw = mc.player.getYaw();
            this.pitch = mc.player.getPitch();
            this.previous = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        } else if (this.previous != null) {
            mc.options.setPerspective(this.previous);
        }
        this.active = enable;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.active && (mc.currentScreen != null || mc.player == null)) {
            toggle(false);
        }
    }

    @EventTarget
    public void a(LookEvent event) {
        if (!this.active) {
            return;
        }
        float scale = this.sensitivity.c().floatValue();
        this.yaw += (float) (event.getYaw() * scale);
        this.pitch = MathHelper.clamp(this.pitch + ((float) (event.c() * scale)), -90.0f, 90.0f);
        // отменяем ванильный поворот: модель и направление движения остаются на месте
        event.a(true);
    }

    /** Камера берёт наши углы вместо углов игрока. */
    @EventTarget
    public void a(RotationEvent event) {
        if (this.active) {
            event.setYaw(this.yaw);
            event.setPitch(this.pitch);
        }
    }

    public boolean q() {
        return this.active;
    }

    public float r() {
        return this.yaw;
    }

    public float s() {
        return this.pitch;
    }
}
