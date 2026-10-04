package platform.inject.mixin;

import aethereal.core.Socket;
import aethereal.module.render.EnchantGlow;
import aethereal.render.TintedVertexConsumer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ItemRenderer.class})
public class ItemRendererMixin {

    @ModifyReturnValue(method = {"getItemGlintConsumer"}, at = {@At("RETURN")})
    private static VertexConsumer onGetItemGlintConsumer(VertexConsumer original, VertexConsumerProvider provider,
                                                        RenderLayer layer, boolean solid, boolean glint) {
        if (!glint) {
            return original;
        }
        try {
            EnchantGlow module = Socket.getInstance().getModuleProcessor().t().bs();
            if (!module.m()) {
                return original;
            }
            return new TintedVertexConsumer(original, module.q(), module.r());
        } catch (Throwable ignored) {
            return original;
        }
    }
}
