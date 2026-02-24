package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyTab {

    public static void update(Player player) {
        // Получаем текст
        String header = LanguageManager.getString(player, "lobby.tab.header");
        String footer = LanguageManager.getString(player, "lobby.tab.footer");

        // Заменяем переменные
        header = header.replace("%player%", player.getName());
        footer = footer.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        // Имя в списке
        String tabName = RankManager.getPrefix(player) + " <white>" + player.getName();
        player.playerListName(ChatUtil.parse(tabName));
    }
}