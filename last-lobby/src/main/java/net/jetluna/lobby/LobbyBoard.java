package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class LobbyBoard {

    public static void update(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // Берем заголовок из языка
        String title = LanguageManager.getString(player, "lobby.board.title");
        Objective obj = board.registerNewObjective("LastLobby", Criteria.DUMMY, ChatUtil.parseLegacy(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Отступ
        createScore(obj, " ", 10);

        // НИКНЕЙМ
        createScore(obj, LanguageManager.getString(player, "lobby.board.nickname"), 9);
        createScore(obj, "<white> " + player.getName(), 8);

        createScore(obj, "  ", 7);

        // РАНГ (С ТВОЕЙ ЛОГИКОЙ)
        createScore(obj, LanguageManager.getString(player, "lobby.board.rank"), 6);

        // 1. Получаем объект ранга
        Rank rank = RankManager.getRank(player);
        String rankDisplay;

        // 2. Проверяем вес (lombok уже создал метод getWeight())
        if (rank.getWeight() == 1) {
            // Если это обычный игрок - пишем просто текст
            rankDisplay = "<gray>Player";
        } else {
            // Если донатер или админ - пишем префикс
            rankDisplay = rank.getPrefix();
        }

        createScore(obj, " " + rankDisplay, 5);

        createScore(obj, "   ", 4);

        // ОНЛАЙН
        createScore(obj, LanguageManager.getString(player, "lobby.board.online"), 3);
        createScore(obj, "<white> " + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers(), 2);

        createScore(obj, "    ", 1);

        // САЙТ
        createScore(obj, LanguageManager.getString(player, "lobby.board.footer"), 0);

        player.setScoreboard(board);
    }

    private static void createScore(Objective obj, String text, int score) {
        // ChatUtil.parseLegacy нужен, так как скорборды любят старые цвета (§)
        Score line = obj.getScore(ChatUtil.parseLegacy(text));
        line.setScore(score);
    }
}