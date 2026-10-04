package socket.discord;

import socket.config.BaseProcessor;
import socket.core.DiscordUser;
import socket.lib.log4j.LogManager;
import socket.lib.log4j.Logger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public class DiscordProcessor extends BaseProcessor {
    private static final Logger LOGGER = LogManager.b(DiscordProcessor.class);

    private static final String DETAILS = "Socket Client";
    private static final String STATE = "No cheat, only visual";
    private static final String LOGO_ASSET = "socket-client";
    private static final String SITE_URL = "https://socketclient.ru/";

    private DiscordIPC ipc;

    @Override
    public void setup() {
        long clientId = readClientId();
        if (clientId <= 0L) {
            LOGGER.g("Discord RPC disabled: set custom value 'socket:discordClientId' in fabric.mod.json");
            return;
        }
        this.ipc = DiscordIPC.a(clientId);
        this.ipc.a(new DiscordEventListener() {
            @Override
            public void a(DiscordUser user) {
                pushActivity();
            }

            @Override
            public void b(int errorCode, String message) {
                LOGGER.g("Discord RPC disconnected: {} ({})", message, errorCode);
            }
        });
        try {
            this.ipc.b();
            LOGGER.a("Discord RPC connecting (client id {})", clientId);
        } catch (Exception e) {
            LOGGER.g("Discord RPC connect failed", e);
        }
    }

    @Override
    public void unSetup() {
        if (this.ipc != null) {
            try {
                this.ipc.close();
            } catch (Exception e) {
                LOGGER.g("Discord RPC close failed", e);
            }
            this.ipc = null;
        }
    }

    public DiscordIPC a() {
        return this.ipc;
    }

    public void pushActivity() {
        DiscordIPC ipc = this.ipc;
        if (ipc == null) {
            return;
        }
        try {
            ipc.b(new Activity.a()
                    .type(ActivityType.PLAYING)
                    .b(DETAILS)
                    .state(STATE)
                    .largeImage(LOGO_ASSET, "Socket Client")
                    .startAt(System.currentTimeMillis() / 1000L)
                    .c("Сайт", SITE_URL)
                    .c("Discord", SITE_URL)
                    .build())
                .exceptionally(ex -> {
                    LOGGER.g("Discord RPC activity failed", ex);
                    return null;
                });
        } catch (Exception e) {
            LOGGER.g("Discord RPC activity failed", e);
        }
    }

    private long readClientId() {
        try {
            Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("socket");
            if (container.isEmpty()) {
                return -1L;
            }
            return Optional.ofNullable(container.get().getMetadata().getCustomValue("socket:discordClientId"))
                    .map(value -> {
                        try {
                            return Long.parseLong(value.getAsString());
                        } catch (NumberFormatException e) {
                            return -1L;
                        }
                    })
                    .orElse(-1L);
        } catch (Throwable t) {
            LOGGER.g("Discord RPC client id lookup failed", t);
            return -1L;
        }
    }
}
