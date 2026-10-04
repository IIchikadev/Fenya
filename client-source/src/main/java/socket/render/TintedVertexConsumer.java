package socket.render;

import net.minecraft.client.render.VertexConsumer;

/**
 * Обёртка над буфером вершин, домножающая цвет каждой вершины на заданный оттенок.
 * Нужна, чтобы перекрасить ванильное свечение зачарования, не трогая его шейдер:
 * блик рисуется белым, поэтому умножение задаёт итоговый цвет напрямую.
 */
public class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float red;
    private final float green;
    private final float blue;

    public TintedVertexConsumer(VertexConsumer delegate, int tint, float brightness) {
        this.delegate = delegate;
        int[] unpack = ColorUtil.b(tint);
        this.red = (unpack[0] / 255.0f) * brightness;
        this.green = (unpack[1] / 255.0f) * brightness;
        this.blue = (unpack[2] / 255.0f) * brightness;
    }

    private static int clamp(float value) {
        return (int) Math.max(0.0f, Math.min(255.0f, value));
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        this.delegate.color(clamp(r * this.red), clamp(g * this.green), clamp(b * this.blue), a);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }
}
