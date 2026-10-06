package socket.module.render;
import socket.core.*;
import socket.core.Module;
import socket.event.AttackEvent;
import socket.event.DrawEvent;
import socket.setting.*;
import socket.util.ProjectUtil;
import socket.render.RenderState;
import socket.config.ThemeInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import com.mojang.blaze3d.systems.RenderSystem;
@ModuleRegister(name="TargetESP",description="Вращающийся ромб или маркер вокруг выбранной цели",category=Category.Render)
public class TargetESP extends Module {
 public final ModeSetting texture = new ModeSetting("Текстура","Ромб","Ромб","Маркер");
 public final SliderSetting size = new SliderSetting("Размер",75,25,150,5);
 public final SliderSetting hold = new SliderSetting("После удара, сек",3,0,10,.5f);
 private LivingEntity target;private long expires;private long renderedFrames;
 public long renderedFrames(){return renderedFrames;}
 public TargetESP(){a(texture,size,hold);}
 @Override public void c(){target=null;super.c();}
 @EventTarget public void attack(AttackEvent event){if(event.b() instanceof LivingEntity entity){target=entity;expires=System.nanoTime()+(long)(hold.c()*1_000_000_000d);}}
 @EventTarget public void draw(DrawEvent event){
  if(!event.b()||mc.player==null||mc.world==null||mc.options.hudHidden)return;
  boolean underCrosshair = false;
  if(mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity entity){underCrosshair=true;target=entity;expires=System.nanoTime()+(long)(hold.c()*1_000_000_000d);}
  if(target==null||!target.isAlive()||target.getWorld()!=mc.world||target==mc.player||(!underCrosshair && System.nanoTime()>expires)||!mc.player.canSee(target))return;
  var position=target.getLerpedPos(event.g()).add(0,target.getHeight()/2d,0);
  var screen=ProjectUtil.project(position.x,position.y,position.z);
  if(!Float.isFinite(screen.x)||!Float.isFinite(screen.y)||screen.x<0||screen.y<0||screen.x>mc.getWindow().getScaledWidth()||screen.y>mc.getWindow().getScaledHeight())return;
  var matrices=event.h();matrices.push();
  try(RenderState state=new RenderState(false)){
   RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
   RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
   RenderSystem.setShaderTexture(0,Identifier.of("socket","textures/targetesp/"+(texture.l("Маркер")?"target3.png":"target2.png")));
   matrices.translate(screen.x,screen.y,0);matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(System.nanoTime()/1_000_000_000d)*360));
   var matrix=matrices.peek().getPositionMatrix();float half=size.c()/2;
   int primary=Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
   float[] hsb=java.awt.Color.RGBtoHSB(primary>>16&255,primary>>8&255,primary&255,null);
   var buffer=Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS,VertexFormats.POSITION_TEXTURE_COLOR);
   float[] x={-half,-half,half,half},y={-half,half,half,-half},u={0,0,1,1},v={0,1,1,0};
   for(int i=0;i<4;i++){int color=java.awt.Color.HSBtoRGB(hsb[0]+i*.06f,hsb[1],hsb[2]);buffer.vertex(matrix,x[i],y[i],0).texture(u[i],v[i]).color((color&0xFFFFFF)|0xDC000000);}
   BufferRenderer.drawWithGlobalProgram(buffer.end());renderedFrames++;
  }finally{matrices.pop();}
 }
}
