package socket.module.misc;

import socket.config.ThemeInfo;
import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.core.Socket;
import socket.event.ContainerEvent;
import socket.render.ColorUtil;
import socket.render.Draw2DProcessor;
import socket.render.Fonts;
import socket.setting.BooleanSetting;
import socket.ui.GlassStyle;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import platform.inject.accessors.HandledScreenAccessor;

/**
 * Предпросмотр содержимого шалкера: при наведении на него в любом окне инвентаря над курсором
 * появляется сетка 9×3 с предметами. Данные берём из компонента {@code CONTAINER} самого предмета,
 * поэтому ничего не открываем и пакетов не шлём.
 */
@ModuleRegister(name = "Container Preview", description = "Показывает содержимое шалкера при наведении в инвентаре", category = Category.Hud)
public class ContainerPreview extends Module {
    private static final int COLUMNS = 9;
    private static final int SLOTS = 27;
    private static final float CELL = 18.0f;
    private static final float PADDING = 5.0f;
    private static final float HEADER = 11.0f;
    private static final float CORNER = 4.0f;

    private final BooleanSetting shiftOnly = new BooleanSetting("Только с зажатым Shift", false);
    private final BooleanSetting showEmpty = new BooleanSetting("Показывать пустые", false);
    private final BooleanSetting dyeColor = new BooleanSetting("Рамка цвета шалкера", true);

    public ContainerPreview() {
        a(this.shiftOnly, this.showEmpty, this.dyeColor);
    }

    @EventTarget
    public void a(ContainerEvent event) {
        if (event.h() != ContainerEvent.Phase.POST || event.getContext() == null) {
            return;
        }
        if (this.shiftOnly.c().booleanValue() && !Screen.hasShiftDown()) {
            return;
        }
        Slot slot = ((HandledScreenAccessor) event.getScreen()).getFocusedSlot();
        if (slot == null || !slot.hasStack()) {
            return;
        }
        ItemStack stack = slot.getStack();
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            return;
        }
        DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOTS, ItemStack.EMPTY);
        container.copyTo(items);
        if (!this.showEmpty.c().booleanValue() && items.stream().allMatch(ItemStack::isEmpty)) {
            return;
        }
        a(event.getContext(), stack, items, event.f(), event.g());
    }

    private void a(DrawContext context, ItemStack box, DefaultedList<ItemStack> items, int mouseX, int mouseY) {
        int rows = SLOTS / COLUMNS;
        float width = (COLUMNS * CELL) + (PADDING * 2.0f);
        float height = HEADER + (rows * CELL) + (PADDING * 2.0f);
        // над курсором, чтобы не перекрывать ванильную подсказку; если места нет — под ним
        float x = Math.min(mouseX + 8.0f, mc.getWindow().getScaledWidth() - width - 4.0f);
        float y = mouseY - height - 8.0f;
        if (y < 4.0f) {
            y = mouseY + 20.0f;
        }
        x = Math.max(4.0f, x);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0f, 0.0f, 400.0f);
        Draw2DProcessor draw = Socket.getInstance().getProcessors().draw2D();
        int accent = accent(box);
        int background = ColorUtil.combineColorWithAlpha(
                Socket.getInstance().getProcessors().themes().a(ThemeInfo.BACKGROUND_HUD).toIntColor(), 210);
        boolean glass = GlassStyle.hud();
        if (glass) {
            background = GlassStyle.tint(background);
        }
        draw.b(matrices, x, y, width, height, CORNER, background, glass ? GlassStyle.blurAlpha(1.0f) : 1.0f);
        draw.a(matrices, x, y, width, height, CORNER, 0.5f, ColorUtil.applyAlphaToColor(accent, 0.7f));
        if (glass) {
            GlassStyle.sheen(draw, matrices, x, y, width, height, CORNER, 1.0f);
        }

        String title = box.getName().getString();
        float maxTitle = width - (PADDING * 2.0f);
        while (title.length() > 1 && Fonts.e.a(title, 7.5f) > maxTitle) {
            title = title.substring(0, title.length() - 1);
        }
        Fonts.e.a(matrices, title, x + PADDING, y + PADDING - 1.0f, 7.5f, ColorUtil.a(255, 255, 255));

        float top = y + PADDING + HEADER;
        for (int i = 0; i < SLOTS; i++) {
            float cellX = x + PADDING + ((i % COLUMNS) * CELL);
            float cellY = top + ((i / COLUMNS) * CELL);
            draw.a(matrices, cellX, cellY, CELL - 1.0f, CELL - 1.0f, 2.0f,
                    ColorUtil.applyAlphaToColor(ColorUtil.a(255, 255, 255), 0.06f));
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                context.drawItem(item, (int) cellX, (int) cellY);
                context.drawStackOverlay(mc.textRenderer, item, (int) cellX, (int) cellY);
            }
        }
        matrices.pop();
    }

    /** Цвет рамки: краситель шалкера или основной цвет темы. */
    private int accent(ItemStack box) {
        if (this.dyeColor.c().booleanValue() && box.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock shulker) {
            DyeColor color = shulker.getColor();
            if (color != null) {
                return 0xFF000000 | color.getEntityColor();
            }
        }
        return Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
    }
}
