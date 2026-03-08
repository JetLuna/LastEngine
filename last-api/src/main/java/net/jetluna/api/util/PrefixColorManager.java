package net.jetluna.api.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrefixColorManager {

    private static File file;
    private static YamlConfiguration config;

    // Кэш для быстрого доступа, чтобы не читать файл каждый раз при сообщении в чат
    private static final Map<UUID, String> cache = new HashMap<>();

    public static void init(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "prefix_colors.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        // Загружаем все цвета из файла в кэш при запуске сервера
        if (config.contains("colors")) {
            for (String key : config.getConfigurationSection("colors").getKeys(false)) {
                try {
                    cache.put(UUID.fromString(key), config.getString("colors." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // --- ПОЛУЧИТЬ ЦВЕТ ---
    public static String getPlayerColor(UUID uuid) {
        return cache.get(uuid); // Вернет null, если игрок не выбирал цвет
    }

    // --- УСТАНОВИТЬ ЦВЕТ ---
    public static void setPlayerColor(UUID uuid, String colorCode) {
        cache.put(uuid, colorCode);
        config.set("colors." + uuid.toString(), colorCode);
        save();
    }

    // --- УДАЛИТЬ ЦВЕТ (СБРОС) ---
    public static void removePlayerColor(UUID uuid) {
        cache.remove(uuid);
        config.set("colors." + uuid.toString(), null);
        save();
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}