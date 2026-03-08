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

public class LobbyTab {

    public static void update(Player player) {
        String header = color(LanguageManager.getString(player, "lobby.tab.header"))
                .replace("%player%", player.getName());

        String footer = color(LanguageManager.getString(player, "lobby.tab.footer"))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        Rank rank = RankManager.getRank(player);
        String formattedName = NameFormatUtil.getFormattedName(player, rank);

        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? color(stats.getSuffix()) : "";

        // Устанавливаем красивое имя с префиксом прямо в строчку ТАБ-листа
        player.setPlayerListName(ChatUtil.parseLegacy(formattedName + suffix));

        // ВЕСЬ КОД СО SCOREBOARD BOARD ОТСЮДА УДАЛЕН!
        // Теперь за команды и префиксы над головой отвечает только LobbyBoard.
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}