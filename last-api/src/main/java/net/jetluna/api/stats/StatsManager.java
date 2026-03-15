package net.jetluna.api.stats;

import net.jetluna.api.LastApi;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatsManager {

    private static File statsFolder;
    private static final Map<UUID, PlayerStats> cache = new HashMap<>();

    public static void init(JavaPlugin plugin) {
        statsFolder = new File(plugin.getDataFolder(), "stats");
        if (!statsFolder.exists()) statsFolder.mkdirs();

        // Создаем МЕГА-таблицу со всеми нужными данными
        try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_stats (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "username VARCHAR(16), " +
                             "emeralds INT DEFAULT 0, " +
                             "coins INT DEFAULT 0, " +
                             "level INT DEFAULT 1, " +
                             "exp INT DEFAULT 0, " +
                             "active_effect VARCHAR(32) DEFAULT '', " +
                             "unlocked_effects MEDIUMTEXT, " +
                             "active_pet VARCHAR(32) DEFAULT '', " +
                             "owned_pets MEDIUMTEXT, " +
                             "active_gadget VARCHAR(32) DEFAULT '', " +
                             "owned_gadgets MEDIUMTEXT" +
                             ");")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PlayerStats getStats(Player player) {
        if (player == null) return null;
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return cache.get(uuid);
        return loadStats(player);
    }

    private static PlayerStats loadStats(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(statsFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        long lastReward = config.getLong("rewards.last_time", 0);
        int day = config.getInt("rewards.day", 1);

        PlayerStats stats = new PlayerStats(0, 0, lastReward, day, 1, 0);

        try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM player_stats WHERE uuid = ?")) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats.setEmeralds(rs.getInt("emeralds"));
                stats.setCoins(rs.getInt("coins"));
                stats.setLevel(rs.getInt("level"));
                stats.setExp(rs.getInt("exp"));

                // Загружаем эффекты
                stats.setActiveEffect(rs.getString("active_effect"));
                String effStr = rs.getString("unlocked_effects");
                if (effStr != null && !effStr.isEmpty()) stats.getUnlockedEffects().addAll(Arrays.asList(effStr.split(",")));

                // Загружаем питомцев
                stats.setActivePet(rs.getString("active_pet"));
                String petStr = rs.getString("owned_pets");
                if (petStr != null && !petStr.isEmpty()) stats.getOwnedPets().addAll(Arrays.asList(petStr.split(",")));

                // Загружаем гаджеты
                stats.setActiveGadget(rs.getString("active_gadget"));
                String gadStr = rs.getString("owned_gadgets");
                if (gadStr != null && !gadStr.isEmpty()) stats.getOwnedGadgets().addAll(Arrays.asList(gadStr.split(",")));
            } else {
                try (PreparedStatement insert = conn.prepareStatement("INSERT INTO player_stats (uuid, username, emeralds, coins, level, exp, active_effect, unlocked_effects, active_pet, owned_pets, active_gadget, owned_gadgets) VALUES (?, ?, 0, 0, 1, 0, '', '', '', '', '', '')")) {
                    insert.setString(1, uuid.toString());
                    insert.setString(2, player.getName());
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cache.put(uuid, stats);
        return stats;
    }

    public static void saveStats(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (!cache.containsKey(uuid)) return;
        PlayerStats stats = cache.get(uuid);

        File file = new File(statsFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("rewards.last_time", stats.getLastRewardTime());
        config.set("rewards.day", stats.getRewardDay());
        try { config.save(file); } catch (IOException ignored) {}

        // Превращаем списки обратно в строки через запятую
        String effStr = String.join(",", stats.getUnlockedEffects());
        String petStr = String.join(",", stats.getOwnedPets());
        String gadStr = String.join(",", stats.getOwnedGadgets());

        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE player_stats SET emeralds = ?, coins = ?, level = ?, exp = ?, username = ?, active_effect = ?, unlocked_effects = ?, active_pet = ?, owned_pets = ?, active_gadget = ?, owned_gadgets = ? WHERE uuid = ?")) {

                ps.setInt(1, stats.getEmeralds());
                ps.setInt(2, stats.getCoins());
                ps.setInt(3, stats.getLevel());
                ps.setInt(4, stats.getExp());
                ps.setString(5, player.getName());
                ps.setString(6, stats.getActiveEffect() == null ? "" : stats.getActiveEffect());
                ps.setString(7, effStr);
                ps.setString(8, stats.getActivePet() == null ? "" : stats.getActivePet());
                ps.setString(9, petStr);
                ps.setString(10, stats.getActiveGadget() == null ? "" : stats.getActiveGadget());
                ps.setString(11, gadStr);
                ps.setString(12, uuid.toString());
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void unloadStats(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) {
            saveStats(player);
            cache.remove(uuid);
        }
    }
}