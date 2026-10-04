package aethereal.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Restores render state after offscreen passes and immediate GUI drawing. */
public final class RenderState implements AutoCloseable {
    private final net.minecraft.client.gl.ShaderProgram shader = RenderSystem.getShader();
    private final int texture0 = RenderSystem.getShaderTexture(0);
    private final int texture1 = RenderSystem.getShaderTexture(1);
    private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean mask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
    public RenderState(boolean offscreen) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (offscreen) {
            RenderSystem.disableBlend();
            GlStateManager._disableScissorTest();
        }
    }
    @Override public void close() {
        RenderSystem.setShader(shader);
        RenderSystem.setShaderTexture(0, texture0);
        RenderSystem.setShaderTexture(1, texture1);
        if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        RenderSystem.depthMask(mask);
        if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
        if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        if (scissor) GlStateManager._enableScissorTest(); else GlStateManager._disableScissorTest();
    }
}
