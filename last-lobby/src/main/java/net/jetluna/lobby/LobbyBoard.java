package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class LobbyBoard {

    public static void update(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // 🟢 МАГИЯ ГРАДИЕНТА: От салатового (0, 255, 127) к темно-зеленому (0, 85, 0)
        String title = makeGradient("LAST ENGINE", new java.awt.Color(0, 255, 127), new java.awt.Color(0, 85, 0), true);

        Objective obj = board.registerNewObjective("LastLobby", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Получаем статистику игрока
        PlayerStats stats = StatsManager.getStats(player);
        int emeralds = (stats != null) ? stats.getEmeralds() : 0;
        int level = (stats != null) ? stats.getLevel() : 1;
        int exp = (stats != null) ? stats.getExp() : 0;

        // Расчет прогресс-бара
        int maxExp = level * 1000;
        int percent = (int) Math.min(100, ((double) exp / maxExp) * 100);
        String progressBar = getProgressBar(exp, maxExp, 10, "-", "&a", "&8");

        // Получаем красивый ранг
        Rank rank = RankManager.getRank(player);
        String rankName = (rank.getWeight() == 1) ? "&7Player" :
                rank.getPrefix().replace("<dark_red>", "&4").replace("<bold>", "&l").replaceAll("<[^>]+>", "");

        // --- СБОРКА СКОРБОРДА ---
        createScore(obj, " ", 15);

        createScore(obj, "&2Информация:", 14);
        createScore(obj, " &fСтатус: " + rankName, 13);
        createScore(obj, " &fГильдия: &7N/A", 12);
        createScore(obj, " &fИзумруды: &a" + emeralds, 11);

        createScore(obj, "  ", 10);

        createScore(obj, "&2Серверный уровень:", 9);
        createScore(obj, " &fУровень: &a" + level + "&2*", 8);
        createScore(obj, " &7↳ " + progressBar + " &a" + percent + "%", 7);

        createScore(obj, "   ", 6);

        createScore(obj, "&2Сервер:", 5);
        createScore(obj, " &fОбщий онлайн: &a" + Bukkit.getOnlinePlayers().size(), 4);
        createScore(obj, " &fЛобби: &a#1", 3);

        createScore(obj, "    ", 2);

        // САЙТ
        String footer = LanguageManager.getString(player, "lobby.board.footer");
        createScore(obj, footer, 1);

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

    // --- МЕТОД ГЕНЕРАЦИИ ГРАДИЕНТА (Твоя лаба по Алгоритмам!) ---
    public static String makeGradient(String text, java.awt.Color start, java.awt.Color end, boolean isBold) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            // Вычисляем процент перехода от 0.0 до 1.0
            float ratio = (length > 1) ? (float) i / (length - 1) : 0;

            // Интерполируем RGB
            int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            // Применяем BungeeCord Hex-цвет к символу
            sb.append(net.md_5.bungee.api.ChatColor.of(new java.awt.Color(red, green, blue)));
            if (isBold) sb.append(net.md_5.bungee.api.ChatColor.BOLD);
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }
}