package socket.render;
import socket.core.Interface;
import socket.core.Socket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
/** Camera reprojection adapted from the supplied MotionBlur; no temporal color trails. */
public final class CameraMotionBlur implements Interface {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(Identifier.of("socket", "core/camera_motion_blur"), VertexFormats.POSITION, Defines.EMPTY);
    private static final Matrix4f view = new Matrix4f(), projection = new Matrix4f(), previousView = new Matrix4f(), previousProjection = new Matrix4f();
    private static final Vector3f position = new Vector3f(), previousPosition = new Vector3f();
    private static SimpleFramebuffer scene;
    private static boolean captured, ready;
    private static Object world;
    private static long lastFrame;
    private static float compensation = 1;
    private static long renderedFrames;
    public static long renderedFrames() { return renderedFrames; }
    private CameraMotionBlur() {}
    public static void reset() { captured = ready = false; lastFrame = 0; }
    public static void capture(Matrix4f modelView, Matrix4f proj) {
        var module = Socket.getInstance().getProcessors().modules().motionBlur();
        if (Socket.getInstance().getProcessors().modules().optimization().suppressEffects() || !module.m() || mc.world == null || mc.currentScreen != null || mc.gameRenderer.isRenderingPanorama()
            || (!module.thirdPerson.c() && !mc.options.getPerspective().isFirstPerson())) { reset(); return; }
        if (world != mc.world) { reset(); world = mc.world; }
        previousView.set(view); previousProjection.set(projection); previousPosition.set(position);
        view.set(modelView); projection.set(proj);
        var pos = mc.gameRenderer.getCamera().getPos();
        position.set((float)(pos.x % 30000), (float)(pos.y % 30000), (float)(pos.z % 30000));
        ready = captured && position.distanceSquared(previousPosition) <= 16;
        captured = true;
        long now = System.nanoTime(), elapsed = now - lastFrame; lastFrame = now;
        var mode = GLFW.glfwGetVideoMode(GLFW.glfwGetWindowMonitor(mc.getWindow().getHandle()) != 0
            ? GLFW.glfwGetWindowMonitor(mc.getWindow().getHandle()) : GLFW.glfwGetPrimaryMonitor());
        int hz = mode == null ? 60 : Math.max(1, mode.refreshRate());
        compensation = elapsed > 0 ? Math.max(1, Math.min(8, 1_000_000_000f / hz / elapsed)) : 1;
        var target = mc.getFramebuffer();
        if (scene == null || scene.textureWidth != target.textureWidth || scene.textureHeight != target.textureHeight) {
            if (scene != null) scene.delete();
            scene = new SimpleFramebuffer(target.textureWidth, target.textureHeight, true); ready = false;
        }
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, target.fbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.fbo);
        GlStateManager._glBlitFrameBuffer(0,0,target.textureWidth,target.textureHeight,0,0,scene.textureWidth,scene.textureHeight,GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
        target.beginWrite(true);
    }
    public static void render() {
        var module = Socket.getInstance().getProcessors().modules().motionBlur();
        if (!ready || !module.m() || scene == null || module.strength.c() <= 0 || mc.currentScreen != null) return;
        var target = mc.getFramebuffer();
        try (RenderState state = new RenderState(true)) {
            WorldEffects.copy(target, scene); target.beginWrite(true);
            var shader = RenderSystem.setShader(KEY);
            RenderSystem.setShaderTexture(0, scene.getColorAttachment());
            RenderSystem.setShaderTexture(1, scene.getDepthAttachment());
            shader.getUniform("MvInverse").set(new Matrix4f(view).invert());
            shader.getUniform("ProjInverse").set(new Matrix4f(projection).invert());
            shader.getUniform("PrevModelView").set(previousView);
            shader.getUniform("PrevProjection").set(previousProjection);
            shader.getUniform("CameraDelta").set(module.depth.c() ? new Vector3f(position).sub(previousPosition) : new Vector3f());
            shader.getUniform("Strength").set(module.strength.c() * (module.refreshCompensation.c() ? compensation : 1));
            shader.getUniform("Samples").set(Math.min(Math.round(module.samples.c()), Socket.getInstance().getProcessors().modules().optimization().blurSampleLimit()));
            shader.getUniform("Centered").set(module.centered.c() ? 1 : 0);
            shader.getUniform("UseDepth").set(module.depth.c() ? 1 : 0);
            shader.getUniform("ProtectHand").set(IrisBridge.active() ? 1 : 0);
            WorldEffects.quad();
            renderedFrames++;
        } finally { target.beginWrite(true); }
    }
}
