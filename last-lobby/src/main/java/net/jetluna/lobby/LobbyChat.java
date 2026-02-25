package net.jetluna.lobby;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class LobbyChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Простой формат: [Ранг] Ник: Сообщение
        // Если есть Vault/LuckPerms, можно подключить их сюда позже.
        // Пока сделаем красиво через права.

        String prefix = "<gray>";
        if (player.hasPermission("last.admin")) prefix = "<red><bold>ADMIN <red>";
        else if (player.hasPermission("last.vip")) prefix = "<green><bold>VIP <green>";
        else prefix = "<gray>Игрок <gray>";

        // Формируем сообщение (Используем ChatUtil для цветов)
        String format = prefix + player.getName() + " <dark_gray>» <white>" + message;

        event.setFormat(ChatUtil.parseLegacy(format).replace("%", "%%"));
    }
}