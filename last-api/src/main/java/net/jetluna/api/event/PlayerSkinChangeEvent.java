package net.jetluna.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSkinChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String value;
    private final String signature;

    public PlayerSkinChangeEvent(Player player, String value, String signature) {
        this.player = player;
        this.value = value;
        this.signature = signature;
    }

    public Player getPlayer() { return player; }
    public String getValue() { return value; }
    public String getSignature() { return signature; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}