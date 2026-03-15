package net.jetluna.api.skin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jetluna.api.LastApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SkinManager {

    private static final Map<String, String[]> skinCache = new HashMap<>();

    // Кэш истории скинов в оперативной памяти (вместо YAML-файла)
    private static final Map<UUID, List<String>> historyCache = new HashMap<>();

    public static void init(LastApi plugin) {
        // Автоматически создаем таблицу для скинов, если её еще нет
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_skins (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "history MEDIUMTEXT" + // В этой колонке мы будем хранить список скинов, разделенных знаком @@@
                             ");")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Загружаем историю из MySQL при входе
    public static void loadHistory(UUID uuid) {
        List<String> history = new ArrayList<>();
        try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT history FROM player_skins WHERE uuid = ?")) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String raw = rs.getString("history");
                if (raw != null && !raw.isEmpty()) {
                    // Разделяем обратно на список
                    history.addAll(Arrays.asList(raw.split("@@@")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        historyCache.put(uuid, history);
    }

    // Удаляем из памяти при выходе
    public static void unloadHistory(UUID uuid) {
        historyCache.remove(uuid);
    }

    public static CompletableFuture<String[]> getSkin(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String lowerName = playerName.toLowerCase();
            if (skinCache.containsKey(lowerName)) return skinCache.get(lowerName);

            try {
                URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection uuidConn = (HttpURLConnection) uuidUrl.openConnection();
                uuidConn.setRequestMethod("GET");
                uuidConn.setConnectTimeout(5000);

                if (uuidConn.getResponseCode() == 200) {
                    InputStreamReader uuidReader = new InputStreamReader(uuidConn.getInputStream());
                    JsonObject uuidJson = JsonParser.parseReader(uuidReader).getAsJsonObject();
                    uuidReader.close();

                    String uuid = uuidJson.get("id").getAsString();

                    URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                    HttpURLConnection profileConn = (HttpURLConnection) profileUrl.openConnection();
                    profileConn.setRequestMethod("GET");
                    profileConn.setConnectTimeout(5000);

                    if (profileConn.getResponseCode() == 200) {
                        InputStreamReader profileReader = new InputStreamReader(profileConn.getInputStream());
                        JsonObject profileJson = JsonParser.parseReader(profileReader).getAsJsonObject();
                        profileReader.close();

                        for (JsonElement element : profileJson.getAsJsonArray("properties")) {
                            JsonObject property = element.getAsJsonObject();
                            if (property.get("name").getAsString().equals("textures")) {
                                String value = property.get("value").getAsString();
                                String signature = property.get("signature").getAsString();

                                String[] skinData = new String[]{value, signature};
                                skinCache.put(lowerName, skinData);
                                return skinData;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Игрок пират или ошибка сети
            }
            return null;
        });
    }

    public static void applySkin(Player player, String skinName, String value, String signature, boolean saveHistory) {
        try {
            com.destroystokyo.paper.profile.PlayerProfile profile = player.getPlayerProfile();
            profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", value, signature));
            player.setPlayerProfile(profile);

            if (saveHistory) saveToHistory(player, skinName, value, signature);

            if (net.jetluna.api.bestplayer.BestPlayerManager.getCurrentBest() != null &&
                    net.jetluna.api.bestplayer.BestPlayerManager.getCurrentBest().equals(player.getUniqueId())) {
                net.jetluna.api.bestplayer.BestPlayerManager.updateTexture(value, signature);
            }

            Bukkit.getPluginManager().callEvent(new net.jetluna.api.event.PlayerSkinChangeEvent(player, value, signature));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToHistory(Player player, String skinName, String textureValue, String signature) {
        UUID uuid = player.getUniqueId();
        List<String> history = historyCache.getOrDefault(uuid, new ArrayList<>());

        String entry = skinName + ";" + textureValue + ";" + signature;
        history.removeIf(s -> s.startsWith(skinName + ";"));

        history.add(entry);
        if (history.size() > 27) history.remove(0);

        historyCache.put(uuid, history);

        // МАГИЯ: Асинхронно сохраняем всю историю скинов в MySQL!
        String joinedHistory = String.join("@@@", history);
        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO player_skins (uuid, history) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE history = VALUES(history)")) {

                ps.setString(1, uuid.toString());
                ps.setString(2, joinedHistory);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static List<String> getHistory(Player player) {
        return historyCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}