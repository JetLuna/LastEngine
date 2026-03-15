package net.jetluna.bedwars.state;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EndingState extends GameState {

    public EndingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: КОНЕЦ ИГРЫ (Ending)");

        plugin.getNpcManager().clearNpcs(); // Удаляем торговцев с карты

        // Делаем всех наблюдателями и телепортируем на спавн мира
        org.bukkit.Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(spawn);
            player.sendTitle("§6§lИГРА ОКОНЧЕНА", "§eВозврат на спавн...", 10, 100, 10);
        }

        // Перезагрузка через 10 секунд
        new BukkitRunnable() {
            int countdown = 10;
            @Override
            public void run() {
                if (countdown <= 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer("§cИгра окончена!\n§eСервер перезапускается.");
                    }
                    Bukkit.shutdown(); // Выключаем сервер
                    cancel();
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void onDisable() {}

    @Override
    public String getName() {
        return "ENDING";
    }
}