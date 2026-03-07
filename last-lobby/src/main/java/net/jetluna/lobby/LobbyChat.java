package net.jetluna.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.jetluna.api.punish.PunishmentManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.lobby.gui.SettingsGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LobbyChat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (PunishmentManager.isMuted(player.getName())) {
            event.setCancelled(true);
            ChatUtil.sendMessage(player, PunishmentManager.getMuteMessage(player.getName()));
            return;
        }

        event.setCancelled(true);

        Rank rank = RankManager.getRank(player);

        // Используем локальный метод color() вместо удаленного toLegacy
        String prefix = color(rank.getPrefix());
        String name = player.getName();
        String message = ChatUtil.serialize(event.message());

        // Получаем суффикс из ГЛОБАЛЬНОЙ статистики
        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? color(stats.getSuffix()) : "";

        // Используем старые коды цвета (&7 и &f) вместо тегов
        String nameColor = (rank.getWeight() == 1) ? "&7" : "&f";

        // Формат: [Prefix] Nickname [Suffix]: message
        String format = prefix + nameColor + name + suffix + "&8: &f" + message;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!SettingsGui.isChatHidden(p)) {
                ChatUtil.sendMessage(p, format);
            }
        }

        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(format));
    }

    // Добавляем наш новый стандартный метод парсинга амперсандов
    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}