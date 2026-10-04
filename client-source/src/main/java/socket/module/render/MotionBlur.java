package socket.module.render;
import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.setting.SliderSetting;
import socket.render.WorldEffects;
@ModuleRegister(name = "Motion Blur", description = "Смаз мира при движении камеры без шлейфов интерфейса", category = Category.Animations)
public class MotionBlur extends Module {
    public final SliderSetting amount = new SliderSetting("Сила смаза", 45, 5, 85, 5);
    public MotionBlur() { a(amount); }
    @Override public void b() { super.b(); WorldEffects.resetHistory(); }
    @Override public void c() { WorldEffects.release(); super.c(); }
}
