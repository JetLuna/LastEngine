package net.jetluna.lobby;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class LobbyTab {

    public static void update(Player player) {
        // --- ВИЗУАЛЬНАЯ ЧАСТЬ ТАБА ---
        String title = LobbyBoard.makeGradient("LAST ENGINE", new java.awt.Color(0, 255, 127), new java.awt.Color(0, 85, 0), true);

        String header = "\n" +
                title + "\n" +
                "&fПривилегии, кейсы, и\n" +
                "&fмногое другое:\n" +
                "&awww.lastengine.net\n" +
                "\n" +
                "&7Список игроков:\n";

        String footer = "\n" +
                "&fОбщий онлайн ➔ &a" + Bukkit.getOnlinePlayers().size() + "\n" +
                "\n" +
                "&7Вы находитесь на сервере &aЛобби-1\n";

        header = header.replace("&", "§");
        footer = footer.replace("&", "§");

        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        // Настройка имени конкретного игрока
        Rank rank = RankManager.getRank(player);
        String prefix = net.jetluna.lobby.gui.JoinerGui.toLegacy(rank.getPrefix()).replace("&", "§");

        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? stats.getSuffix().replace("&", "§") : "";

        String nameColor = (rank.getWeight() == 1) ? "§7" : "§f";

        String tabName = prefix + nameColor + player.getName() + suffix;
        player.playerListName(ChatUtil.parse(tabName));

        // --- МАГИЯ СОРТИРОВКИ (Организация данных!) ---
        Scoreboard board = player.getScoreboard();
        if (board != null) {
            // Перебираем всех игроков, чтобы раскидать их по "весовым" командам
            for (Player target : Bukkit.getOnlinePlayers()) {
                Rank targetRank = RankManager.getRank(target);
                int weight = targetRank.getWeight();

                // 1. Формируем имя команды (1000 минус вес ранга)
                // Чем больше вес, тем меньше итоговое число, тем выше игрок в Табе.
                String teamName = String.format("%04d", 1000 - weight);

                Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                // 2. Вторичная сортировка по нику (A-Z, А-Я, 0-9) происходит АВТОМАТИЧЕСКИ
                // самим клиентом Minecraft внутри одной команды!
                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }
            }
        }
    }
}