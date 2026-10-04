package socket.module.misc;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.setting.BindSetting;
import socket.setting.ButtonSetting;
import socket.setting.ModeSetting;
import socket.setting.SliderSetting;
import socket.setting.StringSetting;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Профили инвентаря: запоминаем, какой предмет лежит в каждом из 36 слотов, и по клавише
 * раскладываем инвентарь обратно. Работает при открытом окне инвентаря; перекладывает
 * по одному обмену за шаг теми же кликами, что сделал бы игрок. Сравниваются только типы
 * предметов: «меч в первом слоте», а не конкретный меч с зачарованиями.
 */
@ModuleRegister(name = "Inventory Profiles", description = "Сохраняет раскладку инвентаря и восстанавливает её по клавише", category = Category.Misc)
public class InventoryProfiles extends Module {
    private static final int SIZE = 36;
    private static final int MAX_STEPS = 72;

    private final ModeSetting profile = new ModeSetting("Профиль", "1", "1", "2", "3");
    private final SliderSetting delay = new SliderSetting("Задержка между обменами, мс", 120.0f, 0.0f, 500.0f, 10.0f);
    private final StringSetting[] data = {
            new StringSetting("profile-1", "").a(() -> false),
            new StringSetting("profile-2", "").a(() -> false),
            new StringSetting("profile-3", "").a(() -> false)
    };

    private Item[] desired;
    private int syncId = -1;
    private int steps;
    private long stepped;

    public InventoryProfiles() {
        ButtonSetting save = new ButtonSetting("Сохранить текущую раскладку", this::save);
        BindSetting apply = new BindSetting("Применить профиль", Integer.valueOf(-1), 0).a(this::apply);
        a(this.profile, save, apply, this.delay, this.data[0], this.data[1], this.data[2]);
    }

    @Override
    public void c() {
        super.c();
        this.desired = null;
    }

    private StringSetting current() {
        return this.data[Integer.parseInt(this.profile.c()) - 1];
    }

    private void save() {
        if (mc.player == null) {
            return;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            if (i > 0) {
                out.append(';');
            }
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                out.append(Registries.ITEM.getId(stack.getItem()));
            }
        }
        current().a(out.toString());
        message("Профиль " + this.profile.c() + " сохранён");
    }

    private void apply() {
        if (mc.player == null || mc.interactionManager == null
                || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }
        String saved = current().c();
        if (saved == null || saved.isEmpty()) {
            message("Профиль " + this.profile.c() + " пуст — сначала сохраните раскладку");
            return;
        }
        String[] parts = saved.split(";", -1);
        Item[] target = new Item[SIZE];
        for (int i = 0; i < SIZE && i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                Item item = Registries.ITEM.get(Identifier.of(parts[i]));
                target[i] = item == Items.AIR ? null : item;
            }
        }
        this.desired = target;
        this.syncId = screen.getScreenHandler().syncId;
        this.steps = 0;
        this.stepped = 0L;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.desired == null) {
            return;
        }
        if (mc.player == null || mc.interactionManager == null
                || !(mc.currentScreen instanceof HandledScreen<?> screen)
                || screen.getScreenHandler().syncId != this.syncId || this.steps >= MAX_STEPS) {
            this.desired = null;
            return;
        }
        if (System.currentTimeMillis() - this.stepped < this.delay.c().longValue()) {
            return;
        }
        Slot[] slots = slots(screen);
        for (int i = 0; i < SIZE; i++) {
            Item want = this.desired[i];
            if (want == null || slots[i] == null || slots[i].getStack().isOf(want)) {
                continue;
            }
            int source = source(slots, i, want);
            if (source == -1) {
                continue;
            }
            mc.interactionManager.clickSlot(this.syncId, slots[source].id, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(this.syncId, slots[i].id, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(this.syncId, slots[source].id, 0, SlotActionType.PICKUP, mc.player);
            }
            this.steps++;
            this.stepped = System.currentTimeMillis();
            return;
        }
        this.desired = null;
        message("Профиль " + this.profile.c() + " применён");
    }

    /** Слот, откуда взять нужный предмет: не тот, что уже стоит на своём месте. */
    private int source(Slot[] slots, int target, Item want) {
        for (int i = 0; i < SIZE; i++) {
            if (i == target || slots[i] == null || !slots[i].getStack().isOf(want)) {
                continue;
            }
            if (this.desired[i] != want) {
                return i;
            }
        }
        return -1;
    }

    /** Слоты окна, соответствующие 36 ячейкам инвентаря игрока (0–8 — хотбар). */
    private Slot[] slots(HandledScreen<?> screen) {
        Slot[] result = new Slot[SIZE];
        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.inventory == mc.player.getInventory() && slot.getIndex() >= 0 && slot.getIndex() < SIZE) {
                result[slot.getIndex()] = slot;
            }
        }
        return result;
    }

    private void message(String text) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[Inventory Profiles] " + text), false);
        }
    }
}
