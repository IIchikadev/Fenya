package platform.inject.mixin;


import socket.core.Socket;
import socket.module.misc.StreamerMode;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SkinTextures.class})
public class SkinTexturesMixin {
    @Inject(method = {"texture"}, at = {@At("HEAD")}, cancellable = true)
    public void texture(CallbackInfoReturnable<Identifier> cir) {
        StreamerMode streamerMode = Socket.getInstance().getProcessors().modules().streamerMode();
        if (streamerMode.m() && streamerMode.q().c().booleanValue()) {
            cir.setReturnValue(Identifier.of("socket", "pictures/skin.png"));
        }
    }
}
