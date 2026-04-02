package net.jetluna.bedwars.state;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.endlobby.EndLobbyHologramManager;
import net.jetluna.bedwars.endlobby.TopNPCManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class EndingState extends GameState {

    public EndingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("§a[DEBUG] Активация состояния ENDING...");

        try {
            // 1. Спавним топы
            if (plugin.getGameStats() != null) {
                EndLobbyHologramManager.createGameTops(plugin.getGameStats());
                List<UUID> killsTop = plugin.getGameStats().getTopKills()
                        .stream()
                        .map(java.util.Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toList());
                TopNPCManager.spawnAll(plugin.getGameStats());
                Bukkit.broadcastMessage("§a[DEBUG] Голограммы и NPC созданы!");
            }

            // 2. Телепортация
            Location winLobby = plugin.getConfig().getLocation("locations.win-lobby");

            if (winLobby == null) {
                Bukkit.broadcastMessage("§c[ОШИБКА] win-lobby не найден в конфиге!");
                // Если лобби нет, берем хотя бы центр мира, чтобы не зависнуть
                winLobby = new Location(Bukkit.getWorld("world"), 0, 75, 1000);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setGameMode(GameMode.ADVENTURE); // Снимаем спектатора
                player.teleport(winLobby);
                player.sendTitle("§6§lИГРА ОКОНЧЕНА", "§7Спасибо за игру!", 10, 80, 10);
            }

        } catch (Throwable e) { // <--- Throwable ловит даже фатальные ошибки уровня ядра!
            Bukkit.broadcastMessage("§c[КРИТИЧЕСКАЯ ОШИБКА] Код финала упал!");
            e.printStackTrace();
        }

        startRestartTimer();
    }

    private void startRestartTimer() {
        new org.bukkit.scheduler.BukkitRunnable() {
            int time = 15;
            @Override
            public void run() {
                if (time <= 0) {
                    Bukkit.broadcastMessage("§c[!] Перезагрузка сервера...");
                    Bukkit.shutdown();
                    cancel();
                    return;
                }
                if (time <= 5) Bukkit.broadcastMessage("§eРестарт через " + time + "...");
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void onDisable() {
        TopNPCManager.clearAll();
        plugin.getNpcManager().clearNpcs();
    }

    @Override
    public String getName() { return "ENDING"; }
}