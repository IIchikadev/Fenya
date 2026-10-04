package socket.render;

import socket.core.Interface;
import socket.core.Socket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Effects run before HUD rendering and never read from their current render target. */
public final class WorldEffects implements Interface {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(Identifier.of("socket", "core/world_effect"), VertexFormats.POSITION, Defines.EMPTY);
    private static SimpleFramebuffer scene, history;
    private static Object world;
    private static boolean validHistory;
    private static long lastFrameNanos;
    private WorldEffects() {}
    public static void release() {
        if (scene != null) scene.delete();
        if (history != null) history.delete();
        scene = history = null;
        validHistory = false;
        lastFrameNanos = 0;
    }
    public static void resetHistory() { validHistory = false; lastFrameNanos = 0; }
    public static void render(boolean beforeHand) {
        var modules = Socket.getInstance().getProcessors().modules();
        var saturation = modules.saturation();
        var bloom = modules.bloom();
        var motion = modules.motionBlur();
        boolean sat = saturation.m() && saturation.worldOnly.c() == beforeHand;
        boolean glow = bloom.m() && bloom.worldOnly.c() == beforeHand;
        boolean blur = motion.m() && !beforeHand && mc.currentScreen == null;
        if (world != mc.world) { world = mc.world; validHistory = false; }
        if (mc.currentScreen != null || !motion.m()) validHistory = false;
        int w = mc.getWindow().getFramebufferWidth(), h = mc.getWindow().getFramebufferHeight();
        if (mc.world == null || w <= 0 || h <= 0) { release(); return; }
        if (!sat && !glow && !blur) return;
        Framebuffer target = mc.getFramebuffer();
        try (RenderState state = new RenderState(true)) {
            if (scene == null || scene.textureWidth != w || scene.textureHeight != h) {
                release();
                scene = new SimpleFramebuffer(w, h, false);
                history = new SimpleFramebuffer(w, h, false);
            }
            copy(target, scene);
            target.beginWrite(true);
            ShaderProgram shader = RenderSystem.setShader(KEY);
            RenderSystem.setShaderTexture(0, scene.getColorAttachment());
            RenderSystem.setShaderTexture(1, history.getColorAttachment());
            shader.getUniform("Saturation").set(sat ? saturation.amount.c() : 1f);
            shader.getUniform("Threshold").set(bloom.threshold.c());
            shader.getUniform("Glow").set(glow ? bloom.intensity.c() * bloom.strength.c() : 0f);
            shader.getUniform("Texel").set(1f / w, 1f / h);
            long now = System.nanoTime();
            double frameSeconds = lastFrameNanos == 0 ? 1d / 60 : Math.max(.001, Math.min(.25, (now - lastFrameNanos) / 1_000_000_000d));
            float historyMix = (float)Math.pow(motion.amount.c() / 100f, frameSeconds * 60);
            shader.getUniform("HistoryMix").set(blur && validHistory ? historyMix : 0f);
            if (blur && !validHistory) copy(scene, history);
            target.beginWrite(true);
            quad();
            if (blur) { copy(target, history); validHistory = true; lastFrameNanos = now; }
        } finally { target.beginWrite(true); }
    }
    public static void copy(Framebuffer source, Framebuffer destination) {
        // Minecraft caches read/draw bindings. Raw glBindFramebuffer leaves that cache
        // stale, so beginWrite may skip rebinding and draw back into the sampled texture.
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.fbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destination.fbo);
        GlStateManager._glBlitFrameBuffer(0, 0, source.textureWidth, source.textureHeight, 0, 0, destination.textureWidth, destination.textureHeight, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
    }
    public static void quad() {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(-1, -1, 0); buffer.vertex(1, -1, 0); buffer.vertex(1, 1, 0); buffer.vertex(-1, 1, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
