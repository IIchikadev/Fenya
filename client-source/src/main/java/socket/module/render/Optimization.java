package socket.module.render;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.setting.*;
import socket.render.IrisBridge;
import socket.util.ChatUtil;
import net.minecraft.client.option.*;
import net.minecraft.particle.ParticlesMode;

@ModuleRegister(name = "Optimization", description = "Профили FPS, частицы, симуляция и экономия эффектов; настройки восстанавливаются при выключении", category = Category.Misc)
public class Optimization extends Module {
    public final ModeSetting profile = new ModeSetting("Профиль", "Баланс", "Максимум FPS", "Баланс", "Красиво", "Вручную");
    private final MultiModeSetting disabled = new MultiModeSetting("Что отключить",
        new BooleanSetting("Быстрая графика", true), new BooleanSetting("Облака", true),
        new BooleanSetting("Тени сущностей", true), new BooleanSetting("Плавное освещение", false),
        new BooleanSetting("Смешивание биомов", true), new BooleanSetting("Вертикальная синхронизация", true));
    private final SliderSetting entities = new SliderSetting("Дальность сущностей", 60, 25, 100, 5);
    private final SliderSetting distance = new SliderSetting("Дальность прорисовки", 10, 2, 32, 1);
    private final SliderSetting fps = new SliderSetting("Лимит FPS", 260, 30, 260, 10);
    private final BooleanSetting manageDistance = new BooleanSetting("Управлять дальностью", true);
    private final SliderSetting simulation = new SliderSetting("Дальность симуляции", 6, 5, 16, 1);
    private final ModeSetting particles = new ModeSetting("Частицы", "Меньше", "Все", "Меньше", "Минимум");
    private final BooleanSetting effects = new BooleanSetting("Экономить эффекты", true);
    private final BooleanSetting shaders = new BooleanSetting("Отключать шейдеры в FPS", true);
    private Snapshot saved;
    private boolean suspendedShaders;
    private int ticks;

    public Optimization() {
        a(profile, effects, shaders, disabled, manageDistance, entities, distance, simulation, particles, fps);
        for (Setting<?> setting : new Setting<?>[]{disabled, manageDistance, entities, distance, simulation, particles})
            setting.a(() -> profile.l("Вручную"));
        profile.a(value -> { if (m() && saved != null) apply(); });
        effects.a(value -> { if (m() && saved != null) apply(); });
        shaders.a(value -> { if (m() && saved != null) apply(); });
    }

    public boolean suppressEffects() { return m() && effects.c() && profile.l("Максимум FPS"); }
    public int blurSampleLimit() { return m() && effects.c() && profile.l("Баланс") ? 16 : 128; }

    @Override public void b() {
        super.b();
        ticks = 0;
        if (mc.options != null) {
            saved = snapshot();
            apply();
        }
    }
    @Override public void c() {
        super.c();
        if (saved == null || mc.options == null) return;
        applyOptions(saved);
        restoreShaders();
        saved = null;
    }
    @EventTarget public void tick(TickEvent event) {
        // No repeated terrain rebuilds: SimpleOption is written only on actual changes.
        if (++ticks % 20 == 0 && mc.options != null) {
            if (saved == null) saved = snapshot();
            apply();
        }
    }
    private Snapshot snapshot() {
        var o = mc.options;
        return new Snapshot(o.getGraphicsMode().getValue(), o.getCloudRenderMode().getValue(),
            o.getEntityShadows().getValue(), o.getAo().getValue(), o.getBiomeBlendRadius().getValue(),
            o.getEnableVsync().getValue(), o.getEntityDistanceScaling().getValue(),
            o.getViewDistance().getValue(), o.getMaxFps().getValue(),
            o.getSimulationDistance().getValue(), o.getParticles().getValue());
    }
    private void apply() {
        if (saved == null) return;
        Snapshot target;
        if (profile.l("Максимум FPS")) {
            target = new Snapshot(GraphicsMode.FAST, CloudRenderMode.OFF, false, false, 0,
                false, .5, 6, fps.c().intValue(), 5, ParticlesMode.MINIMAL);
        } else if (profile.l("Баланс")) {
            target = new Snapshot(GraphicsMode.FAST, CloudRenderMode.OFF, false, saved.ao, 0,
                false, .75, 10, fps.c().intValue(), 6, ParticlesMode.DECREASED);
        } else if (profile.l("Красиво")) {
            target = new Snapshot(GraphicsMode.FANCY, CloudRenderMode.FANCY, true, true, 2,
                saved.vsync, 1, 14, fps.c().intValue(), 8, ParticlesMode.ALL);
        } else {
            target = new Snapshot(off("Быстрая графика") ? GraphicsMode.FAST : saved.graphics,
                off("Облака") ? CloudRenderMode.OFF : saved.clouds,
                off("Тени сущностей") ? false : saved.shadows,
                off("Плавное освещение") ? false : saved.ao,
                off("Смешивание биомов") ? 0 : saved.biome,
                off("Вертикальная синхронизация") ? false : saved.vsync,
                entities.c() / 100d, manageDistance.c() ? distance.c().intValue() : saved.view,
                fps.c().intValue(), simulation.c().intValue(),
                particles.l("Минимум") ? ParticlesMode.MINIMAL : particles.l("Меньше") ? ParticlesMode.DECREASED : ParticlesMode.ALL);
        }
        applyOptions(target);
        boolean suspend = profile.l("Максимум FPS") && shaders.c();
        if (suspend) {
            try {
                if (IrisBridge.configuredEnabled()) {
                    IrisBridge.setTemporaryEnabled(false);
                    suspendedShaders = true;
                }
            } catch (ReflectiveOperationException e) {
                ChatUtil.sendMessage("Не удалось приостановить шейдеры: " + e.getMessage());
            }
        } else restoreShaders();
    }
    private boolean off(String name) { return disabled.a(name).c(); }
    private void restoreShaders() {
        if (!suspendedShaders) return;
        try {
            IrisBridge.setTemporaryEnabled(true);
            suspendedShaders = false;
        } catch (ReflectiveOperationException e) {
            ChatUtil.sendMessage("Не удалось восстановить шейдеры: " + e.getMessage());
        }
    }
    private static <T> boolean set(SimpleOption<T> option, T value) {
        if (java.util.Objects.equals(option.getValue(), value)) return false;
        option.setValue(value);
        return true;
    }
    private void applyOptions(Snapshot s) {
        var o = mc.options;
        boolean reload = set(o.getGraphicsMode(), s.graphics);
        reload |= set(o.getAo(), s.ao);
        reload |= set(o.getBiomeBlendRadius(), s.biome);
        set(o.getCloudRenderMode(), s.clouds);
        set(o.getEntityShadows(), s.shadows);
        set(o.getEnableVsync(), s.vsync);
        set(o.getEntityDistanceScaling(), s.entities);
        // View distance and simulation distance already have engine callbacks.
        set(o.getViewDistance(), s.view);
        set(o.getSimulationDistance(), s.simulation);
        set(o.getParticles(), s.particles);
        set(o.getMaxFps(), s.fps);
        if (reload && mc.world != null) mc.worldRenderer.reload();
    }
    private record Snapshot(GraphicsMode graphics, CloudRenderMode clouds, boolean shadows,
        boolean ao, int biome, boolean vsync, double entities, int view, int fps,
        int simulation, ParticlesMode particles) {}
}
