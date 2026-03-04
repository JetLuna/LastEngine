package net.jetluna.api.stats;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("last.admin")) {
            ChatUtil.sendMessage(sender, "&cУ вас нет прав!");
            return true;
        }

        if (args.length < 4) {
            ChatUtil.sendMessage(sender, "&eИспользование: /eco <give|set> <игрок> <coins|emeralds> <сумма>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            ChatUtil.sendMessage(sender, "&cИгрок не найден.");
            return true;
        }

        String type = args[2].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            ChatUtil.sendMessage(sender, "&cСумма должна быть числом.");
            return true;
        }

        PlayerStats stats = StatsManager.getStats(target);
        if (stats == null) {
            ChatUtil.sendMessage(sender, "&cСтатистика игрока не загружена.");
            return true;
        }

        if (type.equals("coins") || type.equals("coin") || type.equals("c")) {
            if (action.equals("give") || action.equals("add")) stats.setCoins(stats.getCoins() + amount);
            else if (action.equals("set")) stats.setCoins(amount);
            ChatUtil.sendMessage(sender, "&aВыдано &e" + amount + " &aмонет игроку &b" + target.getName());
        }
        else if (type.equals("emeralds") || type.equals("emerald") || type.equals("e")) {
            if (action.equals("give") || action.equals("add")) stats.setEmeralds(stats.getEmeralds() + amount);
            else if (action.equals("set")) stats.setEmeralds(amount);
            ChatUtil.sendMessage(sender, "&aВыдано &a" + amount + " &aизумрудов игроку &b" + target.getName());
        }
        else {
            ChatUtil.sendMessage(sender, "&cНеизвестная валюта. Используйте coins или emeralds.");
        }

        return true;
    }
}