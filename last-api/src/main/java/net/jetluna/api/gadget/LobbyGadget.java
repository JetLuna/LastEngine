package net.jetluna.api.gadget;

import org.bukkit.entity.Player;

public abstract class LobbyGadget {
    protected final Player player;

    public LobbyGadget(Player player) {
        this.player = player;
    }

    // Вызывается один раз при клике
    public abstract void onUse();

    // Вызывается каждый тик. Верни true, если гаджет закончил работу.
    public abstract boolean onTick(int tick);

    // Вызывается для очистки мусора
    public abstract void onClear();
}