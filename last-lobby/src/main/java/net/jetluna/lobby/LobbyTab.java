package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class LobbyTab {

    public static void update(Player player) {
        // --- ВИЗУАЛЬНАЯ ЧАСТЬ ТАБА (Тянем из конфига) ---
        String header = color(LanguageManager.getString(player, "lobby.tab.header"))
                .replace("%player%", player.getName());

        String footer = color(LanguageManager.getString(player, "lobby.tab.footer"))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        // Настройка имени конкретного игрока
        Rank rank = RankManager.getRank(player);
        String prefix = color(rank.getPrefix());

        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? color(stats.getSuffix()) : "";

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
                String teamName = String.format("%04d", 1000 - weight);

                Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                // 2. Вторичная сортировка по нику (A-Z, А-Я, 0-9) происходит АВТОМАТИЧЕСКИ
                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }
            }
        }
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}