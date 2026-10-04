package socket.module.render;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import java.util.UUID;

@ModuleRegister(name = "FakePlayer", description = "Локальная копия игрока для проверки визуала", category = Category.Render)
public class FakePlayer extends Module {
    private OtherClientPlayerEntity fake;
    private ClientWorld owner;
    @Override public void b() { super.b(); spawn(); }
    private void spawn() {
        if (mc.world == null || mc.player == null) return;
        owner = mc.world;
        fake = new OtherClientPlayerEntity(owner, new GameProfile(UUID.randomUUID(), "System"));
        int id = -1;
        while (owner.getEntityById(id) != null) id--;
        fake.setId(id);
        fake.copyPositionAndRotation(mc.player);
        fake.bodyYaw = mc.player.bodyYaw;
        fake.headYaw = mc.player.headYaw;
        for (EquipmentSlot slot : EquipmentSlot.values()) fake.equipStack(slot, mc.player.getEquippedStack(slot).copy());
        owner.addEntity(fake);
    }
    @EventTarget public void tick(TickEvent event) {
        if (owner != null && owner != mc.world) { a(false); return; }
        if (fake == null) spawn();
    }
    @Override public void c() {
        if (owner != null && fake != null && owner.getEntityById(fake.getId()) == fake)
            owner.removeEntity(fake.getId(), Entity.RemovalReason.DISCARDED);
        fake = null;
        owner = null;
        super.c();
    }
}
