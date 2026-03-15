package net.jetluna.api.skin;

import net.jetluna.api.LastApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class SkinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {

            // 1. ЗАГРУЖАЕМ ИСТОРИЮ ИЗ MySQL В ПАМЯТЬ СЕРВЕРА
            SkinManager.loadHistory(player.getUniqueId());

            // 2. Получаем загруженную историю
            List<String> history = SkinManager.getHistory(player);

            if (!history.isEmpty()) {
                String lastSkinEntry = history.get(history.size() - 1);
                String[] parts = lastSkinEntry.split(";", 3);

                if (parts.length == 3) {
                    Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> {
                        SkinManager.applySkin(player, parts[0], parts[1], parts[2], false);
                    });
                    return;
                }
            }

            SkinManager.getSkin(player.getName()).thenAccept(skinData -> {
                if (skinData != null) {
                    Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> {
                        SkinManager.applySkin(player, player.getName(), skinData[0], skinData[1], true);
                    });
                }
            });
        });
    }

    // !!! НОВОЕ: Очищаем кэш при выходе !!!
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        SkinManager.unloadHistory(event.getPlayer().getUniqueId());
    }
}