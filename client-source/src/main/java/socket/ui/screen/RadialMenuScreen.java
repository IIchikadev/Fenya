package socket.ui.screen;

import socket.config.ThemeInfo;
import socket.core.Category;
import socket.core.Interface;
import socket.core.Module;
import socket.core.Socket;
import socket.render.AnimationUtil;
import socket.render.ColorUtil;
import socket.render.Draw2DProcessor;
import socket.render.EasingList;
import socket.render.Fonts;
import socket.render.ScaleUtil;
import socket.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Круговое меню быстрого доступа: в центре категории, по кольцу — модули выбранной категории. */
public class RadialMenuScreen extends Screen implements Interface {
    private final Map<String, AnimationUtil> animations = new HashMap<>();
    private final AnimationUtil open = new AnimationUtil();
    private Category selected = Category.Render;
    private Module hovered;
    private Category hoveredCategory;

    public RadialMenuScreen() {
        super(Text.empty());
    }

    private AnimationUtil a(String key) {
        return this.animations.computeIfAbsent(key, name -> new AnimationUtil());
    }

    private List<Module> a() {
        List<Module> modules = new ArrayList<>();
        for (Module module : Socket.getInstance().getProcessors().modules().e()) {
            if (module.l() == this.selected) {
                modules.add(module);
            }
        }
        modules.sort((first, second) -> first.j().compareToIgnoreCase(second.j()));
        return modules;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.open.a(true);
        this.open.a(0.0f, 1.0f, 0.25f, EasingList.g, delta);
        float appear = EasingList.s.ease(this.open.c());
        double mx = MathUtil.scale(mouseX, 2);
        double my = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        float width = mc.getWindow().getScaledWidth();
        float height = mc.getWindow().getScaledHeight();
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;
        Draw2DProcessor draw = Socket.getInstance().getProcessors().draw2D();
        MatrixStack matrices = context.getMatrices();
        int primary = Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
        draw.a(matrices, 0.0f, 0.0f, width, height, 0.0f, ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(6, 7, 9, 255), 0.55f * appear));
        float radius = Math.min(width, height) * 0.3f * appear;
        for (int ring = 0; ring < 2; ring++) {
            float size = (radius * 2.0f) - (ring * 26.0f);
            draw.a(matrices, centerX - (size * 0.5f), centerY - (size * 0.5f), size, size, size * 0.5f, 0.5f,
                    ColorUtil.convertToARGB(255, 255, 255, (int) (12.0f * appear)));
        }
        a(context, centerX, centerY, (float) mx, (float) my, appear, delta);
        b(context, centerX, centerY, radius, (float) mx, (float) my, appear, delta);
        ScaleUtil.a(context);
    }

    // центральный столбик категорий
    private void a(DrawContext context, float centerX, float centerY, float mouseX, float mouseY, float appear, float delta) {
        Draw2DProcessor draw = Socket.getInstance().getProcessors().draw2D();
        MatrixStack matrices = context.getMatrices();
        int primary = Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
        Category[] categories = Category.values();
        float chipHeight = 16.0f;
        float chipWidth = 74.0f;
        float gap = 3.0f;
        float total = (categories.length * chipHeight) + ((categories.length - 1) * gap);
        float y = centerY - (total * 0.5f);
        this.hoveredCategory = null;
        for (Category category : categories) {
            float x = centerX - (chipWidth * 0.5f);
            boolean hover = MathUtil.a(mouseX, mouseY, x, y, chipWidth, chipHeight);
            if (hover) {
                this.hoveredCategory = category;
            }
            AnimationUtil animation = a("category:" + category.name());
            animation.a(hover || category == this.selected);
            animation.a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
            float active = animation.c();
            draw.b(matrices, x, y, chipWidth, chipHeight, 6.0f, ColorUtil.convertToARGB(13, 15, 19, 175), appear);
            draw.a(matrices, x, y, chipWidth, chipHeight, 6.0f, ColorUtil.applyAlphaToColor(primary, 0.12f * active * appear));
            draw.a(matrices, x, y, chipWidth, chipHeight, 6.0f, 0.5f,
                    ColorUtil.applyAlphaToColor(primary, (0.2f + (0.5f * active)) * appear));
            String icon = category.a();
            Fonts.a.a(matrices, icon, x + 7.0f, Fonts.a.a(icon, 7.5f, y + (chipHeight * 0.5f)), 7.5f,
                    ColorUtil.applyAlphaToColor(primary, appear));
            Fonts.c.a(matrices, category.b(), x + 20.0f, Fonts.c.a(category.b(), 7.5f, y + (chipHeight * 0.5f)), 7.5f,
                    ColorUtil.convertToARGB(255, 255, 255, (int) ((150.0f + (105.0f * active)) * appear)));
            y += chipHeight + gap;
        }
    }

    // модули по кольцу
    private void b(DrawContext context, float centerX, float centerY, float radius, float mouseX, float mouseY, float appear, float delta) {
        Draw2DProcessor draw = Socket.getInstance().getProcessors().draw2D();
        MatrixStack matrices = context.getMatrices();
        int primary = Socket.getInstance().getProcessors().themes().a(ThemeInfo.PRIMARY).toIntColor();
        List<Module> modules = a();
        this.hovered = null;
        if (modules.isEmpty()) {
            return;
        }
        float pillHeight = 15.0f;
        for (int index = 0; index < modules.size(); index++) {
            Module module = modules.get(index);
            double angle = ((index / (double) modules.size()) * Math.PI * 2.0d) - (Math.PI * 0.5d);
            float pillWidth = Fonts.c.a(module.j(), 7.0f) + 26.0f;
            float x = (float) (centerX + (Math.cos(angle) * radius)) - (pillWidth * 0.5f);
            float y = (float) (centerY + (Math.sin(angle) * radius)) - (pillHeight * 0.5f);
            boolean hover = MathUtil.a(mouseX, mouseY, x, y, pillWidth, pillHeight);
            if (hover) {
                this.hovered = module;
            }
            AnimationUtil hoverAnimation = a("hover:" + module.j());
            hoverAnimation.a(hover);
            hoverAnimation.a(0.0f, 1.0f, 0.25f, EasingList.i, delta);
            module.f().a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
            float enabled = module.f().c();
            float hoverValue = hoverAnimation.c();
            draw.b(matrices, x, y, pillWidth, pillHeight, 7.0f, ColorUtil.convertToARGB(13, 15, 19, 180), appear);
            draw.a(matrices, x, y, pillWidth, pillHeight, 7.0f, ColorUtil.applyAlphaToColor(primary, (0.1f + (0.18f * hoverValue)) * enabled * appear));
            draw.a(matrices, x, y, pillWidth, pillHeight, 7.0f, 0.5f,
                    ColorUtil.applyAlphaToColor(primary, (0.18f + (0.55f * Math.max(enabled, hoverValue))) * appear));
            float dot = 5.0f + (1.5f * enabled);
            draw.a(matrices, x + 7.0f - (dot * 0.5f) + 1.0f, y + (pillHeight * 0.5f) - (dot * 0.5f), dot, dot, dot * 0.5f,
                    ColorUtil.applyAlphaToColor(ColorUtil.lerpColor(ColorUtil.convertToARGB(255, 255, 255, 60), primary, enabled), appear));
            Fonts.c.a(matrices, module.j(), x + 15.0f, Fonts.c.a(module.j(), 7.0f, y + (pillHeight * 0.5f)), 7.0f,
                    ColorUtil.convertToARGB(255, 255, 255, (int) ((155.0f + (100.0f * Math.max(enabled, hoverValue))) * appear)));
        }
        String hint = this.hovered != null ? this.hovered.k() : this.selected.b();
        Fonts.c.a(matrices, hint, centerX - (Fonts.c.a(hint, 7.0f) * 0.5f), centerY + radius + 20.0f, 7.0f,
                ColorUtil.convertToARGB(255, 255, 255, (int) (110.0f * appear)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoveredCategory != null) {
            this.selected = this.hoveredCategory;
            return true;
        }
        if (this.hovered != null) {
            this.hovered.a();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
