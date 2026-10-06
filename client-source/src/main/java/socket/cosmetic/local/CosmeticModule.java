package socket.cosmetic.local;
import socket.core.*;
import socket.core.Module;
import socket.event.TickEvent;
import socket.setting.*;
import java.util.*;
@ModuleRegister(name="Cosmetic",description="Модели персонажа, питомцы и аксессуары из локального каталога",category=Category.Render)
public class CosmeticModule extends Module {
 private static final Map<String,String> MODEL = choices("Мику","model/miku","Тето","model/teto","Гусь","model/goose","Хаул","model/howlpendragon","Калтист","model/kaltist","REPO","model/repo","Starhorn","model/starhorm");
 private static final Map<String,String> PET = choices("Аллей","pet/allay","Птичка","pet/birb","Мотылёк","pet/mothli","Калтист","pet/kaltist");
 private static final Map<String,String> ACCESSORY = choices("Крылья","accessory/simplewings");
 public final ModeSetting model = new ModeSetting("Модель","Нет",MODEL.keySet().toArray(String[]::new));
 public final ModeSetting pet = new ModeSetting("Питомец","Нет",PET.keySet().toArray(String[]::new));
 public final ModeSetting accessory = new ModeSetting("Аксессуар","Нет",ACCESSORY.keySet().toArray(String[]::new));
 private boolean initialized;
 public CosmeticModule() { a(model,pet,accessory,new ButtonSetting("Каталог косметики",()->mc.execute(()->{initialize();mc.setScreen(new ScreenCosmetic());})),new ButtonSetting("Папка косметики",CosmeticRepository::openCosmeticsFolder),new ButtonSetting("Обновить каталог",()->{CosmeticManager.getInstance().init();initialized=true;update();})); }
 private static Map<String,String> choices(String... values) {var m=new LinkedHashMap<String,String>();m.put("Нет","");for(int i=0;i<values.length;i+=2)m.put(values[i],"builtin:"+values[i+1].replace('/','_'));return m;}
 public void initialize(){if(!initialized){CosmeticManager.getInstance().init();initialized=true;}update();}
 private void update(){var mgr=CosmeticManager.getInstance();mgr.setActive(CosmeticType.MODEL,resolve(MODEL,model.c()));mgr.setActive(CosmeticType.PET,resolve(PET,pet.c()));mgr.setActive(CosmeticType.ACCESSORY,resolve(ACCESSORY,accessory.c()));}
 private CosmeticModel resolve(Map<String,String> map,String key){String id=map.getOrDefault(key,key);return id.isBlank()?null:CosmeticRepository.getInstance().get(id);}
 public void select(CosmeticType type,CosmeticModel choice){var map=type==CosmeticType.MODEL?MODEL:type==CosmeticType.PET?PET:ACCESSORY;var setting=type==CosmeticType.MODEL?model:type==CosmeticType.PET?pet:accessory;String id=choice==null?"":choice.getId();String label=map.entrySet().stream().filter(e->e.getValue().equals(id)).map(Map.Entry::getKey).findFirst().orElse(id);setting.a(label);update();}
 @Override public void b(){super.b();initialize();}
 @EventTarget public void tick(TickEvent event){if(mc.player!=null){initialize();CosmeticManager.getInstance().getAnimator().tick(mc.player);}}
}
