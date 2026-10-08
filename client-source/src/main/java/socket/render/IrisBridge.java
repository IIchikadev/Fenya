package socket.render;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/** Optional integration: Socket also runs without Iris installed. */
public final class IrisBridge {
    private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("iris");
    private static Object api;
    private static java.lang.reflect.Method active;

    private IrisBridge() {}

    public static boolean active() {
        if (!AVAILABLE) return false;
        try {
            if (api == null) {
                Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = type.getMethod("getInstance").invoke(null);
                active = type.getMethod("isShaderPackInUse");
            }
            return (boolean) active.invoke(api);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
    /** Iris 1.8.8 owns the real scene color and pre-hand depth outside the main framebuffer. */
    public static int[] handTextures() {
        try {
            Object manager=Class.forName("net.irisshaders.iris.Iris").getMethod("getPipelineManager").invoke(null);
            Object pipeline=manager.getClass().getMethod("getPipelineNullable").invoke(manager);
            if(pipeline==null) return null;
            var field=pipeline.getClass().getDeclaredField("renderTargets"); field.setAccessible(true);
            Object targets=field.get(pipeline);
            Object config=Class.forName("net.irisshaders.iris.Iris").getMethod("getIrisConfig").invoke(null);
            String pack=String.valueOf(config.getClass().getMethod("getShaderPackName").invoke(config));
            int index=pack.toLowerCase(java.util.Locale.ROOT).contains("makeup") ? 1 : 0;
            Object color=targets.getClass().getMethod("get",int.class).invoke(targets,index);
            var flipped=(java.util.Set<?>)pipeline.getClass().getMethod("getFlippedAfterTranslucent").invoke(pipeline);
            int colorId=(int)color.getClass().getMethod(flipped.contains(index)?"getAltTexture":"getMainTexture").invoke(color);
            Object noHand=targets.getClass().getMethod("getDepthTextureNoHand").invoke(targets);
            int beforeDepth=(int)noHand.getClass().getMethod("getTextureId").invoke(noHand);
            int afterDepth=(int)targets.getClass().getMethod("getDepthTexture").invoke(targets);
            return new int[]{colorId,beforeDepth,afterDepth};
        } catch(ReflectiveOperationException | RuntimeException e) { return null; }
    }

    public static void setEnabled(boolean enabled) throws ReflectiveOperationException {
        if (!AVAILABLE) throw new IllegalStateException("Установите Iris и Sodium через Socket Loader");
        Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
        Object config = iris.getMethod("getIrisConfig").invoke(null);
        Class<?> type = config.getClass();
        if (enabled) type.getMethod("setShaderPackName", String.class).invoke(config, "MakeUp-UltraFast-9.5g.zip");
        type.getMethod("setShadersEnabled", boolean.class).invoke(config, enabled);
        type.getMethod("save").invoke(config);
        // Initial configuration loads before the renderer is ready; Iris reads it itself.
        if (MinecraftClient.getInstance().world != null) iris.getMethod("reload").invoke(null);
    }

    public static boolean configuredEnabled() throws ReflectiveOperationException {
        if (!AVAILABLE) return false;
        Object config = Class.forName("net.irisshaders.iris.Iris").getMethod("getIrisConfig").invoke(null);
        return (boolean) config.getClass().getMethod("areShadersEnabled").invoke(config);
    }
    /** Session-only optimization: preserve the selected shader pack and config file. */
    public static void setTemporaryEnabled(boolean enabled) throws ReflectiveOperationException {
        if (!AVAILABLE) return;
        Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
        Object config = iris.getMethod("getIrisConfig").invoke(null);
        config.getClass().getMethod("setShadersEnabled", boolean.class).invoke(config, enabled);
        if (MinecraftClient.getInstance().world != null) {
            // Iris.reload reloads its file before rebuilding pipelines. Supply the
            // temporary state, then put the user's exact file back immediately.
            java.nio.file.Path file = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
            byte[] previous;
            try { previous = java.nio.file.Files.exists(file) ? java.nio.file.Files.readAllBytes(file) : null; }
            catch (java.io.IOException e) { throw new ReflectiveOperationException(e); }
            try {
                config.getClass().getMethod("save").invoke(config);
                iris.getMethod("reload").invoke(null);
            } finally {
                try {
                    if (previous == null) java.nio.file.Files.deleteIfExists(file);
                    else java.nio.file.Files.write(file, previous);
                } catch (java.io.IOException e) { throw new ReflectiveOperationException(e); }
            }
        }
    }

    public static void openSettings() throws ReflectiveOperationException {
        if (!AVAILABLE) throw new IllegalStateException("Iris не установлен");
        Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        Object instance = type.getMethod("getInstance").invoke(null);
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen((Screen) type.getMethod("openMainIrisScreenObj", Object.class).invoke(instance, mc.currentScreen));
    }
}
