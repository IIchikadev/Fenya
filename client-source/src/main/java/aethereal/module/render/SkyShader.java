package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.render.RenderState;
import aethereal.render.WorldEffects;
import aethereal.setting.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

@ModuleRegister(name = "SkyShader", description = "Процедурное небо: туманность, сияние, звёзды, плазма, неон", category = Category.Render)
public class SkyShader extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Туманность", "Туманность", "Сияние", "Звёзды", "Плазма", "Неон");
    private final ColorSetting color = new ColorSetting("Цвет", 0xff3399ff);
    private final SliderSetting speed = new SliderSetting("Скорость", 1, .1f, 5, .1f);
    private final SliderSetting size = new SliderSetting("Размер", 5, 1, 20, .5f);
    private final SliderSetting intensity = new SliderSetting("Интенсивность", 1, .1f, 5, .1f);
    private final SliderSetting opacity = new SliderSetting("Прозрачность", 1, .3f, 1, .05f);
    private final ModeSetting palette = new ModeSetting("Палитра неона", "RGB", "RGB", "Тёмные", "Светлые", "Свои");
    private final ColorSetting neon1 = new ColorSetting("Неон цвет 1", 0xffff0000);
    private final ColorSetting neon2 = new ColorSetting("Неон цвет 2", 0xff00ff00);
    private final ColorSetting neon3 = new ColorSetting("Неон цвет 3", 0xff0000ff);
    private static final ShaderProgramKey KEY = new ShaderProgramKey(Identifier.of("socket", "core/sky_effect"), VertexFormats.POSITION, Defines.EMPTY);
    private long start = System.nanoTime();
    public SkyShader() { a(mode, color, speed, size, intensity, opacity, palette, neon1, neon2, neon3); }
    @Override public void b() { start = System.nanoTime(); super.b(); }
    public void render(float red, float green, float blue) {
        if (!m() || mc.world == null) return;
        var camera = mc.gameRenderer.getCamera();
        try (RenderState state = new RenderState(true)) {
            ShaderProgram shader = RenderSystem.setShader(KEY);
            shader.getUniform("Time").set((System.nanoTime() - start) / 1_000_000_000f * speed.c());
            shader.getUniform("Mode").set((float)mode.k().indexOf(mode.c()));
            shader.getUniform("Scale").set(size.c());
            shader.getUniform("Intensity").set(intensity.c());
            shader.getUniform("Opacity").set(opacity.c());
            shader.getUniform("Background").set(red, green, blue);
            setColor(shader, "Neon1", palette.l("Тёмные") ? 0xff191919 : palette.l("Светлые") ? 0xffcccccc : palette.l("Свои") ? neon1.c() : 0xffff0000);
            setColor(shader, "Neon2", palette.l("Тёмные") ? 0xff404040 : palette.l("Светлые") ? 0xfff2f2f2 : palette.l("Свои") ? neon2.c() : 0xff00ff00);
            setColor(shader, "Neon3", palette.l("Тёмные") ? 0xff737373 : palette.l("Светлые") ? 0xffffffff : palette.l("Свои") ? neon3.c() : 0xff0000ff);
            int rgb = color.c();
            shader.getUniform("Tint").set((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
            shader.getUniform("Camera").set((float)Math.toRadians(camera.getYaw()), (float)Math.toRadians(camera.getPitch()));
            shader.getUniform("Aspect").set((float)mc.getWindow().getFramebufferWidth() / Math.max(1, mc.getWindow().getFramebufferHeight()));
            shader.getUniform("Fov").set((float)Math.tan(Math.toRadians(mc.options.getFov().getValue()) * .5));
            WorldEffects.quad();
        }
    }
    private void setColor(ShaderProgram shader, String uniform, int rgb) {
        shader.getUniform(uniform).set((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
    }
}
