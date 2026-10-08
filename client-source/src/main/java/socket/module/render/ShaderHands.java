package socket.module.render;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.Socket;
import socket.setting.*;

@ModuleRegister(name="Shader Hands", description="Переливающийся шейдер рук и предметов от первого лица", category=Category.Render)
public final class ShaderHands extends Module {
    public final ModeSetting mode = new ModeSetting("Стиль", "Новый", "Новый", "Классический");
    public final BooleanSetting glass = new BooleanSetting("Стеклянная основа", true);
    public final SliderSetting saturation = new SliderSetting("Насыщенность", 1.45f, 0, 3, .05f);
    public final SliderSetting brightness = new SliderSetting("Яркость", .78f, 0, 1, .01f);
    public final SliderSetting distortion = new SliderSetting("Искажение", .012f, 0, .05f, .001f);
    public final SliderSetting tint = new SliderSetting("Сила оттенка", .22f, 0, 1, .01f);
    public final BooleanSetting glow = new BooleanSetting("Свечение", true);
    public final SliderSetting intensity = new SliderSetting("Сила свечения", 1, .1f, 3, .1f);
    public final ColorSetting color = new ColorSetting("Цвет свечения", 0xFFFFFFFF);
    public ShaderHands() { a(mode, glass, saturation, brightness, distortion, tint, glow, intensity, color); }
    @Override public void b() {
        Socket.getInstance().getProcessors().modules().glassHands().a(false);
        super.b();
    }
}
