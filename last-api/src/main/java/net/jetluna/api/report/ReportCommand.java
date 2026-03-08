package net.jetluna.api.report;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Rank rank = RankManager.getRank(player);

        if (label.equalsIgnoreCase("reports")) {
            if (rank.getWeight() >= 7) {
                ReportsGui.open(player, 1);
            } else {
                LanguageManager.sendMessage(player, "report.commands.no_permission");
            }
            return true;
        }

        if (args.length < 2) {
            LanguageManager.sendMessage(player, "report.commands.usage");
            return true;
        }

        String targetName = args[0];

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        if (player.getName().equalsIgnoreCase(targetName)) {
            LanguageManager.sendMessage(player, "report.commands.self_report");
            return true;
        }

        boolean alreadyReported = ReportManager.getReportsFor(targetName).stream()
                .anyMatch(r -> r.sender.equalsIgnoreCase(player.getName()));

        if (alreadyReported) {
            String msg = LanguageManager.getString(player, "report.commands.already_reported").replace("%player%", targetName);
            ChatUtil.sendMessage(player, msg);
            return true;
        }

        ReportManager.addReport(player.getName(), targetName, reason);

        // --- БЕРЕМ КРАСИВЫЕ НИКИ ---
        String formattedSender = NameFormatUtil.getFormattedName(player, rank);
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        // Если нарушитель онлайн - берем красивый ник, если нет - оставляем обычный
        String formattedTarget = targetPlayer != null ? NameFormatUtil.getFormattedName(targetPlayer, RankManager.getRank(targetPlayer)) : targetName;

        String successMsg = LanguageManager.getString(player, "report.commands.success").replace("%player%", formattedTarget);
        ChatUtil.sendMessage(player, successMsg);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (RankManager.getRank(p).getWeight() >= 7) {
                String alert1 = LanguageManager.getString(p, "report.commands.staff_alert_1")
                        .replace("%sender%", formattedSender)
                        .replace("%target%", formattedTarget);
                String alert2 = LanguageManager.getString(p, "report.commands.staff_alert_2")
                        .replace("%reason%", reason);

                ChatUtil.sendMessage(p, alert1);
                ChatUtil.sendMessage(p, alert2);
            }
        }

        return true;
    }
}