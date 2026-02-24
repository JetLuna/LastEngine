package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
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

        createScore(obj, " ", 10);

        createScore(obj, LanguageManager.getString(player, "lobby.board.nickname"), 9);
        createScore(obj, "<white> " + player.getName(), 8);

        createScore(obj, "  ", 7);

        createScore(obj, LanguageManager.getString(player, "lobby.board.rank"), 6);
        createScore(obj, " " + RankManager.getPrefix(player), 5);

        createScore(obj, "   ", 4);

        createScore(obj, LanguageManager.getString(player, "lobby.board.online"), 3);
        createScore(obj, "<white> " + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers(), 2);

        createScore(obj, "    ", 1);

        createScore(obj, LanguageManager.getString(player, "lobby.board.footer"), 0);

        player.setScoreboard(board);
    }

    private static void createScore(Objective obj, String text, int score) {
        Score line = obj.getScore(ChatUtil.parseLegacy(text));
        line.setScore(score);
    }
}