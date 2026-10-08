package platform.inject.mixin;


import socket.core.Socket;
import socket.core.EventManager;
import socket.core.Interface;
import socket.event.AttackEvent;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin implements Interface {
    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void socketProtectDrop(int syncId, int slotId, int button, net.minecraft.screen.slot.SlotActionType action, PlayerEntity player, CallbackInfo ci) {
        var lock = Socket.getInstance().getProcessors().modules().lockSlot();
        var handler = player.currentScreenHandler;
        if (!lock.m()) return;
        if (action == net.minecraft.screen.slot.SlotActionType.THROW && slotId >= 0 && slotId < handler.slots.size()) {
            var slot = handler.slots.get(slotId);
            if (slot.inventory == player.getInventory() && lock.protects(slot.getStack(), slot.getIndex())) ci.cancel();
        } else if (action == net.minecraft.screen.slot.SlotActionType.PICKUP && slotId == -999 && lock.protects(handler.getCursorStack(), -1)) {
            ci.cancel();
        }
    }
    @Inject(method = {"attackEntity"}, at = {@At("HEAD")}, cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        AttackEvent event = new AttackEvent(target);
        EventManager.a(event);
        if (event.a()) {
            ci.cancel();
        }
    }
}
