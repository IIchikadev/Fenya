package socket.module.render;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.setting.BooleanSetting;
import socket.setting.MultiModeSetting;
import socket.setting.SliderSetting;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;

@ModuleRegister(name = "Optimization", description = "Поднимает FPS: убирает дорогие эффекты игры и ограничивает дальность прорисовки", category = Category.Misc)
public class Optimization extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Что отключить",
            new BooleanSetting("Быстрая графика", true),
            new BooleanSetting("Облака", true),
            new BooleanSetting("Тени сущностей", true),
            new BooleanSetting("Плавное освещение", false),
            new BooleanSetting("Смешивание биомов", true),
            new BooleanSetting("Вертикальная синхронизация", true));
    private final SliderSetting c = new SliderSetting("Дальность сущностей", 60.0f, 25.0f, 100.0f, 5.0f);
    private final SliderSetting d = new SliderSetting("Дальность прорисовки", 10.0f, 2.0f, 32.0f, 1.0f);
    private final SliderSetting e = new SliderSetting("Лимит FPS", 120.0f, 30.0f, 260.0f, 10.0f);
    private final BooleanSetting f = new BooleanSetting("Управлять дальностью", true);
    private a saved;

    public Optimization() {
        a(this.b, this.f, this.c, this.d, this.e);
    }

    @Override
    public void b() {
        super.b();
        if (mc.options == null) {
            return;
        }
        this.saved = new a(
                mc.options.getGraphicsMode().getValue(),
                mc.options.getCloudRenderMode().getValue(),
                mc.options.getEntityShadows().getValue(),
                mc.options.getAo().getValue(),
                mc.options.getBiomeBlendRadius().getValue(),
                mc.options.getEnableVsync().getValue(),
                mc.options.getEntityDistanceScaling().getValue(),
                mc.options.getViewDistance().getValue(),
                mc.options.getMaxFps().getValue());
        q();
    }

    @Override
    public void c() {
        super.c();
        if (mc.options == null || this.saved == null) {
            return;
        }
        mc.options.getGraphicsMode().setValue(this.saved.a);
        mc.options.getCloudRenderMode().setValue(this.saved.b);
        mc.options.getEntityShadows().setValue(this.saved.c);
        mc.options.getAo().setValue(this.saved.d);
        mc.options.getBiomeBlendRadius().setValue(this.saved.e);
        mc.options.getEnableVsync().setValue(this.saved.f);
        mc.options.getEntityDistanceScaling().setValue(this.saved.h);
        mc.options.getViewDistance().setValue(this.saved.i);
        mc.options.getMaxFps().setValue(this.saved.j);
        this.saved = null;
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        // раз в секунду возвращаем настройки на место, если игрок или игра их сбросили
        if (mc.options != null && (mc.player == null || mc.player.age % 20 == 0)) {
            q();
        }
    }

    private void q() {
        boolean fast = this.b.a("Быстрая графика").c().booleanValue();
        boolean clouds = this.b.a("Облака").c().booleanValue();
        boolean shadows = this.b.a("Тени сущностей").c().booleanValue();
        boolean ao = this.b.a("Плавное освещение").c().booleanValue();
        boolean biome = this.b.a("Смешивание биомов").c().booleanValue();
        boolean vsync = this.b.a("Вертикальная синхронизация").c().booleanValue();
        boolean reload = false;
        if (fast && mc.options.getGraphicsMode().getValue() != GraphicsMode.FAST) {
            mc.options.getGraphicsMode().setValue(GraphicsMode.FAST);
            reload = true;
        }
        if (clouds && mc.options.getCloudRenderMode().getValue() != CloudRenderMode.OFF) {
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        }
        if (shadows && mc.options.getEntityShadows().getValue().booleanValue()) {
            mc.options.getEntityShadows().setValue(Boolean.FALSE);
        }
        if (ao && mc.options.getAo().getValue().booleanValue()) {
            mc.options.getAo().setValue(Boolean.FALSE);
            reload = true;
        }
        if (biome && mc.options.getBiomeBlendRadius().getValue().intValue() != 0) {
            mc.options.getBiomeBlendRadius().setValue(Integer.valueOf(0));
            reload = true;
        }
        if (vsync && mc.options.getEnableVsync().getValue().booleanValue()) {
            mc.options.getEnableVsync().setValue(Boolean.FALSE);
        }
        double entityScale = this.c.c().doubleValue() / 100.0d;
        if (Math.abs(mc.options.getEntityDistanceScaling().getValue().doubleValue() - entityScale) > 0.01d) {
            mc.options.getEntityDistanceScaling().setValue(Double.valueOf(entityScale));
        }
        int fps = this.e.c().intValue();
        if (mc.options.getMaxFps().getValue().intValue() != fps) {
            mc.options.getMaxFps().setValue(Integer.valueOf(fps));
        }
        if (this.f.c().booleanValue()) {
            int view = this.d.c().intValue();
            if (mc.options.getViewDistance().getValue().intValue() != view) {
                mc.options.getViewDistance().setValue(Integer.valueOf(view));
                reload = true;
            }
        }
        if (reload && mc.worldRenderer != null && mc.world != null) {
            mc.worldRenderer.reload();
        }
    }

    private static final class a {
        private final GraphicsMode a;
        private final CloudRenderMode b;
        private final Boolean c;
        private final Boolean d;
        private final Integer e;
        private final Boolean f;
        private final Double h;
        private final Integer i;
        private final Integer j;

        private a(GraphicsMode graphics, CloudRenderMode clouds, Boolean shadows, Boolean ao, Integer biome,
                  Boolean vsync, Double entityScale, Integer view, Integer maxFps) {
            this.a = graphics;
            this.b = clouds;
            this.c = shadows;
            this.d = ao;
            this.e = biome;
            this.f = vsync;
            this.h = entityScale;
            this.i = view;
            this.j = maxFps;
        }
    }
}
