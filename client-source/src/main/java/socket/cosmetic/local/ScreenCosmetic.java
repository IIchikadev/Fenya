package socket.cosmetic.local;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import socket.core.Socket;
public final class ScreenCosmetic extends Screen {
 private CosmeticType type=CosmeticType.MODEL;private int page;
 public ScreenCosmetic(){super(Text.literal("Косметика"));}
 @Override protected void init(){
  int left=width/2-150,y=55;
  for(var t:CosmeticType.values()){int x=left+t.ordinal()*100;addDrawableChild(ButtonWidget.builder(Text.literal(t.getDisplayName()),b->{type=t;page=0;clearAndInit();}).dimensions(x,28,98,20).build());}
  var module=Socket.getInstance().getProcessors().modules().localCosmetics();
  var list=CosmeticRepository.getInstance().byType(type);
  int rows=Math.max(1,(height-145)/24),perPage=rows*2;
  for(int i=page*perPage;i<Math.min(list.size(),(page+1)*perPage);i++){var item=list.get(i);int n=i-page*perPage;var active=CosmeticManager.getInstance().getActive(type);String label=(active==item?"✓ ":"")+item.getName();addDrawableChild(ButtonWidget.builder(Text.literal(label),b->{module.select(type,item);module.a(true);clearAndInit();}).dimensions(left+(n%2)*150,y+(n/2)*24,148,20).build());}
  addDrawableChild(ButtonWidget.builder(Text.literal("Снять"),b->{module.select(type,null);clearAndInit();}).dimensions(left,height-70,98,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("←"),b->{page=Math.max(0,page-1);clearAndInit();}).dimensions(left+100,height-70,48,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("→"),b->{if((page+1)*perPage<list.size())page++;clearAndInit();}).dimensions(left+150,height-70,48,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Обновить"),b->{CosmeticManager.getInstance().init();page=0;clearAndInit();}).dimensions(left+200,height-70,98,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Готово"),b->close()).dimensions(left,height-44,298,20).build());
 }
 @Override public void render(DrawContext c,int mx,int my,float delta){renderBackground(c,mx,my,delta);super.render(c,mx,my,delta);c.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("Косметика видна локально. Просмотр — F5."),width/2,height-94,0xFFCCCCCC);}
}
