package net.jetluna.api.friends;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FriendCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length < 2) {
            LanguageManager.sendMessage(player, "friends.usage");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            LanguageManager.sendMessage(player, "friends.player_not_found");
            return true;
        }

        if (player.equals(target)) {
            LanguageManager.sendMessage(player, "friends.cannot_interact_self");
            return true;
        }

        String targetName = NameFormatUtil.getFormattedName(target, RankManager.getRank(target));
        String playerName = NameFormatUtil.getFormattedName(player, RankManager.getRank(player));

        switch (action) {
            case "add":
                if (FriendManager.isFriend(player.getUniqueId(), target.getUniqueId())) {
                    LanguageManager.sendMessage(player, "friends.already_friends");
                    return true;
                }
                if (FriendManager.hasRequest(target.getUniqueId(), player.getUniqueId())) {
                    LanguageManager.sendMessage(player, "friends.request_already_sent");
                    return true;
                }

                FriendManager.sendRequest(player.getUniqueId(), target.getUniqueId());
                ChatUtil.sendMessage(player, LanguageManager.getString(player, "friends.request_sent").replace("%player%", targetName));
                ChatUtil.sendMessage(target, LanguageManager.getString(target, "friends.request_received").replace("%player%", playerName));
                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;

            case "accept":
                if (!FriendManager.hasRequest(player.getUniqueId(), target.getUniqueId())) {
                    LanguageManager.sendMessage(player, "friends.no_request");
                    return true;
                }

                FriendManager.removeRequest(target.getUniqueId(), player.getUniqueId());
                FriendManager.addFriend(player.getUniqueId(), target.getUniqueId());

                ChatUtil.sendMessage(player, LanguageManager.getString(player, "friends.request_accepted").replace("%player%", targetName));
                ChatUtil.sendMessage(target, LanguageManager.getString(target, "friends.request_accepted_sender").replace("%player%", playerName));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                break;

            case "deny":
                if (!FriendManager.hasRequest(player.getUniqueId(), target.getUniqueId())) {
                    LanguageManager.sendMessage(player, "friends.no_request");
                    return true;
                }

                FriendManager.removeRequest(target.getUniqueId(), player.getUniqueId());
                ChatUtil.sendMessage(player, LanguageManager.getString(player, "friends.request_denied").replace("%player%", targetName));
                ChatUtil.sendMessage(target, LanguageManager.getString(target, "friends.request_denied_sender").replace("%player%", playerName));
                break;

            case "remove":
                if (!FriendManager.isFriend(player.getUniqueId(), target.getUniqueId())) {
                    LanguageManager.sendMessage(player, "friends.not_friends");
                    return true;
                }

                FriendManager.removeFriend(player.getUniqueId(), target.getUniqueId());
                ChatUtil.sendMessage(player, LanguageManager.getString(player, "friends.friend_removed").replace("%player%", targetName));
                ChatUtil.sendMessage(target, LanguageManager.getString(target, "friends.friend_removed_target").replace("%player%", playerName));
                break;

            default:
                LanguageManager.sendMessage(player, "friends.usage");
                break;
        }

        return true;
    }
}