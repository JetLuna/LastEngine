package net.jetluna.api.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettingsManager {

    private static File file;
    private static YamlConfiguration config;

    // Кэш для быстрого доступа
    private static final Map<UUID, Integer> pmSettings = new HashMap<>(); // 0 - Все, 1 - Друзья, 2 - Никто
    private static final Map<UUID, Boolean> donateChatSettings = new HashMap<>(); // true - включен, false - выключен

    public static void init(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "player_settings.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("pm")) {
            for (String key : config.getConfigurationSection("pm").getKeys(false)) {
                pmSettings.put(UUID.fromString(key), config.getInt("pm." + key));
            }
        }
        if (config.contains("donate_chat")) {
            for (String key : config.getConfigurationSection("donate_chat").getKeys(false)) {
                donateChatSettings.put(UUID.fromString(key), config.getBoolean("donate_chat." + key));
            }
        }
    }

    // --- НАСТРОЙКИ ЛС (0 = Все, 1 = Друзья, 2 = Никто) ---
    public static int getPMSetting(UUID uuid) {
        return pmSettings.getOrDefault(uuid, 0); // По умолчанию могут писать все
    }

    public static void cyclePMSetting(UUID uuid) {
        int current = getPMSetting(uuid);
        int next = (current >= 2) ? 0 : current + 1; // Переключаем: 0 -> 1 -> 2 -> 0
        pmSettings.put(uuid, next);
        config.set("pm." + uuid.toString(), next);
        save();
    }

    // --- НАСТРОЙКИ ДОНАТ ЧАТА ---
    public static boolean isDonateChatEnabled(UUID uuid) {
        return donateChatSettings.getOrDefault(uuid, true); // По умолчанию включен
    }

    public static void toggleDonateChat(UUID uuid) {
        boolean current = isDonateChatEnabled(uuid);
        donateChatSettings.put(uuid, !current); // Переключаем true/false
        config.set("donate_chat." + uuid.toString(), !current);
        save();
    }

    private static void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}