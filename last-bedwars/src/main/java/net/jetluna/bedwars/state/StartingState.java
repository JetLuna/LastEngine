package net.jetluna.bedwars.state;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class StartingState extends GameState {

    private BukkitRunnable timerTask;
    private int timeLeft = 10; // Время до начала (секунды)

    public StartingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: ЗАПУСК (Starting)");

        // Запускаем таймер
        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                handleTimerTick();
            }
        };
        // 0L - задержка перед стартом, 20L - повторять каждые 20 тиков (1 секунда)
        this.timerTask.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(); // Обязательно убиваем таймер при смене статуса
        }

        // Очищаем уровень опыта игрокам (визуальный таймер)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(0);
            player.setExp(0);
        }
    }

    @Override
    public String getName() {
        return "STARTING";
    }

    private void handleTimerTick() {
        // Визуальный отсчет уровнями (XP bar)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(timeLeft);
            player.setExp((float) timeLeft / 10f); // Полоска опыта убывает
        }

        // Сообщения в чат и звуки на важных отметках
        if (timeLeft > 0 && (timeLeft <= 5 || timeLeft == 10)) {
            Bukkit.broadcastMessage("§eИгра начнется через §6" + timeLeft + " §eсек!");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                // Титул на экран
                player.sendTitle("§a" + timeLeft, "§7Приготовься!", 0, 25, 0);
            }
        }

        // КОГДА ВРЕМЯ ВЫШЛО
        if (timeLeft <= 0) {
            timerTask.cancel();

            List<GameTeam> teams = plugin.getTeamManager().getActiveTeams();

            // Защита: если админ забыл просканировать арену
            if (teams.isEmpty()) {
                Bukkit.broadcastMessage("§c§lОШИБКА: §7На арене не найдены базы! Админ должен написать /bw scan");
                plugin.getGameManager().setGameState(new WaitingState(plugin));
                return;
            }

            // 1. Раскидываем игроков по командам по очереди
            int teamIndex = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                GameTeam team = teams.get(teamIndex % teams.size());
                team.getPlayers().add(p.getUniqueId());
                teamIndex++;
            }

            // 2. ЛОМАЕМ КРОВАТИ ПУСТЫХ КОМАНД
            for (GameTeam team : teams) {
                if (team.getPlayers().isEmpty()) { // Если в команде никого нет
                    team.setHasBed(false); // Отключаем её в логике

                    // Физически удаляем блок кровати с карты
                    if (team.getBedLocation() != null) {
                        org.bukkit.block.Block bedBlock = team.getBedLocation().getBlock();
                        if (bedBlock.getType().name().contains("BED")) {
                            bedBlock.setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }

            // 3. Запускаем саму игру!
            plugin.getGameManager().setGameState(new IngameState(plugin));
            return;
        }

        timeLeft--;
    }

    @org.bukkit.event.EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        int online = Bukkit.getOnlinePlayers().size() - 1;
        if (online < 2) {
            Bukkit.broadcastMessage("§cНедостаточно игроков! Запуск отменен.");
            plugin.getGameManager().setGameState(new WaitingState(plugin)); // Возврат в лобби
        }
    }
}