package socket.ui.screen;

import socket.ui.GlassStyle;
import socket.config.ModuleProcessor;
import socket.config.ThemeInfo;
import socket.config.ThemeProcessor;
import socket.core.Socket;
import socket.render.AnimationUtil;
import socket.render.ColorUtil;
import socket.render.Draw2DProcessor;
import socket.render.EasingList;
import socket.render.Fonts;
import socket.render.ScissorUtil;
import socket.ui.element.TextField;
import socket.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Боковая панель конфигов рядом с ClickGUI: список сохранённых профилей,
 * поле имени, кнопка сохранения и удаление по иконке в строке.
 */
public class ConfigPanel {
    public static final float WIDTH = 104.0f;
    private static final float HEADER = 24.0f;
    private static final float ROW = 14.0f;
    private static final float FIELD = 18.0f;

    private final AnimationUtil appear = new AnimationUtil();
    private final TextField name = new TextField(TextField.type.GUI);
    private final List<String> configs = new ArrayList<>();

    private float x;
    private float y;
    private float height;
    private float scroll;
    private int hovered = -1;
    private boolean hoveredDelete;
    private boolean hoveredSave;
    private long refreshed;

    public ConfigPanel() {
        this.name.setPlaceholder("Имя конфига");
    }

    private static ModuleProcessor processor() {
        return Socket.getInstance().getProcessors().modules();
    }

    public AnimationUtil a() {
        return this.appear;
    }

    public TextField b() {
        return this.name;
    }

    /** Список файлов обновляем не каждый кадр, а раз в полсекунды. */
    private void c() {
        long now = System.currentTimeMillis();
        if (now - this.refreshed < 500L && !this.configs.isEmpty()) {
            return;
        }
        this.refreshed = now;
        this.configs.clear();
        File dir = processor().d();
        File[] files = dir.exists() ? dir.listFiles((file, fileName) -> fileName.endsWith(".json")) : null;
        if (files == null) {
            return;
        }
        Arrays.sort(files, (left, right) -> left.getName().compareToIgnoreCase(right.getName()));
        for (File file : files) {
            this.configs.add(file.getName().substring(0, file.getName().length() - 5));
        }
    }

    public void a(DrawContext context, float panelX, float panelY, float panelHeight, int mouseX, int mouseY, float delta) {
        c();
        // без явного признака «раскрываемся» анимация остаётся на нуле и панель не рисуется
        this.appear.a(true);
        this.appear.a(0.0f, 1.0f, 0.35f, EasingList.i, delta);
        float fade = EasingList.p.ease(this.appear.c());
        if (fade <= 0.01f) {
            return;
        }
        this.x = panelX;
        this.y = panelY;
        this.height = panelHeight;
        Draw2DProcessor draw = Socket.getInstance().getProcessors().draw2D();
        ThemeProcessor theme = Socket.getInstance().getProcessors().themes();
        int background = ColorUtil.combineColorWithAlpha(ColorUtil.lerpColor(theme.a(ThemeInfo.BACKGROUND_GUI).toIntColor(),
                theme.a(ThemeInfo.PRIMARY).toIntColor(), theme.a(ThemeInfo.PRIMARY).getAlphaFloat() / 4.0f), 200);
        boolean glass = GlassStyle.menu();
        if (glass) {
            background = GlassStyle.tint(background);
        }
        context.getMatrices().push();
        context.getMatrices().translate((-(1.0f - fade)) * 12.0f, 0.0f, 0.0f);
        draw.a(context.getMatrices(), this.x, this.y, WIDTH, this.height, 8.0f, background, 1.0f, background, 16.0f);
        draw.a(context.getMatrices(), this.x, this.y, WIDTH, this.height, 8.0f, 0.5f,
                theme.a(ThemeInfo.OUTLINE_MEDIUM).toIntColor());
        if (glass) {
            GlassStyle.sheen(draw, context.getMatrices(), this.x, this.y, WIDTH, this.height, 8.0f, 1.0f);
        }
        int titleColor = ColorUtil.lerpColor(ColorUtil.convertToARGB(255, 255, 255, 255),
                theme.a(ThemeInfo.PRIMARY).toIntColor(), 0.25f);
        Fonts.c.a(context.getMatrices(), "Конфиги", this.x + 10.0f,
                Fonts.c.a("Конфиги", 9.0f, this.y + (HEADER / 2.0f)), 9.0f, titleColor);
        Fonts.a.a(context.getMatrices(), "f", (this.x + WIDTH) - 18.0f,
                Fonts.a.a("f", 9.0f, this.y + (HEADER / 2.0f)), 9.0f, titleColor);
        a(context, mouseX, mouseY, delta, theme, draw);
        context.getMatrices().pop();
    }

    /** Список конфигов, поле имени и кнопка сохранения. */
    private void a(DrawContext context, int mouseX, int mouseY, float delta, ThemeProcessor theme, Draw2DProcessor draw) {
        float listTop = this.y + HEADER;
        float listBottom = ((this.y + this.height) - FIELD) - 10.0f;
        float view = listBottom - listTop;
        float content = this.configs.size() * ROW;
        this.scroll = MathUtil.b(this.scroll, Math.min(0.0f, view - content), 0.0f);
        this.hovered = -1;
        this.hoveredDelete = false;
        int textColor = theme.a(ThemeInfo.TEXT).toIntColor();
        int accent = theme.a(ThemeInfo.PRIMARY).toIntColor();
        ScissorUtil.a(context.getMatrices(), this.x, listTop, WIDTH, view);
        float rowY = listTop + this.scroll;
        for (int index = 0; index < this.configs.size(); index++) {
            String config = this.configs.get(index);
            boolean active = "default".equalsIgnoreCase(config);
            boolean hover = mouseY >= listTop && mouseY <= listBottom
                    && MathUtil.a(mouseX, mouseY, this.x + 5.0f, rowY, WIDTH - 10.0f, ROW - 1.5f);
            if (hover) {
                this.hovered = index;
                this.hoveredDelete = mouseX >= (this.x + WIDTH) - 18.0f;
            }
            if (hover || active) {
                draw.a(context.getMatrices(), this.x + 5.0f, rowY, WIDTH - 10.0f, ROW - 1.5f, 3.0f,
                        ColorUtil.applyAlphaToColor(active ? accent : ColorUtil.convertToARGB(255, 255, 255, 255),
                                active ? 0.12f : 0.05f));
            }
            if (active) {
                draw.a(context.getMatrices(), this.x + 6.5f, rowY + 3.0f, 1.5f, ROW - 7.5f, 0.75f,
                        ColorUtil.applyAlphaToColor(accent, 0.9f));
            }
            Fonts.c.a(context.getMatrices(), config, this.x + 11.0f,
                    Fonts.c.a(config, 7.25f, rowY + ((ROW - 1.5f) / 2.0f)), 7.25f,
                    ColorUtil.applyAlphaToColor(textColor, active ? 1.0f : 0.85f));
            if (hover && !active) {
                Fonts.a.a(context.getMatrices(), "x", (this.x + WIDTH) - 16.0f,
                        Fonts.a.a("x", 7.0f, rowY + ((ROW - 1.5f) / 2.0f)), 7.0f,
                        ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(235, 90, 90, 255),
                                this.hoveredDelete ? 1.0f : 0.5f));
            }
            rowY += ROW;
        }
        ScissorUtil.a(context.getMatrices());
        float fieldY = listBottom + 4.0f;
        this.name.setSize(new Vector2f(WIDTH - 34.0f, FIELD));
        this.name.setPosition(new Vector2f(this.x + 5.0f, fieldY));
        this.name.render(context, mouseX, mouseY, delta, 1.0f);
        float buttonX = (this.x + WIDTH) - 27.0f;
        this.hoveredSave = MathUtil.a(mouseX, mouseY, buttonX, fieldY, 22.0f, FIELD);
        draw.a(context.getMatrices(), buttonX, fieldY, 22.0f, FIELD, 4.0f,
                ColorUtil.applyAlphaToColor(accent, this.hoveredSave ? 0.35f : 0.18f));
        Fonts.a.a(context.getMatrices(), "S", buttonX + 8.0f,
                Fonts.a.a("S", 8.0f, fieldY + (FIELD / 2.0f)), 8.0f,
                ColorUtil.applyAlphaToColor(textColor, this.hoveredSave ? 1.0f : 0.75f));
    }

    /** @return true, если клик обработан панелью */
    public boolean a(double mouseX, double mouseY, int button) {
        this.name.onMouseClick(mouseX, mouseY, button);
        if (this.hoveredSave && button == 0) {
            String typed = this.name.getTextBuffer().toString().trim();
            processor().b(typed.isEmpty() ? "default" : typed);
            this.refreshed = 0L;
            return true;
        }
        if (this.hovered < 0 || this.hovered >= this.configs.size()) {
            return MathUtil.a(mouseX, mouseY, this.x, this.y, WIDTH, this.height);
        }
        String config = this.configs.get(this.hovered);
        if (button == 0 && this.hoveredDelete && !"default".equalsIgnoreCase(config)) {
            processor().d(config);
            this.refreshed = 0L;
            return true;
        }
        if (button == 0) {
            processor().c(config);
            return true;
        }
        return true;
    }

    public boolean a(double mouseX, double mouseY, double amount) {
        if (!MathUtil.a(mouseX, mouseY, this.x, this.y, WIDTH, this.height)) {
            return false;
        }
        this.scroll += (float) amount * 12.0f;
        return true;
    }

    public boolean a(int keyCode, int scanCode, int modifiers) {
        if (!this.name.isFocused()) {
            return false;
        }
        this.name.a(keyCode, scanCode, modifiers);
        return true;
    }

    public boolean a(char character, int modifiers) {
        if (!this.name.isFocused()) {
            return false;
        }
        this.name.a(character, modifiers);
        return true;
    }
}
