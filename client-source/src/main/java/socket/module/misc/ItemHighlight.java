package socket.module.misc;

import socket.config.ThemeInfo;
import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.Socket;
import socket.event.ContainerEvent;
import socket.render.ColorUtil;
import socket.setting.BooleanSetting;
import socket.setting.ColorSetting;
import socket.setting.MultiModeSetting;
import socket.setting.SliderSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import platform.inject.accessors.HandledScreenAccessor;

/** Подсветка нужных предметов рамкой в инвентаре и сундуках. */
@ModuleRegister(name = "Item Highlight", description = "Подсвечивает нужные предметы в контейнерах", category = Category.Hud)
public class ItemHighlight extends Module {
    private final MultiModeSetting groups = new MultiModeSetting("Что подсвечивать",
            new BooleanSetting("Тотемы", true), new BooleanSetting("Жемчуг", true),
            new BooleanSetting("Золотые яблоки", true), new BooleanSetting("Еда", false),
            new BooleanSetting("Шалкеры", true), new BooleanSetting("Зачарованное", false),
            new BooleanSetting("Элитра", true));
    private final ColorSetting color = new ColorSetting("Цвет рамки", Integer.valueOf(ColorUtil.a(184, 255, 60)));
    private final SliderSetting thickness = new SliderSetting("Толщина", 1.0f, 0.5f, 2.5f, 0.5f);
    private final BooleanSetting fill = new BooleanSetting("Заливка слота", true);

    public ItemHighlight() {
        a(this.groups, this.color, this.thickness, this.fill);
    }

    @EventTarget
    public void a(ContainerEvent event) {
        if (event.h() != ContainerEvent.Phase.POST || event.getScreen() == null || event.getContext() == null) {
            return;
        }
        HandledScreenAccessor accessor = (HandledScreenAccessor) event.getScreen();
        float originX = accessor.getX();
        float originY = accessor.getY();
        int frame = ColorUtil.applyAlphaToColor(this.color.c().intValue(), 0.9f);
        int inside = ColorUtil.applyAlphaToColor(this.color.c().intValue(), 0.18f);
        for (Slot slot : event.e()) {
            if (!slot.hasStack() || !a(slot.getStack())) {
                continue;
            }
            float x = originX + slot.x - 1.0f;
            float y = originY + slot.y - 1.0f;
            if (this.fill.c().booleanValue()) {
                event.getContext().getMatrices().push();
                Socket.getInstance().getProcessors().draw2D().a(event.getContext().getMatrices(), x, y, 18.0f, 18.0f, 2.0f, inside);
                event.getContext().getMatrices().pop();
            }
            Socket.getInstance().getProcessors().draw2D().a(event.getContext().getMatrices(), x, y, 18.0f, 18.0f, 2.0f,
                    this.thickness.c().floatValue(), frame);
        }
    }

    private boolean a(ItemStack stack) {
        if (this.groups.a("Тотемы").c().booleanValue() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
            return true;
        }
        if (this.groups.a("Жемчуг").c().booleanValue() && stack.isOf(Items.ENDER_PEARL)) {
            return true;
        }
        if (this.groups.a("Золотые яблоки").c().booleanValue()
                && (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) || stack.isOf(Items.GOLDEN_APPLE))) {
            return true;
        }
        if (this.groups.a("Еда").c().booleanValue() && stack.contains(DataComponentTypes.FOOD)) {
            return true;
        }
        if (this.groups.a("Шалкеры").c().booleanValue() && stack.contains(DataComponentTypes.CONTAINER)) {
            return true;
        }
        if (this.groups.a("Элитра").c().booleanValue() && stack.isOf(Items.ELYTRA)) {
            return true;
        }
        return this.groups.a("Зачарованное").c().booleanValue() && stack.hasEnchantments();
    }
}
