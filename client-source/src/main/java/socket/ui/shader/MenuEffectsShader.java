package socket.ui.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import socket.core.Socket;
import org.joml.Matrix4f;

/** Astra-style opening flash and cursor-reactive halftone, in one GUI draw. */
public final class MenuEffectsShader extends Shader {
    public MenuEffectsShader() {
        super(Identifier.of("socket", "core/menu_effects"), VertexFormats.POSITION_COLOR);
    }

    @Override protected void b() {}

    public void render(DrawContext context, float width, float height, float mouseX, float mouseY, float progress) {
        context.draw();
        a();
        if (d() == null) return;
        a("Size").set(width, height);
        a("Mouse").set(mouseX, mouseY);
        a("Progress").set(progress);
        int color = Socket.getInstance().getProcessors().themes().a(socket.config.ThemeInfo.PRIMARY).toIntColor();
        a("Accent").set(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0, 0, 0).color(-1);
        buffer.vertex(matrix, 0, height, 0).color(-1);
        buffer.vertex(matrix, width, height, 0).color(-1);
        buffer.vertex(matrix, width, 0, 0).color(-1);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}

