package platform.inject.mixin;


import socket.core.EventManager;
import socket.event.PortalEvent;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PortalManager.class})
public abstract class PortalManagerMixin {
    @Inject(method = {"isInPortal"}, at = {@At("RETURN")}, cancellable = true)
    private void onIsInPortal(CallbackInfoReturnable<Boolean> cir) {
        PortalEvent event = new PortalEvent(cir.getReturnValue().booleanValue());
        EventManager.a(event);
        cir.setReturnValue(Boolean.valueOf(event.b()));
    }
}
