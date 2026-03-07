package net.jetluna.api.stats;

import net.jetluna.api.lang.LanguageManager;
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
        // Определяем, кто отправил команду (игрок или консоль), чтобы получить нужный язык
        Player p = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission("last.admin")) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.no_permission"));
            return true;
        }

        if (args.length < 4) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.player_not_found"));
            return true;
        }

        String type = args[2].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.not_a_number"));
            return true;
        }

        PlayerStats stats = StatsManager.getStats(target);
        if (stats == null) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.stats_not_loaded"));
            return true;
        }

        if (type.equals("coins") || type.equals("coin") || type.equals("c")) {
            if (action.equals("give") || action.equals("add")) stats.setCoins(stats.getCoins() + amount);
            else if (action.equals("set")) stats.setCoins(amount);

            String msg = LanguageManager.getString(p, "eco.success_coins")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", target.getName());
            ChatUtil.sendMessage(sender, msg);
        }
        else if (type.equals("emeralds") || type.equals("emerald") || type.equals("e")) {
            if (action.equals("give") || action.equals("add")) stats.setEmeralds(stats.getEmeralds() + amount);
            else if (action.equals("set")) stats.setEmeralds(amount);

            String msg = LanguageManager.getString(p, "eco.success_emeralds")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", target.getName());
            ChatUtil.sendMessage(sender, msg);
        }
        else {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "eco.unknown_currency"));
        }

        return true;
    }
}