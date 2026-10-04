package aethereal.module.render;

import aethereal.config.ThemeInfo;
import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.core.Socket;
import aethereal.event.HeadFeatureEvent;
import aethereal.render.ColorUtil;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Украшения на модели игрока: энергетические крылья, ореол над головой и орбита предметов.
 * Геометрия рисуется в том же проходе, что и China Hat, поэтому работает и на себе, и на друзьях.
 */
@ModuleRegister(name = "Adornments", description = "Крылья, ореол и орбита предметов на модели игрока", category = Category.Render)
public class Adornments extends Module {
    /** Декоративный набор для орбиты — предметы со «магическим» видом. */
    private static final ItemStack[] ORBIT_ITEMS = {
            new ItemStack(Items.AMETHYST_SHARD), new ItemStack(Items.ECHO_SHARD),
            new ItemStack(Items.NETHER_STAR), new ItemStack(Items.END_CRYSTAL),
            new ItemStack(Items.PRISMARINE_CRYSTALS), new ItemStack(Items.GLOWSTONE_DUST)
    };

    private final MultiModeSetting parts = new MultiModeSetting("Украшения",
            new BooleanSetting("Крылья", true), new BooleanSetting("Ореол", true),
            new BooleanSetting("Орбита предметов", false));
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Тема клиента", "Тема клиента", "Свой цвет");
    private final ColorSetting color;
    private final SliderSetting scale = new SliderSetting("Размер", 1.0f, 0.5f, 2.0f, 0.1f);
    private final SliderSetting orbitCount = new SliderSetting("Предметов на орбите", 3.0f, 1.0f, 6.0f, 1.0f);
    private final BooleanSetting onFriends = new BooleanSetting("Показывать на друзьях", true);

    public Adornments() {
        this.color = new ColorSetting("Свой цвет", Integer.valueOf(ColorUtil.a(150, 120, 255)))
                .a(() -> Boolean.valueOf(this.colorMode.l("Свой цвет")));
        this.orbitCount.a(() -> Boolean.valueOf(this.parts.a("Орбита предметов").c().booleanValue()));
        a(this.parts, this.colorMode, this.color, this.scale, this.orbitCount, this.onFriends);
    }

    @EventTarget
    public void a(HeadFeatureEvent event) {
        if (!(event.getModel() instanceof BipedEntityModel<?> model)) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        boolean self = player == mc.player;
        boolean friend = this.onFriends.c().booleanValue()
                && Socket.getInstance().getModuleProcessor().e().d(player.getName().getString());
        if (!self && !friend) {
            return;
        }
        int base = this.colorMode.l("Свой цвет") ? this.color.c().intValue()
                : Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor();
        MatrixStack matrices = event.getMatrixStack();
        VertexConsumerProvider provider = event.getVertexConsumerProvider();
        float size = this.scale.c().floatValue();
        float time = (System.currentTimeMillis() % 100000L) / 1000.0f;
        if (this.parts.a("Крылья").c().booleanValue()) {
            a(matrices, provider, model, player, base, size, time);
        }
        if (this.parts.a("Ореол").c().booleanValue()) {
            b(matrices, provider, model, base, size, time);
        }
        if (this.parts.a("Орбита предметов").c().booleanValue()) {
            a(matrices, provider, model, event.getLight(), size, time);
        }
        if (provider instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    /** Крылья: по семь перьев на сторону, взмах зависит от времени и скорости игрока. */
    private void a(MatrixStack matrices, VertexConsumerProvider provider, BipedEntityModel<?> model,
                   PlayerEntity player, int base, float size, float time) {
        double speed = Math.sqrt((player.getVelocity().x * player.getVelocity().x)
                + (player.getVelocity().z * player.getVelocity().z));
        float boost = (float) Math.min(1.0d, speed * 4.0d);
        float flap = (MathHelper.sin(time * (4.0f + (4.0f * boost))) * (6.0f + (14.0f * boost))) + (10.0f * boost);
        matrices.push();
        model.body.rotate(matrices);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = provider.getBuffer(RenderLayer.getDebugQuads());
        int feathers = 7;
        for (int side = -1; side <= 1; side += 2) {
            for (int k = 0; k < feathers; k++) {
                float t = k / (float) (feathers - 1);
                float pitch = MathHelper.lerp(t, 30.0f, -45.0f) * 0.017453292f;
                float yaw = ((20.0f + (42.0f * t)) + flap) * 0.017453292f;
                float length = (0.9f - (0.3f * Math.abs(t - 0.35f))) * size;
                float rootX = side * 0.06f;
                float rootY = 0.02f - (0.06f * t);
                float rootZ = 0.09f;
                float tipX = rootX + (side * MathHelper.sin(yaw) * length);
                float tipZ = rootZ + (MathHelper.cos(yaw) * length * 0.75f);
                float tipY = rootY + (MathHelper.sin(pitch) * length);
                int rootColor = ColorUtil.applyAlphaToColor(base, 0.85f);
                int tipColor = ColorUtil.applyAlphaToColor(ColorUtil.b(base, 1.25f), 0.05f);
                a(buffer, matrix, rootX, rootY - 0.012f, rootZ, rootColor);
                a(buffer, matrix, rootX, rootY + 0.012f, rootZ, rootColor);
                a(buffer, matrix, tipX, tipY + 0.05f, tipZ, tipColor);
                a(buffer, matrix, tipX, tipY - 0.05f, tipZ, tipColor);
            }
        }
        matrices.pop();
    }

    /** Ореол: тонкое кольцо над головой с наклоном и медленным вращением. */
    private void b(MatrixStack matrices, VertexConsumerProvider provider, BipedEntityModel<?> model,
                   int base, float size, float time) {
        matrices.push();
        model.head.rotate(matrices);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
        matrices.translate(0.0f, 0.42f + (MathHelper.sin(time * 1.5f) * 0.015f), 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((time * 25.0f) % 360.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(12.0f));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = provider.getBuffer(RenderLayer.getDebugQuads());
        int segments = 48;
        float radius = 0.26f * size;
        float band = 0.035f * size;
        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * 6.2831855f;
            float a2 = ((i + 1) / (float) segments) * 6.2831855f;
            float fade = 0.35f + (0.65f * ((MathHelper.sin((a1 * 3.0f) + (time * 4.0f)) * 0.5f) + 0.5f));
            int col = ColorUtil.applyAlphaToColor(base, 0.9f * fade);
            float inner1X = MathHelper.sin(a1) * (radius - band);
            float inner1Z = MathHelper.cos(a1) * (radius - band);
            float outer1X = MathHelper.sin(a1) * (radius + band);
            float outer1Z = MathHelper.cos(a1) * (radius + band);
            float inner2X = MathHelper.sin(a2) * (radius - band);
            float inner2Z = MathHelper.cos(a2) * (radius - band);
            float outer2X = MathHelper.sin(a2) * (radius + band);
            float outer2Z = MathHelper.cos(a2) * (radius + band);
            a(buffer, matrix, inner1X, 0.0f, inner1Z, col);
            a(buffer, matrix, outer1X, 0.0f, outer1Z, col);
            a(buffer, matrix, outer2X, 0.0f, outer2Z, col);
            a(buffer, matrix, inner2X, 0.0f, inner2Z, col);
        }
        matrices.pop();
    }

    /** Орбита: предметы по кругу вокруг корпуса, каждый крутится вокруг своей оси. */
    private void a(MatrixStack matrices, VertexConsumerProvider provider, BipedEntityModel<?> model,
                   int light, float size, float time) {
        int count = this.orbitCount.c().intValue();
        matrices.push();
        model.body.rotate(matrices);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
        matrices.translate(0.0f, -0.25f, 0.0f);
        for (int i = 0; i < count; i++) {
            float angle = (((i / (float) count) * 360.0f) + (time * 40.0f)) % 360.0f;
            float radius = 0.6f * size;
            float bob = MathHelper.sin((time * 2.0f) + (i * 1.7f)) * 0.06f;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
            matrices.translate(0.0f, bob, radius);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((time * 90.0f) % 360.0f));
            matrices.scale(0.35f * size, 0.35f * size, 0.35f * size);
            mc.getItemRenderer().renderItem(ORBIT_ITEMS[i % ORBIT_ITEMS.length], ModelTransformationMode.GROUND,
                    light, OverlayTexture.DEFAULT_UV, matrices, provider, mc.world, 0);
            matrices.pop();
        }
        matrices.pop();
    }

    private void a(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int rgba) {
        int[] unpack = ColorUtil.b(rgba);
        buffer.vertex(matrix, x, y, z).color(unpack[0], unpack[1], unpack[2], unpack[3]);
    }
}
