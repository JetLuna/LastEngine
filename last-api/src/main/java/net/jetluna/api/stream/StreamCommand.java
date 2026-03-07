package net.jetluna.api.stream;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StreamCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Rank rank = RankManager.getRank(player);

        if (label.equalsIgnoreCase("streams")) {
            StreamsGui.open(player);
            return true;
        }

        if (args.length == 0) {
            LanguageManager.sendMessage(player, "stream.commands.usage_header");
            if (rank.getWeight() >= 12) LanguageManager.sendMessage(player, "stream.commands.usage_link");
            if (rank.getWeight() >= 6) {
                LanguageManager.sendMessage(player, "stream.commands.usage_add");
                LanguageManager.sendMessage(player, "stream.commands.usage_stop");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("link") && rank.getWeight() >= 12) {
            if (args.length < 3) {
                LanguageManager.sendMessage(player, "stream.commands.link_usage");
                return true;
            }
            String target = args[1];
            String url = args[2];
            StreamManager.linkChannel(target, url);

            String msg = LanguageManager.getString(player, "stream.commands.link_success")
                    .replace("%url%", url)
                    .replace("%player%", target);
            ChatUtil.sendMessage(player, msg);
            return true;
        }

        if (args[0].equalsIgnoreCase("stop") && rank.getWeight() >= 6) {
            if (StreamManager.getActiveStreams().containsKey(player.getName())) {
                StreamManager.removeActiveStream(player.getName());
                LanguageManager.sendMessage(player, "stream.commands.stop_success");
            } else {
                LanguageManager.sendMessage(player, "stream.commands.stop_fail");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("add") && rank.getWeight() >= 6) {
            if (args.length < 2) {
                LanguageManager.sendMessage(player, "stream.commands.add_usage");
                return true;
            }

            Set<String> linkedChannels = StreamManager.getLinkedChannels(player.getName());
            if (linkedChannels == null || linkedChannels.isEmpty()) {
                LanguageManager.sendMessage(player, "stream.commands.add_no_links");
                return true;
            }

            String url = args[1].toLowerCase();

            boolean isValid = false;
            for (String linked : linkedChannels) {
                if (url.contains(linked)) {
                    isValid = true;
                    break;
                }
                if (linked.contains("youtube.com") || linked.contains("youtu.be")) {
                    if (url.contains("youtube.com/live/") || url.contains("youtube.com/watch") || url.contains("youtu.be/")) {
                        isValid = true;
                        break;
                    }
                }
            }

            if (!isValid) {
                LanguageManager.sendMessage(player, "stream.commands.add_invalid");
                return true;
            }

            StreamManager.addActiveStream(player.getName(), args[1]);

            Bukkit.getScheduler().runTaskLaterAsynchronously(LastApi.getInstance(), () -> {
                StreamManager.removeActiveStream(player.getName());
            }, 144000L);

            announceStream(player, args[1]);
            return true;
        }

        return true;
    }

    private void announceStream(Player streamer, String url) {
        String separator = ChatColor.translateAlternateColorCodes('&', "&d================================================");

        for (Player p : Bukkit.getOnlinePlayers()) {
            String title = color(LanguageManager.getString(p, "stream.announcement.title").replace("%streamer%", streamer.getName()));
            String subtitle = color(LanguageManager.getString(p, "stream.announcement.subtitle"));
            String linkText = color(LanguageManager.getString(p, "stream.announcement.link_text"));
            String hoverText = color(LanguageManager.getString(p, "stream.announcement.hover_text"));

            TextComponent link = new TextComponent(linkText);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(hoverText)));

            p.sendMessage(separator);
            p.sendMessage(title);
            p.sendMessage(subtitle);
            p.sendMessage("");
            p.spigot().sendMessage(link);
            p.sendMessage(separator);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.5f);
        }
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}