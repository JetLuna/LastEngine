package net.jetluna.lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyTask extends BukkitRunnable {

    private final LobbyPlugin plugin;

    // Вот этот конструктор, которого не хватало!
    public LobbyTask(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                // Обновляем Скорборд
                LobbyBoard.update(player);

                // Обновляем Таб (Префиксы и т.д.)
                LobbyTab.update(player);
            } catch (Exception e) {
                // Если вдруг ошибка в обновлении, не крашим весь таск
                // e.printStackTrace(); // Можно включить для отладки
            }
        }
    }
}