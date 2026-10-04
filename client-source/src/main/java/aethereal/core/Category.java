package aethereal.core;


public enum Category {
    Render("t", "Визуал"),
    Animations("A", "Камера"),
    Hud("f", "Интерфейс"),
    Misc("z", "Разное");

    private final String icon;
    private final String title;

    Category(String icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public String a() {
        return this.icon;
    }

    public String b() {
        return this.title;
    }
}
