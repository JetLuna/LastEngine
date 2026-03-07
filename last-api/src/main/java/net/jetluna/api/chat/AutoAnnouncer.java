package net.jetluna.api.chat;

import net.jetluna.api.lang.LanguageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AutoAnnouncer implements Runnable {

    private int index = 0;

    // Теперь храним только ID секции в конфиге и ссылку
    private static class Announcement {
        String id;
        String url;

        Announcement(String id, String url) {
            this.id = id;
            this.url = url;
        }
    }

    private final List<Announcement> messages = Arrays.asList(
            new Announcement("forum", "https://forum.lastengine.net"),
            new Announcement("telegram", "https://t.me/lastengine"),
            new Announcement("website", "https://www.lastengine.net"),
            new Announcement("store", "https://store.lastengine.net"),
            new Announcement("discord", "https://discord.gg/lastengine")
    );

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        Announcement ann = messages.get(index);
        index = (index + 1) % messages.size();

        String separator = ChatColor.translateAlternateColorCodes('&', "&8================================================");
        String title = ChatColor.translateAlternateColorCodes('&', "  &a&lLAST ENGINE");

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Достаем текст для конкретного игрока
            String linkText = LanguageManager.getString(player, "chat.announcer." + ann.id + ".link");
            String hoverText = LanguageManager.getString(player, "chat.announcer.hover");
            List<String> lines = LanguageManager.getList(player, "chat.announcer." + ann.id + ".text");

            TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&', "  &e&n" + linkText));
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ann.url));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverText))));

            player.sendMessage(separator);
            player.sendMessage(title);
            if (lines != null) {
                for (String line : lines) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "  " + line));
                }
            }
            player.sendMessage("");
            player.spigot().sendMessage(link);
            player.sendMessage(separator);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
        }
    }
}