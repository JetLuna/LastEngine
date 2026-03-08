package net.jetluna.api.lang;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLanguageChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String newLanguage;

    public PlayerLanguageChangeEvent(Player player, String newLanguage) {
        this.player = player;
        this.newLanguage = newLanguage;
    }

    public Player getPlayer() { return player; }
    public String getNewLanguage() { return newLanguage; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}