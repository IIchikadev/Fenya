package socket.module.player;

import socket.config.ThemeInfo;
import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.Socket;
import socket.event.DrawEvent;
import socket.event.TickEvent;
import socket.render.ColorUtil;
import socket.setting.BindSetting;
import socket.setting.BooleanSetting;
import socket.setting.SliderSetting;
import socket.util.ChatUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Координаты смерти в чат плюс метка на месте смерти: столб света и дистанция.
 * Метка держится до подбора вещей или до сброса по клавише.
 */
@ModuleRegister(name = "Death Coords", description = "Координаты последней смерти и метка на месте", category = Category.Hud)
public class DeathCoords extends Module {
    private final BooleanSetting chat = new BooleanSetting("Писать в чат", true);
    private final BooleanSetting marker = new BooleanSetting("Метка в мире", true);
    private final SliderSetting height = new SliderSetting("Высота столба", 12.0f, 3.0f, 40.0f, 1.0f);
    private final SliderSetting hideDistance = new SliderSetting("Убирать в радиусе", 4.0f, 0.0f, 20.0f, 1.0f);

    private Vec3d position;

    public DeathCoords() {
        BindSetting reset = new BindSetting("Сбросить метку", Integer.valueOf(GLFW.GLFW_KEY_UNKNOWN), 0)
                .a(() -> this.position = null);
        a(this.chat, this.marker, this.height, this.hideDistance, reset);
    }

    @Override
    public void c() {
        super.c();
        this.position = null;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (mc.player == null) {
            return;
        }
        if (mc.player.deathTime == 1) {
            this.position = mc.player.getPos();
            if (this.chat.c().booleanValue()) {
                ChatUtil.sendMessage(String.format("Вы погибли на координатах: &c[%d, %d, %d]",
                        Integer.valueOf(mc.player.getBlockPos().getX()),
                        Integer.valueOf(mc.player.getBlockPos().getY()),
                        Integer.valueOf(mc.player.getBlockPos().getZ())));
            }
        }
        float radius = this.hideDistance.c().floatValue();
        if (this.position != null && radius > 0.0f && mc.player.getPos().distanceTo(this.position) <= radius) {
            this.position = null;
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (!event.c() || this.position == null || !this.marker.c().booleanValue() || mc.player == null) {
            return;
        }
        int accent = Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
        float top = this.height.c().floatValue();
        Vec3d base = this.position;
        // столб света: несколько вложенных отрезков, чтобы линия читалась на любом фоне
        event.getDraw3DProcessor().a(event.h(), base, base.add(0.0d, top, 0.0d), null,
                ColorUtil.applyAlphaToColor(accent, 0.85f), 2.0f);
        event.getDraw3DProcessor().a(event.h(), base, base.add(0.0d, top, 0.0d), null,
                ColorUtil.applyAlphaToColor(accent, 0.25f), 5.0f);
        event.getDraw3DProcessor().a(event.h(), new Box(base.x - 0.4d, base.y, base.z - 0.4d,
                base.x + 0.4d, base.y + 0.15d, base.z + 0.4d), ColorUtil.applyAlphaToColor(accent, 0.8f), 1.5f);
    }

    /** @return метка смерти или null */
    public Vec3d q() {
        return this.position;
    }
}
