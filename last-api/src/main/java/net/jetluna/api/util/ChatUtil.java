package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    // !!! ИСПРАВЛЕНИЕ: Теперь метод жрет всё (и <red>, и &c, и §c)
    public static Component parse(String text) {
        if (text == null) return Component.empty();

        // 1. Если текст содержит старые цвета (&c или §c), конвертируем их в MiniMessage формат
        if (text.contains("&") || text.contains("§")) {
            // Сначала превращаем &c -> §c
            String legacy = ChatColor.translateAlternateColorCodes('&', text);
            // Потом §c -> <red> (конвертация в Component)
            return LegacyComponentSerializer.legacySection().deserialize(legacy);
        }

        // 2. Если это чистый MiniMessage (<red>Text), просто парсим
        return mm.deserialize(text);
    }

    // Метод для Скорборда (ему нужны параграфы §)
    public static String parseLegacy(String text) {
        if (text == null) return "";
        // Если текст уже с §, просто возвращаем
        if (text.contains("§")) return text;

        // Если текст с &, красим
        if (text.contains("&")) return ChatColor.translateAlternateColorCodes('&', text);

        // Если текст MiniMessage (<red>), превращаем в §c
        return LegacyComponentSerializer.legacySection().serialize(mm.deserialize(text));
    }

    // Очистка цветов
    public static String strip(String text) {
        if (text == null) return "";
        String stripped = ChatColor.stripColor(text);
        return stripped.replaceAll("<[^>]*>", "");
    }

    // Отправка сообщений
    public static void sendMessage(CommandSender sender, String text) {
        if (sender != null && text != null) {
            sender.sendMessage(parse(text));
        }
    }
}