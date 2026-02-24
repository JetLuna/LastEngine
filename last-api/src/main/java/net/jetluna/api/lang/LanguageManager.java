package net.jetluna.api.lang;

import net.jetluna.api.LastApi;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LanguageManager {

    private static final Map<String, YamlConfiguration> languages = new HashMap<>();
    private static final Map<UUID, String> playerLangs = new HashMap<>();
    private static final String DEFAULT_LANG = "ru";

    public static void load() {
        loadLang("ru");
        loadLang("en");
        loadLang("ua");
    }

    private static void loadLang(String lang) {
        File file = new File(LastApi.getInstance().getDataFolder(), "lang/" + lang + ".yml");
        if (!file.exists()) {
            LastApi.getInstance().saveResource("lang/" + lang + ".yml", false);
        }
        languages.put(lang, YamlConfiguration.loadConfiguration(file));
        LastApi.getInstance().getLogger().info("Language " + lang + " loaded!");
    }

    public static void setLang(Player player, String lang) {
        if (!languages.containsKey(lang)) return;
        playerLangs.put(player.getUniqueId(), lang);
        ChatUtil.sendMessage(player, getString(player, "general.lang_changed"));
    }

    // --- ПОЛУЧЕНИЕ СТРОКИ ---
    public static String getString(Player player, String key) {
        String lang = playerLangs.getOrDefault(player.getUniqueId(), DEFAULT_LANG);
        YamlConfiguration config = languages.get(lang);

        if (config == null || !config.contains(key)) {
            return "<red>Key not found: " + key;
        }

        String text = config.getString(key);

        if (text.contains("%prefix_server%")) {
            text = text.replace("%prefix_server%", config.getString("prefix.server", ""));
        }
        if (text.contains("%prefix_error%")) {
            text = text.replace("%prefix_error%", config.getString("prefix.error", ""));
        }

        return text;
    }

    // --- ПОЛУЧЕНИЕ СПИСКА (ДЛЯ LORE) ---
    public static List<String> getList(Player player, String key) {
        String lang = playerLangs.getOrDefault(player.getUniqueId(), DEFAULT_LANG);
        YamlConfiguration config = languages.get(lang);

        if (config == null || !config.contains(key)) {
            return Collections.singletonList("<red>List not found: " + key);
        }

        return config.getStringList(key);
    }

    // --- ОТПРАВКА СООБЩЕНИЯ (НОВЫЙ МЕТОД) ---
    public static void sendMessage(Player player, String key) {
        String text = getString(player, key);
        ChatUtil.sendMessage(player, text);
    }
}