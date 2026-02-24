package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class ChatUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    // Превращает текст (<red>Привет) в Компонент (для чата и таба)
    public static Component parse(String text) {
        return mm.deserialize(text);
    }

    // --- ВОТ ЭТОГО МЕТОДА НЕ ХВАТАЛО ---
    // Превращает текст в старый формат (§cПривет) для Скорборда
    public static String parseLegacy(String text) {
        Component component = parse(text);
        // Включаем поддержку HEX цветов (§x§f§f...)
        return LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build()
                .serialize(component);
    }
    // -----------------------------------

    public static void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(parse(text));
    }
}