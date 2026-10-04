package aethereal.module.combat;

import aethereal.core.Category;
import aethereal.core.Socket;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BindSetting;
import aethereal.setting.ModeSetting;
import aethereal.ui.screen.SwapScreen;
import aethereal.util.InventoryUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

@ModuleRegister(name = "Auto Swap", description = "Мгновенно перекладывает сферу или тотем во вторую руку по нажатию клавиши", category = Category.Misc)
public class AutoSwap extends Module {
    private final ModeSetting c = new ModeSetting("Первый предмет", "Сфера", "Сфера", "Тотем");
    private final ModeSetting d = new ModeSetting("Второй предмет", "Тотем", "Сфера", "Тотем");
    private final SwapScreen e = new SwapScreen(Text.literal("SwapMenu"));

    public AutoSwap() {
        BindSetting f = new BindSetting("Кнопка перемещения", 86, 0).a(() -> {
            Socket.getInstance().getModuleProcessor().v().getInventoryHandler()
                    .moveItem(InventoryUtil.c(mc.player.getOffHandStack().getItem() == a(this.c) ? a(this.d) : a(this.c)), 45, 1);
        }).b(() -> {
            if (mc.currentScreen instanceof SwapScreen) {
                mc.setScreen(null);
            }
        });
        a(f, this.c, this.d);
    }

    public SwapScreen q() {
        return this.e;
    }

    private Item a(ModeSetting modeSetting) {
        switch (modeSetting.c()) {
            case "Сфера":
                return Items.PLAYER_HEAD;
            case "Тотем":
                return Items.TOTEM_OF_UNDYING;
            default:
                return null;
        }
    }
}
