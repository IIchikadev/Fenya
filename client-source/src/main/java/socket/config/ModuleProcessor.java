package socket.config;


import socket.core.Socket;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.Processor;
import socket.event.KeyEvent;
import socket.lib.json.JSONArray;
import socket.lib.json.JSONObject;
import socket.module.combat.*;
import socket.module.misc.*;
import socket.module.movement.*;
import socket.module.player.*;
import socket.module.render.*;
import socket.render.Animations;
import socket.setting.BindSetting;
import socket.setting.Setting;
import socket.ui.screen.RadialScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModuleProcessor extends ConfigProcessor<Module> {
    private final GlassHands glassHands = new GlassHands();
    private final ShaderHands shaderHands = new ShaderHands();
    private final TotemGhost totemGhost = new TotemGhost();
    private final ScanWorld scanWorld = new ScanWorld();
    private final LockSlot lockSlot = new LockSlot();
    public GlassHands glassHands() { return glassHands; }
    public ShaderHands shaderHands() { return shaderHands; }
    public TotemGhost totemGhost() { return totemGhost; }
    public ScanWorld scanWorld() { return scanWorld; }
    public LockSlot lockSlot() { return lockSlot; }
    private final AuctionHelper auctionHelper = new AuctionHelper();
    private final AutoResell autoResell = new AutoResell();
    private final Saturation saturation = new Saturation();
    private final Bloom bloom = new Bloom();
    private final SkyShader skyShader = new SkyShader();
    private final CustomFog customFog = new CustomFog();
    private final FakePlayer fakePlayer = new FakePlayer();
    private final ChunkAnimator chunkAnimator = new ChunkAnimator();
    public AuctionHelper auctionHelper() { return auctionHelper; }
    public Saturation saturation() { return saturation; }
    public Bloom bloom() { return bloom; }
    public MotionBlur motionBlur() { return bp; }
    public SkyShader skyShader() { return skyShader; }
    public ChunkAnimator chunkAnimator() { return chunkAnimator; }
    private final FastPlace fastPlace = new FastPlace();
    public FastPlace fastPlace() { return fastPlace; }
    private final Glass glass = new Glass();
    private final WorldShaders worldShaders = new WorldShaders();
    public Glass glass() { return glass; }
    private final ContainerPreview containerPreview = new ContainerPreview();
    public ContainerPreview containerPreview() { return containerPreview; }
    private final InventoryProfiles inventoryProfiles = new InventoryProfiles();
    public InventoryProfiles inventoryProfiles() { return inventoryProfiles; }
    private final Sprint o = new Sprint();
    private final SoundReducer r = new SoundReducer();
    private final DeathCoords J = new DeathCoords();
    private final AutoSwap L = new AutoSwap();
    private final Animations Q = new Animations();
    private final SwingAnimation R = new SwingAnimation();
    private final ItemPhysic ag = new ItemPhysic();
    private final Removals ai = new Removals();
    private final ViewModel ao = new ViewModel();
    private final ChatHelper as = new ChatHelper();
    private final Sounds at = new Sounds();
        private final ChinaHat ay = new ChinaHat();
    private final AspectRatio aB = new AspectRatio();
    private final StreamerMode aE = new StreamerMode();
        private final FullBright aO = new FullBright();
                        private final RadialMenu bj = new RadialMenu();
                        private final MotionBlur bp = new MotionBlur();
    private final socket.cosmetic.local.CosmeticModule localCosmetics = new socket.cosmetic.local.CosmeticModule();
    public socket.cosmetic.local.CosmeticModule localCosmetics(){return localCosmetics;}
    private final TargetESP targetEsp = new TargetESP();
    public TargetESP targetEsp(){return targetEsp;}
    private final Optimization bq = new Optimization();
    public Optimization optimization() { return bq; }
    private final Adornments br = new Adornments();
    private final EnchantGlow bs = new EnchantGlow();
    private final Cape bt = new Cape();
    private final Zoom cb = new Zoom();
    private final Freelook ch = new Freelook();
    private final ItemInfo cc = new ItemInfo();
    private final ItemHighlight cd = new ItemHighlight();
    private final ElytraSwap ce = new ElytraSwap();
    private final ItemScroller cf = new ItemScroller();
    private final InventorySort cg = new InventorySort();
                            private Interface bd;

    public static void a(JSONObject obj, Module module) {
        module.a(obj.a("activated", false));
        module.a(obj.a("bind", -1));
        if (obj.m("settings")) {
            JSONObject settingsObj = obj.j("settings");
            for (Setting<?> setting : module.e()) {
                if (settingsObj.m(setting.i())) {
                    ConverterUtil.a(setting, settingsObj.a(setting.i()));
                }
            }
        }
    }

    @Override

    public void setup() {
        this.bd = new Interface();
        a(glassHands, shaderHands, totemGhost, scanWorld, lockSlot, targetEsp, localCosmetics, auctionHelper, autoResell, saturation, bloom, skyShader, customFog, fakePlayer, chunkAnimator, fastPlace, glass, worldShaders, containerPreview, inventoryProfiles);
        a(this.J, this.aE, this.ao, this.Q, this.ag, this.r, this.as, this.R, this.ay, this.L, this.o, this.ai, this.bd, this.at, this.aB, this.aO, this.bj, this.bp, this.bq, this.br, this.bs, this.bt, this.cb, this.cc, this.cd, this.ce, this.cf, this.cg, this.ch);
        super.setup();
    }

    @Override

    protected List<Module> loadConfig(String json) {
        if (json == null || json.isBlank() || json.trim().startsWith("[")) {
            return null;
        }
        JSONObject jSONObject = new JSONObject(json);
        JSONArray jSONArrayI = jSONObject.i("modules");
        if (jSONArrayI == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < jSONArrayI.a(); i++) {
            final JSONObject jSONObjectJ = jSONArrayI.j(i);
            if (jSONObjectJ == null) {
                throw new NullPointerException();
            }
            final String savedName = jSONObjectJ.l("name");
            final String strL = "Miniature Shader".equalsIgnoreCase(savedName) ? "Shaders" : savedName;
            List<Module> listE = e();
            if (listE == null) {
                throw new NullPointerException();
            }
            Stream<Module> stream = listE.stream();
            Predicate<? super Module> predicate = obj -> obj.j().equalsIgnoreCase(strL);
            if (stream == null) {
                throw new NullPointerException();
            }
            Stream<Module> streamFilter = stream.filter(predicate);
            if (streamFilter == null) {
                throw new NullPointerException();
            }
            Optional<Module> optionalFindFirst = streamFilter.findFirst();
            Consumer<? super Module> consumer = obj -> ModuleProcessor.a(jSONObjectJ, obj);
            if (optionalFindFirst == null) {
                throw new NullPointerException();
            }
            optionalFindFirst.ifPresent(consumer);
        }
        return new ArrayList<>(e());
    }

    @Override

    protected String saveConfig(List<Module> data) {
        JSONArray jSONArray = new JSONArray();
        if (data == null) {
            throw new NullPointerException();
        }
        Iterator<Module> it = data.iterator();
        if (it == null) {
            throw new NullPointerException();
        }
        while (it.hasNext()) {
            Module next = it.next();
            JSONObject jSONObject = new JSONObject();
            if (next != null && !(next instanceof Module)) {
                throw new ClassCastException();
            }
            Module module = next;
            if (module == null) {
                throw new NullPointerException();
            }
            jSONObject.c("name", module.j());
            jSONObject.b("activated", module.m());
            jSONObject.b("bind", module.p());
            JSONObject jSONObject2 = new JSONObject();
            List<Setting<?>> listE = module.e();
            if (listE == null) {
                throw new NullPointerException();
            }
            Iterator<Setting<?>> it2 = listE.iterator();
            if (it2 == null) {
                throw new NullPointerException();
            }
            while (it2.hasNext()) {
                Setting<?> next2 = it2.next();
                if (next2 != null && !(next2 instanceof Setting)) {
                    throw new ClassCastException();
                }
                Setting<?> setting = next2;
                if (setting == null) {
                    throw new NullPointerException();
                }
                if (setting.j()) {
                    jSONObject2.c(setting.i(), ConverterUtil.a(setting));
                }
            }
            jSONObject.c("settings", jSONObject2);
            jSONArray.a(jSONObject);
        }
        JSONObject jSONObject4 = new JSONObject();
        jSONObject4.c("modules", jSONArray);
        return jSONObject4.a(2);
    }

    public Sprint sprint() {
        return this.o;
    }

    public SoundReducer soundReducer() {
        return this.r;
    }

    public DeathCoords deathCoords() {
        return this.J;
    }

    public AutoSwap autoSwap() {
        return this.L;
    }

    public Animations animations() {
        return this.Q;
    }

    public SwingAnimation swingAnimation() {
        return this.R;
    }

    public ItemPhysic itemPhysic() {
        return this.ag;
    }

    public Removals removals() {
        return this.ai;
    }

    public ViewModel viewModel() {
        return this.ao;
    }

    public ChatHelper chatHelper() {
        return this.as;
    }

    public Sounds sounds() {
        return this.at;
    }

    public Adornments adornments() {
        return this.br;
    }

    public EnchantGlow enchantGlow() {
        return this.bs;
    }

    public Cape cape() {
        return this.bt;
    }

    public Zoom zoom() {
        return this.cb;
    }

    public Freelook freelook() {
        return this.ch;
    }

    public ItemInfo itemInfo() {
        return this.cc;
    }

    public ItemHighlight itemHighlight() {
        return this.cd;
    }

    public ElytraSwap elytraSwap() {
        return this.ce;
    }

    public ItemScroller itemScroller() {
        return this.cf;
    }

    public InventorySort inventorySort() {
        return this.cg;
    }

    public ChinaHat chinaHat() {
        return this.ay;
    }

    public AspectRatio aspectRatio() {
        return this.aB;
    }

    public StreamerMode streamerMode() {
        return this.aE;
    }

    public FullBright fullBright() {
        return this.aO;
    }

    public Interface interfaceModule() {
        return this.bd;
    }

    @Override
    public void unSetup() {
        super.unSetup();
    }

    @Override
    public File d() {
        return this.b;
    }

    @Override
    protected String getConfigFileName() {
        return "default.json";
    }

    @EventTarget
    public void a(KeyEvent event) {
        int action = event.getAction();
        int key = event.getKey();
        for (Module module : e()) {
            if (module.p() != -1 && module.p() == key && action == 1) {
                module.a();
            }
            if (module.m()) {
                for (Setting<?> setting : module.e()) {
                    if (setting instanceof BindSetting bind) {
                        if (bind.e().get().booleanValue() && bind.c().intValue() != -1 && bind.c().intValue() == key) {
                            if (action == 1) {
                                bind.k().execute();
                            } else if (action == 0 && bind.m() == 0) {
                                bind.l().execute();
                            }
                        }
                    }
                }
            }
        }
    }

    public void b(String configName) {
        try {
            File dir = d();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Files.writeString(new File(dir, configName + ".json").toPath(), saveConfig((List<Module>) this.d));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean c(String str) {
        try {
            File file = new File(d(), str + ".json");
            if (!file.exists()) {
                return false;
            }
            List<Module> listA = loadConfig(Files.readString(file.toPath()));
            if (listA != null) {
                this.d.clear();
                this.d.addAll(listA);
                return true;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean d(String configName) {
        File configFile = new File(d(), configName + ".json");
        if (configFile.exists() && !configName.equals(getConfigFileName())) {
            return configFile.delete();
        }
        return false;
    }

    private void a(Module... modules) {
        Collections.addAll(this.d, modules);
    }
}
