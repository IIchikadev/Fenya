package runtime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import socket.cosmetic.local.*;
import socket.core.Socket;
import socket.event.AttackEvent;
public final class CosmeticTargetSmokeTest implements ClientModInitializer {
 private int ticks;private PigEntity pig;
 private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(mc.world==null||mc.player==null)return;
  try{
   ticks++;
   var modules=Socket.getInstance().getProcessors().modules();var cosmetic=modules.localCosmetics();var target=modules.targetEsp();
   mc.options.pauseOnLostFocus=false;if(ticks<150)mc.setScreen(null);
   if(ticks==40){
    cosmetic.a(true);cosmetic.initialize();var catalog=CosmeticRepository.getInstance().all();
    require(catalog.size()>=12,"Expected 12 cosmetics, found "+catalog.size());
    for(var model:catalog){require(!model.getRenderRoots().isEmpty(),"Empty model "+model.getId());model.loadTexture();require(model.isTextureLoaded(),"Texture missing "+model.getId());for(Object slot:model.getTextureSlots()){var loaded=slot.getClass().getDeclaredMethod("isLoaded");loaded.setAccessible(true);require((boolean)loaded.invoke(slot),"Texture slot missing "+model.getId());}}
    cosmetic.model.a("Гусь");cosmetic.pet.a("Аллей");cosmetic.accessory.a("Крылья");
    mc.options.setPerspective(Perspective.THIRD_PERSON_FRONT);mc.player.setYaw(0);mc.player.setPitch(0);
    pig=new PigEntity(EntityType.PIG,mc.world);pig.setPosition(mc.player.getX()+1.5,mc.player.getY()+1,mc.player.getZ());mc.world.addEntity(pig);target.a(true);target.hold.a(10f);target.attack(new AttackEvent(pig));
   }
   if(ticks==75){require(CosmeticRenderer.renderedFrames()>10,"Cosmetic renderer did not run");require(target.renderedFrames()>5,"TargetESP renderer did not run");ScreenshotRecorder.saveScreenshot(mc.runDirectory,"cosmetic-goose-target-qa.png",mc.getFramebuffer(),t->{});cosmetic.model.a("Мику");target.texture.a("Маркер");}
   if(ticks==100){ScreenshotRecorder.saveScreenshot(mc.runDirectory,"cosmetic-miku-target-qa.png",mc.getFramebuffer(),t->{});cosmetic.model.a("Тето");}
   if(ticks==125){ScreenshotRecorder.saveScreenshot(mc.runDirectory,"cosmetic-teto-qa.png",mc.getFramebuffer(),t->{});cosmetic.model.a("Нет");}
   if(ticks==150){ScreenshotRecorder.saveScreenshot(mc.runDirectory,"cosmetic-wings-pet-qa.png",mc.getFramebuffer(),t->{});mc.setScreen(new ScreenCosmetic());}
   if(ticks==170){ScreenshotRecorder.saveScreenshot(mc.runDirectory,"cosmetic-catalog-qa.png",mc.getFramebuffer(),t->{});require(org.lwjgl.opengl.GL11.glGetError()==0,"OpenGL error");System.out.println("PASS Cosmetic catalog/textures/model/pet/wings/menu and TargetESP both textures");mc.scheduleStop();}
  }catch(Throwable error){error.printStackTrace();System.out.println("FAIL Cosmetic/TargetESP runtime QA");mc.scheduleStop();}
 });}
}
