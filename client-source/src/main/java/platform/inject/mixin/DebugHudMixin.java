package platform.inject.mixin;


import aethereal.core.Socket;
import aethereal.core.Interface;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Mixin({DebugHud.class})
public class DebugHudMixin implements Interface {
    @ModifyReturnValue(method = {"getLeftText"}, at = {@At("RETURN")})
    private List<String> onGetLeftText(List<String> original) {
        return replaceText(original, true);
    }

    @ModifyReturnValue(method = {"getRightText"}, at = {@At("RETURN")})
    private List<String> onGetRightText(List<String> original) {
        return replaceText(original, false);
    }

    @Unique
    private List<String> replaceText(List<String> lines, boolean hasXyz) {
        return lines;
    }
}
