package net.jetluna.lobby.gui;

import net.jetluna.api.friends.FriendManager;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.LobbyGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.friends_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        List<UUID> friends = FriendManager.getFriends(player.getUniqueId());

        if (friends.isEmpty()) {
            // Если нет друзей 😔
            gui.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.friends_gui.empty.name")))
                    .setLore(colorList(player, "lobby.friends_gui.empty.lore"))
                    .build());
        } else {
            int slot = 0;
            for (UUID friendId : friends) {
                if (slot >= 45) break; // Пока что лимит в 45 друзей на 1 страницу

                OfflinePlayer friend = Bukkit.getOfflinePlayer(friendId);
                boolean isOnline = friend.isOnline();

                String statusLoreKey = isOnline ? "lobby.friends_gui.friend.lore_online" : "lobby.friends_gui.friend.lore_offline";
                List<String> lore = colorList(player, statusLoreKey);

                String friendName = friend.getName() != null ? friend.getName() : "Unknown";

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(friend);
                    meta.setDisplayName(color(LanguageManager.getString(player, "lobby.friends_gui.friend.name").replace("%player%", friendName)));
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                }

                gui.setItem(slot++, head);
            }
        }

        // Кнопка назад
        gui.setItem(49, new ItemBuilder(Material.ARROW)
                .setName(color(LanguageManager.getString(player, "lobby.settings_gui.back")))
                .build());

        player.openInventory(gui);

        // --- НОВАЯ КНОПКА: ЗАПРОСЫ В ДРУЗЬЯ ---
        gui.setItem(53, new ItemBuilder(Material.PAPER)
                .setName(color(LanguageManager.getString(player, "lobby.friends_gui.requests.name")))
                .setLore(colorList(player, "lobby.friends_gui.requests.lore"))
                .build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = color(LanguageManager.getString(player, "lobby.friends_gui.title"));
        if (!ChatUtil.strip(event.getView().getTitle()).equals(ChatUtil.strip(title))) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 49) {
            LobbyGui.openProfile(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (slot == 53 && item.getType() == Material.PAPER) {
            net.jetluna.lobby.gui.FriendRequestsGui.openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        // Удаление друга при клике на голову
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                UUID friendId = meta.getOwningPlayer().getUniqueId();
                String friendName = meta.getOwningPlayer().getName();

                FriendManager.removeFriend(player.getUniqueId(), friendId);

                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "friends.friend_removed").replace("%player%", friendName != null ? friendName : "Unknown")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);

                // Переоткрываем, чтобы голова сразу пропала из меню
                open(player);
            }
        }
    }

    private static String color(String text) {
        return text == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> org.bukkit.ChatColor.translateAlternateColorCodes('&', s));
        return list;
    }
}