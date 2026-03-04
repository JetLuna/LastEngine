package net.jetluna.api.stream;

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

import java.util.Map;

public class StreamsGui implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "Прямые трансляции");

        Map<String, String> streams = StreamManager.getActiveStreams();

        if (streams.isEmpty()) {
            gui.setItem(13, new ItemBuilder(Material.BARRIER)
                    .setName("&cВ данный момент стримов нет")
                    .setLore("&7Возвращайтесь позже!")
                    .build());
        } else {
            int slot = 0;
            for (Map.Entry<String, String> entry : streams.entrySet()) {
                if (slot >= 27) break;

                String streamerName = entry.getKey();
                String url = entry.getValue();

                // Делаем платформу более понятной по ссылке
                String platform = url.contains("twitch.tv") ? "&dTwitch" : (url.contains("youtube") ? "&cYouTube" : "&fТрансляция");

// Создаем предмет без .setSkullOwner
                org.bukkit.inventory.ItemStack headItem = new ItemBuilder(Material.PLAYER_HEAD)
                        .setName("&b" + streamerName)
                        .setLore(
                                "&7Платформа: " + platform,
                                "",
                                "&eНажмите, чтобы получить",
                                "&eссылку в чат!"
                        ).build();

                // Применяем текстуру головы стандартным API Bukkit
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
        if (!event.getView().getTitle().equals("Прямые трансляции")) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PLAYER_HEAD) return;

        Player player = (Player) event.getWhoClicked();
        String streamerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        String url = StreamManager.getActiveStreams().get(streamerName);

        if (url != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            // Отправляем кликабельную ссылку лично игроку
            TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&8[&bСтримы&8] &fСсылка на стрим &b" + streamerName + "&f: &e&n" + url));
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7Открыть трансляцию"))));

            player.spigot().sendMessage(link);
        }
    }
}