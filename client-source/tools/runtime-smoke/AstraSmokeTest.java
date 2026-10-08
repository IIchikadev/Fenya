package runtime;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.option.Perspective;
import net.minecraft.item.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.screen.slot.SlotActionType;
import socket.core.Socket;
import socket.render.*;

public final class AstraSmokeTest implements ClientModInitializer {
    private int ticks, total;
    private long handFrames;
    private static void require(boolean value,String message) { if(!value) throw new IllegalStateException(message); }
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if(++total>2400) { System.out.println("FAIL Astra world load timed out"); mc.scheduleStop(); }
            if(mc.world==null || mc.player==null) return;
            try {
                ticks++;
                mc.options.pauseOnLostFocus=false; mc.setScreen(null);
                var modules=Socket.getInstance().getProcessors().modules();
                if(ticks==30) {
                    modules.scanWorld().a(false); modules.glassHands().a(false); modules.shaderHands().a(false); modules.lockSlot().a(false); modules.totemGhost().a(false);
                    for(var slot:modules.lockSlot().slots) slot.a(false);
                    modules.lockSlot().onlyPvp.a(false);
                    modules.optimization().a(false);
                    mc.options.setPerspective(Perspective.FIRST_PERSON);
                    mc.player.setPitch(20);
                    modules.scanWorld().a(true); modules.glassHands().a(true); modules.lockSlot().a(true);
                    modules.scanWorld().duration.a(8f);
                    modules.scanWorld().interval.a(1f);
                    mc.player.getInventory().setStack(0,new ItemStack(Items.DIAMOND_SWORD)); mc.player.getInventory().selectedSlot=0;
                    var before=mc.player.getMainHandStack().copy();
                    require(!mc.player.dropSelectedItem(false),"Q drop not blocked");
                    require(ItemStack.areEqual(before,mc.player.getMainHandStack()),"Protected hand changed");
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,36,1,SlotActionType.THROW,mc.player);
                    require(ItemStack.areEqual(before,mc.player.getMainHandStack()),"Inventory drop not blocked");
                    require(modules.lockSlot().protects(new ItemStack(Items.RED_SHULKER_BOX),20),"Shulker protection missing");
                    require(!modules.lockSlot().protects(new ItemStack(Items.DIRT),1),"Unselected slot wrongly protected");
                    modules.lockSlot().slots[1].a(true);
                    require(modules.lockSlot().protects(new ItemStack(Items.DIRT),1),"Selected slot not protected");
                    modules.totemGhost().a(true);
                    var pig=new PigEntity(EntityType.PIG,mc.world); pig.setPosition(mc.player.getPos().add(0,0,4));
                    modules.totemGhost().spawn(pig);
                }
                if(ticks==60) {
                    require(HandEffects.renderedFrames()>5,"Glass Hands pass did not render, Iris="+IrisBridge.active());
                    require(ScanWorldEffect.renderedFrames()>5,"Scan World pass did not render, waves="+modules.scanWorld().waves().size());
                    require(modules.totemGhost().renderedFrames()>5,"Totem Ghost pass did not render");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory,"astra-glass-qa.png",mc.getFramebuffer(),t->{});
                    for(String name:new String[]{"before","after"}) {
                        var field=HandEffects.class.getDeclaredField(name); field.setAccessible(true);
                        ScreenshotRecorder.saveScreenshot(mc.runDirectory,"astra-mask-"+name+".png",(net.minecraft.client.gl.Framebuffer)field.get(null),t->{});
                    }
                    handFrames=HandEffects.renderedFrames(); modules.shaderHands().a(true);
                    require(!modules.glassHands().m(),"Both hand effects active");
                }
                if(ticks==100) {
                    require(HandEffects.renderedFrames()>handFrames+5,"Shader Hands pass did not render");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory,"astra-shader-qa.png",mc.getFramebuffer(),t->{});
                    modules.shaderHands().mode.a("Классический");
                }
                if(ticks==130) {
                    require(org.lwjgl.opengl.GL11.glGetError()==0,"OpenGL error");
                    handFrames=HandEffects.renderedFrames();
                    IrisBridge.setTemporaryEnabled(false);
                    modules.glassHands().a(true);
                }
                if(ticks==165) {
                    require(HandEffects.renderedFrames()>handFrames+5,"Vanilla hand pass did not render");
                    require(org.lwjgl.opengl.GL11.glGetError()==0,"Vanilla OpenGL error");
                    ScreenshotRecorder.saveScreenshot(mc.runDirectory,"astra-vanilla-glass-qa.png",mc.getFramebuffer(),t->{});
                    modules.glassHands().a(false); modules.shaderHands().a(false); modules.scanWorld().a(false); modules.totemGhost().a(false); modules.lockSlot().a(false);
                    System.out.println("PASS Astra five modules render, hand styles with and without Iris, and Q/inventory protection");
                    mc.scheduleStop();
                }
            } catch(Throwable error) { error.printStackTrace(); System.out.println("FAIL Astra runtime QA"); mc.scheduleStop(); }
        });
    }
}
