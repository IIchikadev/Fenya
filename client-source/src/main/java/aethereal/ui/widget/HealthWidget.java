package aethereal.ui.widget;

import aethereal.config.ThemeInfo;
import aethereal.core.GlobalEvent;
import aethereal.core.Interface;
import aethereal.core.Socket;
import aethereal.event.DrawEvent;
import aethereal.render.ColorUtil;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.setting.BooleanSetting;
import aethereal.ui.element.DragInfo;

import java.util.Locale;

/** Числовое здоровье и броня со шкалой, отдельно от ванильных сердец. */
public class HealthWidget extends Widget implements Interface {
    private final BooleanSetting absorption = new BooleanSetting("Учитывать абсорбцию", true);
    private final BooleanSetting armor = new BooleanSetting("Показывать броню", true);
    private final BooleanSetting meter = new BooleanSetting("Шкала", true);

    public HealthWidget() {
        super(new DragInfo("Здоровье", 0.0f, 0.0f, 0.0f, 0.0f));
        j().setWidget(this);
        j().setDragStatus(1);
        a(this.absorption, this.armor, this.meter);
    }

    @Override
    public void a(GlobalEvent event) {
        d().a(true);
        super.a(event);
    }

    @Override
    public void a(DrawEvent event) {
        if (!event.b() || mc.player == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float animation = a();
        float health = mc.player.getHealth()
                + (this.absorption.c().booleanValue() ? mc.player.getAbsorptionAmount() : 0.0f);
        String value = String.format(Locale.US, "%.1f", Float.valueOf(health));
        String suffix = this.armor.c().booleanValue() ? " / " + ((int) mc.player.getArmor()) : "";
        float width = 17.5f + Fonts.e.a(value + suffix, 9.0f) + 6.0f;
        j().setWidth(width);
        j().setHeight(this.d + (this.meter.c().booleanValue() ? 4.0f : 0.0f));
        if (animation <= 0.0f) {
            super.a(event);
            return;
        }
        float x = j().getClampedX();
        float y = j().getClampedY();
        a(event, "$", value + suffix, width, animation);
        if (this.meter.c().booleanValue()) {
            float ratio = Math.min(1.0f, health / Math.max(1.0f, mc.player.getMaxHealth()));
            int color = ColorUtil.lerpColor(ColorUtil.a(255, 80, 90),
                    Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor(), ratio);
            HudSkin.meter(event, x + 4.5f, y + this.d + 0.5f, width - 9.0f, 1.5f, ratio, color, animation);
        }
        super.a(event);
    }
}
