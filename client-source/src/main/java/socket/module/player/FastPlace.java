package socket.module.player;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.GlobalEvent;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.setting.BooleanSetting;
import socket.setting.SliderSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import platform.inject.accessors.MinecraftClientAccessor;

@ModuleRegister(name = "Fast Place", description = "Убирает задержку при установке блоков и использовании предметов", category = Category.Misc)
public class FastPlace extends Module {
    private final SliderSetting delay = new SliderSetting("Задержка", 0.0f, 0.0f, 3.0f, 1.0f);
    private final BooleanSetting blocksOnly = new BooleanSetting("Только блоки", false);
    private final BooleanSetting expBottles = new BooleanSetting("Пузырьки опыта с 'Только блоки'", true).a(() -> this.blocksOnly.c().booleanValue());
    private final BooleanSetting crystals = new BooleanSetting("Кристаллы Энда с 'Только блоки'", true).a(() -> this.blocksOnly.c().booleanValue());

    public FastPlace() {
        a(this.delay, this.blocksOnly, this.expBottles, this.crystals);
    }

    @EventTarget
    public void a(TickEvent event) {
        updateCooldown();
    }

    @EventTarget
    public void a(GlobalEvent event) {
        updateCooldown();
    }

    private void updateCooldown() {
        if (!canFastPlace()) {
            return;
        }

        int targetDelay = (int) this.delay.c().floatValue();
        int targetCooldown = targetDelay == 0 ? 0 : targetDelay + 1;
        MinecraftClientAccessor accessor = (MinecraftClientAccessor) mc;
        if (accessor.getItemUseCooldown() > targetCooldown) {
            accessor.setItemUseCooldown(targetCooldown);
        }
    }

    private boolean canFastPlace() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return false;
        }
        if (mc.player.isUsingItem()) {
            return false;
        }
        if (this.blocksOnly.c().booleanValue()) {
            return isAllowedItem(mc.player.getMainHandStack()) || isAllowedItem(mc.player.getOffHandStack());
        }
        return true;
    }

    private boolean isAllowedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem) {
            return true;
        }
        if (this.expBottles.c().booleanValue() && stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            return true;
        }
        if (this.crystals.c().booleanValue() && stack.isOf(Items.END_CRYSTAL)) {
            return true;
        }
        return false;
    }
}
