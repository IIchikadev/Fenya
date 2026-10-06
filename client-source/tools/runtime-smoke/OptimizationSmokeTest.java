package runtime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.particle.ParticlesMode;
import socket.core.Socket;
import socket.render.IrisBridge;
import socket.render.CameraMotionBlur;
import java.util.List;
public final class OptimizationSmokeTest implements ClientModInitializer {
    private int ticks;
    private List<Object> baseline;
    private boolean originalShaders;
    private long blurFrames;
    private static List<Object> options(MinecraftClient mc) {
        var o = mc.options;
        return List.of(o.getGraphicsMode().getValue(),o.getCloudRenderMode().getValue(),
            o.getEntityShadows().getValue(),o.getAo().getValue(),o.getBiomeBlendRadius().getValue(),
            o.getEnableVsync().getValue(),o.getEntityDistanceScaling().getValue(),o.getViewDistance().getValue(),
            o.getMaxFps().getValue(),o.getSimulationDistance().getValue(),o.getParticles().getValue());
    }
    private static void require(boolean state, String message) { if (!state) throw new IllegalStateException(message); }
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null) return;
            var opt = Socket.getInstance().getProcessors().modules().optimization();
            try {
                ticks++;
                if (ticks == 40) {
                    opt.a(false);
                    baseline = options(mc); originalShaders = IrisBridge.configuredEnabled();
                    Socket.getInstance().getProcessors().modules().motionBlur().a(true);
                    opt.profile.a("Максимум FPS"); opt.a(true); blurFrames = CameraMotionBlur.renderedFrames();
                }
                if (ticks == 90) {
                    require(mc.options.getViewDistance().getValue()==6,"Max view distance");
                    require(mc.options.getSimulationDistance().getValue()==5,"Max simulation distance");
                    require(mc.options.getParticles().getValue()==ParticlesMode.MINIMAL,"Max particles");
                    require(!IrisBridge.configuredEnabled() && !IrisBridge.active(),"Shader suspension");
                    require(CameraMotionBlur.renderedFrames()==blurFrames,"MotionBlur suppression");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory,"optimization-fps-qa.png",mc.getFramebuffer(),t->{});
                    opt.profile.a("Баланс");
                }
                if (ticks == 130) {
                    require(mc.options.getViewDistance().getValue()==10,"Balanced view");
                    require(mc.options.getSimulationDistance().getValue()==6,"Balanced simulation");
                    require(mc.options.getParticles().getValue()==ParticlesMode.DECREASED,"Balanced particles");
                    require(IrisBridge.configuredEnabled()==originalShaders,"Balanced shader restoration");
                    require(opt.blurSampleLimit()==16,"Balanced sample cap");
                    opt.profile.a("Красиво");
                }
                if (ticks == 160) {
                    require(mc.options.getViewDistance().getValue()==14,"Quality view");
                    require(mc.options.getParticles().getValue()==ParticlesMode.ALL,"Quality particles");
                    opt.a(false);
                }
                if (ticks == 180) {
                    require(options(mc).equals(baseline),"Full option restoration: "+options(mc)+" vs "+baseline);
                    require(IrisBridge.configuredEnabled()==originalShaders,"Shader restoration on disable");
                    opt.profile.a("Вручную"); opt.a(true);
                }
                if (ticks == 205) {
                    require(mc.options.getViewDistance().getValue()==10,"Manual distance");
                    opt.a(false);
                    require(options(mc).equals(baseline),"Manual restoration");
                    require(org.lwjgl.opengl.GL11.glGetError()==0,"OpenGL error");
                    System.out.println("PASS Optimization presets FPS/Balanced/Quality/Manual, effects suspension, exact option and shader restoration");
                    mc.scheduleStop();
                }
            } catch (Throwable error) {
                error.printStackTrace();
                opt.a(false);
                System.out.println("FAIL Optimization runtime QA"); mc.scheduleStop();
            }
        });
    }
}
