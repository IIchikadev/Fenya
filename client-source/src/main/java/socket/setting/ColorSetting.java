package socket.setting;

import socket.ui.element.ColorElement;
import socket.ui.element.Element;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public Element<?> createBooleanElement() {
        return new ColorElement(this);
    }
}
