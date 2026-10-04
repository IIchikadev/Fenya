package aethereal.module.misc;

import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.render.AnimationUtil;
import aethereal.render.EasingList;
import aethereal.setting.BindSetting;
import aethereal.setting.BooleanSetting;
import aethereal.setting.SliderSetting;
import org.lwjgl.glfw.GLFW;

/** Приближение по клавише: плавно сужает угол обзора, пока клавиша зажата. */
@ModuleRegister(name = "Zoom", description = "Приближение по клавише", category = Category.Animations)
public class Zoom extends Module {
    private final SliderSetting factor = new SliderSetting("Кратность", 4.0f, 1.5f, 10.0f, 0.5f);
    private final SliderSetting speed = new SliderSetting("Скорость", 0.18f, 0.05f, 0.6f, 0.01f);
    private final BooleanSetting toggle = new BooleanSetting("По нажатию, а не по зажатию", false);
    private final AnimationUtil animation = new AnimationUtil();
    private boolean active;

    public Zoom() {
        BindSetting bind = new BindSetting("Клавиша", Integer.valueOf(GLFW.GLFW_KEY_C), 0).a(() -> {
            this.active = this.toggle.c().booleanValue() ? !this.active : true;
        }).b(() -> {
            if (!this.toggle.c().booleanValue()) {
                this.active = false;
            }
        });
        a(bind, this.factor, this.speed, this.toggle);
    }

    @Override
    public void c() {
        super.c();
        this.active = false;
        this.animation.c(0.0f);
    }

    /** Вызывается из миксина камеры: получает ванильный угол обзора и возвращает свой. */
    public float a(float fov) {
        this.animation.a(this.active);
        this.animation.a(0.0f, 1.0f, this.speed.c().floatValue(), EasingList.i, 1.0f);
        float progress = EasingList.p.ease(this.animation.c());
        if (progress <= 0.001f) {
            return fov;
        }
        float target = fov / this.factor.c().floatValue();
        return fov + ((target - fov) * progress);
    }

    public boolean q() {
        return this.active || this.animation.c() > 0.001f;
    }
}
