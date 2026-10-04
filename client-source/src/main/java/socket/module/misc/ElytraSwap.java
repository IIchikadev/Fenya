package socket.module.misc;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.setting.BindSetting;
import socket.setting.BooleanSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

/**
 * Быстрая смена нагрудника и элитры по клавише.
 * Работает обычными клик-пакетами инвентаря: берём предмет, ставим в слот брони,
 * снятое возвращаем на освободившееся место — то же, что игрок делает руками.
 */
@ModuleRegister(name = "Elytra Swap", description = "Меняет нагрудник и элитру по клавише", category = Category.Misc)
public class ElytraSwap extends Module {
    /** Слот нагрудника в инвентаре игрока. */
    private static final int CHEST_SLOT = 6;

    private final BooleanSetting closeAfter = new BooleanSetting("Закрывать инвентарь после", true);

    public ElytraSwap() {
        BindSetting bind = new BindSetting("Клавиша", Integer.valueOf(GLFW.GLFW_KEY_G), 0).a(this::q);
        a(bind, this.closeAfter);
    }

    private void q() {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }
        ItemStack worn = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean wearingElytra = worn.isOf(Items.ELYTRA);
        int source = findSwapSlot(wearingElytra);
        if (source == -1) {
            return;
        }
        int syncId = mc.player.playerScreenHandler.syncId;
        // берём цель в курсор, кладём в слот брони, снятое отправляем в исходный слот
        mc.interactionManager.clickSlot(syncId, source, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, source, 0, SlotActionType.PICKUP, mc.player);
        if (this.closeAfter.c().booleanValue()) {
            mc.player.playerScreenHandler.syncState();
        }
    }

    /** @return слот инвентаря игрока с нужной заменой или -1 */
    private int findSwapSlot(boolean wearingElytra) {
        for (int slot = 9; slot < 45; slot++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(slot).getStack();
            if (stack.isEmpty()) {
                continue;
            }
            if (wearingElytra && a(stack)) {
                return slot;
            }
            if (!wearingElytra && stack.isOf(Items.ELYTRA)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean a(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_CHESTPLATE) || stack.isOf(Items.DIAMOND_CHESTPLATE)
                || stack.isOf(Items.IRON_CHESTPLATE) || stack.isOf(Items.GOLDEN_CHESTPLATE)
                || stack.isOf(Items.CHAINMAIL_CHESTPLATE) || stack.isOf(Items.LEATHER_CHESTPLATE);
    }
}
