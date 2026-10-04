package socket.event;

import socket.core.Event;


public class GammaEvent extends Event {
    private double gamma;

    public GammaEvent(double gamma) {
        this.gamma = gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double b() {
        return this.gamma;
    }
}
