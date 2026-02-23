package net.jetluna.bedwars.state;

import org.bukkit.event.Listener;
import net.jetluna.bedwars.BedWarsPlugin;

public abstract class GameState implements Listener {

    protected final BedWarsPlugin plugin;

    public GameState(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    // Включение фазы (например, запускаем таймер)
    public abstract void onEnable();

    // Выключение фазы (например, удаляем таймер)
    public abstract void onDisable();

    // Имя фазы для отладки
    public abstract String getName();
}