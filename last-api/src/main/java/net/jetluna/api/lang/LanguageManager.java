package net.jetluna.api.lang;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LanguageManager {

    private static final Map<String, YamlConfiguration> languages = new HashMap<>();
    private static final Map<UUID, String> playerLangs = new HashMap<>();
    private static File langFolder;

    public static void init(JavaPlugin plugin) {
        langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();

        loadLang("ru", plugin);
        loadLang("en", plugin);
        loadLang("ua", plugin);
    }

    private static void loadLang(String lang, JavaPlugin plugin) {
        File file = new File(langFolder, lang + ".yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("lang/" + lang + ".yml", false);
            } catch (Exception e) {}
        }
        languages.put(lang, YamlConfiguration.loadConfiguration(file));
    }

    public static String getString(Player player, String key) {
        String lang = "ru";
        if (player != null && playerLangs.containsKey(player.getUniqueId())) {
            lang = playerLangs.get(player.getUniqueId());
        }

        YamlConfiguration config = languages.get(lang);
        if (config == null) config = languages.get("ru");

        String msg = config.getString(key);
        if (msg == null) return "Key not found: " + key;

        // !!! ВАЖНО: Возвращаем сырой текст (с <red>, <gradient>), не переводим в § !!!
        return msg;
    }

    public static List<String> getList(Player player, String key) {
        String lang = "ru";
        if (player != null && playerLangs.containsKey(player.getUniqueId())) {
            lang = playerLangs.get(player.getUniqueId());
        }

        YamlConfiguration config = languages.get(lang);
        if (config == null) config = languages.get("ru");

        // Возвращаем список как есть
        return config.getStringList(key);
    }

    public static void setLang(Player player, String lang) {
        if (languages.containsKey(lang)) {
            playerLangs.put(player.getUniqueId(), lang);
        }
    }

    public static void sendMessage(Player player, String key) {
        if (player != null) {
            String text = getString(player, key);
            // А вот здесь красим, потому что отправляем в чат
            ChatUtil.sendMessage(player, text);
        }
    }
}