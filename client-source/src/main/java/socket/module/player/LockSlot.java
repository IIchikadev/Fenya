package socket.module.player;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.EventTarget;
import socket.event.DropItemEvent;
import socket.setting.BooleanSetting;
import socket.util.ServerUtil;
import net.minecraft.item.*;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.registry.Registries;

@ModuleRegister(name="Lock Slot", description="Защита выбранных слотов, оружия и шалкеров от случайного выброса", category=Category.Misc)
public final class LockSlot extends Module {
    public final BooleanSetting[] slots = new BooleanSetting[9];
    public final BooleanSetting onlyPvp = new BooleanSetting("Только в PvP", false);
    public final BooleanSetting swords = new BooleanSetting("Защитить мечи", true);
    public final BooleanSetting mace = new BooleanSetting("Защитить булаву", true);
    public final BooleanSetting shulkers = new BooleanSetting("Защитить шалкеры", true);
    public LockSlot() {
        for(int i=0;i<9;i++) { slots[i]=new BooleanSetting("Защитить слот "+(i+1), false); a(slots[i]); }
        a(onlyPvp,swords,mace,shulkers);
    }
    public boolean protects(ItemStack stack, int inventorySlot) {
        if(!m() || stack.isEmpty() || (onlyPvp.c() && !ServerUtil.e())) return false;
        if(inventorySlot>=0 && inventorySlot<9 && slots[inventorySlot].c()) return true;
        if(shulkers.c() && stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ShulkerBoxBlock) return true;
        return inventorySlot>=0 && inventorySlot<9 && ((swords.c() && Registries.ITEM.getId(stack.getItem()).getPath().endsWith("_sword")) || (mace.c() && stack.isOf(Items.MACE)));
    }
    @EventTarget public void drop(DropItemEvent event) {
        if(mc.player!=null && protects(mc.player.getInventory().getStack(event.b()),event.b())) event.a(true);
    }
}
