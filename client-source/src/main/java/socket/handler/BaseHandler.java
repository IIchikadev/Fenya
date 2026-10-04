package socket.handler;

import socket.core.EventManager;

public class BaseHandler {
    public BaseHandler() {
        EventManager.a(this);
    }
}
