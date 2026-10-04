package socket.module.misc;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.setting.BindSetting;
import socket.setting.BooleanSetting;
import socket.setting.ModeSetting;
import socket.setting.SliderSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Сортировка открытого контейнера или своего инвентаря по клавише.
 * Считаем желаемый порядок, затем доводим до него обменами через курсор —
 * ровно та же последовательность кликов, что сделал бы игрок.
 */
@ModuleRegister(name = "Inventory Sort", description = "Сортирует контейнер или инвентарь по клавише", category = Category.Misc)
public class InventorySort extends Module {
    private final ModeSetting order = new ModeSetting("Порядок", "По имени", "По имени", "По идентификатору", "По количеству");
    private final BooleanSetting containerFirst = new BooleanSetting("Сортировать контейнер, если открыт", true);
    private final SliderSetting delay = new SliderSetting("Задержка между обменами, мс", 120.0f, 0.0f, 500.0f, 10.0f);

    private List<ItemStack> desired;
    private int index;
    private int syncId = -1;
    private long stepped;

    public InventorySort() {
        BindSetting bind = new BindSetting("Клавиша", Integer.valueOf(GLFW.GLFW_KEY_R), 0).a(this::q);
        a(bind, this.order, this.containerFirst, this.delay);
    }

    @Override
    public void c() {
        super.c();
        reset();
    }

    /** Сбрасываем начатую сортировку. */
    private void reset() {
        this.desired = null;
        this.index = 0;
        this.syncId = -1;
    }

    /** По клавише только считаем целевой порядок, сами обмены идут по тикам. */
    private void q() {
        if (mc.player == null || mc.interactionManager == null
                || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }
        if (this.desired != null) {
            reset();
            return;
        }
        List<Slot> target = a(screen);
        if (target.size() < 2) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Slot slot : target) {
            if (slot.hasStack()) {
                stacks.add(slot.getStack().copy());
            }
        }
        stacks.sort(comparator());
        this.desired = stacks;
        this.index = 0;
        this.syncId = screen.getScreenHandler().syncId;
        this.stepped = 0L;
    }

    /**
     * Один обмен за шаг, между шагами пауза. Состояние слотов перечитываем каждый раз,
     * поэтому рассинхрон с сервером во время сортировки не ломает результат.
     */
    @EventTarget
    public void a(TickEvent event) {
        if (this.desired == null) {
            return;
        }
        if (mc.player == null || mc.interactionManager == null
                || !(mc.currentScreen instanceof HandledScreen<?> screen)
                || screen.getScreenHandler().syncId != this.syncId) {
            reset();
            return;
        }
        if (System.currentTimeMillis() - this.stepped < this.delay.c().longValue()) {
            return;
        }
        List<Slot> target = a(screen);
        while (this.index < target.size()) {
            Slot slot = target.get(this.index);
            ItemStack want = this.index < this.desired.size() ? this.desired.get(this.index) : ItemStack.EMPTY;
            if (a(slot.getStack(), want)) {
                this.index++;
                continue;
            }
            int source = a(target, this.index, want);
            if (source == -1) {
                this.index++;
                continue;
            }
            mc.interactionManager.clickSlot(this.syncId, target.get(source).id, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(this.syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(this.syncId, target.get(source).id, 0, SlotActionType.PICKUP, mc.player);
            }
            this.index++;
            this.stepped = System.currentTimeMillis();
            return;
        }
        reset();
    }

    /** Ищем, в каком из ещё не разложенных слотов лежит нужный предмет. */
    private int a(List<Slot> slots, int from, ItemStack want) {
        for (int i = from + 1; i < slots.size(); i++) {
            if (a(slots.get(i).getStack(), want)) {
                return i;
            }
        }
        return -1;
    }

    private boolean a(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        return !left.isEmpty() && !right.isEmpty() && ItemStack.areItemsAndComponentsEqual(left, right)
                && left.getCount() == right.getCount();
    }

    private Comparator<ItemStack> comparator() {
        if (this.order.l("По идентификатору")) {
            return Comparator.comparing(stack -> Registries.ITEM.getId(stack.getItem()).toString());
        }
        if (this.order.l("По количеству")) {
            return Comparator.<ItemStack>comparingInt(stack -> -stack.getCount())
                    .thenComparing(stack -> stack.getName().getString());
        }
        return Comparator.comparing((ItemStack stack) -> stack.getName().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(stack -> -stack.getCount());
    }

    /** Половина окна, которую сортируем: содержимое контейнера или основная часть инвентаря. */
    private List<Slot> a(HandledScreen<?> screen) {
        List<Slot> result = new ArrayList<>();
        boolean container = this.containerFirst.c().booleanValue();
        for (Slot slot : screen.getScreenHandler().slots) {
            boolean playerSide = slot.inventory == mc.player.getInventory();
            if (container && !playerSide) {
                result.add(slot);
            } else if (!container && playerSide && slot.getIndex() >= 9 && slot.getIndex() < 36) {
                result.add(slot);
            }
        }
        if (result.isEmpty()) {
            for (Slot slot : screen.getScreenHandler().slots) {
                if (slot.inventory == mc.player.getInventory() && slot.getIndex() >= 9 && slot.getIndex() < 36) {
                    result.add(slot);
                }
            }
        }
        return result;
    }
}
