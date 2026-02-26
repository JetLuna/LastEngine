package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class ChatUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Превращаем текст в компонент (с поддержкой цветов)
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        // Если есть старые цвета (§ или &), используем Legacy, иначе MiniMessage
        if (text.contains("&") || text.contains("§")) {
            String legacy = org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
            return LegacyComponentSerializer.legacySection().deserialize(legacy);
        }
        return mm.deserialize(text);
    }

    // Для старых методов (Scoreboard, Title), которым нужен String
    public static String parseLegacy(String text) {
        if (text == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(parse(text));
    }

    // !!! НОВЫЙ МЕТОД: Превращаем Компонент обратно в Текст (для чата) !!!
    public static String serialize(Component component) {
        return mm.serialize(component);
    }

    // Удобная отправка сообщения
    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }

    // Убираем цвета (для проверок)
    public static String strip(String text) {
        return parseLegacy(text).replaceAll("§.", "");
    }
}