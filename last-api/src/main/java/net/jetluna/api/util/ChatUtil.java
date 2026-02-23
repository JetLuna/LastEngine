package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class ChatUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Превращает строку "<red>Привет" в компонент
    public static Component parse(String text) {
        return mm.deserialize(text);
    }

    // Отправляет сообщение игроку или консоли
    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }

    // Перегрузка для отправки сообщения с префиксом
    public static void sendMessage(CommandSender sender, String prefix, String text) {
        sender.sendMessage(parse(prefix + " <gray>» " + text));
    }
}