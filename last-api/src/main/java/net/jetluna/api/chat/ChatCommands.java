package net.jetluna.api.chat;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        // Получаем суффикс
        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? stats.getSuffix().replace("&", "§") : "";

        // Очищаем префикс от MiniMessage тегов
        String prefix = toLegacy(rank.getPrefix());

        // --- STAFF CHAT (/sc) ---
        if (type.equals("sc")) {
            if (rank.getWeight() < 6) {
                ChatUtil.sendMessage(player, "&cУ вас нет доступа к Staff Chat.");
                return true;
            }
            if (args.length == 0) {
                ChatUtil.sendMessage(player, "&eИспользование: /sc <сообщение>");
                return true;
            }

            String message = String.join(" ", args);
            // Используем старые цвета: &8 (тёмно-серый), &b (аква)
            String format = "&8[&bSC&8] " + prefix + player.getName() + suffix + "&8: &b" + message;

            broadcast(format, 6);
        }
        // --- DONOR CHAT (/dc) ---
        else if (type.equals("dc")) {
            if (rank.getWeight() < 2) {
                ChatUtil.sendMessage(player, "&cУ вас нет доступа к Donor Chat. Купите привилегию!");
                return true;
            }
            if (args.length == 0) {
                ChatUtil.sendMessage(player, "&eИспользование: /dc <сообщение>");
                return true;
            }

            String message = String.join(" ", args);
            // Используем старые цвета: &8 (тёмно-серый), &6 (золотой), &f (белый)
            String format = "&8[&6DC&8] " + prefix + player.getName() + suffix + "&8: &f" + message;

            broadcast(format, 2);
        }

        return true;
    }

    private void broadcast(String message, int minWeight) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank pRank = RankManager.getRank(p);
            if (pRank.getWeight() >= minWeight) {
                ChatUtil.sendMessage(p, message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(message));
    }

    // Тот самый спасительный метод-переводчик
    private String toLegacy(String text) {
        if (text == null) return "";
        return text
                .replace("<dark_red>", "&4").replace("</dark_red>", "")
                .replace("<red>", "&c").replace("</red>", "")
                .replace("<gold>", "&6").replace("</gold>", "")
                .replace("<yellow>", "&e").replace("</yellow>", "")
                .replace("<dark_green>", "&2").replace("</dark_green>", "")
                .replace("<green>", "&a").replace("</green>", "")
                .replace("<aqua>", "&b").replace("</aqua>", "")
                .replace("<dark_aqua>", "&3").replace("</dark_aqua>", "")
                .replace("<dark_blue>", "&1").replace("</dark_blue>", "")
                .replace("<blue>", "&9").replace("</blue>", "")
                .replace("<light_purple>", "&d").replace("</light_purple>", "")
                .replace("<dark_purple>", "&5").replace("</dark_purple>", "")
                .replace("<white>", "&f").replace("</white>", "")
                .replace("<gray>", "&7").replace("</gray>", "")
                .replace("<dark_gray>", "&8").replace("</dark_gray>", "")
                .replace("<black>", "&0").replace("</black>", "")
                .replace("<bold>", "&l").replace("</bold>", "")
                .replace("<italic>", "&o").replace("</italic>", "")
                .replace("<strikethrough>", "&m").replace("</strikethrough>", "")
                .replace("<underlined>", "&n").replace("</underlined>", "")
                .replace("<obfuscated>", "&k").replace("</obfuscated>", "")
                .replace("<reset>", "&r").replace("</reset>", "")
                .replaceAll("<[^>]+>", "");
    }
}