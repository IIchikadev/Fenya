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
import aethereal.setting.SliderSetting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Плащ с физикой: цепочка сегментов, каждый на своей пружине.
 * Наклон задаёт скорость игрока, поверх идёт волна ветра, поэтому плащ
 * отстаёт при разгоне и продолжает качаться после остановки.
 */
@ModuleRegister(name = "Cape", description = "Плащ с физикой на модели игрока", category = Category.Render)
public class Cape extends Module {
    private static final int SEGMENTS = 6;

    private final ModeSetting colorMode = new ModeSetting("Цвет", "Тема клиента", "Тема клиента", "Свой цвет");
    private final ColorSetting color;
    private final SliderSetting length = new SliderSetting("Длина", 0.13f, 0.08f, 0.2f, 0.01f);
    private final SliderSetting width = new SliderSetting("Ширина", 0.24f, 0.14f, 0.34f, 0.02f);
    private final SliderSetting stiffness = new SliderSetting("Жёсткость", 26.0f, 8.0f, 60.0f, 2.0f);
    private final SliderSetting damping = new SliderSetting("Затухание", 6.0f, 2.0f, 14.0f, 0.5f);
    private final SliderSetting wind = new SliderSetting("Ветер", 6.0f, 0.0f, 20.0f, 1.0f);
    private final BooleanSetting onFriends = new BooleanSetting("Показывать на друзьях", true);

    private final Map<UUID, State> states = new HashMap<>();

    public Cape() {
        this.color = new ColorSetting("Свой цвет", Integer.valueOf(ColorUtil.a(184, 255, 60)))
                .a(() -> Boolean.valueOf(this.colorMode.l("Свой цвет")));
        a(this.colorMode, this.color, this.length, this.width, this.stiffness, this.damping, this.wind, this.onFriends);
    }

    @Override
    public void b() {
        super.b();
        this.states.clear();
    }

    @Override
    public void c() {
        super.c();
        this.states.clear();
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
        if (player.isInSneakingPose() && !self) {
            return;
        }
        State state = this.states.computeIfAbsent(player.getUuid(), uuid -> new State());
        state.update(player, this.stiffness.c().floatValue(), this.damping.c().floatValue(), this.wind.c().floatValue());
        int base = this.colorMode.l("Свой цвет") ? this.color.c().intValue()
                : Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor();
        a(event.getMatrixStack(), event.getVertexConsumerProvider(), model, state, base);
        if (event.getVertexConsumerProvider() instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private void a(MatrixStack matrices, VertexConsumerProvider provider, BipedEntityModel<?> model, State state, int base) {
        matrices.push();
        model.body.rotate(matrices);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
        matrices.translate(0.0f, 0.02f, 0.13f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = provider.getBuffer(RenderLayer.getDebugQuads());
        float segment = this.length.c().floatValue();
        float half = this.width.c().floatValue() / 2.0f;
        float y = 0.0f;
        float z = 0.0f;
        for (int i = 0; i < SEGMENTS; i++) {
            float angle = state.angles[i] * 0.017453292f;
            float nextY = y - (MathHelper.cos(angle) * segment);
            float nextZ = z + (MathHelper.sin(angle) * segment);
            float topHalf = half * (1.0f - (0.06f * i));
            float bottomHalf = half * (1.0f - (0.06f * (i + 1)));
            int topColor = ColorUtil.applyAlphaToColor(base, 0.92f - (0.07f * i));
            int bottomColor = ColorUtil.applyAlphaToColor(base, 0.92f - (0.07f * (i + 1)));
            a(buffer, matrix, -topHalf, y, z, topColor);
            a(buffer, matrix, -bottomHalf, nextY, nextZ, bottomColor);
            a(buffer, matrix, bottomHalf, nextY, nextZ, bottomColor);
            a(buffer, matrix, topHalf, y, z, topColor);
            // обратная намотка, чтобы плащ был виден с обеих сторон
            a(buffer, matrix, topHalf, y, z, topColor);
            a(buffer, matrix, bottomHalf, nextY, nextZ, bottomColor);
            a(buffer, matrix, -bottomHalf, nextY, nextZ, bottomColor);
            a(buffer, matrix, -topHalf, y, z, topColor);
            y = nextY;
            z = nextZ;
        }
        matrices.pop();
    }

    private void a(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int rgba) {
        int[] unpack = ColorUtil.b(rgba);
        buffer.vertex(matrix, x, y, z).color(unpack[0], unpack[1], unpack[2], unpack[3]);
    }

    /** Состояние плаща одного игрока: угол и скорость каждого сегмента. */
    private static final class State {
        private final float[] angles = new float[SEGMENTS];
        private final float[] velocities = new float[SEGMENTS];
        private long updated = System.currentTimeMillis();

        private void update(PlayerEntity player, float stiffness, float damping, float wind) {
            long now = System.currentTimeMillis();
            float delta = Math.min(0.05f, (now - this.updated) / 1000.0f);
            this.updated = now;
            if (delta <= 0.0f) {
                return;
            }
            double speed = Math.sqrt((player.getVelocity().x * player.getVelocity().x)
                    + (player.getVelocity().z * player.getVelocity().z));
            float lift = (float) Math.min(78.0d, speed * 260.0d);
            if (player.isGliding()) {
                lift = Math.max(lift, 70.0f);
            }
            float time = (now % 100000L) / 1000.0f;
            for (int i = 0; i < SEGMENTS; i++) {
                float share = 1.0f - ((i / (float) SEGMENTS) * 0.35f);
                float wave = MathHelper.sin((time * 2.4f) + (i * 0.7f)) * wind * (0.4f + (0.6f * (i / (float) SEGMENTS)));
                float target = (lift * share) + wave;
                this.velocities[i] = this.velocities[i] + ((target - this.angles[i]) * stiffness * delta);
                this.velocities[i] = this.velocities[i] - (this.velocities[i] * Math.min(1.0f, damping * delta));
                this.angles[i] = MathHelper.clamp(this.angles[i] + (this.velocities[i] * delta), -25.0f, 100.0f);
            }
        }
    }
}
