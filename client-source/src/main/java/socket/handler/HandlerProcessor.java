package socket.handler;

import socket.config.BaseProcessor;
import socket.lib.log4j.LoggerFactory;
import socket.network.DistributionHandler;

public class HandlerProcessor extends BaseProcessor {

    static {
        LoggerFactory.a(HandlerProcessor.class);
    }

    private final InventoryHandler c = new InventoryHandler();
    private final StopHandler e = new StopHandler();
    private final MainHandler j = new MainHandler();
    private final TPSHandler l = new TPSHandler();
    private final InteractHandler m = new InteractHandler();
    private final DistributionHandler o = new DistributionHandler();

    @Override

    public void setup() {
    }

    public InventoryHandler getInventoryHandler() {
        return this.c;
    }

    public StopHandler getStopHandler() {
        return this.e;
    }

    public MainHandler getMainHandler() {
        return this.j;
    }

    public TPSHandler getTPSHandler() {
        return this.l;
    }

    public InteractHandler getInteractHandler() {
        return this.m;
    }

    public void performNoOperation() {
    }

    public DistributionHandler getDistributionHandler() {
        return this.o;
    }

    @Override
    public void unSetup() {
    }
}
