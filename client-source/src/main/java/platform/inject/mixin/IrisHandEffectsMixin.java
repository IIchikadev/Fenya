package platform.inject.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Iris flushes its hand vertices outside vanilla renderHand. */
@Mixin(targets="net.irisshaders.iris.pathways.HandRenderer", remap=false)
public class IrisHandEffectsMixin {
    @Inject(method={"renderSolid","renderTranslucent"}, at=@At(value="INVOKE", target="Lnet/irisshaders/batchedentityrendering/impl/FullyBufferedMultiBufferSource;method_22993()V", shift=At.Shift.BEFORE), remap=false)
    private void socketBeforeFlush(CallbackInfo ci) { socket.render.HandEffects.begin(); }
    @Inject(method={"renderSolid","renderTranslucent"}, at=@At(value="INVOKE", target="Lnet/irisshaders/batchedentityrendering/impl/FullyBufferedMultiBufferSource;method_22993()V", shift=At.Shift.AFTER), remap=false)
    private void socketAfterFlush(CallbackInfo ci) { socket.render.HandEffects.end(); }
}
