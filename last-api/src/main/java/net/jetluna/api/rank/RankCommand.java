package net.jetluna.api.rank;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            Player p = sender instanceof Player ? (Player) sender : null;
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "rank.no_permission"));
            return true;
        }

        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        if (args.length != 2) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(senderPlayer, "rank.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(senderPlayer, "rank.player_not_found"));
            return true;
        }

        try {
            // 1. Ищем ранг (превращаем текст в Rank Enum)
            Rank rank = Rank.valueOf(args[1].toUpperCase());

            // 2. Выдаем ранг (СТАТИЧЕСКИЙ МЕТОД)
            RankManager.setRank(target, rank);

            // Сообщение отправителю (или консоли)
            String senderMsg = LanguageManager.getString(senderPlayer, "rank.success_sender")
                    .replace("%player%", target.getName())
                    .replace("%rank%", rank.getName());
            ChatUtil.sendMessage(sender, senderMsg);

            // Сообщение игроку, которому выдали ранг
            String targetMsg = LanguageManager.getString(target, "rank.success_target")
                    .replace("%rank%", rank.getPrefix());
            ChatUtil.sendMessage(target, targetMsg);

        } catch (IllegalArgumentException e) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(senderPlayer, "rank.invalid_rank"));
        }

        return true;
    }
}