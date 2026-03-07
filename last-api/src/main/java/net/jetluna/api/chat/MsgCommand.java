package net.jetluna.api.chat;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MsgCommand implements CommandExecutor {

    private static final Map<UUID, UUID> replies = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        Player target = null;
        String message = "";

        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w")) {
            if (args.length < 2) {
                LanguageManager.sendMessage(player, "chat.msg.usage");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                LanguageManager.sendMessage(player, "chat.msg.not_found");
                return true;
            }
            message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        }
        else if (cmd.equals("r") || cmd.equals("reply")) {
            if (args.length < 1) {
                LanguageManager.sendMessage(player, "chat.reply.usage");
                return true;
            }
            if (!replies.containsKey(player.getUniqueId())) {
                LanguageManager.sendMessage(player, "chat.reply.no_target");
                return true;
            }
            target = Bukkit.getPlayer(replies.get(player.getUniqueId()));
            if (target == null) {
                LanguageManager.sendMessage(player, "chat.reply.offline");
                return true;
            }
            message = String.join(" ", args);
        }

        sendPrivateMessage(player, target, message);
        return true;
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        replies.put(sender.getUniqueId(), target.getUniqueId());
        replies.put(target.getUniqueId(), sender.getUniqueId());

        String senderPrefix = toLegacy(RankManager.getPrefix(sender));
        String targetPrefix = toLegacy(RankManager.getPrefix(target));

        PlayerStats senderStats = StatsManager.getStats(sender);
        String senderSuffix = (senderStats != null && senderStats.getSuffix() != null) ? senderStats.getSuffix().replace("&", "§") : "";

        PlayerStats targetStats = StatsManager.getStats(target);
        String targetSuffix = (targetStats != null && targetStats.getSuffix() != null) ? targetStats.getSuffix().replace("&", "§") : "";

        // --- Формат для ОТПРАВИТЕЛЯ (Я -> Игрок) ---
        String senderMe = LanguageManager.getString(sender, "chat.msg.me");
        String senderHover = LanguageManager.getString(sender, "chat.msg.hover_suggest");

        String sText = "&8[" + senderMe + " &8-> " + targetPrefix + target.getName() + targetSuffix + "&8] &f" + message;
        BaseComponent[] senderComp = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', sText));
        for (BaseComponent c : senderComp) {
            c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "));
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', senderHover))));
        }

        // --- Формат для ПОЛУЧАТЕЛЯ (Игрок -> Я) ---
        String targetMe = LanguageManager.getString(target, "chat.msg.me");
        String targetHover = LanguageManager.getString(target, "chat.msg.hover_reply");

        String tText = "&8[" + senderPrefix + sender.getName() + senderSuffix + " &8-> " + targetMe + "&8] &f" + message;
        BaseComponent[] targetComp = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', tText));
        for (BaseComponent c : targetComp) {
            c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "));
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', targetHover))));
        }

        sender.spigot().sendMessage(senderComp);
        target.spigot().sendMessage(targetComp);

        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f);
    }

    private String toLegacy(String text) {
        if (text == null) return "";
        return text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
    }
}