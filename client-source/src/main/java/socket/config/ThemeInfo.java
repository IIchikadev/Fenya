package socket.config;

import socket.core.InterfaceC0020Opcode;

public enum ThemeInfo {
    PRIMARY(new ThemeConstructor("primary", 108, 184, 255, 255), new ThemeConstructor("primary", 108, 184, 255, 255)),
    BACKGROUND_HUD(new ThemeConstructor("background_hud", 23, 26, 31, InterfaceC0020Opcode.cY), new ThemeConstructor("background_hud", 229, 233, 239, InterfaceC0020Opcode.cY)),
    BACKGROUND_GUI(new ThemeConstructor("background_gui", 20, 23, 28, 255), new ThemeConstructor("background_gui", 229, 233, 239, 255)),
    OUTLINE_SMALL(new ThemeConstructor("outline_small", 157, 166, 178, 22), new ThemeConstructor("outline_small", 157, 166, 178, 22)),
    OUTLINE_MEDIUM(new ThemeConstructor("outline_medium", 157, 166, 178, 46), new ThemeConstructor("outline_medium", 157, 166, 178, 46)),
    TEXT(new ThemeConstructor("typography_text", 238, 242, 247, 255), new ThemeConstructor("typography_text", 30, 35, 42, 255)),
    TEXT_DISABLED(new ThemeConstructor("typography_disabled", 157, 166, 178, 255), new ThemeConstructor("typography_disabled", 91, 101, 114, 255));

    private final ThemeConstructor dark;
    private final ThemeConstructor light;

    ThemeInfo(ThemeConstructor dark, ThemeConstructor light) {
        this.dark = dark;
        this.light = light;
    }

    public ThemeConstructor a(ThemeType theme) {
        return theme == ThemeType.LIGHT ? this.light : this.dark;
    }

    public ThemeConstructor a() {
        return this.light;
    }
}
