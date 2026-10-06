package socket.module.render;

import socket.core.Category;
import socket.core.Module;
import socket.core.ModuleRegister;
import socket.render.IrisBridge;
import socket.setting.ButtonSetting;
import socket.util.ChatUtil;

@ModuleRegister(name = "Shaders", description = "MakeUp Ultra Fast: освещение, тени, вода; настройки через Iris", category = Category.Render)
public class WorldShaders extends Module {
    public WorldShaders() {
        a(new ButtonSetting("Настройки шейдера", () -> mc.execute(() -> {
            try { IrisBridge.openSettings(); }
            catch (Exception e) { ChatUtil.sendMessage("Не удалось открыть настройки Iris: " + e.getMessage()); }
        })));
    }

    @Override public void b() { super.b(); update(true); }
    @Override public void c() { super.c(); update(false); }

    private void update(boolean enabled) {
        mc.execute(() -> {
            try { IrisBridge.setEnabled(enabled); }
            catch (Exception e) { ChatUtil.sendMessage("Ошибка шейдера: " + e.getMessage()); }
        });
    }
}
