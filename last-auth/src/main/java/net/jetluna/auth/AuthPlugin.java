package net.jetluna.auth;

import net.jetluna.auth.manager.CodeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthPlugin extends JavaPlugin {

    private AuthManager authManager;

    @Override
    public void onEnable() {
        this.authManager = new AuthManager();

        getServer().getPluginManager().registerEvents(new AuthListener(this, authManager), this);

        AuthCommand cmd = new AuthCommand(this, authManager);

        getCommand("reg").setExecutor(cmd);
        getCommand("l").setExecutor(cmd);
        getCommand("code").setExecutor(cmd);

        // --- РЕГИСТРАЦИЯ HEAL ---
        if (getCommand("heal") != null) {
            getCommand("heal").setExecutor(cmd);
        }
        // ------------------------

        if (getCommand("setauthspawn") != null) {
            getCommand("setauthspawn").setExecutor(cmd);
        } else {
            getLogger().warning("Команда setauthspawn не найдена в plugin.yml!");
        }

        getLogger().info("LastAuth успешно запущен!");
    }
}