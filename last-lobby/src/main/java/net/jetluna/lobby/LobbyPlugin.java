package net.jetluna.lobby;

import org.bukkit.plugin.java.JavaPlugin;

public class LobbyPlugin extends JavaPlugin {

    private static LobbyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Регистрируем команды
        getCommand("setlobbyspawn").setExecutor(new LobbyCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this));

        // Регистрируем события
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);

        // --- ЗАПУСКАЕМ ОБНОВЛЕНИЕ ТАБА И СКОРБОРДА ---
        // 20 тиков = 1 секунда. Запускаем сразу (0) и повторяем каждые 20 тиков.
        new LobbyTask().runTaskTimer(this, 0L, 20L);
        // ---------------------------------------------

        getLogger().info("LastLobby успешно запущен!");
    }
}