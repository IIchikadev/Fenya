package socket.module.render;

import socket.config.ThemeInfo;
import socket.config.ThemeType;
import socket.core.*;
import socket.core.Module;
import socket.event.BackendEvent;
import socket.event.DrawEvent;
import socket.event.PacketEvent;
import socket.setting.BooleanSetting;
import socket.setting.ColorSetting;
import socket.setting.ModeSetting;
import socket.setting.MultiModeSetting;
import socket.ui.widget.*;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "Interface", description = "Отображает выбранные элементы интерфейса на экране", category = Category.Hud)
public class Interface extends Module {
    private final ModeSetting themeMode = new ModeSetting("Тема оформления", "Тёмная", "Тёмная", "Светлая")
            .a(selected -> {
                Socket.getInstance().getProcessors().themes().a(this.themeMode.l("Светлая") ? ThemeType.LIGHT : ThemeType.DARK);
            });
    private final ColorSetting globalColor = new ColorSetting("Глобальный цвет интерфейса",
            Integer.valueOf(Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor()));
    private final MultiModeSetting widgetToggles = new MultiModeSetting("Элементы интерфейса",
            new BooleanSetting("Клавиши", true), new BooleanSetting("Таргет-худ", true),
            new BooleanSetting("Задержки", true), new BooleanSetting("Инфо-панель", true),
            new BooleanSetting("Уведомления", true), new BooleanSetting("Зелья", true),
            new BooleanSetting("Предметы", true), new BooleanSetting("Броня", true),
            new BooleanSetting("Окружение", true), new BooleanSetting("Инвентарь", false),
            new BooleanSetting("Клавиатура", false), new BooleanSetting("Здоровье", false));
    private final List<Widget> widgets = new ArrayList<>();

    public Interface() {
        ThemeType current = Socket.getInstance().getProcessors().themes().a();
        this.themeMode.a(current == ThemeType.LIGHT ? "Светлая" : "Тёмная");
        a(this.themeMode, this.globalColor, this.widgetToggles);
        this.widgets.add(new ArmorWidget());
        this.widgets.add(new HotkeysWidget());
        this.widgets.add(new CooldownsWidget());
        this.widgets.add(new TargetWidget());
        this.widgets.add(new WatermarkWidget());
        this.widgets.add(new PotionWidget());
        this.widgets.add(new NotificationWidget());
        this.widgets.add(new EnvironmentWidget());
        this.widgets.add(new InventoryWidget());
        this.widgets.add(new KeyStrokesWidget());
        this.widgets.add(new HealthWidget());
    }

    public List<Widget> q() {
        return this.widgets;
    }

    @EventTarget
    public void onDraw(DrawEvent event) {
        if (event.b()) {
            Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).fromIntColor(this.globalColor.c().intValue());
            for (Widget widget : this.widgets) {
                if (this.widgetToggles.a(widget.j().getName()).c().booleanValue()) {
                    widget.a(event);
                }
            }
        }
    }

    @EventTarget
    public void a(GlobalEvent event) {
        for (Widget widget : this.widgets) {
            if (this.widgetToggles.a(widget.j().getName()).c().booleanValue()) {
                widget.a(event);
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        for (Widget widget : this.widgets) {
            if (this.widgetToggles.a(widget.j().getName()).c().booleanValue()) {
                widget.a(event);
            }
        }
    }

    @EventTarget
    public void a(BackendEvent event) {
        for (Widget widget : this.widgets) {
            if (this.widgetToggles.a(widget.j().getName()).c().booleanValue()) {
                widget.a(event);
            }
        }
    }
}
