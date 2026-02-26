package net.jetluna.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.jetluna.api.punish.PunishmentManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.lobby.gui.SettingsGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LobbyChat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // 1. ПРОВЕРКА МУТА
        if (PunishmentManager.isMuted(player.getName())) {
            event.setCancelled(true);
            // Отправляем сообщение о муте (оно уже красивое в PunishmentManager)
            ChatUtil.sendMessage(player, PunishmentManager.getMuteMessage(player.getName()));
            return;
        }

        event.setCancelled(true);

        // Получаем ранг и префикс
        Rank rank = RankManager.getRank(player);
        String prefix = rank.getPrefix();
        String name = player.getName();
        String message = ChatUtil.serialize(event.message()); // Превращаем Component в текст

        // Формат: [Prefix] Nickname: message
        // Если ранг Player (вес 1) - цвет серый, иначе белый
        String color = (rank.getWeight() == 1) ? "<gray>" : "<white>";

        // Финальное сообщение
        String format = prefix + name + "<dark_gray>: " + color + message;

        // Отправляем всем, кто не скрыл чат
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!SettingsGui.isChatHidden(p)) {
                ChatUtil.sendMessage(p, format);
            }
        }

        // В консоль тоже пишем
        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(format));
    }
}