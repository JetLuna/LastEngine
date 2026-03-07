package net.jetluna.api.stream;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamsGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "stream.gui.title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        Map<String, String> streams = StreamManager.getActiveStreams();

        if (streams.isEmpty()) {
            gui.setItem(13, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "stream.gui.empty_name")))
                    .setLore(colorList(player, "stream.gui.empty_lore"))
                    .build());
        } else {
            int slot = 0;
            for (Map.Entry<String, String> entry : streams.entrySet()) {
                if (slot >= 27) break;

                String streamerName = entry.getKey();
                String url = entry.getValue();

                String platformType = url.contains("twitch.tv") ? "twitch" : (url.contains("youtube") ? "youtube" : "other");
                String platformStr = color(LanguageManager.getString(player, "stream.gui.platforms." + platformType));

                List<String> lore = colorList(player, "stream.gui.item_lore");
                lore.replaceAll(s -> s.replace("%platform%", platformStr));

                org.bukkit.inventory.ItemStack headItem = new ItemBuilder(Material.PLAYER_HEAD)
                        .setName("&b" + streamerName)
                        .setLore(lore).build();

                org.bukkit.inventory.meta.ItemMeta meta = headItem.getItemMeta();
                if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                    ((org.bukkit.inventory.meta.SkullMeta) meta).setOwner(streamerName);
                    headItem.setItemMeta(meta);
                }

                gui.setItem(slot++, headItem);
            }
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String expectedTitle = color(LanguageManager.getString(player, "stream.gui.title"));

        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PLAYER_HEAD) return;

        String streamerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        String url = StreamManager.getActiveStreams().get(streamerName);

        if (url != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            String chatLinkRaw = color(LanguageManager.getString(player, "stream.gui.chat_link")
                    .replace("%streamer%", streamerName)
                    .replace("%url%", url));

            String hoverText = color(LanguageManager.getString(player, "stream.announcement.hover_text"));

            TextComponent link = new TextComponent(chatLinkRaw);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));

            player.spigot().sendMessage(link);
        }
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        return list;
    }
}