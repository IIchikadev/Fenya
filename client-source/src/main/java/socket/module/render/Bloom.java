package socket.module.render;
import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.setting.*;
@ModuleRegister(name = "Bloom", description = "Свечение ярких участков мира", category = Category.Render)
public class Bloom extends Module {
    public final SliderSetting threshold = new SliderSetting("Порог", .9f, 0, 1, .01f);
    public final SliderSetting intensity = new SliderSetting("Интенсивность", 4, 0, 10, .05f);
    public final SliderSetting strength = new SliderSetting("Сила свечения", 3, 0, 8, .05f);
    public final BooleanSetting worldOnly = new BooleanSetting("Только мир (без рук)", true);
    public Bloom() { a(threshold, intensity, strength, worldOnly); }
}
