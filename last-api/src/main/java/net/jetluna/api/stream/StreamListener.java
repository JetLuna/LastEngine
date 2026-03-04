package net.jetluna.api.stream;

import net.jetluna.api.LastApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StreamListener implements Listener {

    // Храним задачи на удаление: UUID игрока -> Сама задача (чтобы можно было её отменить)
    private final Map<UUID, BukkitTask> quitTasks = new HashMap<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Проверяем, есть ли у вышедшего игрока запущенный стрим
        if (StreamManager.getActiveStreams().containsKey(player.getName())) {

            // Запускаем таймер на 3 минуты (3 мин * 60 сек * 20 тиков = 3600 тиков)
            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(LastApi.getInstance(), () -> {
                // Если прошло 3 минуты и задача не была отменена - удаляем стрим
                StreamManager.removeActiveStream(player.getName());
                quitTasks.remove(player.getUniqueId());
            }, 3600L);

            // Сохраняем задачу в память
            quitTasks.put(player.getUniqueId(), task);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Если игрок зашел, и у него висел таймер на удаление стрима
        if (quitTasks.containsKey(player.getUniqueId())) {
            // Отменяем задачу (стрим НЕ удалится)
            quitTasks.get(player.getUniqueId()).cancel();
            quitTasks.remove(player.getUniqueId());
        }
    }
}