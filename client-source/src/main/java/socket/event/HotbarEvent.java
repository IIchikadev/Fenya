package socket.event;

import socket.core.Event;


public class HotbarEvent extends Event {
    private final int slot;

    public HotbarEvent(int slot) {
        this.slot = slot;
    }

    public int b() {
        return this.slot;
    }
}
