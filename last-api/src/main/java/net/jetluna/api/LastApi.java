package net.jetluna.api;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {
    private static LastApi instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        StatsManager.init(this);
        LanguageManager.init(this); // Был load(), стало init()
    }

    public static LastApi getInstance() { return instance; }
}