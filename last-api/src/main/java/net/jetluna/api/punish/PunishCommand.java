package net.jetluna.api.punish;

import net.jetluna.api.lang.LanguageManager;
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
                LanguageManager.sendMessage(admin, "punish.commands.no_permission");
                return true;
            }
            Set<String> bans = PunishmentManager.getActiveBans();
            String header = LanguageManager.getString(admin, "punish.commands.active_bans_header").replace("%count%", String.valueOf(bans.size()));
            ChatUtil.sendMessage(admin, header);

            for (String banned : bans) {
                String format = LanguageManager.getString(admin, "punish.commands.active_bans_format").replace("%player%", banned);
                ChatUtil.sendMessage(admin, format);
            }
            return true;
        }

        // --- HISTORY ---
        if (cmd.equals("history")) {
            if (adminRank.getWeight() < 7) {
                LanguageManager.sendMessage(admin, "punish.commands.no_permission");
                return true;
            }
            if (args.length < 1) {
                LanguageManager.sendMessage(admin, "punish.commands.usage_history");
                return true;
            }
            String target = args[0];

            String header = LanguageManager.getString(admin, "punish.commands.history_header").replace("%player%", target);
            ChatUtil.sendMessage(admin, header);

            LanguageManager.sendMessage(admin, "punish.commands.history_bans");
            for (String line : PunishmentManager.getHistory(target, "BAN")) {
                ChatUtil.sendMessage(admin, line);
            }

            LanguageManager.sendMessage(admin, "punish.commands.history_mutes");
            for (String line : PunishmentManager.getHistory(target, "MUTE")) {
                ChatUtil.sendMessage(admin, line);
            }
            return true;
        }

        // --- KICK / MUTE / BAN / UN... ---

        if ((cmd.equals("kick") || cmd.equals("mute") || cmd.equals("unmute")) && adminRank.getWeight() < 7) {
            LanguageManager.sendMessage(admin, "punish.commands.rank_junior");
            return true;
        }
        if ((cmd.equals("ban") || cmd.equals("unban")) && adminRank.getWeight() < 11) {
            LanguageManager.sendMessage(admin, "punish.commands.rank_moder");
            return true;
        }

        if (args.length < 1) {
            String usage = LanguageManager.getString(admin, "punish.commands.usage_punish").replace("%cmd%", cmd);
            ChatUtil.sendMessage(admin, usage);
            return true;
        }
        String targetName = args[0];

        // Снятие
        if (cmd.equals("unmute")) {
            PunishmentManager.unmute(targetName, admin);
            LanguageManager.sendMessage(admin, "punish.commands.unmuted");
            return true;
        }
        if (cmd.equals("unban")) {
            PunishmentManager.unban(targetName, admin);
            LanguageManager.sendMessage(admin, "punish.commands.unbanned");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target != null && !PunishmentManager.canPunish(admin, target)) {
            LanguageManager.sendMessage(admin, "punish.commands.cannot_punish_higher");
            return true;
        }

        String adminDisplay = net.jetluna.api.util.NameFormatUtil.getFormattedName(admin, adminRank);

        // KICK
        if (cmd.equals("kick")) {
            if (target == null) {
                LanguageManager.sendMessage(admin, "punish.commands.player_offline");
                return true;
            }
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Без причины";
            PunishmentManager.kick(target, adminDisplay, reason);
            return true;
        }

        // MUTE / BAN
        if (args.length < 2) {
            LanguageManager.sendMessage(admin, "punish.commands.usage_time");
            return true;
        }
        long time = TimeUtil.parseDuration(args[1]);
        if (time == 0) {
            LanguageManager.sendMessage(admin, "punish.commands.invalid_time");
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