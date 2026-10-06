package runtime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import socket.core.Socket;
import socket.ui.screen.GUIScreen;
public final class GraphiteSmokeTest implements ClientModInitializer {
 private int ticks;
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
 if(mc.world==null||mc.player==null)return;
 try {ticks++;mc.options.pauseOnLostFocus=false;
 if(ticks==40){Socket.getInstance().getProcessors().modules().glass().a(true);mc.setScreen(new GUIScreen(Text.literal("Graphite QA")));}
 if(ticks==80){ScreenshotRecorder.saveScreenshot(mc.runDirectory,"graphite-menu-qa.png",mc.getFramebuffer(),t->{});if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new IllegalStateException("OpenGL error");System.out.println("PASS Graphite menu rendering");mc.scheduleStop();}
 }catch(Throwable e){e.printStackTrace();System.out.println("FAIL Graphite QA");mc.scheduleStop();}
 });}
}
