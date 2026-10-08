package runtime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import socket.ui.screen.GUIScreen;
import socket.ui.shader.MenuEffectsShader;
public final class MenuEffectsSmokeTest implements ClientModInitializer {
    private int ticks;
    private GUIScreen screen;
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.getOverlay() != null) return;
            try {
                ticks++;
                if (ticks == 40 || ticks == 100) {
                    screen = new GUIScreen(Text.literal("Menu effects QA"));
                    mc.setScreen(screen);
                }
                if (ticks == 42) ScreenshotRecorder.saveScreenshot(mc.runDirectory, "menu-opening-flash.png", mc.getFramebuffer(), t -> {});
                if (ticks == 70 || ticks == 130) {
                    var field = GUIScreen.class.getDeclaredField("menuEffects");
                    field.setAccessible(true);
                    if (((MenuEffectsShader)field.get(screen)).d() == null) throw new IllegalStateException("Menu shader not loaded");
                    if (org.lwjgl.opengl.GL11.glGetError() != 0) throw new IllegalStateException("OpenGL error");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory, "menu-effects-" + ticks + ".png", mc.getFramebuffer(), t -> {});
                    mc.setScreen(null);
                    if (ticks == 130) { System.out.println("PASS Menu effects shader, render and reopen"); mc.scheduleStop(); }
                }
            } catch (Throwable error) { error.printStackTrace(); System.out.println("FAIL Menu effects QA"); mc.scheduleStop(); }
        });
    }
}
