package socket.module.render;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.event.TooltipEvent;
import socket.setting.*;
import socket.util.AuctionPrice;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import java.util.Locale;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@ModuleRegister(name = "AuctionHelper", description = "Подсветка минимальной общей и поштучной цены на аукционе", category = Category.Misc)
public class AuctionHelper extends Module {
    private final BooleanSetting unit = new BooleanSetting("Цена за 1 предмет", true);
    private final SliderSetting durability = new SliderSetting("Мин. прочность, %", 0, 0, 100, 1);
    private final ColorSetting cheapest = new ColorSetting("Дешёвый предмет", 0x804bff4b);
    private final ColorSetting best = new ColorSetting("Выгодный предмет", 0x804bffff);
    private final ModeSetting highlight = new ModeSetting("Подсветка", "Статик", "Статик", "Мигать");
    private final BooleanSetting healing = new BooleanSetting("Только хилки + рега", false);
    private final BooleanSetting strength = new BooleanSetting("Только сила + скорость", false);
    private final BooleanSetting bulldozer = new BooleanSetting("Только кирка с Бульдозером", false);
    private final BooleanSetting mace = new BooleanSetting("Только булава с чарами", false);
    private final BooleanSetting armorFilter = new BooleanSetting("Фильтры брони", false);
    private final StringSetting armorRequired = new StringSetting("Броня: нужные чары", "protection=5,unbreaking=5,mending=1");
    private final StringSetting armorExcluded = new StringSetting("Броня: исключить чары", "thorns=1");
    private final BooleanSetting swordFilter = new BooleanSetting("Фильтры меча", false);
    private final StringSetting swordRequired = new StringSetting("Меч: нужные чары", "sharpness=7,unbreaking=5,fire_aspect=2");
    private final StringSetting swordExcluded = new StringSetting("Меч: исключить чары", "knockback=2");
    private GenericContainerScreen screen;
    private int totalSlot = -1, unitSlot = -1;
    public AuctionHelper() { a(highlight, unit, durability, cheapest, best, healing, strength, bulldozer, mace,
            armorFilter, armorRequired, armorExcluded, swordFilter, swordRequired, swordExcluded); }
    public static boolean isAuction(GenericContainerScreen screen) {
        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        return title.contains("аукцион") || title.contains("auction") || title.contains("поиск") || title.contains("маркет");
    }
    public static long price(ItemStack stack) {
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) for (Text line : lore.lines()) {
            long price = AuctionPrice.parse(line.getString());
            if (price > 0) return price;
        }
        return -1;
    }
    @EventTarget public void tick(TickEvent event) {
        totalSlot = unitSlot = -1;
        screen = null;
        if (!(mc.currentScreen instanceof GenericContainerScreen s) || !isAuction(s)) return;
        screen = s;
        long lowest = Long.MAX_VALUE;
        double lowestUnit = Double.POSITIVE_INFINITY;
        for (Slot slot : s.getScreenHandler().slots) {
            if (slot.id >= s.getScreenHandler().getRows() * 9 || !slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            if (!matches(stack)) continue;
            if (stack.isDamageable() && (stack.getMaxDamage() - stack.getDamage()) * 100f / stack.getMaxDamage() < durability.c()) continue;
            long price = price(stack);
            if (price <= 0) continue;
            if (price < lowest) { lowest = price; totalSlot = slot.id; }
            double perItem = (double) price / stack.getCount();
            if (perItem < lowestUnit) { lowestUnit = perItem; unitSlot = slot.id; }
        }
    }
    public void drawSlot(DrawContext context, Slot slot) {
        if (!m() || mc.currentScreen != screen) return;
        if (slot.id == totalSlot || slot.id == unitSlot) {
            int color = slot.id == totalSlot ? cheapest.c() : best.c();
            if (highlight.l("Мигать")) color = (color & 0xffffff) | ((int)((color >>> 24) * (.65 + .35 * Math.sin(System.nanoTime() / 400_000_000d))) << 24);
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
            context.draw();
        }
    }
    @EventTarget public void tooltip(TooltipEvent event) {
        if (!unit.c() || !(mc.currentScreen instanceof GenericContainerScreen s) || !isAuction(s)) return;
        long price = price(event.b());
        if (price > 0 && event.b().getCount() > 1)
            event.c().add(Text.literal(String.format(Locale.ROOT, "§aЦена за 1 шт: $%.2f", (double) price / event.b().getCount())));
    }
    @Override public void c() { screen = null; totalSlot = unitSlot = -1; super.c(); }

    private int level(ItemStack stack, RegistryKey<Enchantment> key) {
        if (mc.world == null) return 0;
        return mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(key.getValue())
                .map(entry -> EnchantmentHelper.getLevel(entry, stack)).orElse(0);
    }
    private int effect(ItemStack stack, RegistryEntry<StatusEffect> type) {
        var potion = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potion != null) for (var effect : potion.getEffects())
            if (effect.getEffectType().equals(type)) return effect.getAmplifier() + 1;
        return 0;
    }
    private boolean enchantments(ItemStack stack, String required, String excluded) {
        if (mc.world == null) return false;
        var registry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        for (int side = 0; side < 2; side++) {
            String input = side == 0 ? required : excluded;
            if (input.isBlank()) continue;
            for (String rule : input.split(",")) {
                String[] parts = rule.trim().split("=");
                if (parts.length != 2) return false;
                Identifier id = Identifier.tryParse(parts[0].trim());
                if (id == null) return false;
                var entry = registry.getEntry(id);
                if (entry.isEmpty()) return false;
                try {
                    int minimum = Integer.parseInt(parts[1].trim());
                    if (minimum < 1) return false;
                    if (!entry.get().value().isAcceptableItem(stack)) continue;
                    boolean reached = EnchantmentHelper.getLevel(entry.get(), stack) >= minimum;
                    if (side == 0 && !reached || side == 1 && reached) return false;
                } catch (NumberFormatException ignored) { return false; }
            }
        }
        return true;
    }
    private boolean matches(ItemStack stack) {
        if (stack.isOf(Items.BARRIER) || stack.isOf(Items.GRAY_DYE) || stack.isOf(Items.LIGHT_GRAY_DYE) || stack.isOf(Items.STRUCTURE_VOID)) return false;
        if (armorFilter.c() && stack.contains(DataComponentTypes.EQUIPPABLE) && !enchantments(stack, armorRequired.c(), armorExcluded.c())) return false;
        if (swordFilter.c() && stack.isIn(ItemTags.SWORDS) && !enchantments(stack, swordRequired.c(), swordExcluded.c())) return false;
        if (healing.c() && effect(stack, StatusEffects.INSTANT_HEALTH) > 0 && !(stack.isOf(Items.POTION) && effect(stack, StatusEffects.INSTANT_HEALTH) >= 2 && effect(stack, StatusEffects.REGENERATION) > 0)) return false;
        if (strength.c() && effect(stack, StatusEffects.STRENGTH) > 0 && !(stack.isOf(Items.POTION) && effect(stack, StatusEffects.STRENGTH) >= 3 && effect(stack, StatusEffects.SPEED) >= 3)) return false;
        if (bulldozer.c() && stack.isIn(ItemTags.PICKAXES)) {
            var lore = stack.get(DataComponentTypes.LORE);
            if (level(stack, Enchantments.EFFICIENCY) < 5 || lore == null || lore.lines().stream().noneMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains("бульдозер"))) return false;
        }
        return !mace.c() || !stack.isOf(Items.MACE) || level(stack, Enchantments.SHARPNESS) >= 7 && level(stack, Enchantments.BREACH) >= 3;
    }
}
