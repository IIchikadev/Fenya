package socket.module.misc;

import socket.core.Category;
import socket.core.EventTarget;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.event.TickEvent;
import socket.event.PacketEvent;
import socket.module.render.AuctionHelper;
import socket.setting.SliderSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import java.util.Locale;
import java.util.regex.Pattern;

@ModuleRegister(name = "AutoResell", description = "Перевыставление через хранилище /ah и кнопку часов", category = Category.Misc)
public class AutoResell extends Module {
    private enum State { WAIT, AUCTION, STORAGE, RESULT }
    private final SliderSetting interval = new SliderSetting("Интервал, сек", 60, 15, 300, 5);
    private static final Pattern COOLDOWN = Pattern.compile("(?iu)подождите\\s+(\\d+)\\s+сек");
    private State state = State.WAIT;
    private long next, entered;
    private ClientWorld world;
    public AutoResell() { a(interval); }
    private long now() { return System.nanoTime() / 1_000_000; }
    private void reset() { state = State.WAIT; next = now() + (long)(interval.c() * 1000); }
    @Override public void b() { super.b(); world = mc.world; reset(); }
    @Override public void c() { reset(); world = null; super.c(); }
    private void enter(State value) { state = value; entered = now(); }
    private boolean storage(GenericContainerScreen s) { return s.getTitle().getString().toLowerCase(Locale.ROOT).contains("хранилище"); }
    private boolean click(GenericContainerScreen s, Item item) {
        if (!s.getScreenHandler().getCursorStack().isEmpty()) return false;
        for (var slot : s.getScreenHandler().slots) {
            if (slot.id < s.getScreenHandler().getRows() * 9 && slot.getStack().isOf(item)) {
                mc.interactionManager.clickSlot(s.getScreenHandler().syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
                return true;
            }
        }
        return false;
    }
    @EventTarget public void tick(TickEvent event) {
        if (mc.world != world) { world = mc.world; reset(); }
        if (mc.player == null || mc.interactionManager == null || mc.getNetworkHandler() == null) return;
        if (state == State.WAIT) {
            if (now() < next || mc.currentScreen != null) return;
            mc.getNetworkHandler().sendChatCommand("ah");
            enter(State.AUCTION);
            return;
        }
        if (now() - entered > 12000) {
            mc.player.sendMessage(Text.literal("[AutoResell] Нет подтверждения от аукциона; цикл остановлен."), false);
            reset(); return;
        }
        if (now() - entered < 300) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen s)) return;
        if (state == State.AUCTION && AuctionHelper.isAuction(s) && click(s, Items.ENDER_CHEST)) enter(State.STORAGE);
        else if (state == State.STORAGE && storage(s) && click(s, Items.CLOCK)) enter(State.RESULT);
    }
    @EventTarget public void packet(PacketEvent event) {
        if (!event.isReceive() || !(event.getPacket() instanceof GameMessageS2CPacket packet) || packet.overlay()) return;
        String message = packet.content().getString();
        mc.execute(() -> {
            if (!m() || state == State.WAIT) return;
            String lower = message.toLowerCase(Locale.ROOT);
            if (lower.contains("успешно перевыставлены") || lower.contains("отсутствуют предметы для перевыставления")) reset();
            else {
                var match = COOLDOWN.matcher(message);
                if (match.find()) {
                    try { reset(); next = now() + Math.min(3600, Long.parseLong(match.group(1)) + 1) * 1000; }
                    catch (NumberFormatException ignored) { reset(); }
                }
            }
        });
    }
}
