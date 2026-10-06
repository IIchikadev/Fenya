package platform.inject.mixin;


import socket.core.Socket;
import socket.core.EventManager;
import socket.event.CameraPositionEvent;
import socket.event.RemovalsEvent;
import socket.event.RotationEvent;
import socket.render.Animations;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import platform.inject.accessors.CameraAccessor;

@Mixin({Camera.class})
public abstract class CameraMixin {
    @ModifyReturnValue(method = {"isThirdPerson"}, at = {@At("RETURN")})
    private boolean isThirdPerson(boolean original) {
        return original;
    }

    @Inject(method = {"update"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER)})
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        CameraPositionEvent posEvent = new CameraPositionEvent(camera.getPos());
        EventManager.a(posEvent);
        if (posEvent.a() && posEvent.b() != null) {
            Vec3d pos = posEvent.b();
            ((CameraAccessor) this).invokeSetPos(pos.getX(), pos.getY(), pos.getZ());
        }
        RotationEvent event = new RotationEvent(focusedEntity.getYaw(tickDelta), focusedEntity.getPitch(tickDelta));
        EventManager.a(event);
        ((CameraAccessor) this).invokeSetRotation(event.yaw, event.pitch);
    }

    @Inject(method = {"getSubmersionType"}, at = {@At("HEAD")}, cancellable = true)
    private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.type.WATER);
        EventManager.a(event);
        if (event.a()) {
            ci.setReturnValue(CameraSubmersionType.NONE);
        }
    }

    /**
     * Только масштабирует желаемую дистанцию камеры (анимация отъезда).
     * Ванильная проверка столкновений со стенами всегда выполняется,
     * поэтому в третьем лице и во Freelook камера не проходит сквозь блоки.
     */
    @ModifyVariable(method = {"clipToSpace"}, at = @At("HEAD"), argsOnly = true)
    private float onClipToSpace(float desiredCameraDistance) {
        Animations animations = Socket.getInstance().getProcessors().modules().animations();
        if (animations.m()) {
            desiredCameraDistance *= animations.u().c();
        }
        return desiredCameraDistance;
    }

    @ModifyArg(method = "clipToSpace", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/RaycastContext;<init>(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/RaycastContext$ShapeType;Lnet/minecraft/world/RaycastContext$FluidHandling;Lnet/minecraft/entity/Entity;)V"), index = 2)
    private RaycastContext.ShapeType freelookCollisionShape(RaycastContext.ShapeType original) {
        return Socket.getInstance().getProcessors().modules().freelook().q()
                ? RaycastContext.ShapeType.COLLIDER : original;
    }

    @ModifyReturnValue(method = "clipToSpace", at = @At("RETURN"))
    private float freelookWallClearance(float clippedDistance) {
        // Keep the near clipping plane on the player's side of the wall. The
        // vanilla corner rays stop at the surface, which permits edge peeking.
        return Socket.getInstance().getProcessors().modules().freelook().q()
                ? Math.max(0, clippedDistance - 0.2f) : clippedDistance;
    }
}
