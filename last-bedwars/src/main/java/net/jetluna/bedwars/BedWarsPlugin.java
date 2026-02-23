package net.jetluna.bedwars;

import net.jetluna.bedwars.manager.GameManager;
import net.jetluna.bedwars.state.WaitingState;
import org.bukkit.plugin.java.JavaPlugin;

public class BedWarsPlugin extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        // 1. Создаем менеджера
        this.gameManager = new GameManager(this);

        // 2. Включаем режим Ожидания
        this.gameManager.setGameState(new WaitingState(this));

        getLogger().info("LastBedWars успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Логика выключения
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}