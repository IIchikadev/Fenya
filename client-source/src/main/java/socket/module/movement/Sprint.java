package socket.module.movement;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import net.minecraft.entity.effect.StatusEffects;

@ModuleRegister(name = "Sprint", description = "Автоматически включает спринт при движении", category = Category.Misc)
public class Sprint extends Module {
    @EventTarget
    public void a(TickEvent event) {
        mc.player.setSprinting(mc.player.input.movementForward > 0.0f && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && (mc.player.getAbilities().invulnerable || mc.player.getHungerManager().getFoodLevel() > 6));
    }
}
