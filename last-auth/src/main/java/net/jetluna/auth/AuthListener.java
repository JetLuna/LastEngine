package net.jetluna.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthListener implements Listener {

    private final AuthManager authManager;

    public AuthListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        // Тут потом добавим проверку: если игрок есть в БД -> "Войдите", если нет -> "Регистрируйтесь"
        event.getPlayer().sendMessage(net.jetluna.api.util.ChatUtil.parse("<red>Пожалуйста, авторизуйтесь!"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authManager.logout(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!authManager.isAuthorized(event.getPlayer())) {
            // Если игрок пытается сдвинуться с блока — возвращаем обратно
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!authManager.isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}