package net.jetluna.bedwars.manager;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final BedWarsPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    // Временное хранилище для статистики сломанных кроватей (потом объединим с поинтами)
    private final Map<UUID, Integer> brokenBeds = new HashMap<>();

    public ScoreboardManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    // Метод для добавления сломанной кровати в стату
    public void addBrokenBed(Player player) {
        brokenBeds.put(player.getUniqueId(), brokenBeds.getOrDefault(player.getUniqueId(), 0) + 1);
    }

    private void startUpdateTask() {
        // Обновляем каждую секунду (20 тиков)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTabList(player);
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateTabList(Player player) {
        int online = Bukkit.getOnlinePlayers().size();

        String header = "\n§a§lLAST §2§lENGINE\n§fПривилегии, кейсы, и\n§fмногое другое:\n§awww.lastengine.net\n\n§7Список игроков:\n";
        String footer = "\n§fОбщий онлайн ➔ §a" + online + "\n\n§7Вы на сервере §aBedWars-1\n";

        player.setPlayerListHeaderFooter(header, footer);

        // --- НОВОЕ: Красим ник в Табе ---
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            // Если в команде — красим в её цвет
            player.setPlayerListName(team.getColor().getChatColor() + player.getName());
        } else {
            // Если наблюдатель — серый цвет
            player.setPlayerListName("§7" + player.getName());
        }
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("bw_board", "dummy", "§a§lBedWars");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 15;
        String dateStr = "§7" + dateFormat.format(new Date());

        // Собираем квадратики команд
        StringBuilder teamsLine = new StringBuilder();
        for (GameTeam team : plugin.getTeamManager().getActiveTeams()) {
            String colorCode = team.getColor().getChatColor().toString();

            if (team.hasBed()) {
                // 1) Команда с кроватью
                teamsLine.append(colorCode).append("⯀ ");
            } else if (!team.getPlayers().isEmpty()) {
                // 2) Команда без кровати, но игроки живы
                teamsLine.append(colorCode).append("□ ");
            } else {
                // 3) Команда без кровати и игроков нет (уничтожена)
                teamsLine.append(colorCode).append("✕ ");
            }
        }

        int beds = brokenBeds.getOrDefault(player.getUniqueId(), 0);

        // Строим скорборд (используем разное кол-во пробелов для пустых строк)
        obj.getScore(dateStr).setScore(line--);
        obj.getScore(" ").setScore(line--);
        obj.getScore("§fАпгрейд опалов:: §a10:00").setScore(line--); // Заглушка таймера
        obj.getScore("  ").setScore(line--);
        obj.getScore("§fИнформация о командах:").setScore(line--);
        obj.getScore(teamsLine.toString().trim()).setScore(line--);
        obj.getScore("   ").setScore(line--);
        obj.getScore("§fКроватей разрушено: §a" + beds).setScore(line--);
        int points = plugin.getEconomyManager().getPoints(player);
        obj.getScore("§fПоинты: §a" + points).setScore(line--); // Заглушка для экономики
        obj.getScore("    ").setScore(line--);
        obj.getScore("§fРежим: §aSolo").setScore(line--);
        obj.getScore("§fКарта: §aLast City").setScore(line--);
        obj.getScore("     ").setScore(line--);
        obj.getScore("§aplay.lastengine.net").setScore(line--);

        player.setScoreboard(board);
    }
}