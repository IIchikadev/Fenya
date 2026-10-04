package socket.event;

import socket.core.Event;


public class WillLandEvent extends Event {
    private final boolean willLand;

    public WillLandEvent(boolean willLand) {
        this.willLand = willLand;
    }

    public boolean b() {
        return this.willLand;
    }
}
