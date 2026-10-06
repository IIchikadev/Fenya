package platform.inject.mixin;

import socket.core.Socket;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.math.BlockPos;

@Mixin(WorldRenderer.class)
public class ChunkAnimatorMixin {
    @ModifyArgs(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V", ordinal = 0))
    private void socketChunkOffset(Args args, @Local BlockPos origin) {
        float[] offset = Socket.getInstance().getProcessors().modules().chunkAnimator().offset(origin);
        if (offset != null) for (int i = 0; i < 3; i++) args.set(i, (float) args.get(i) + offset[i]);
    }
}
