package aethereal.ui.screen;


import aethereal.config.ThemeInfo;
import aethereal.core.Socket;
import aethereal.core.Interface;
import aethereal.render.*;
import aethereal.ui.element.Button;
import aethereal.ui.widget.EffectMarker;
import aethereal.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Главное меню Socket Client — «коммутационная панель».
 * Слева вертикальная надпись клиента, по центру список портов-разделов,
 * справа хаб: при наведении от порта к хабу тянется живой кабель.
 */
public class MainScreen extends Screen {
    private static final float[] a = new float[2];
    private static final Identifier LOGO_MAIN = Identifier.of("socket", "pictures/logo_main.png");
    private static final float PORT_WIDTH = 172.0f;
    private static final float PORT_HEIGHT = 30.0f;
    private static final float PORT_GAP = 7.0f;
    private static final float CABLE_GAP = 96.0f;
    private static final float WORDMARK_SPACE = 82.0f;

    private final AnimationUtil b;
    private final List<Button> g;
    private final List<EffectMarker.a> h;
    private final String[] m = {"h", "P", "L", "%"};
    private final String[] n = {"локальный мир", "внешний сервер", "смена профиля", "клиент и игра"};
    private final float[] o = new float[4];
    private float i;
    private float j;
    private float k;
    private float l;
    private float q;
    private int r = -1;
    private int s = -1;

    public MainScreen() {
        super(Text.empty());
        this.b = new AnimationUtil();
        this.h = new ArrayList<>();
        this.j = -1.0f;
        if (Interface.mc.currentScreen instanceof MainScreen) {
            this.b.c(1.0f);
            this.b.d(1.0f);
            this.b.e(1.0f);
        }
        List<Button> ports = new ArrayList<>();
        ports.add(new Button(PORT_WIDTH, PORT_HEIGHT, "Одиночная игра", () -> Interface.mc.setScreen(new SelectWorldScreen(null))));
        ports.add(new Button(PORT_WIDTH, PORT_HEIGHT, "Сетевая игра", () -> Interface.mc.setScreen(new MultiplayerScreen(null))));
        ports.add(new Button(PORT_WIDTH, PORT_HEIGHT, "Аккаунты", () -> Interface.mc.setScreen(new AltScreen())));
        ports.add(new Button(PORT_WIDTH, PORT_HEIGHT, "Настройки", () -> Interface.mc.setScreen(new OptionsScreen(null, Interface.mc.options))));
        this.g = List.copyOf(ports);
    }

    private static int accent() {
        return Socket.getInstance().getModuleProcessor().o().a(ThemeInfo.PRIMARY).toIntColor();
    }

    private static float time(float period) {
        return (System.currentTimeMillis() % ((long) period)) / period;
    }

    public static void a(DrawContext context, int width, int height, int mouseX, int mouseY, float scale) {
        float marginX = width * 0.025f;
        float marginY = height * 0.025f;
        a[0] += (MathHelper.clamp((((float) mouseX / width) - 0.5f) * 2.0f * marginX, -marginX * 0.9f, marginX * 0.9f) - a[0]) * 0.03f;
        a[1] += (MathHelper.clamp((((float) mouseY / height) - 0.5f) * 2.0f * marginY, -marginY * 0.9f, marginY * 0.9f) - a[1]) * 0.03f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(width / 2.0f, height / 2.0f, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-width / 2.0f, -height / 2.0f, 0.0f);
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        int top = ColorUtil.convertToARGB(12, 14, 18, 255);
        int bottom = ColorUtil.convertToARGB(6, 7, 9, 255);
        draw.a(matrices, -marginX, -marginY, width + (marginX * 2.0f), height + (marginY * 2.0f), 0.0f, top, top, bottom, bottom);
        int primary = accent();
        // мягкое свечение под хабом справа и холодная подсветка слева
        float glow = Math.min(width, height) * 0.52f;
        draw.a(matrices, (width * 0.64f) - (glow * 0.5f) + (a[0] * 1.5f), (height * 0.5f) - (glow * 0.5f) + (a[1] * 1.5f), glow, glow,
                glow * 0.5f, ColorUtil.applyAlphaToColor(primary, 0.0f), 1.0f, ColorUtil.applyAlphaToColor(primary, 0.16f), glow * 0.5f);
        float glow2 = Math.min(width, height) * 0.4f;
        draw.a(matrices, (width * 0.3f) - (glow2 * 0.5f) - (a[0] * 1.5f), (height * 0.55f) - (glow2 * 0.5f) - (a[1] * 1.5f), glow2, glow2,
                glow2 * 0.5f, ColorUtil.convertToARGB(60, 120, 190, 0), 1.0f, ColorUtil.convertToARGB(60, 120, 190, 20), glow2 * 0.5f);
        // диагональные шлейфы на фоне
        for (int i = 0; i < 5; i++) {
            float y = (height * (0.16f + (i * 0.17f))) + a[1];
            matrices.push();
            matrices.translate(width * 0.5f, y, 0.0f);
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(i % 2 == 0 ? -18.0f : 14.0f)));
            matrices.translate(-width * 0.5f, -y, 0.0f);
            draw.a(matrices, -width * 0.2f, y, width * 1.4f, 0.5f, 0.25f, ColorUtil.convertToARGB(255, 255, 255, 6));
            matrices.pop();
        }
        matrices.pop();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.b.a(Interface.mc.currentScreen instanceof MainScreen);
        this.b.a(0.0f, 1.0f, 0.15f, EasingList.g, delta);
        float open = Math.min(1.0f, this.b.c() / 0.9f);
        int mx = (int) MathUtil.scale(mouseX, 2);
        int my = (int) MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        int width = Interface.mc.getWindow().getScaledWidth();
        int height = Interface.mc.getWindow().getScaledHeight();
        a(context, width, height, mx, my, 1.25f - (EasingList.s.ease(open) * 0.2f));
        Socket.getInstance().getModuleProcessor().i().e().a(context.getMatrices());
        b(width, height);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0f, (1.0f - EasingList.p.ease(open)) * 12.0f, 0.0f);
        a(context, width, height, open);
        float hubX = c(width) + PORT_WIDTH + CABLE_GAP + (hubOuter(open) * 0.5f);
        float hubY = height * 0.5f;
        this.r = -1;
        for (int i = 0; i < this.g.size(); i++) {
            Button port = this.g.get(i);
            boolean hover = MathUtil.a(mx, my, port.getX(), port.getY(), port.getWidth(), port.getHeight());
            if (hover) {
                this.r = i;
            }
            this.o[i] += ((hover ? 1.0f : 0.0f) - this.o[i]) * 0.18f;
        }
        this.q += (((this.r >= 0 ? 1.0f : 0.0f)) - this.q) * 0.12f;
        a(context, hubX, hubY, open, delta);
        for (int i = 0; i < this.g.size(); i++) {
            a(context, this.g.get(i), i, this.o[i], open, hubX, hubY);
        }
        b(context, open, mx);
        matrices.pop();
        EffectMarker.a(context.getMatrices(), delta, this.h);
        ScaleUtil.a(context);
    }

    private static float hubOuter(float open) {
        return 96.0f * EasingList.s.ease(open);
    }

    /** Левая граница блока: порты + кабель + хаб центрируются в окне как одно целое. */
    private float c(int width) {
        float block = PORT_WIDTH + CABLE_GAP + 96.0f;
        return Math.max(WORDMARK_SPACE, (width - block) * 0.5f);
    }

    private void b(int width, int height) {
        float total = (this.g.size() * PORT_HEIGHT) + ((this.g.size() - 1) * PORT_GAP);
        float x = c(width);
        float y = (height - total) * 0.5f;
        for (int i = 0; i < this.g.size(); i++) {
            this.g.get(i).setPosition(x, y + (i * (PORT_HEIGHT + PORT_GAP)));
        }
        this.k = x;
        this.l = Math.min(height - 26.0f, y + total + 34.0f);
    }

    // вертикальный логотип у левого края
    private void a(DrawContext context, int width, int height, float open) {
        MatrixStack matrices = context.getMatrices();
        float pivotX = c(width) - (WORDMARK_SPACE * 0.5f);
        float pivotY = height * 0.5f;
        matrices.push();
        matrices.translate(pivotX, pivotY, 0.0f);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(-90.0f)));
        matrices.translate(-pivotX, -pivotY, 0.0f);
        float size = 19.0f;
        float spacing = 3.0f;
        float textWidth = a(Fonts.e, "SOCKET", size, spacing);
        float startX = pivotX - (textWidth * 0.5f);
        a(matrices, Fonts.e, "SOCKET", startX, pivotY - (size * 0.5f), size, spacing,
                ColorUtil.convertToARGB(255, 255, 255, (int) (42.0f * open)));
        String sub = "CLIENT · 1.21.4";
        float subWidth = a(Fonts.c, sub, 6.0f, 1.8f);
        a(matrices, Fonts.c, sub, pivotX - (subWidth * 0.5f), pivotY + (size * 0.75f), 6.0f, 1.8f,
                ColorUtil.applyAlphaToColor(accent(), 0.55f * open));
        matrices.pop();
    }

    private static float a(Font font, String text, float size, float spacing) {
        float total = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            total += font.a(String.valueOf(text.charAt(i)), size) + spacing;
        }
        return total - spacing;
    }

    private static void a(MatrixStack matrices, Font font, String text, float x, float y, float size, float spacing, int color) {
        float cursor = x;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            font.a(matrices, ch, cursor, y, size, color);
            cursor += font.a(ch, size) + spacing;
        }
    }

    // строка-порт: номер, гнездо с иконкой, название, подпись и кабель к хабу
    private void a(DrawContext context, Button port, int index, float hover, float open, float hubX, float hubY) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        int primary = accent();
        float x = port.getX();
        float y = port.getY();
        float w = port.getWidth();
        float h = port.getHeight();
        float cy = y + (h * 0.5f);
        float slide = 9.0f * EasingList.s.ease(hover);
        float appear = MathHelper.clamp((open * 1.35f) - (index * 0.09f), 0.0f, 1.0f);
        if (appear <= 0.0f) {
            return;
        }
        if (hover > 0.01f) {
            a(context, x + w + 5.0f, cy, hubX, hubY, hover * appear);
        }
        draw.b(matrices, x, y, w, h, 9.0f, ColorUtil.convertToARGB(13, 15, 19, 150), appear);
        draw.a(matrices, x, y, w, h, 9.0f, ColorUtil.applyAlphaToColor(primary, 0.06f * hover * appear));
        draw.a(matrices, x, y, w, h, 9.0f, 0.5f, ColorUtil.convertToARGB(255, 255, 255, (int) ((8.0f + (24.0f * hover)) * appear)));
        float barHeight = 7.0f + (13.0f * EasingList.s.ease(hover));
        draw.a(matrices, x + 4.0f, cy - (barHeight * 0.5f), 2.0f, barHeight, 1.0f, ColorUtil.applyAlphaToColor(primary, (0.45f + (0.55f * hover)) * appear));
        float socketX = x + 12.0f + slide;
        float ring = 15.0f;
        draw.a(matrices, socketX, cy - (ring * 0.5f), ring, ring, ring * 0.5f, ColorUtil.applyAlphaToColor(primary, (0.1f + (0.22f * hover)) * appear));
        draw.a(matrices, socketX, cy - (ring * 0.5f), ring, ring, ring * 0.5f, 0.5f, ColorUtil.applyAlphaToColor(primary, (0.4f + (0.6f * hover)) * appear));
        String icon = this.m[index];
        Fonts.a.a(matrices, icon, socketX + ((ring - Fonts.a.b(icon, 8.0f)) * 0.5f), Fonts.a.a(icon, 8.0f, cy), 8.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.lerpColor(ColorUtil.convertToARGB(255, 255, 255, 255), primary, hover), appear));
        float textX = socketX + ring + 8.0f;
        Fonts.e.a(matrices, port.getLabel(), textX, cy - 7.5f, 8.5f,
                ColorUtil.convertToARGB(255, 255, 255, (int) ((185.0f + (70.0f * hover)) * appear)));
        Fonts.c.a(matrices, this.n[index], textX, cy + 1.5f, 6.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.lerpColor(ColorUtil.convertToARGB(120, 130, 145, 255), primary, hover), (0.55f + (0.45f * hover)) * appear));
        String num = "0" + (index + 1);
        Fonts.c.a(matrices, num, (x + w) - 11.0f - Fonts.c.a(num, 6.5f), Fonts.c.a(num, 6.5f, cy), 6.5f,
                ColorUtil.convertToARGB(255, 255, 255, (int) ((30.0f + (60.0f * hover)) * appear)));
    }

    // кабель от порта к хабу: точки по кривой Безье и бегущий по ним импульс
    private void a(DrawContext context, float fromX, float fromY, float toX, float toY, float alpha) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        int primary = accent();
        float controlX = (fromX + toX) * 0.5f;
        float controlY = fromY + ((toY - fromY) * 0.08f);
        int steps = 30;
        float pulse = time(1600.0f);
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float inv = 1.0f - t;
            float px = (inv * inv * fromX) + (2.0f * inv * t * controlX) + (t * t * toX);
            float py = (inv * inv * fromY) + (2.0f * inv * t * controlY) + (t * t * toY);
            float dist = Math.abs(t - pulse);
            float highlight = Math.max(0.0f, 1.0f - (dist * 7.0f));
            float size = 1.3f + (1.7f * highlight);
            draw.a(matrices, px - (size * 0.5f), py - (size * 0.5f), size, size, size * 0.5f,
                    ColorUtil.applyAlphaToColor(primary, (0.18f + (0.7f * highlight)) * alpha));
        }
    }

    // хаб справа: кольца, вращающаяся дуга и подпись выбранного раздела
    private void a(DrawContext context, float cx, float cy, float open, float delta) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        int primary = accent();
        float ease = EasingList.s.ease(open);
        float outer = hubOuter(open);
        float spin = time(9000.0f) * 360.0f;
        for (int ringIndex = 0; ringIndex < 3; ringIndex++) {
            float size = outer - (ringIndex * 22.0f);
            draw.a(matrices, cx - (size * 0.5f), cy - (size * 0.5f), size, size, size * 0.5f, 0.5f,
                    ColorUtil.convertToARGB(255, 255, 255, (int) ((10.0f - (ringIndex * 2.0f)) * open)));
        }
        int dots = 44;
        for (int i = 0; i < dots; i++) {
            float angle = (float) Math.toRadians((i * (360.0f / dots)) + spin);
            float radius = outer * 0.5f;
            float px = cx + (((float) Math.cos(angle)) * radius);
            float py = cy + (((float) Math.sin(angle)) * radius);
            float wave = (float) ((Math.sin((i * 0.55f) + (time(2600.0f) * Math.PI * 2.0f)) * 0.5f) + 0.5f);
            float size = 1.2f + (1.4f * wave);
            draw.a(matrices, px - (size * 0.5f), py - (size * 0.5f), size, size, size * 0.5f,
                    ColorUtil.applyAlphaToColor(primary, (0.15f + (0.55f * wave)) * open));
        }
        b(context, cx, cy, outer, open, delta);
    }

    // ядро хаба: диск, иконка выбранного порта и подпись под ним
    private void b(DrawContext context, float cx, float cy, float outer, float open, float delta) {
        MatrixStack matrices = context.getMatrices();
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        int primary = accent();
        if (this.r >= 0) {
            this.s = this.r;
        }
        float core = 52.0f;
        draw.b(matrices, cx - (core * 0.5f), cy - (core * 0.5f), core, core, core * 0.5f, ColorUtil.convertToARGB(13, 15, 19, 190), open);
        draw.a(matrices, cx - (core * 0.5f), cy - (core * 0.5f), core, core, core * 0.5f, 0.6f, ColorUtil.applyAlphaToColor(primary, (0.35f + (0.4f * this.q)) * open));
        float inner = 30.0f + (4.0f * this.q);
        draw.a(matrices, cx - (inner * 0.5f), cy - (inner * 0.5f), inner, inner, inner * 0.5f, ColorUtil.applyAlphaToColor(primary, (0.08f + (0.14f * this.q)) * open));
        String icon = this.s >= 0 ? this.m[this.s] : "y";
        float iconSize = 15.0f;
        if (this.s >= 0 && this.q > 0.02f) {
            float logo = (core - 14.0f) * (1.0f - (0.25f * this.q));
            draw.a(matrices, LOGO_MAIN, cx - (logo * 0.5f), cy - (logo * 0.5f), logo, logo, logo * 0.5f,
                    ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(255, 255, 255, 255), (1.0f - this.q) * open));
            Fonts.a.a(matrices, icon, cx - (Fonts.a.b(icon, iconSize) * 0.5f), Fonts.a.a(icon, iconSize, cy), iconSize,
                    ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(255, 255, 255, 255), this.q * open));
        } else {
            float logo = core - 14.0f;
            draw.a(matrices, LOGO_MAIN, cx - (logo * 0.5f), cy - (logo * 0.5f), logo, logo, logo * 0.5f,
                    ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(255, 255, 255, 255), open));
        }
        String title = this.s >= 0 && this.q > 0.02f ? this.g.get(this.s).getLabel().toUpperCase() : "SOCKET CLIENT";
        float titleAlpha = this.s >= 0 ? Math.max(this.q, 1.0f - this.q) : 1.0f;
        float titleWidth = a(Fonts.e, title, 9.0f, 1.6f);
        // подписи уводим ниже внешнего кольца, иначе они попадают под бегущие точки
        float captionY = cy + (outer * 0.5f) + 20.0f;
        a(matrices, Fonts.e, title, cx - (titleWidth * 0.5f), captionY, 9.0f, 1.6f,
                ColorUtil.convertToARGB(255, 255, 255, (int) (200.0f * open * titleAlpha)));
        String caption = this.s >= 0 && this.q > 0.02f ? this.n[this.s] : "максимум комфорта · build 1.0";
        Fonts.c.a(matrices, caption, cx - (Fonts.c.a(caption, 6.5f) * 0.5f), captionY + 13.0f, 6.5f,
                ColorUtil.convertToARGB(255, 255, 255, (int) (70.0f * open * titleAlpha)));
    }

    // ползунок выхода: тянуть до конца, чтобы закрыть игру
    private void b(DrawContext context, float open, int mouseX) {
        float target = this.j >= 0.0f ? MathHelper.clamp((((mouseX - this.j) - this.k) - 1.75f) / 98.5f, 0.0f, 1.0f) : 0.0f;
        this.i += (target - this.i) * 0.25f;
        Draw2DProcessor draw = Socket.getInstance().getModuleProcessor().i();
        MatrixStack matrices = context.getMatrices();
        float scale = 0.85f + (0.15f * EasingList.s.ease(open));
        matrices.push();
        matrices.translate(this.k + 59.0f, this.l + 9.75f, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate((-this.k) - 59.0f, (-this.l) - 9.75f, 0.0f);
        float knobX = this.k + 1.75f + (this.i * 98.5f);
        float knobY = this.l + 1.75f;
        float centerX = knobX + 8.0f;
        float centerY = knobY + 8.0f;
        draw.b(matrices, this.k, this.l, 118.0f, 19.5f, 8.0f, ColorUtil.convertToARGB(11, 11, 13, 120), open);
        draw.a(matrices, this.k, this.l, 118.0f, 19.5f, 8.0f, 0.5f, ColorUtil.convertToARGB(255, 255, 255, (int) (15.0f * open)));
        Fonts.c.a(matrices, "потяни, чтобы выйти", this.k + 22.0f, Fonts.c.a("потяни, чтобы выйти", 6.0f, this.l + 9.75f), 6.0f,
                ColorUtil.convertToARGB(255, 255, 255, (int) (70.0f * (1.0f - this.i) * open)));
        Fonts.e.c(matrices, "Выйти из игры", this.k + 9.0f, this.l + 5.75f, 7.0f,
                ColorUtil.applyAlphaToColor(ColorUtil.convertToARGB(220, 80, 80, 255), this.i * open), ((knobX - 3.0f) - this.k) - 9.0f);
        int knob = ColorUtil.lerpColor(ColorUtil.convertToARGB(255, 255, 255, 13), ColorUtil.convertToARGB(220, 80, 80, 40), this.i);
        draw.a(matrices, knobX, knobY, 16.0f, 16.0f, 7.0f, ColorUtil.applyAlphaToColor(knob, (ColorUtil.b(knob)[3] / 255.0f) * open));
        matrices.push();
        matrices.translate(centerX, centerY, 0.0f);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians((-90.0f) + (180.0f * this.i))));
        matrices.translate(-centerX, -centerY, 0.0f);
        Fonts.a.a(matrices, "c", (centerX - (Fonts.a.a("c", 8.5f) / 2.0f)) + 1.0f, centerY - 4.5f, 8.5f,
                ColorUtil.applyAlphaToColor(ColorUtil.lerpColor(-1, ColorUtil.convertToARGB(220, 80, 80, 255), this.i), open));
        matrices.pop();
        matrices.pop();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = MathUtil.scale(mouseX, 2);
        double my = MathUtil.scale(mouseY, 2);
        EffectMarker.a(this.h, (float) mx, (float) my);
        float knobX = this.k + 1.75f + (this.i * 98.5f);
        if (MathUtil.a(mx, my, knobX, this.l + 1.75f, 16.0f, 16.0f)) {
            this.j = ((float) mx) - knobX;
            return true;
        }
        for (Button port : this.g) {
            if (port.getAction() != null && MathUtil.a(mx, my, port.getX(), port.getY(), port.getWidth(), port.getHeight())) {
                port.getAction().run();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.j >= 0.0f) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.j < 0.0f) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        double mx = MathUtil.scale(mouseX, 2);
        float progress = MathHelper.clamp((((((float) mx) - this.j) - this.k) - 1.75f) / 98.5f, 0.0f, 1.0f);
        this.j = -1.0f;
        if (progress < 0.95f) {
            return true;
        }
        Interface.mc.scheduleStop();
        return true;
    }

    public void close() {
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
