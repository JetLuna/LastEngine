package net.jetluna.auth;

import org.bukkit.plugin.java.JavaPlugin;

public class AuthPlugin extends JavaPlugin {

    private AuthManager authManager;

    @Override
    public void onEnable() {
        // 1. Создаем менеджера
        this.authManager = new AuthManager();

        // 2. Регистрируем события (защита от движения)
        getServer().getPluginManager().registerEvents(new AuthListener(authManager), this);

        // 3. Регистрируем команды /reg и /l
        // ВАЖНО: Убедись, что AuthCommand импортирован или находится в том же пакете
        AuthCommand cmd = new AuthCommand(this, authManager);

        if (getCommand("reg") != null) {
            getCommand("reg").setExecutor(cmd);
        }
        if (getCommand("l") != null) {
            getCommand("l").setExecutor(cmd);
        }

        getLogger().info("LastAuth успешно запущен!");
    }
}