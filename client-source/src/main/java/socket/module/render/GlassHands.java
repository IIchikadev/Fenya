package socket.module.render;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.Socket;
import socket.setting.*;

@ModuleRegister(name="Glass Hands", description="Прозрачные стеклянные руки с размытием и светлой кромкой", category=Category.Render)
public final class GlassHands extends Module {
    public final SliderSetting opacity = new SliderSetting("Плотность стекла", .22f, .05f, 1, .01f);
    public final BooleanSetting blur = new BooleanSetting("Размытие", true);
    public final SliderSetting radius = new SliderSetting("Радиус размытия", 2.5f, 1, 5, .5f);
    public final SliderSetting saturation = new SliderSetting("Насыщенность", 0, 0, 2, .1f);
    public final BooleanSetting outline = new BooleanSetting("Светящаяся кромка", true);
    public final BooleanSetting shimmer = new BooleanSetting("Бегущий блик", true);
    public final ModeSetting colorMode = new ModeSetting("Цвет", "Тема", "Тема", "Свой");
    public final ColorSetting color = new ColorSetting("Свой цвет", 0xFF70DAFF);
    public GlassHands() { a(opacity, blur, radius, saturation, outline, shimmer, colorMode, color); }
    @Override public void b() {
        Socket.getInstance().getProcessors().modules().shaderHands().a(false);
        super.b();
    }
}
