package socket.ui.shader;

import socket.core.EventManager;
import socket.core.EventTarget;
import socket.core.Interface;
import socket.event.ResizeEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class BlurShader extends Shader implements Interface {
    /** Уровней в цепочке размытия; каждый следующий буфер вдвое меньше предыдущего. */
    private static final int LEVELS = 4;
    /** Первый буфер делается вдвое меньше кадра — размытию полное разрешение не нужно. */
    private static final int BASE_SHIFT = 1;

    private final List<SimpleFramebuffer> n;
    private final ShaderProgramKey o;
    private final ShaderProgramKey p;
    private int bufferWidth;
    private int bufferHeight;
    private boolean sampled;
    private int forcedFrames;
    public Uniform c;
    public Uniform d;
    public Uniform e;
    public Uniform f;
    public Uniform g;
    public Uniform h;
    public Uniform i;
    public Uniform j;
    public Uniform k;
    public Uniform l;
    public Uniform m;

    public BlurShader() {
        super(Identifier.of("socket", "core/rect/blurred_rect"), VertexFormats.POSITION_TEXTURE_COLOR);
        this.n = new ArrayList<>();
        this.o = new ShaderProgramKey(Identifier.of("socket", "core/blur/upscale"), VertexFormats.POSITION, Defines.EMPTY);
        this.p = new ShaderProgramKey(Identifier.of("socket", "core/blur/downscale"), VertexFormats.POSITION, Defines.EMPTY);
        EventManager.a(this);
    }

    public List<SimpleFramebuffer> e() {
        return this.n;
    }

    /** Отмечает, что размытый кадр в этом кадре кто-то использует, — иначе цепочку не считаем. */
    public void markSampled() {
        this.sampled = true;
    }

    @EventTarget
    public void a(ResizeEvent event) {
        rebuildBuffers();
    }

    /** Пересобирает цепочку под текущий размер кадра; при свёрнутом окне буферы не создаём. */
    private void rebuildBuffers() {
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            releaseBuffers();
            this.bufferWidth = 0;
            this.bufferHeight = 0;
            return;
        }
        if (!this.n.isEmpty() && this.bufferWidth == width && this.bufferHeight == height) {
            return;
        }
        releaseBuffers();
        for (int level = 0; level < LEVELS; level++) {
            int shift = BASE_SHIFT + level;
            this.n.add(new SimpleFramebuffer(Math.max(1, width >> shift), Math.max(1, height >> shift), false));
        }
        this.bufferWidth = width;
        this.bufferHeight = height;
        // после пересоздания буферы пустые — прогоняем цепочку принудительно, чтобы не мигало
        this.forcedFrames = 2;
    }

    /** Удаляет буферы вместе с текстурами — без этого каждый ресайз тёк в видеопамять. */
    private void releaseBuffers() {
        for (SimpleFramebuffer framebuffer : this.n) {
            framebuffer.delete();
        }
        this.n.clear();
    }

    @Override
    protected void b() {
        this.c = a("uSize");
        this.d = a("uRadius");
        this.e = a("uSmoothness");
        this.f = a("uMix");
        this.g = a("uAlpha");
        this.h = a("uTopLeftColor");
        this.i = a("uBottomLeftColor");
        this.j = a("uTopRightColor");
        this.k = a("uBottomRightColor");
        this.l = a("uGlowColor");
        this.m = a("uGlowRadius");
    }

    public void a(MatrixStack matrixStack) {
        rebuildBuffers();
        if (this.n.isEmpty()) {
            return;
        }
        // цепочку считаем только если размытие реально используется — иначе это 7 проходов в пустоту
        boolean needed = this.sampled || mc.currentScreen != null || this.forcedFrames > 0;
        this.sampled = false;
        if (this.forcedFrames > 0) {
            this.forcedFrames--;
        }
        if (!needed) {
            return;
        }
        int actualPasses = Math.max(this.n.size() - 1, 1);
        try (socket.render.RenderState state = new socket.render.RenderState(true)) {
            a(matrixStack, this.p, mc.getFramebuffer(), this.n.getFirst(), 0, 24);
            for (int i = 0; i < actualPasses; i++) {
                a(matrixStack, this.p, this.n.get(i), this.n.get(i + 1), i + 1, 24);
            }
            for (int i2 = actualPasses; i2 > 0; i2--) {
                a(matrixStack, this.o, this.n.get(i2), this.n.get(i2 - 1), i2, 24);
            }
        } finally {
            mc.getFramebuffer().beginWrite(true);
        }
    }

    private void a(MatrixStack matrixStack, ShaderProgramKey shaderKey, Framebuffer source, Framebuffer destination, int pass, int offset) {
        destination.beginWrite(true);
        RenderSystem.setShaderTexture(0, source.getColorAttachment());
        ShaderProgram shader = RenderSystem.setShader(shaderKey);
        GlUniform class_284VarMethod_34582 = shader.getUniform("uHalfTexelSize");
        GlUniform class_284VarMethod_34583 = shader.getUniform("uOffset");
        if (class_284VarMethod_34582 != null) {
            class_284VarMethod_34582.set(0.5f / source.textureWidth, 0.5f / source.textureHeight);
        }
        if (class_284VarMethod_34583 != null) {
            class_284VarMethod_34583.set(offset * (pass / 3.0f));
        }
        a(matrixStack.peek().getPositionMatrix());
        destination.endWrite();
    }

    private void a(Matrix4f matrix4f) {
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        builder.vertex(matrix4f, 0.0f, 0.0f, 0.0f);
        builder.vertex(matrix4f, 0.0f, mc.getWindow().getScaledHeight(), 0.0f);
        builder.vertex(matrix4f, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0.0f);
        builder.vertex(matrix4f, mc.getWindow().getScaledWidth(), 0.0f, 0.0f);
        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    public void a(float width, float height) {
        if (this.c != null) {
            this.c.set(width, height);
        }
    }

    public void a(Vector4f radius) {
        if (this.d != null) {
            this.d.set(radius.x, radius.z, radius.w, radius.y);
        }
    }

    public void a(float smoothness) {
        if (this.e != null) {
            this.e.set(smoothness);
        }
    }

    public void b(float mix) {
        if (this.f != null) {
            this.f.set(mix);
        }
    }

    public void c(float alpha) {
        if (this.g != null) {
            this.g.set(alpha);
        }
    }

    public void a(float r, float g, float b, float a) {
        if (this.h != null) {
            this.h.set(r, g, b, a);
        }
    }

    public void b(float r, float g, float b, float a) {
        if (this.i != null) {
            this.i.set(r, g, b, a);
        }
    }

    public void c(float r, float g, float b, float a) {
        if (this.j != null) {
            this.j.set(r, g, b, a);
        }
    }

    public void d(float r, float g, float b, float a) {
        if (this.k != null) {
            this.k.set(r, g, b, a);
        }
    }

    public void e(float r, float g, float b, float a) {
        if (this.l != null) {
            this.l.set(r, g, b, a);
        }
    }

    public void d(float r) {
        if (this.m != null) {
            this.m.set(r);
        }
    }
}
