package socket.module.render;

import socket.core.*;
import socket.core.Module;
import socket.event.*;
import socket.setting.*;
import socket.render.ScanWorldEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;

@ModuleRegister(name="Scan World", description="Световая волна по поверхностям мира: по таймеру, тотему и убийству", category=Category.Render)
public final class ScanWorld extends Module {
    public final BooleanSetting periodic = new BooleanSetting("Автоматическая волна", true);
    public final SliderSetting interval = new SliderSetting("Интервал, сек", 3, 1, 15, .5f);
    public final BooleanSetting onTotem = new BooleanSetting("При тотеме", true);
    public final BooleanSetting onKill = new BooleanSetting("При убийстве", true);
    public final SliderSetting duration = new SliderSetting("Длительность, сек", 2.5f, .5f, 8, .1f);
    public final SliderSetting width = new SliderSetting("Ширина волны", 10, 1, 32, 1);
    public final SliderSetting radius = new SliderSetting("Радиус", 80, 10, 256, 2);
    public final ModeSetting colorMode = new ModeSetting("Цвет", "Тема", "Тема", "Свой");
    public final ColorSetting color = new ColorSetting("Свой цвет", 0xFF70DAFF);
    public record Wave(Vec3d center, long start) {}
    private final ArrayList<Wave> waves = new ArrayList<>();
    private Object world;
    private long lastScan, lastAttack;
    private int target = -1;
    public ScanWorld() { a(periodic, interval, onTotem, onKill, duration, width, radius, colorMode, color, new ButtonSetting("Запустить волну", () -> { if(mc.player!=null) start(mc.player.getPos()); })); }
    @Override public void b() { super.b(); if(mc.player!=null) start(mc.player.getPos()); }
    @Override public void c() { waves.clear(); ScanWorldEffect.release(); super.c(); }
    public void start(Vec3d center) {
        if (!m() || mc.world == null) return;
        if(world!=mc.world) { waves.clear(); world=mc.world; target=-1; }
        if(waves.size()>=12) waves.remove(0);
        waves.add(new Wave(center,System.nanoTime())); lastScan=System.nanoTime();
    }
    public java.util.List<Wave> waves() {
        if(world!=mc.world) { waves.clear(); world=mc.world; target=-1; }
        waves.removeIf(w -> (System.nanoTime()-w.start)/1e9 >= duration.c());
        return waves;
    }
    @EventTarget public void tick(TickEvent event) {
        waves();
        if(mc.player!=null && periodic.c() && (System.nanoTime()-lastScan)/1e9>=interval.c()) start(mc.player.getPos());
    }
    @EventTarget public void attack(AttackEvent event) {
        if(event.b() instanceof LivingEntity && event.b()!=mc.player) { target=event.b().getId(); lastAttack=System.nanoTime(); }
    }
    @EventTarget public void packet(PacketEvent event) {
        if(!event.isReceive() || !(event.getPacket() instanceof EntityStatusS2CPacket packet)) return;
        var packetWorld=mc.world;
        mc.execute(() -> {
            if(!m() || packetWorld==null || packetWorld!=mc.world) return;
            var entity=packet.getEntity(packetWorld);
            if(entity==null) return;
            if(packet.getStatus()==35 && onTotem.c()) start(entity.getPos());
            else if(packet.getStatus()==3 && onKill.c() && entity.getId()==target && System.nanoTime()-lastAttack<6_500_000_000L) { start(entity.getPos()); target=-1; }
        });
    }
}
