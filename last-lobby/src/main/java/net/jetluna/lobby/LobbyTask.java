package net.jetluna.lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyTask extends BukkitRunnable {

    @Override
    public void run() {
        // Проходимся по всем игрокам в мире лобби
        for (Player player : Bukkit.getOnlinePlayers()) {
            LobbyTab.update(player);
            LobbyBoard.update(player);
        }
    }
}