package net.jetluna.api.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.jetluna.api.LastApi;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(String host, String port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();

        // Настройки подключения
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        config.setUsername(user);
        config.setPassword(password);

        // Оптимизация (чтобы сервер не лагал)
        config.setMaximumPoolSize(10); // Максимум 10 соединений одновременно
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000); // Ждать подключения 5 сек
        config.setLeakDetectionThreshold(3000); // Искать утечки памяти

        this.dataSource = new HikariDataSource(config);

        LastApi.getInstance().getLogger().info("База данных подключена!");
        createTables();
    }

    private void createTables() {
        try (Connection conn = getConnection(); java.sql.Statement stmt = conn.createStatement()) {

            // Твоя изначальная таблица пользователей (оставляем, пригодится для авторизации)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "ip VARCHAR(45) NOT NULL, " +
                    "password VARCHAR(255), " +
                    "reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 1. Таблица статистики (Экономика + Уровни)
            stmt.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16), " +
                    "emeralds INT DEFAULT 0, " +
                    "coins INT DEFAULT 0, " +
                    "level INT DEFAULT 1, " + // НОВАЯ КОЛОНКА
                    "exp INT DEFAULT 0" +     // НОВАЯ КОЛОНКА
                    ");");

            // 2. Таблица Лучшего игрока (Царь горы)
            stmt.execute("CREATE TABLE IF NOT EXISTS best_player (" +
                    "id INT PRIMARY KEY DEFAULT 1, " + // Делаем id=1, так как царь всегда один
                    "uuid VARCHAR(36), " +
                    "username VARCHAR(16), " +
                    "formatted_name VARCHAR(64), " +
                    "price INT DEFAULT 0, " +
                    "expire_time BIGINT DEFAULT 0, " +
                    "texture_value TEXT, " +
                    "texture_signature TEXT" +
                    ");");

        } catch (SQLException e) {
            LastApi.getInstance().getLogger().severe("Ошибка при создании таблиц MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}