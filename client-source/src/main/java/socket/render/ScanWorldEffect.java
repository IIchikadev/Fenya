package socket.render;

import socket.core.Interface;
import socket.core.Socket;
import socket.config.ThemeInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Reconstructs visible world surfaces from copied depth, before the hand clears depth. */
public final class ScanWorldEffect implements Interface {
    private static final ShaderProgramKey KEY=new ShaderProgramKey(Identifier.of("socket","core/scan_world"),VertexFormats.POSITION,Defines.EMPTY);
    private static SimpleFramebuffer scene;
    private static long frames;
    public static long renderedFrames() { return frames; }
    private ScanWorldEffect() {}
    public static void release() { if(scene!=null) scene.delete(); scene=null; }
    public static void render(Matrix4f view, Matrix4f projection) {
        var module=Socket.getInstance().getProcessors().modules().scanWorld();
        if(!module.m() || mc.world==null) { release(); return; }
        if(Socket.getInstance().getProcessors().modules().optimization().suppressEffects()) return;
        var waves=module.waves(); if(waves.isEmpty()) return;
        var target=mc.getFramebuffer();
        if(target.textureWidth<=0 || target.textureHeight<=0) return;
        if(scene==null || scene.textureWidth!=target.textureWidth || scene.textureHeight!=target.textureHeight) {
            release(); scene=new SimpleFramebuffer(target.textureWidth,target.textureHeight,true);
        }
        int color=module.colorMode.l("Тема") ? Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor() : module.color.c();
        var camera=mc.gameRenderer.getCamera().getPos();
        try(RenderState state=new RenderState(true)) {
            WorldEffects.copy(target,scene);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,target.fbo);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,scene.fbo);
            GlStateManager._glBlitFrameBuffer(0,0,target.textureWidth,target.textureHeight,0,0,scene.textureWidth,scene.textureHeight,GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
            target.beginWrite(true);
            var shader=RenderSystem.setShader(KEY);
            RenderSystem.setShaderTexture(0,scene.getColorAttachment());
            RenderSystem.setShaderTexture(1,scene.getDepthAttachment());
            shader.getUniform("InverseViewProjection").set(new Matrix4f(projection).mul(view).invert());
            shader.getUniform("Color").set((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);
            shader.getUniform("Width").set(module.width.c());
            shader.getUniform("Count").set(waves.size());
            for(int i=0;i<12;i++) {
                if(i>=waves.size()) { shader.getUniform("Wave"+i).set(0f,0f,0f,-1f); continue; }
                var wave=waves.get(i);
                float t=(System.nanoTime()-wave.start())/1e9f/module.duration.c();
                shader.getUniform("Wave"+i).set((float)(wave.center().x-camera.x),(float)(wave.center().y-camera.y),(float)(wave.center().z-camera.z),t*module.radius.c());
                shader.getUniform("Fade"+i).set(Math.min(1,(1-t)*4));
            }
            WorldEffects.quad(); frames++;
        } finally { target.beginWrite(true); }
    }
}
