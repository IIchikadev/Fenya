package platform.inject.mixin;


import socket.core.Socket;
import socket.core.EventManager;
import socket.core.Interface;
import socket.event.RemovalsEvent;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public class WorldRendererMixin implements Interface {
    @org.spongepowered.asm.mixin.injection.ModifyArgs(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V", ordinal = 0))
    private void socketChunkOffset(org.spongepowered.asm.mixin.injection.invoke.arg.Args args,
            @com.llamalad7.mixinextras.sugar.Local net.minecraft.util.math.BlockPos origin) {
        float[] offset = Socket.getInstance().getProcessors().modules().chunkAnimator().offset(origin);
        if (offset != null) for (int i = 0; i < 3; i++) args.set(i, (float)args.get(i) + offset[i]);
    }
    @Inject(method = {"renderWeather"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.type.WEATHER);
        EventManager.a(event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = {"setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"}, at = @At("HEAD"), argsOnly = true, index = 4)
    private boolean onSetupTerrain(boolean spectator) {
        return spectator;
    }
}
