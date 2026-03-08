package net.jetluna.lobby;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class LobbyChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Rank rank = RankManager.getRank(player);
        String displayName = NameFormatUtil.getFormattedName(player, rank);
        event.setFormat(displayName + " §8» §f%2$s");
    }
}