package socket.config;

import socket.core.EventManager;

import socket.core.Interface;

public abstract class BaseProcessor implements Interface {
    public BaseProcessor() {
        EventManager.a(this);
    }

    public abstract void setup();

    public abstract void unSetup();
}
