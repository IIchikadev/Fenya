package socket.core;

import socket.render.BatchProcessor;
import socket.config.BaseProcessor;
import socket.config.ModuleProcessor;
import socket.config.ResourcePacksProcessor;
import socket.config.ThemeProcessor;
import socket.cosmetic.CosmeticsProcessor;
import socket.discord.DiscordProcessor;
import socket.friend.FriendProcessor;
import socket.handler.HandlerProcessor;
import socket.lib.log4j.LoggerFactory;
import socket.network.AccountProcessor;
import socket.notification.NotificationProcessor;
import socket.render.Draw2DProcessor;
import socket.render.Draw3DProcessor;
import socket.ui.element.DragProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Processor implements Interface {

    static {
        LoggerFactory.a(Processor.class);
    }

    private final List<BaseProcessor> processors;
    private final FriendProcessor friendProcessor;
    private final DiscordProcessor discordProcessor;
    private final AccountProcessor accountProcessor;
    private final Draw2DProcessor draw2DProcessor;
    private final Draw3DProcessor draw3DProcessor;
    private final BatchProcessor batchProcessor;
    private final NotificationProcessor notificationProcessor;
    private final ResourcePacksProcessor resourcePacksProcessor;
    private final ThemeProcessor themeProcessor;
    private final CosmeticsProcessor cosmeticsProcessor;
    private final DragProcessor dragProcessor;
    private final ModuleProcessor moduleProcessor;
    private final HandlerProcessor handlerProcessor;

    public Processor() {
        Socket.getInstance().a(this);
        this.processors = new ArrayList<>();
        this.friendProcessor = new FriendProcessor();
        this.discordProcessor = new DiscordProcessor();
        this.accountProcessor = new AccountProcessor();
        this.draw2DProcessor = new Draw2DProcessor();
        this.draw3DProcessor = new Draw3DProcessor();
        this.batchProcessor = new BatchProcessor();
        this.notificationProcessor = new NotificationProcessor();
        this.resourcePacksProcessor = new ResourcePacksProcessor();
        this.themeProcessor = new ThemeProcessor();
        this.cosmeticsProcessor = new CosmeticsProcessor();
        this.dragProcessor = new DragProcessor();
        this.moduleProcessor = new ModuleProcessor();
        this.handlerProcessor = new HandlerProcessor();
    }

    public void a() {
        Collections.addAll(this.processors, this.cosmeticsProcessor, this.friendProcessor, this.notificationProcessor, this.resourcePacksProcessor, this.themeProcessor, this.accountProcessor,
                this.moduleProcessor, this.discordProcessor, this.draw2DProcessor, this.dragProcessor, this.draw3DProcessor, this.batchProcessor, this.handlerProcessor);
        this.processors.forEach(new Consumer<BaseProcessor>() {
            @Override
            public void accept(BaseProcessor obj) {
                obj.setup();
            }
        });
        System.out.println("setup - ".concat(String.valueOf(this.processors.stream().map(new Function<BaseProcessor, String>() {
            @Override
            public String apply(BaseProcessor obj) {
                return obj.getClass().getSimpleName();
            }
        }).toList())));
    }

    public List<BaseProcessor> c() {
        return this.processors;
    }

    public FriendProcessor friends() {
        return this.friendProcessor;
    }

    public DiscordProcessor discord() {
        return this.discordProcessor;
    }

    public AccountProcessor accounts() {
        return this.accountProcessor;
    }

    public Draw2DProcessor draw2D() {
        return this.draw2DProcessor;
    }

    public Draw3DProcessor draw3D() {
        return this.draw3DProcessor;
    }

    public BatchProcessor batch() {
        return this.batchProcessor;
    }

    public NotificationProcessor notifications() {
        return this.notificationProcessor;
    }

    public ResourcePacksProcessor resourcePacks() {
        return this.resourcePacksProcessor;
    }

    public ThemeProcessor themes() {
        return this.themeProcessor;
    }

    public CosmeticsProcessor cosmetics() {
        return this.cosmeticsProcessor;
    }

    public DragProcessor drag() {
        return this.dragProcessor;
    }

    public ModuleProcessor modules() {
        return this.moduleProcessor;
    }

    public HandlerProcessor handlers() {
        return this.handlerProcessor;
    }

    public void b() {
        this.processors.forEach(processor -> {
            try {
                processor.unSetup();
            } catch (Throwable th) {
            }
        });
        System.out.println("unSetup - " + this.processors.stream().map(processor2 -> {
            return processor2.getClass().getSimpleName();
        }).toList());
    }
}
