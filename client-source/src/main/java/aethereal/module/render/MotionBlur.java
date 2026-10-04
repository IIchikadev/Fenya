package aethereal.module.render;
import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.SliderSetting;
import aethereal.render.WorldEffects;
@ModuleRegister(name = "Motion Blur", description = "Смаз мира при движении камеры без шлейфов интерфейса", category = Category.Animations)
public class MotionBlur extends Module {
    public final SliderSetting amount = new SliderSetting("Сила смаза", 45, 5, 85, 5);
    public MotionBlur() { a(amount); }
    @Override public void b() { super.b(); WorldEffects.resetHistory(); }
    @Override public void c() { WorldEffects.release(); super.c(); }
}
