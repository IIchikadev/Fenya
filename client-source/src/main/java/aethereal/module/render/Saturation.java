package aethereal.module.render;
import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.*;
@ModuleRegister(name = "Saturation", description = "Насыщенность цветов мира", category = Category.Render)
public class Saturation extends Module {
    public final SliderSetting amount = new SliderSetting("Насыщенность", 1, 0, 2, .05f);
    public final BooleanSetting worldOnly = new BooleanSetting("Только мир (без рук)", false);
    public Saturation() { a(amount, worldOnly); }
}
