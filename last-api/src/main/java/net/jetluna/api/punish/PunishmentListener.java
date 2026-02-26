package net.jetluna.api.punish;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PunishmentListener implements Listener {

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        // Проверяем, забанен ли игрок
        if (PunishmentManager.isBanned(name)) {
            // Получаем сообщение о бане
            String reason = PunishmentManager.getBanMessage(name);

            // Запрещаем вход (Kick message должен быть Legacy для старых ядер, но ChatUtil справится)
            // AsyncPlayerPreLoginEvent требует String или Component (в Paper).
            // Для надежности используем ChatUtil.parseLegacy, так как этот ивент капризный.
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatUtil.parseLegacy(reason));
        }
    }
}