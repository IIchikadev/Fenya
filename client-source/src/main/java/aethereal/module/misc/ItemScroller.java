package aethereal.module.misc;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.ScrollEvent;
import aethereal.setting.BooleanSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import platform.inject.accessors.HandledScreenAccessor;

/**
 * Перенос предметов колесом мыши: наводишь на слот и крутишь — стак уходит
 * в другую половину окна, как по Shift-клику. С Ctrl переносятся все такие же предметы.
 */
@ModuleRegister(name = "Item Scroller", description = "Перенос предметов колесом мыши в контейнерах", category = Category.Misc)
public class ItemScroller extends Module {
    private final BooleanSetting allMatching = new BooleanSetting("Все такие же с Ctrl", true);
    private final BooleanSetting invert = new BooleanSetting("Инвертировать колесо", false);

    public ItemScroller() {
        a(this.allMatching, this.invert);
    }

    @EventTarget
    public void a(ScrollEvent event) {
        if (mc.player == null || mc.interactionManager == null
                || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }
        double amount = this.invert.c().booleanValue() ? -event.c() : event.c();
        if (amount == 0.0d) {
            return;
        }
        Slot slot = ((HandledScreenAccessor) screen).getFocusedSlot();
        if (slot == null || !slot.hasStack()) {
            return;
        }
        int syncId = screen.getScreenHandler().syncId;
        if (this.allMatching.c().booleanValue() && Screen.hasControlDown()) {
            ItemStack target = slot.getStack().copy();
            for (Slot other : screen.getScreenHandler().slots) {
                if (other.hasStack() && ItemStack.areItemsAndComponentsEqual(other.getStack(), target)
                        && a(other, slot, screen)) {
                    mc.interactionManager.clickSlot(syncId, other.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
            return;
        }
        mc.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    /** Переносим только из той же половины окна, где находится слот под курсором. */
    private boolean a(Slot candidate, Slot hovered, HandledScreen<?> screen) {
        boolean hoveredInInventory = hovered.inventory == mc.player.getInventory();
        boolean candidateInInventory = candidate.inventory == mc.player.getInventory();
        return hoveredInInventory == candidateInInventory;
    }
}
