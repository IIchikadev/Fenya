package socket.render;

import socket.core.Interface;
import socket.core.Socket;
import socket.config.ThemeInfo;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/** Same-frame color difference isolates the hand without touching its transforms or depth. */
public final class HandEffects implements Interface {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(Identifier.of("socket","core/hand_effect"),VertexFormats.POSITION,Defines.EMPTY);
    private static SimpleFramebuffer before, after, finalScene;
    private static boolean captured, ready;
    private static Object world;
    private static long frames;
    private static int sourceFbo;
    private static int copyFbo;
    public static long renderedFrames() { return frames; }
    private HandEffects() {}
    public static void release() {
        captured=ready=false;
        if(before!=null) before.delete(); if(after!=null) after.delete();
        if(finalScene!=null) finalScene.delete();
        before=after=finalScene=null;
        if(copyFbo!=0) GL30.glDeleteFramebuffers(copyFbo);
        copyFbo=0;
    }
    private static boolean enabled() {
        var modules=Socket.getInstance().getProcessors().modules();
        return mc.world!=null && mc.player!=null && mc.options.getPerspective().isFirstPerson() && !mc.options.hudHidden && !modules.optimization().suppressEffects() && (modules.glassHands().m() || modules.shaderHands().m());
    }
    public static void begin() {
        if (IrisBridge.active() && ready) { captured=true; return; }
        captured=false;
        if(!enabled()) { release(); return; }
        var target=mc.getFramebuffer();
        sourceFbo=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int sourceRead=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        if(target.textureWidth<=0 || target.textureHeight<=0) return;
        if(world!=mc.world || before==null || before.textureWidth!=target.textureWidth || before.textureHeight!=target.textureHeight) {
            release(); world=mc.world;
            before=new SimpleFramebuffer(target.textureWidth,target.textureHeight,true);
            after=new SimpleFramebuffer(target.textureWidth,target.textureHeight,false);
            finalScene=new SimpleFramebuffer(target.textureWidth,target.textureHeight,false);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,sourceRead);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,sourceFbo);
        }
        if(IrisBridge.active()) {
            var textures=IrisBridge.handTextures();
            if(textures==null) return;
            copyTexture(textures[0],before);
            copyDepthTexture(textures[2],before);
        } else copyBound(sourceFbo,before);
        captured=true;
    }
    public static void end() {
        if(!captured || !enabled()) return;
        captured=false;
        if(!IrisBridge.active()) copyBound(GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),after);
        ready=true;
        if(!IrisBridge.active()) finish();
    }
    public static void finish() {
        if(!ready || !enabled()) return;
        ready=false;
        var target=mc.getFramebuffer();
        int drawFbo=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        var modules=Socket.getInstance().getProcessors().modules();
        var glass=modules.glassHands(); var shaderHands=modules.shaderHands();
        boolean isGlass=glass.m();
        int color=isGlass ? (glass.colorMode.l("Тема") ? Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor() : glass.color.c()) : shaderHands.color.c();
        try(RenderState state=new RenderState(true)) {
            copyBound(drawFbo,finalScene);
            var shader=RenderSystem.setShader(KEY);
            RenderSystem.setShaderTexture(0,before.getColorAttachment());
            RenderSystem.setShaderTexture(1,after.getColorAttachment());
            RenderSystem.setShaderTexture(2,finalScene.getColorAttachment());
            var textures=IrisBridge.active() ? IrisBridge.handTextures() : null;
            if(IrisBridge.active() && textures==null) return;
            shader.getUniform("DepthMask").set(textures==null ? 0 : 1);
            RenderSystem.setShaderTexture(3,textures==null ? 0 : before.getDepthAttachment());
            RenderSystem.setShaderTexture(4,textures==null ? 0 : textures[2]);
            shader.getUniform("Texel").set(1f/target.textureWidth,1f/target.textureHeight);
            shader.getUniform("Time").set((System.nanoTime()%120_000_000_000L)/1e9f);
            shader.getUniform("Color").set((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);
            shader.getUniform("Style").set(isGlass ? 0 : (shaderHands.mode.l("Новый") ? 2 : 1));
            shader.getUniform("Opacity").set(isGlass ? glass.opacity.c() : (shaderHands.glass.c() ? .65f : 1f));
            shader.getUniform("BlurRadius").set(isGlass && glass.blur.c() ? glass.radius.c() : 0f);
            shader.getUniform("Saturation").set(isGlass ? glass.saturation.c() : shaderHands.saturation.c());
            shader.getUniform("Brightness").set(isGlass ? 0 : shaderHands.brightness.c());
            shader.getUniform("Distortion").set(isGlass ? .002f : shaderHands.distortion.c());
            shader.getUniform("Tint").set(isGlass ? .2f : shaderHands.tint.c());
            shader.getUniform("Glow").set(isGlass ? (glass.outline.c() ? .4f : 0) : (shaderHands.glow.c() ? shaderHands.intensity.c() : 0));
            shader.getUniform("Shimmer").set(isGlass && glass.shimmer.c() ? 1 : 0);
            // Iris has several color outputs bound. Write only the first output;
            // untouched material/depth attachments must survive this color pass.
            int count=drawFbo==0 ? 1 : GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS);
            int[] outputs=new int[count];
            for(int i=0;i<count;i++) outputs[i]=GL11.glGetInteger(GL20.GL_DRAW_BUFFER0+i);
            try { GL20.glDrawBuffers(outputs[0]); WorldEffects.quad(); frames++; }
            finally { GL20.glDrawBuffers(outputs); }
        }
    }
    private static void copyBound(int source, SimpleFramebuffer destination) {
        int read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,source);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,destination.fbo);
            GlStateManager._glBlitFrameBuffer(0,0,destination.textureWidth,destination.textureHeight,0,0,destination.textureWidth,destination.textureHeight,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);
        }
    }
    private static void copyTexture(int texture, SimpleFramebuffer destination) {
        int read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        if(copyFbo==0) copyFbo=GL30.glGenFramebuffers();
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,copyFbo);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,destination.fbo);
            GlStateManager._glBlitFrameBuffer(0,0,destination.textureWidth,destination.textureHeight,0,0,destination.textureWidth,destination.textureHeight,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);
        }
    }
    private static void copyDepthTexture(int texture, SimpleFramebuffer destination) {
        int read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,copyFbo);
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL11.GL_TEXTURE_2D,texture,0);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,destination.fbo);
            GlStateManager._glBlitFrameBuffer(0,0,destination.textureWidth,destination.textureHeight,0,0,destination.textureWidth,destination.textureHeight,GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);
        }
    }
}
