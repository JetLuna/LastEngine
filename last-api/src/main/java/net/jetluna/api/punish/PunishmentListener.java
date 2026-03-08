package net.jetluna.api.punish;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PunishmentListener implements Listener {

    // --- БЛОКИРОВКА ВХОДА ДЛЯ ЗАБАНЕННЫХ ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        if (PunishmentManager.isBanned(name)) {
            String reason = PunishmentManager.getBanMessage(name);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatUtil.parseLegacy(reason));
        }
    }

    // --- БЛОКИРОВКА ЧАТА ДЛЯ ЗАМУЧЕННЫХ ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (PunishmentManager.isMuted(player.getName())) {
            event.setCancelled(true); // Блокируем сообщение
            String msg = PunishmentManager.getMuteMessage(player.getName());
            ChatUtil.sendMessage(player, msg); // Отправляем игроку причину мута
        }
    }
}