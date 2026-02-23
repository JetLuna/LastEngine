package net.jetluna.bedwars.manager;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class GameManager {

    private final BedWarsPlugin plugin;
    private GameState currentState;

    public GameManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void setGameState(GameState newState) {
        if (currentState != null) {
            currentState.onDisable();
            // Отключаем слушатели событий старой фазы
            HandlerList.unregisterAll(currentState);
        }

        this.currentState = newState;

        if (newState != null) {
            newState.onEnable();
            // Включаем слушатели новой фазы
            Bukkit.getPluginManager().registerEvents(newState, plugin);
        }
    }

    public void checkStart() {
        // Заглушка: тут будет логика запуска таймера
        if (Bukkit.getOnlinePlayers().size() >= 2) {
            plugin.getLogger().info("Игроков достаточно! Скоро начнется отсчет.");
        }
    }
}