package net.jetluna.api;

import net.jetluna.api.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {

    private static LastApi instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("LastApi (Core Library) запускается...");

        // Подключение к базе данных
        // ВАЖНО: Убедись, что у тебя запущен MySQL (XAMPP/OpenServer)
        // И создана база данных с именем 'lastengine'
        try {
            // Параметры: хост, порт, имя_базы, юзер, пароль
            // Если у тебя XAMPP, пароль обычно пустой ("")
            this.databaseManager = new DatabaseManager("localhost", "3306", "lastengine", "root", "");
            getLogger().info("База данных успешно подключена!");
        } catch (Exception e) {
            getLogger().severe("КРИТИЧЕСКАЯ ОШИБКА: Не удалось подключиться к БД!");
            getLogger().severe("Проверь, запущен ли MySQL и создана ли база 'lastengine'.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Закрываем соединение при выключении
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    // Этот метод нужен, чтобы другие плагины получали доступ к API
    public static LastApi getInstance() {
        return instance;
    }

    // А ЭТОТ МЕТОД нужен, чтобы AuthCommand мог работать с базой
    // (Именно его не хватало, из-за чего горело красным)
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}