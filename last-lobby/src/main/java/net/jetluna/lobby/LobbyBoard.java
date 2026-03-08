package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class LobbyBoard {

    public static void update(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // 🟢 МАГИЯ ГРАДИЕНТА (Генерируется кодом)
        String title = makeGradient("LAST ENGINE", new java.awt.Color(0, 255, 127), new java.awt.Color(0, 85, 0), true);

        Objective obj = board.registerNewObjective("LastLobby", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = StatsManager.getStats(player);
        int emeralds = (stats != null) ? stats.getEmeralds() : 0;
        int level = (stats != null) ? stats.getLevel() : 1;
        int exp = (stats != null) ? stats.getExp() : 0;

        int maxExp = level * 1000;
        int percent = (int) Math.min(100, ((double) exp / maxExp) * 100);
        String progressBar = getProgressBar(exp, maxExp, 10, "-", "&a", "&8");

        Rank rank = RankManager.getRank(player);

        // Берем готовый красивый ранг с учетом кастомного цвета!
        String rankName = net.jetluna.api.util.NameFormatUtil.getFormattedRank(player, rank);

        // --- СБОРКА СКОРБОРДА ---
        createScore(obj, " ", 15);

        createScore(obj, color(LanguageManager.getString(player, "lobby.board.info_title")), 14);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.status")).replace("%rank%", rankName), 13);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.guild")), 12);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.emeralds")).replace("%emeralds%", String.valueOf(emeralds)), 11);

        createScore(obj, "  ", 10);

        createScore(obj, color(LanguageManager.getString(player, "lobby.board.level_title")), 9);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.level")).replace("%level%", String.valueOf(level)), 8);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.progress")).replace("%progress%", progressBar).replace("%percent%", String.valueOf(percent)), 7);

        createScore(obj, "   ", 6);

        createScore(obj, color(LanguageManager.getString(player, "lobby.board.server_title")), 5);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.online")).replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size())), 4);
        createScore(obj, color(LanguageManager.getString(player, "lobby.board.lobby")), 3);

        createScore(obj, "    ", 2);

        String footer = color(LanguageManager.getString(player, "lobby.board.footer"));
        createScore(obj, footer, 1);

        // Вшиваем префиксы над головой и сортировку в ТАБ прямо в этот скорборд
        for (Player target : Bukkit.getOnlinePlayers()) {
            Rank targetRank = RankManager.getRank(target);

            // Генерируем имя команды для сортировки (вес ранга + ник)
            String rawTeamName = String.format("%04d", 1000 - targetRank.getWeight()) + target.getName();
            String teamName = rawTeamName.substring(0, Math.min(rawTeamName.length(), 16));

            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            // Ставим префикс, используя наш метод из API
            team.setPrefix(net.jetluna.api.util.NameFormatUtil.getNameTagPrefix(target, targetRank));

            org.bukkit.ChatColor teamColor = NameFormatUtil.getNameTagColor(target, targetRank);
            if (teamColor != null) {
                try {
                    team.setColor(teamColor);
                } catch (NoSuchMethodError ignored) {}
            }

            if (!team.hasEntry(target.getName())) {
                team.addEntry(target.getName());
            }
        }

        player.setScoreboard(board);
    }

    private static void createScore(Objective obj, String text, int score) {
        Score line = obj.getScore(ChatUtil.parseLegacy(text));
        line.setScore(score);
    }

    private static String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        int leftOver = (totalBars - progressBars);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatUtil.parseLegacy(completedColor));
        for (int i = 0; i < progressBars; i++) sb.append(symbol);
        sb.append(ChatUtil.parseLegacy(notCompletedColor));
        for (int i = 0; i < leftOver; i++) sb.append(symbol);

        return sb.toString();
    }

    public static String makeGradient(String text, java.awt.Color start, java.awt.Color end, boolean isBold) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (length > 1) ? (float) i / (length - 1) : 0;
            int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            sb.append(net.md_5.bungee.api.ChatColor.of(new java.awt.Color(red, green, blue)));
            if (isBold) sb.append(net.md_5.bungee.api.ChatColor.BOLD);
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}