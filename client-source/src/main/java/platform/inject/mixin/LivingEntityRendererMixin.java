package platform.inject.mixin;


import socket.core.Socket;
import socket.core.Interface;
import socket.render.ColorUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Unique
    private T currentEntity;

    @Unique
    private float pitch;

    @Inject(method = {"updateRenderState*"}, at = {@At("HEAD")})
    private void onUpdateRenderState(T entity, S state, float f, CallbackInfo ci) {
        this.currentEntity = entity;
    }

    @Inject(method = {"updateRenderState*"}, at = {@At("TAIL")})
    private void updateRenderState(T entity, S state, float f, CallbackInfo ci) {
        if (entity == Interface.mc.player) {
            this.pitch = state.pitch;
            state.pitch = this.pitch;
        }
    }

}
