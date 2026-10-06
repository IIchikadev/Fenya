package runtime;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import socket.core.Socket;
import socket.render.IrisBridge;
import socket.ui.screen.GUIScreen;

/** Test entrypoint, added only by miniature-smoke.init.gradle. */
public final class ShaderIntegrationSmokeTest implements ClientModInitializer {
    private int ticks;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null) return;
            try {
                ticks++;
                var motion = Socket.getInstance().getProcessors().modules().motionBlur();
                if (ticks == 40) motion.a(true);
                if (ticks > 40 && ticks < 100) mc.player.setYaw(mc.player.getYaw() + 2);
                if (ticks == 85) {
                    motion.depth.a(false); motion.centered.a(true);
                }
                if (ticks == 90) ScreenshotRecorder.saveScreenshot(mc.runDirectory, "motionblur-moving-qa.png", mc.getFramebuffer(), text -> {});
                if (ticks == 95) { motion.depth.a(true); }

                if (ticks == 100) {
                    if (socket.render.CameraMotionBlur.renderedFrames() < 20) throw new IllegalStateException("MotionBlur pass did not render");
                    int glError = org.lwjgl.opengl.GL11.glGetError();
                    if (glError != 0) throw new IllegalStateException("OpenGL error " + glError);
                    if (!IrisBridge.active()) throw new IllegalStateException("Shader pack is not active");
                    Object error = Class.forName("net.irisshaders.iris.Iris").getMethod("getStoredError").invoke(null);
                    if (((java.util.Optional<?>) error).isPresent()) throw new IllegalStateException(error.toString());
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory, "motionblur-world-qa.png", mc.getFramebuffer(), text -> {});
                    mc.setScreen(new GUIScreen(Text.literal("Shader QA")));
                }
                if (ticks == 120) {
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory, "motionblur-glass-qa.png", mc.getFramebuffer(), text -> {});
                    IrisBridge.setEnabled(false);
                    if (IrisBridge.active()) throw new IllegalStateException("Shader toggle off failed");
                    mc.setScreen(null);
                }
                if (ticks == 135) IrisBridge.setEnabled(true);
                if (ticks > 125 && ticks < 145) mc.player.setYaw(mc.player.getYaw() + 2);
                if (ticks == 145) {
                    if (!IrisBridge.active()) throw new IllegalStateException("Shader toggle on failed");
                    System.out.println("PASS MotionBlur depth/rotation/centered with Iris and shader toggle");
                    mc.scheduleStop();
                }
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("FAIL MakeUp runtime QA");
                mc.scheduleStop();
            }
        });
    }
}
