package socket.module.render;
import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.setting.SliderSetting;
import socket.setting.BooleanSetting;
import socket.render.CameraMotionBlur;
@ModuleRegister(name = "Motion Blur", description = "Смаз по движению камеры и глубине сцены", category = Category.Animations)
public class MotionBlur extends Module {
    public final SliderSetting strength = new SliderSetting("Сила", 1, 0, 3, .05f);
    public final SliderSetting samples = new SliderSetting("Сэмплы", 32, 4, 128, 1);
    public final BooleanSetting depth = new BooleanSetting("По глубине", true);
    public final BooleanSetting centered = new BooleanSetting("По центру", false);
    public final BooleanSetting thirdPerson = new BooleanSetting("От третьего лица", false);
    public final BooleanSetting refreshCompensation = new BooleanSetting("Компенсация Гц", true);
    public MotionBlur() { a(strength, samples, depth, centered, thirdPerson, refreshCompensation); }
    @Override public void b() { super.b(); CameraMotionBlur.reset(); }
    @Override public void c() { CameraMotionBlur.reset(); super.c(); }
}
