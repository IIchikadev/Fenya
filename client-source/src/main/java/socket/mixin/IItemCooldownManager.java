package socket.mixin;


import socket.render.AnimationUtil;

public interface IItemCooldownManager {
    default AnimationUtil getAnimation() {
        return null;
    }

    default void setHealCooldown(int duration) {
    }
}
