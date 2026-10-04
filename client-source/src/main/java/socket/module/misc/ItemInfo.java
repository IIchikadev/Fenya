package socket.module.misc;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TooltipEvent;
import socket.setting.BooleanSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

/** Дополнения к тултипу: питательность еды, прочность и идентификатор предмета. */
@ModuleRegister(name = "Item Info", description = "Питательность еды и прочность предмета в подсказке", category = Category.Hud)
public class ItemInfo extends Module {
    private final BooleanSetting food = new BooleanSetting("Питательность еды", true);
    private final BooleanSetting durability = new BooleanSetting("Прочность", true);
    private final BooleanSetting identifier = new BooleanSetting("Идентификатор предмета", false);

    public ItemInfo() {
        a(this.food, this.durability, this.identifier);
    }

    @EventTarget
    public void a(TooltipEvent event) {
        ItemStack stack = event.b();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (this.food.c().booleanValue()) {
            FoodComponent component = stack.get(DataComponentTypes.FOOD);
            if (component != null) {
                // насыщение считается как в ванили: nutrition * saturation * 2
                float saturation = component.nutrition() * component.saturation() * 2.0f;
                event.c().add(Text.literal("Голод ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.valueOf(component.nutrition())).formatted(Formatting.WHITE))
                        .append(Text.literal("  Насыщение ").formatted(Formatting.GRAY))
                        .append(Text.literal(String.format(Locale.US, "%.1f", Float.valueOf(saturation)))
                                .formatted(Formatting.WHITE)));
            }
        }
        if (this.durability.c().booleanValue() && stack.isDamageable()) {
            int left = stack.getMaxDamage() - stack.getDamage();
            int percent = Math.round((left / (float) stack.getMaxDamage()) * 100.0f);
            Formatting color = percent <= 15 ? Formatting.RED : percent <= 40 ? Formatting.YELLOW : Formatting.GREEN;
            event.c().add(Text.literal("Прочность ").formatted(Formatting.GRAY)
                    .append(Text.literal(left + " / " + stack.getMaxDamage()).formatted(color))
                    .append(Text.literal("  " + percent + "%").formatted(Formatting.DARK_GRAY)));
        }
        if (this.identifier.c().booleanValue()) {
            event.c().add(Text.literal(stack.getItem().toString()).formatted(Formatting.DARK_GRAY));
        }
    }
}
