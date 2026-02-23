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
        // Тут мы создадим таблицы, если их нет
        try (Connection conn = getConnection()) {
            // Таблица игроков (UUID, Имя, IP, Пароль, Дата регистрации)
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "ip VARCHAR(45) NOT NULL, " +
                    "password VARCHAR(255), " + // Хеш пароля
                    "reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
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