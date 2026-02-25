package net.jetluna.api.stats;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private static File statsFolder;
    private static final Map<UUID, PlayerStats> cache = new HashMap<>();

    public static void init(JavaPlugin plugin) {
        statsFolder = new File(plugin.getDataFolder(), "stats");
        if (!statsFolder.exists()) statsFolder.mkdirs();
    }

    // Получить статистику игрока
    public static PlayerStats getStats(Player player) {
        if (player == null) return null;
        UUID uuid = player.getUniqueId();

        // 1. Если есть в кэше (в памяти) - отдаем сразу
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        // 2. Если нет - грузим из файла
        return loadStats(uuid);
    }

    private static PlayerStats loadStats(UUID uuid) {
        File file = new File(statsFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Читаем значения (или 0 по умолчанию)
        int coins = config.getInt("coins", 0);
        int emeralds = config.getInt("emeralds", 0);
        long lastReward = config.getLong("rewards.last_time", 0);
        int day = config.getInt("rewards.day", 1);
        int level = config.getInt("level", 1);
        int exp = config.getInt("exp", 0);

        PlayerStats stats = new PlayerStats(coins, emeralds, lastReward, day, level, exp);
        cache.put(uuid, stats); // Сохраняем в память
        return stats;
    }

    // !!! ГЛАВНЫЙ МЕТОД - СОХРАНЕНИЕ !!!
    public static void saveStats(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();

        if (!cache.containsKey(uuid)) return;
        PlayerStats stats = cache.get(uuid);

        File file = new File(statsFolder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Записываем данные
        config.set("coins", stats.getCoins());
        config.set("emeralds", stats.getEmeralds());
        config.set("rewards.last_time", stats.getLastRewardTime());
        config.set("rewards.day", stats.getRewardDay());
        config.set("level", stats.getLevel());
        config.set("exp", stats.getExp());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}