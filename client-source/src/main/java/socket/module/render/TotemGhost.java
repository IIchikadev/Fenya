package socket.module.render;

import socket.core.*;
import socket.core.Module;
import socket.event.*;
import socket.setting.*;
import socket.config.ThemeInfo;
import socket.render.RenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;

@ModuleRegister(name="Totem Ghost", description="Призрак игрока поднимается после срабатывания тотема", category=Category.Render)
public final class TotemGhost extends Module {
    public final SliderSetting rise = new SliderSetting("Высота", 2, .5f, 4, .1f);
    public final SliderSetting duration = new SliderSetting("Длительность, сек", 2, 1, 4, .1f);
    public final ModeSetting colorMode = new ModeSetting("Цвет", "Тема", "Тема", "Свой");
    public final ColorSetting color = new ColorSetting("Свой цвет", 0xFF70DAFF);
    private record Ghost(Vec3d pos, float yaw, float headYaw, float pitch, float walk, float speed, long start) {}
    private final ArrayList<Ghost> ghosts = new ArrayList<>();
    private Object world;
    private long frames;
    public long renderedFrames() { return frames; }
    private static final float[][] PARTS = {{-.25f,1.5f,-.25f,.25f,2,.25f},{-.25f,.75f,-.125f,.25f,1.5f,.125f},{.25f,.75f,-.125f,.5f,1.5f,.125f},{-.5f,.75f,-.125f,-.25f,1.5f,.125f},{0,0,-.125f,.25f,.75f,.125f},{-.25f,0,-.125f,0,.75f,.125f}};
    private static final float[][] PIVOTS = {{0,1.5f,0},{0,.75f,0},{.375f,1.4f,0},{-.375f,1.4f,0},{.125f,.75f,0},{-.125f,.75f,0}};
    private static final int[][] FACES = {{0,1,3,2},{4,5,7,6},{0,1,5,4},{2,3,7,6},{0,2,6,4},{1,3,7,5}};
    private static final int[][] EDGES = {{0,1},{0,2},{0,4},{1,3},{1,5},{2,3},{2,6},{3,7},{4,5},{4,6},{5,7},{6,7}};
    public TotemGhost() { a(rise, duration, colorMode, color, new ButtonSetting("Предпросмотр", () -> spawn(mc.player))); }
    @Override public void c() { ghosts.clear(); super.c(); }
    @EventTarget public void packet(PacketEvent event) {
        if (event.isReceive() && event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 35) {
            var packetWorld = mc.world;
            mc.execute(() -> { if (m() && packetWorld != null && packetWorld == mc.world) spawn(packet.getEntity(packetWorld)); });
        }
    }
    public void spawn(Entity entity) {
        if (!m() || entity == null || mc.world == null) return;
        if (world != mc.world) { ghosts.clear(); world = mc.world; }
        float body = entity instanceof LivingEntity l ? l.bodyYaw : entity.getYaw();
        float head = entity instanceof LivingEntity l ? l.headYaw - body : 0;
        float walk = entity instanceof LivingEntity l ? l.limbAnimator.getPos() : 0;
        float speed = entity instanceof LivingEntity l ? Math.min(1, l.limbAnimator.getSpeed()) : 0;
        if (ghosts.size() >= 32) ghosts.remove(0);
        ghosts.add(new Ghost(entity.getPos(), body, head, entity.getPitch(), walk, speed, System.nanoTime()));
    }
    @EventTarget public void draw(DrawEvent event) {
        if (!event.c()) return;
        if (mc.world == null || world != mc.world) { ghosts.clear(); return; }
        long now = System.nanoTime();
        ghosts.removeIf(g -> (now-g.start)/1e9 >= duration.c());
        if (ghosts.isEmpty()) return;
        int rgb = colorMode.l("Тема") ? Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor() : color.c();
        var matrices = event.h();
        var camera = mc.gameRenderer.getCamera().getPos();
        try (RenderState state = new RenderState(false)) {
            RenderSystem.enableDepthTest(); RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            for (int pass=0; pass<2; pass++) {
                var buffer = Tessellator.getInstance().begin(pass==0 ? VertexFormat.DrawMode.QUADS : VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                for (Ghost g : ghosts) {
                    float t = (float)((now-g.start)/1e9/duration.c());
                    float alpha = Math.min(1,t*8) * Math.min(1,(1-t)/.45f);
                    int tint = (rgb & 0xFFFFFF) | ((int)(alpha*(pass==0 ? 64 : 160)) << 24);
                    matrices.push();
                    matrices.translate(g.pos.x-camera.x, g.pos.y-camera.y+rise.c()*(1-Math.pow(1-t,3)), g.pos.z-camera.z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g.yaw+t*40));
                    for (int part=0; part<6; part++) {
                        matrices.push();
                        var pivot=PIVOTS[part]; matrices.translate(pivot[0],pivot[1],pivot[2]);
                        if (part==0) matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g.headYaw));
                        float swing=part<2 ? (part==0 ? g.pitch : 0) : (float)Math.toDegrees(Math.cos(g.walk*.6662+(part==2||part==5 ? Math.PI : 0))*g.speed*(part>=4 ? 1.4 : 1));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing));
                        matrices.translate(-pivot[0],-pivot[1],-pivot[2]);
                        float[] box=PARTS[part];
                        for (int[] face : pass==0 ? FACES : EDGES) for (int corner:face) {
                            buffer.vertex(matrices.peek().getPositionMatrix(), box[(corner&1)==0?0:3], box[(corner&2)==0?1:4], box[(corner&4)==0?2:5]).color(tint);
                        }
                        matrices.pop();
                    }
                    matrices.pop();
                }
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            frames++;
        }
    }
}
