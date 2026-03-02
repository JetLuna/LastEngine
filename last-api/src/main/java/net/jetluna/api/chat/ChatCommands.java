package net.jetluna.api.chat;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ChatCommands implements CommandExecutor {

    private final String type; // "sc" или "dc"

    public ChatCommands(String type) {
        this.type = type;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Rank rank = RankManager.getRank(player);

        // --- STAFF CHAT (/sc) ---
        if (type.equals("sc")) {
            // Проверка прав (Junior = 6)
            if (rank.getWeight() < 6) {
                ChatUtil.sendMessage(player, "<red>У вас нет доступа к Staff Chat.");
                return true;
            }
            if (args.length == 0) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /sc <сообщение>");
                return true;
            }

            String message = String.join(" ", args);
            String format = "<dark_gray>[<aqua>SC<dark_gray>] " + rank.getPrefix() + player.getName() + "<dark_gray>: <aqua>" + message;

            // Отправляем всем, у кого вес >= 6
            broadcast(format, 6);
        }

        // --- DONOR CHAT (/dc) ---
        else if (type.equals("dc")) {
            // Проверка прав (GO = 2)
            if (rank.getWeight() < 2) {
                ChatUtil.sendMessage(player, "<red>У вас нет доступа к Donor Chat. Купите привилегию!");
                return true;
            }
            if (args.length == 0) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /dc <сообщение>");
                return true;
            }

            String message = String.join(" ", args);
            // Если пишет стафф в донат чат - показываем их префикс, если донатер - его префикс
            String format = "<dark_gray>[<gold>DC<dark_gray>] " + rank.getPrefix() + player.getName() + "<dark_gray>: <white>" + message;

            // Отправляем всем, у кого вес >= 2
            broadcast(format, 2);
        }

        return true;
    }

    // Рассылка сообщения игрокам с нужным весом ранга
    private void broadcast(String message, int minWeight) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank pRank = RankManager.getRank(p);
            // Видят сообщение только те, у кого достаточный ранг
            if (pRank.getWeight() >= minWeight) {
                ChatUtil.sendMessage(p, message);
            }
        }
        // В консоль тоже пишем для логов
        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(message));
    }
}