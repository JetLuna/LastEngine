package net.jetluna.api.rank;

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
            ChatUtil.sendMessage(sender, "<red>Нет прав!");
            return true;
        }

        if (args.length != 2) {
            ChatUtil.sendMessage(sender, "<yellow>Использование: /setrank <ник> <RANK>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendMessage(sender, "<red>Игрок не найден!");
            return true;
        }

        try {
            // 1. Ищем ранг (превращаем текст в Rank Enum)
            Rank rank = Rank.valueOf(args[1].toUpperCase());

            // 2. Выдаем ранг (СТАТИЧЕСКИЙ МЕТОД)
            RankManager.setRank(target, rank);

            ChatUtil.sendMessage(sender, "<green>Игроку " + target.getName() + " выдан ранг " + rank.getName());
            ChatUtil.sendMessage(target, "<green>Вам выдан ранг " + rank.getPrefix());

        } catch (IllegalArgumentException e) {
            ChatUtil.sendMessage(sender, "<red>Такого ранга нет! (PLAYER, ADMIN, DEV...)");
        }

        return true;
    }
}