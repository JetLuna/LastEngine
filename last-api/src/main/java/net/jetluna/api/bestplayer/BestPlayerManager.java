package net.jetluna.api.bestplayer;

import net.jetluna.api.LastApi;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BestPlayerManager {

    private static UUID currentBest = null;
    private static String currentName = "Никто";
    private static String currentFormattedName = "&7Никто";
    private static int currentPrice = 100;
    private static long expireTime = 0;

    private static String textureValue = "";
    private static String textureSignature = "";

    public static void init(LastApi plugin) {
        // Загружаем актуальные данные из БД при запуске сервера
        loadFromDatabase();

        // МАГИЯ СИНХРОНИЗАЦИИ: Запускаем таймер, который каждые 5 секунд (100 тиков)
        // будет фоном проверять базу данных. Если кто-то купит статус на Lobby-2,
        // Lobby-1 об этом моментально узнает и обновит свой кэш для NPC.
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, BestPlayerManager::loadFromDatabase, 100L, 100L);
    }

    private static void loadFromDatabase() {
        try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM best_player WHERE id = 1")) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String uuidStr = rs.getString("uuid");
                if (uuidStr != null && !uuidStr.isEmpty()) {
                    currentBest = UUID.fromString(uuidStr);
                }
                currentName = rs.getString("username");
                currentFormattedName = rs.getString("formatted_name");
                currentPrice = rs.getInt("price");
                expireTime = rs.getLong("expire_time");
                textureValue = rs.getString("texture_value");
                textureSignature = rs.getString("texture_signature");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    public static int getNextPrice() {
        if (isExpired()) return 100;
        return (int) (currentPrice * 1.25);
    }

    public static void setBestPlayer(Player player, int pricePaid, String val, String sig) {
        Rank rank = RankManager.getRank(player);

        // Обновляем локальные переменные, чтобы меню и чат сразу видели изменения
        currentBest = player.getUniqueId();
        currentName = player.getName();
        currentFormattedName = NameFormatUtil.getFormattedName(player, rank);
        currentPrice = pricePaid;
        expireTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);

        textureValue = val == null ? "" : val;
        textureSignature = sig == null ? "" : sig;

        // Асинхронно отправляем новые данные в MySQL
        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO best_player (id, uuid, username, formatted_name, price, expire_time, texture_value, texture_signature) " +
                                 "VALUES (1, ?, ?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE " +
                                 "uuid = VALUES(uuid), username = VALUES(username), formatted_name = VALUES(formatted_name), " +
                                 "price = VALUES(price), expire_time = VALUES(expire_time), " +
                                 "texture_value = VALUES(texture_value), texture_signature = VALUES(texture_signature)"
                 )) {

                ps.setString(1, currentBest.toString());
                ps.setString(2, currentName);
                ps.setString(3, currentFormattedName);
                ps.setInt(4, currentPrice);
                ps.setLong(5, expireTime);
                ps.setString(6, textureValue);
                ps.setString(7, textureSignature);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void updateTexture(String value, String signature) {
        textureValue = value;
        textureSignature = signature;

        // Отправляем обновленный скин в базу
        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE best_player SET texture_value = ?, texture_signature = ? WHERE id = 1")) {

                ps.setString(1, value);
                ps.setString(2, signature);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static UUID getCurrentBest() { return currentBest; }
    public static String getCurrentName() { return currentName; }
    public static String getCurrentFormattedName() { return currentFormattedName; }
    public static long getExpireTime() { return expireTime; }
    public static String getTextureValue() { return textureValue; }
    public static String getTextureSignature() { return textureSignature; }
}