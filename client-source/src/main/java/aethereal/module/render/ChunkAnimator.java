package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.TickEvent;
import aethereal.setting.*;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;

@ModuleRegister(name = "ChunkAnimator", description = "Плавное появление секций чанков", category = Category.Animations)
public class ChunkAnimator extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Снизу", "Снизу", "Сверху", "Гибрид", "Сбоку");
    private final ModeSetting easing = new ModeSetting("Изинг", "Дуга", "Дуга", "Синус", "Экспонента", "Кварт");
    private final SliderSetting duration = new SliderSetting("Длительность, мс", 1000, 100, 5000, 50);
    private record Entry(long start, float dx, float dy, float dz) {}
    private final Map<Long, Entry> entries = new HashMap<>();
    private Object world;
    private int ticks;
    public ChunkAnimator() { a(mode, duration, easing); }
    @Override public void b() { entries.clear(); world = mc.world; super.b(); }
    @Override public void c() { entries.clear(); world = null; super.c(); }
    public float[] offset(BlockPos origin) {
        if (!m() || mc.world == null || mc.player == null) return null;
        if (world != mc.world) { entries.clear(); world = mc.world; }
        long now = System.nanoTime();
        Entry entry = entries.computeIfAbsent(origin.asLong(), key -> {
            float dx = 0, dy = 0, dz = 0;
            if (mode.l("Сбоку")) {
                double x = origin.getX() + 8 - mc.player.getX(), z = origin.getZ() + 8 - mc.player.getZ();
                if (Math.abs(x) > Math.abs(z)) dx = Math.copySign(200f, (float)x); else dz = Math.copySign(200f, (float)z);
            } else if (mode.l("Сверху") || mode.l("Гибрид") && origin.getY() >= mc.world.getSeaLevel())
                dy = Math.max(0, mc.world.getTopYInclusive() + 1 - origin.getY());
            else dy = -Math.max(0, origin.getY() - mc.world.getBottomY());
            return new Entry(now, dx, dy, dz);
        });
        double t = Math.min(1, (now - entry.start) / (duration.c() * 1_000_000d));
        if (t >= 1) return null;
        double eased = easing.l("Синус") ? Math.sin(t * Math.PI / 2) : easing.l("Экспонента") ? 1 - Math.pow(2, -10 * t) : easing.l("Кварт") ? 1 - Math.pow(1-t, 4) : Math.sqrt(1 - (t-1)*(t-1));
        float remaining = (float)(1 - eased);
        return new float[]{entry.dx * remaining, entry.dy * remaining, entry.dz * remaining};
    }
    @EventTarget public void tick(TickEvent event) {
        if (world != mc.world) { entries.clear(); world = mc.world; }
        if (mc.player != null && ++ticks % 20 == 0) {
            double radius = (mc.options.getViewDistance().getValue() + 2) * 16d;
            entries.keySet().removeIf(key -> Math.pow(BlockPos.unpackLongX(key) - mc.player.getX(), 2) + Math.pow(BlockPos.unpackLongZ(key) - mc.player.getZ(), 2) > radius * radius);
        }
    }
}
