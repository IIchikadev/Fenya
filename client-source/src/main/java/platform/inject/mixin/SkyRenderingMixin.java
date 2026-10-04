package platform.inject.mixin;
import socket.core.Socket;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SkyRendering.class)
public class SkyRenderingMixin {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void socketSky(float red, float green, float blue, CallbackInfo ci) {
        var sky = Socket.getInstance().getProcessors().modules().skyShader();
        if (sky.m()) { sky.render(red, green, blue); ci.cancel(); }
    }
}
