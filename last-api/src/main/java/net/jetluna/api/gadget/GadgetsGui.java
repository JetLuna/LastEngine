package net.jetluna.api.gadget;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GadgetsGui implements Listener {

    public static void open(Player player) {
        String title = toLegacy(LanguageManager.getString(player, "gadgets.gui.title"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 54; i++) gui.setItem(i, filler);

        int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };
        GadgetType[] gadgets = GadgetType.values();

        for (int i = 0; i < gadgets.length; i++) {
            if (i >= slots.length) break;
            GadgetType type = gadgets[i];

            boolean isActive = stats.getActiveGadget() != null && stats.getActiveGadget().equals(type.name());
            boolean isOwned = stats.getOwnedGadgets() != null && stats.getOwnedGadgets().contains(type.name());

            ItemBuilder builder = new ItemBuilder(type.getIcon());
            String gadgetName = toLegacy(LanguageManager.getString(player, "gadgets.list." + type.name().toLowerCase()));

            if (isActive) {
                builder.setName(toLegacy(LanguageManager.getString(player, "gadgets.status.active_name")).replace("%gadget%", gadgetName));
                builder.setLore(getLegacyList(player, "gadgets.status.active_lore")).setGlow(true);
            } else if (isOwned) {
                builder.setName(toLegacy(LanguageManager.getString(player, "gadgets.status.owned_name")).replace("%gadget%", gadgetName));
                builder.setLore(getLegacyList(player, "gadgets.status.owned_lore"));
            } else {
                builder.setName(toLegacy(LanguageManager.getString(player, "gadgets.status.buy_name")).replace("%gadget%", gadgetName));
                List<String> lore = getLegacyList(player, "gadgets.status.buy_lore");
                lore.replaceAll(s -> s.replace("%price%", String.valueOf(type.getPrice())));
                builder.setLore(lore);
            }
            gui.setItem(slots[i], builder.build());
        }

        String removeName = toLegacy(LanguageManager.getString(player, "gadgets.gui.remove"));
        List<String> removeLore = getLegacyList(player, "gadgets.gui.remove_lore");
        gui.setItem(48, new ItemBuilder(Material.BARRIER).setName(removeName).setLore(removeLore).build());

        String closeName = toLegacy(LanguageManager.getString(player, "gadgets.gui.close"));
        List<String> closeLore = getLegacyList(player, "gadgets.gui.close_lore");
        gui.setItem(49, new ItemBuilder(Material.ARROW).setName(closeName).setLore(closeLore).build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = toLegacy(LanguageManager.getString(player, "gadgets.gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        int slot = event.getSlot();

        if (slot == 49) {
            player.closeInventory();
            player.performCommand("profile");
            return;
        }

        if (slot == 48) {
            stats.setActiveGadget("");
            GadgetManager.remove(player);
            String msg = toLegacy(LanguageManager.getString(player, "gadgets.messages.removed"));
            ChatUtil.sendMessage(player, msg);
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
            return;
        }

        for (GadgetType type : GadgetType.values()) {
            // Для проверки клика теперь сверяем локализованное имя
            String gadgetName = toLegacy(LanguageManager.getString(player, "gadgets.list." + type.name().toLowerCase()));

            if (item.getType() == type.getIcon() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains(ChatColor.stripColor(gadgetName))) {
                boolean isOwned = stats.getOwnedGadgets() != null && stats.getOwnedGadgets().contains(type.name());

                if (isOwned) {
                    stats.setActiveGadget(type.name());
                    GadgetManager.equip(player, type);

                    String msg = toLegacy(LanguageManager.getString(player, "gadgets.messages.equipped")).replace("%gadget%", gadgetName);
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1.5f);
                } else {
                    if (stats.getCoins() >= type.getPrice()) {
                        stats.setCoins(stats.getCoins() - type.getPrice());
                        stats.getOwnedGadgets().add(type.name());
                        stats.setActiveGadget(type.name());
                        GadgetManager.equip(player, type);

                        String msg = toLegacy(LanguageManager.getString(player, "gadgets.messages.purchased")).replace("%gadget%", gadgetName);
                        ChatUtil.sendMessage(player, msg);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        String msg = toLegacy(LanguageManager.getString(player, "gadgets.messages.not_enough_coins"));
                        ChatUtil.sendMessage(player, msg);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }
                player.closeInventory();
                break;
            }
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    private static List<String> getLegacyList(Player player, String key) {
        List<String> list = LanguageManager.getList(player, key);
        List<String> legacyList = new ArrayList<>();
        if (list != null) {
            for (String s : list) {
                legacyList.add(toLegacy(s));
            }
        }
        return legacyList;
    }

    private static String toLegacy(String text) {
        if (text == null) return "";
        String legacy = text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }
}