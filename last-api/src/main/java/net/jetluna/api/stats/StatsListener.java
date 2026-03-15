package net.jetluna.api.stats;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatsListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Даем Lobby-1 полсекунды (10L тиков), чтобы он успел выгрузить данные игрока в MySQL
        Bukkit.getScheduler().runTaskLater(net.jetluna.api.LastApi.getInstance(), () -> {
            if (event.getPlayer().isOnline()) {
                StatsManager.getStats(event.getPlayer());
            }
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        System.out.println("[DEBUG] Игрок " + event.getPlayer().getName() + " вышел. Отправляем данные в MySQL...");
        StatsManager.unloadStats(event.getPlayer());
    }

}