package net.jetluna.api.report;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
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

        // --- КОМАНДА /REPORTS (Открываем всегда 1-ю страницу) ---
        if (label.equalsIgnoreCase("reports")) {
            if (rank.getWeight() >= 7) {
                ReportsGui.open(player, 1);
            } else {
                ChatUtil.sendMessage(player, "&cУ вас нет прав для просмотра жалоб.");
            }
            return true;
        }

        // --- КОМАНДА /REPORT ---
        if (args.length < 2) {
            ChatUtil.sendMessage(player, "&cИспользование: /report <ник> <причина>");
            return true;
        }

        String targetName = args[0];

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        if (player.getName().equalsIgnoreCase(targetName)) {
            ChatUtil.sendMessage(player, "&cВы не можете отправить жалобу на самого себя!");
            return true;
        }

        // !!! НОВОЕ: Проверка на дубликат жалобы от этого же игрока !!!
        boolean alreadyReported = ReportManager.getReportsFor(targetName).stream()
                .anyMatch(r -> r.sender.equalsIgnoreCase(player.getName()));

        if (alreadyReported) {
            ChatUtil.sendMessage(player, "&cВы уже отправили жалобу на игрока &e" + targetName + "&c! Дождитесь, пока администрация её проверит.");
            return true;
        }

        // Добавляем репорт в систему
        ReportManager.addReport(player.getName(), targetName, reason);
        ChatUtil.sendMessage(player, "&aВаша жалоба на игрока &e" + targetName + " &aуспешно отправлена!");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (RankManager.getRank(p).getWeight() >= 7) {
                ChatUtil.sendMessage(p, "&8[&cЖалоба&8] &fОт &e" + player.getName() + " &fна &c" + targetName);
                ChatUtil.sendMessage(p, "&8[&cЖалоба&8] &fПричина: &7" + reason);
            }
        }

        return true;
    }
}