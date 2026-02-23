package net.jetluna.api;

import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {

    private static LastApi instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("LastApi (Core Library) loaded successfully!");

        // В будущем тут будет:
        // DatabaseManager.connect();
        // RedisManager.connect();
    }

    @Override
    public void onDisable() {
        // DatabaseManager.disconnect();
    }

    public static LastApi getInstance() {
        return instance;
    }
}