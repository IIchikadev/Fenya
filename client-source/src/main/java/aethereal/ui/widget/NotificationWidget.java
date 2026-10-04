package aethereal.ui.widget;

import aethereal.config.ThemeInfo;
import aethereal.core.Socket;
import aethereal.core.GlobalEvent;
import aethereal.core.Interface;
import aethereal.core.Packet;
import aethereal.event.BackendEvent;
import aethereal.event.DrawEvent;
import aethereal.event.PacketEvent;
import aethereal.notification.Notification;
import aethereal.render.EasingList;
import aethereal.render.Fonts;
import aethereal.setting.BooleanSetting;
import aethereal.ui.element.DragInfo;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.text.Text;

public class NotificationWidget extends Widget implements Interface {
    private final BooleanSetting f;
    private final BooleanSetting g;
    private final BooleanSetting h;
    private final BooleanSetting k;

    public NotificationWidget() {
        super(new DragInfo("Уведомления", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Оповещать о поднятии донат-предметов", true);
        this.g = new BooleanSetting("Обновления и уведомления друзей", true);
        this.h = new BooleanSetting("Показывать в правом углу", false);
        this.k = new BooleanSetting("Полоска времени", true);
        j().setWidget(this);
        j().setDragStatus(1);
        a(this.g, this.f, this.h, this.k);
    }

    @Override
    public void a(GlobalEvent event) {
        d().a((mc.currentScreen instanceof ChatScreen) || !Socket.getInstance().getModuleProcessor().m().b().isEmpty());
        super.a(event);
    }

    @Override
    public void a(DrawEvent event) {
        float fA;
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float contentY = j().getClampedY();
        for (Notification notification : Socket.getInstance().getModuleProcessor().m().b()) {
            float animation = notification.a().c() * a();
            if (animation > 0.0f) {
                Object message = notification.c();
                if (message instanceof Text value) {
                    fA = Fonts.e.a(value, this.e);
                } else {
                    fA = Fonts.e.a(String.valueOf(message), this.e);
                }
                float width = 17.5f + fA + 4.0f;
                float x = this.h.c().booleanValue()
                        ? (mc.getWindow().getScaledWidth() - width) - 6.0f - (12.0f * (1.0f - animation))
                        : (mc.getWindow().getScaledWidth() - width) / 2.0f;
                int color = notification.e() == -1 ? Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor() : notification.e();
                Object objD = notification.d();
                if (objD instanceof ItemStack stack) {
                    a(event, x, contentY, stack, message, width, animation, color);
                } else {
                    a(event, x, contentY, (String) notification.d(), message, width, animation, color);
                }
                if (this.k.c().booleanValue() && notification.f() > 0) {
                    float left = Math.max(0.0f, 1.0f - (notification.b().c() / (float) notification.f()));
                    float lineY = (contentY + this.d) - 2.25f;
                    HudSkin.meter(event, x + 4.5f, lineY, width - 9.0f, 1.25f, left, color, animation);
                }
                j().setX(x);
                j().setWidth(width);
                j().setHeight(this.d);
                contentY += (this.d + 3.5f) * animation;
            }
        }
        super.a(event);
    }

    @Override
    public void a(PacketEvent event) {
        if (this.f.c().booleanValue() && event.isReceive()) {
            ItemPickupAnimationS2CPacket class_2775VarD = (ItemPickupAnimationS2CPacket) event.getPacket();
            if (class_2775VarD instanceof ItemPickupAnimationS2CPacket) {
                ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = class_2775VarD;
                ClientPlayerEntity class_746VarMethod_8469 = (ClientPlayerEntity) mc.world.getEntityById(itemPickupAnimationS2CPacket.getCollectorEntityId());
                if (class_746VarMethod_8469 instanceof PlayerEntity) {
                    ClientPlayerEntity class_746Var = class_746VarMethod_8469;
                    ItemEntity class_1542VarMethod_8469 = (ItemEntity) mc.world.getEntityById(itemPickupAnimationS2CPacket.getEntityId());
                    if (class_1542VarMethod_8469 instanceof ItemEntity) {
                        ItemEntity itemEntity = class_1542VarMethod_8469;
                        if (class_746Var != mc.player && !itemEntity.getStack().getName().getString().contains("Упс.") && ((itemEntity.getStack().contains(DataComponentTypes.CUSTOM_NAME) && itemEntity.getStack().contains(DataComponentTypes.LORE)) || itemEntity.getStack().isOf(Items.ENCHANTED_GOLDEN_APPLE))) {
                            Socket.getInstance().getModuleProcessor().m().a(new Notification(itemEntity.getStack().copy(), class_746Var.getName().copy().append(" подобрал ").append(itemEntity.getStack().getName()).append(itemPickupAnimationS2CPacket.getStackAmount() > 1 ? " x" + itemPickupAnimationS2CPacket.getStackAmount() : ""), 1500));
                        }
                    }
                }
            }
        }
        super.a(event);
    }

    @Override
    public void a(BackendEvent event) {
        Packet packet = event.getPacket();
        String message = packet.getSecurity().extractString(packet.getPayload(), "message");
        if ("friend".equals(packet.getId()) && this.g.c().booleanValue() && message != null) {
            Socket.getInstance().getModuleProcessor().m().a(new Notification("o", message, 5000));
        }
        if ("application".equals(packet.getId()) && message != null) {
            Socket.getInstance().getModuleProcessor().m().a(new Notification("o", message, 15000));
        }
        super.a(event);
    }
}
