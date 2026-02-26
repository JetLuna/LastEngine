package net.jetluna.api.punish;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PunishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player admin = (Player) sender;
        Rank adminRank = RankManager.getRank(admin);
        String cmd = label.toLowerCase();

        // --- BANLIST ---
        if (cmd.equals("banlist")) {
            if (adminRank.getWeight() < 7) {
                ChatUtil.sendMessage(admin, "<red>Нет прав!");
                return true;
            }
            Set<String> bans = PunishmentManager.getActiveBans();
            ChatUtil.sendMessage(admin, "<green>Активные баны (" + bans.size() + "):");
            for (String banned : bans) {
                ChatUtil.sendMessage(admin, "<gray>- " + banned);
            }
            return true;
        }

        // --- HISTORY ---
        if (cmd.equals("history")) {
            if (adminRank.getWeight() < 7) {
                ChatUtil.sendMessage(admin, "<red>Нет прав!");
                return true;
            }
            if (args.length < 1) {
                ChatUtil.sendMessage(admin, "<yellow>Использование: /history <ник>");
                return true;
            }
            String target = args[0];

            ChatUtil.sendMessage(admin, "<green>История наказаний " + target + ":");
            ChatUtil.sendMessage(admin, "<red><bold>BANS:");
            for (String line : PunishmentManager.getHistory(target, "BAN")) {
                ChatUtil.sendMessage(admin, line);
            }
            ChatUtil.sendMessage(admin, "<aqua><bold>MUTES:");
            for (String line : PunishmentManager.getHistory(target, "MUTE")) {
                ChatUtil.sendMessage(admin, line);
            }
            return true;
        }

        // --- KICK / MUTE / BAN / UN... ---

        // Права
        if ((cmd.equals("kick") || cmd.equals("mute") || cmd.equals("unmute")) && adminRank.getWeight() < 6) {
            ChatUtil.sendMessage(admin, "<red>Нужен ранг JUNIOR+");
            return true;
        }
        if ((cmd.equals("ban") || cmd.equals("unban")) && adminRank.getWeight() < 8) {
            ChatUtil.sendMessage(admin, "<red>Нужен ранг MODER+");
            return true;
        }

        if (args.length < 1) {
            ChatUtil.sendMessage(admin, "<yellow>Использование: /" + cmd + " <игрок> ...");
            return true;
        }
        String targetName = args[0];

        // Снятие
        if (cmd.equals("unmute")) {
            PunishmentManager.unmute(targetName, admin);
            ChatUtil.sendMessage(admin, "<green>Игрок размучен.");
            return true;
        }
        if (cmd.equals("unban")) {
            PunishmentManager.unban(targetName, admin);
            ChatUtil.sendMessage(admin, "<green>Игрок разбанен.");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target != null && !PunishmentManager.canPunish(admin, target)) {
            ChatUtil.sendMessage(admin, "<red>Нельзя наказать старшего по званию!");
            return true;
        }

        String adminDisplay = RankManager.getPrefix(admin) + admin.getName();

        // KICK
        if (cmd.equals("kick")) {
            if (target == null) {
                ChatUtil.sendMessage(admin, "<red>Игрок не в сети!");
                return true;
            }
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Без причины";
            PunishmentManager.kick(target, adminDisplay, reason);
            return true;
        }

        // MUTE / BAN
        if (args.length < 2) {
            ChatUtil.sendMessage(admin, "<yellow>Укажите время! (1h, 1d)");
            return true;
        }
        long time = TimeUtil.parseDuration(args[1]);
        if (time == 0) {
            ChatUtil.sendMessage(admin, "<red>Неверное время!");
            return true;
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Нарушение";

        if (cmd.equals("mute")) {
            PunishmentManager.mute(targetName, adminDisplay, time, reason);
        } else {
            PunishmentManager.ban(targetName, adminDisplay, time, reason);
        }

        return true;
    }
}