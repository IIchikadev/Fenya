package aethereal.ui.widget;

import aethereal.config.ThemeInfo;
import aethereal.core.Interface;
import aethereal.core.Socket;
import aethereal.event.DrawEvent;
import aethereal.render.ColorUtil;
import aethereal.render.EasingList;
import aethereal.setting.BooleanSetting;
import aethereal.ui.element.DragInfo;
import net.minecraft.item.ItemStack;

public class InventoryWidget extends Widget implements Interface {
    private final BooleanSetting armor = new BooleanSetting("Показывать броню", true);
    private final BooleanSetting empty = new BooleanSetting("Рисовать пустые слоты", true);

    public InventoryWidget() {
        super(new DragInfo("Инвентарь", 0.0f, 0.0f, 0.0f, 0.0f));
        j().setWidget(this);
        a(this.armor, this.empty);
    }

    @Override
    public void a(DrawEvent event) {
        if (!event.b() || mc.player == null || mc.options.hudHidden || mc.player.isSpectator()) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float animation = a();
        float cell = 16.0f;
        float gap = 1.5f;
        float padding = 5.0f;
        boolean showArmor = this.armor.c().booleanValue();
        float width = (9.0f * cell) + (8.0f * gap) + (padding * 2.0f);
        float rows = showArmor ? 4.0f : 3.0f;
        float height = this.d + 3.0f + (rows * (cell + gap)) + padding;
        j().setWidth(width);
        j().setHeight(height);
        float x = j().getClampedX();
        float y = j().getClampedY();
        a(event, "q", "Инвентарь", width, animation);
        if (animation <= 0.0f) {
            super.a(event);
            return;
        }
        float gridY = y + this.d + 3.0f;
        a(event, x, gridY - 2.0f, width, height - this.d - 3.0f + 2.0f, false, animation);
        for (int index = 0; index < 27; index++) {
            ItemStack stack = mc.player.getInventory().getStack(index + 9);
            float cellX = x + padding + ((index % 9) * (cell + gap));
            float cellY = gridY + ((index / 9) * (cell + gap));
            a(event, cellX, cellY, cell, stack, animation);
        }
        if (showArmor) {
            float armorY = gridY + (3.0f * (cell + gap));
            for (int slot = 0; slot < 4; slot++) {
                ItemStack stack = mc.player.getInventory().getArmorStack(3 - slot);
                a(event, x + padding + (slot * (cell + gap)), armorY, cell, stack, animation);
            }
            ItemStack offhand = mc.player.getOffHandStack();
            a(event, x + padding + (5.0f * (cell + gap)), armorY, cell, offhand, animation);
        }
        super.a(event);
    }

    private void a(DrawEvent event, float x, float y, float cell, ItemStack stack, float animation) {
        if (stack.isEmpty() && !this.empty.c().booleanValue()) {
            return;
        }
        int slotColor = ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(255, 255, 255, 255), 0.045f * animation);
        event.getDraw2DProcessor().a(event.h(), x, y, cell, cell, 3.0f, slotColor);
        event.getDraw2DProcessor().a(event.h(), x, y, cell, cell, 3.0f, 0.5f,
                ColorUtil.applyAlphaToColor(Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.OUTLINE_SMALL).toIntColor(), 0.6f * animation));
        if (!stack.isEmpty()) {
            Socket.getInstance().getModuleProcessor().j().a(event.i(), stack, x + 0.5f, y + 0.5f, 0, animation, 0.95f, true);
        }
    }
}
