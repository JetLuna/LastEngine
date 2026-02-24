package net.jetluna.api;

import net.jetluna.api.database.DatabaseManager;
import net.jetluna.api.rank.RankCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {

    private static LastApi instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("LastApi (Core Library) запускается...");

        // 1. Загружаем языки
        net.jetluna.api.lang.LanguageManager.load();

        // 2. Регистрируем команды
        getCommand("lang").setExecutor(new net.jetluna.api.lang.LangCommand());

        // 1. Создаем папку плагина
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        // 2. Подключение к базе данных (MySQL)
        // Если у тебя пока нет MySQL, можешь закомментировать этот блок try-catch
        try {
            this.databaseManager = new DatabaseManager("localhost", "3306", "lastengine", "root", "");
            getLogger().info("База данных успешно подключена!");
        } catch (Exception e) {
            getLogger().warning("Не удалось подключиться к БД (MySQL). Если это тест - игнорируй.");
        }

        // 3. Регистрируем команду выдачи рангов
        // (Проверка на null не обязательна, если команда есть в plugin.yml, но для безопасности оставим)
        if (getCommand("setrank") != null) {
            getCommand("setrank").setExecutor(new RankCommand());
        }

        getLogger().info("LastApi успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Закрываем соединение при выключении
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    // --- Геттеры для других плагинов ---

    public static LastApi getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}