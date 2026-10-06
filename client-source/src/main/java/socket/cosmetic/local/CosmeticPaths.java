package socket.cosmetic.local;
import net.fabricmc.loader.api.FabricLoader;
final class CosmeticPaths {
 static String game() { return FabricLoader.getInstance().getGameDir().toString(); }
 static String folder() { return FabricLoader.getInstance().getGameDir().resolve("cosmetics").toString(); }
}
