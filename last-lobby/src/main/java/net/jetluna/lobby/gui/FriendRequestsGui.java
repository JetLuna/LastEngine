package net.jetluna.lobby.gui;

import net.jetluna.api.friends.FriendManager;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendRequestsGui implements Listener {

    // Текстуры голов со стрелочками
    private static final String HEAD_INCOMING = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkxMmQ0NWIxYzc4Y2MyMjQ1MjcyM2VlNjZiYTJkMTU3NzdjYzI4ODU2OGQ2YzFiNjJhNTQ1YjI5YzcxODcifX19"; // Зеленая вниз
    private static final String HEAD_OUTGOING = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTk5YWFmMjQ1NmE2MTIyZGU4ZjZiNjI2ODNmMmJjMmVlZDlhYmI4MWZkNWJlYTFiNGMyM2E1ODE1NmI2NjkifX19"; // Красная вверх

    public static void openMain(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.requests_gui.title_main"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        gui.setItem(11, LanguageGui.getHeadItem(HEAD_INCOMING,
                color(LanguageManager.getString(player, "lobby.requests_gui.incoming.name")),
                colorList(player, "lobby.requests_gui.incoming.lore")));

        gui.setItem(15, LanguageGui.getHeadItem(HEAD_OUTGOING,
                color(LanguageManager.getString(player, "lobby.requests_gui.outgoing.name")),
                colorList(player, "lobby.requests_gui.outgoing.lore")));

        gui.setItem(22, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.settings_gui.back"))).build());

        player.openInventory(gui);
    }

    public static void openIncoming(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.requests_gui.title_in"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        List<UUID> incoming = FriendManager.getIncomingRequests(player.getUniqueId());
        fillList(player, gui, incoming, "lobby.requests_gui.item_in");

        gui.setItem(49, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.settings_gui.back"))).build());
        player.openInventory(gui);
    }

    public static void openOutgoing(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.requests_gui.title_out"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        List<UUID> outgoing = FriendManager.getOutgoingRequests(player.getUniqueId());
        fillList(player, gui, outgoing, "lobby.requests_gui.item_out");

        gui.setItem(49, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.settings_gui.back"))).build());
        player.openInventory(gui);
    }

    private static void fillList(Player player, Inventory gui, List<UUID> list, String langKey) {
        if (list.isEmpty()) {
            gui.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.requests_gui.empty.name")))
                    .build());
            return;
        }

        int slot = 0;
        for (UUID uuid : list) {
            if (slot >= 45) break;
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            String name = target.getName() != null ? target.getName() : "Unknown";

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(color(LanguageManager.getString(player, langKey + ".name").replace("%player%", name)));
                meta.setLore(colorList(player, langKey + ".lore"));
                head.setItemMeta(meta);
            }
            gui.setItem(slot++, head);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String mainTitle = color(LanguageManager.getString(player, "lobby.requests_gui.title_main"));
        String inTitle = color(LanguageManager.getString(player, "lobby.requests_gui.title_in"));
        String outTitle = color(LanguageManager.getString(player, "lobby.requests_gui.title_out"));
        String currentTitle = ChatUtil.strip(event.getView().getTitle());

        if (!currentTitle.equals(ChatUtil.strip(mainTitle)) &&
                !currentTitle.equals(ChatUtil.strip(inTitle)) &&
                !currentTitle.equals(ChatUtil.strip(outTitle))) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getSlot();

        // Главное меню
        if (currentTitle.equals(ChatUtil.strip(mainTitle))) {
            if (slot == 11) {
                openIncoming(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 15) {
                openOutgoing(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 22) {
                FriendsGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            return;
        }

        // Кнопка назад в списках
        if (slot == 49) {
            openMain(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        // Клик по голове игрока
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;
            String targetName = meta.getOwningPlayer().getName();
            UUID targetUUID = meta.getOwningPlayer().getUniqueId();

            if (currentTitle.equals(ChatUtil.strip(inTitle))) {
                // Входящие: ЛКМ принять, ПКМ отклонить
                if (event.getClick() == ClickType.LEFT) {
                    player.performCommand("friend accept " + targetName);
                    openIncoming(player); // Обновляем
                } else if (event.getClick() == ClickType.RIGHT) {
                    player.performCommand("friend deny " + targetName);
                    openIncoming(player); // Обновляем
                }
            } else if (currentTitle.equals(ChatUtil.strip(outTitle))) {
                // Исходящие: Клик = отменить запрос
                FriendManager.removeRequest(player.getUniqueId(), targetUUID);
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "friends.request_cancelled").replace("%player%", targetName != null ? targetName : "Unknown")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                openOutgoing(player); // Обновляем
            }
        }
    }

    private static String color(String text) { return text == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', text); }
    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> org.bukkit.ChatColor.translateAlternateColorCodes('&', s));
        return list;
    }
}