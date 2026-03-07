package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.LobbyGui;
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

public class CustomizationGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.customization_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 45, title);

        gui.setItem(11, new ItemBuilder(Material.NAME_TAG)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.joiners.name")))
                .setLore(colorList(player, "lobby.customization_gui.joiners.lore"))
                .build());

        gui.setItem(13, new ItemBuilder(Material.OAK_SIGN)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.suffixes.name")))
                .setLore(colorList(player, "lobby.customization_gui.suffixes.lore"))
                .build());

        gui.setItem(15, new ItemBuilder(Material.BLAZE_POWDER)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.effects.name")))
                .setLore(colorList(player, "lobby.customization_gui.effects.lore"))
                .build());

        gui.setItem(20, new ItemBuilder(Material.LEAD)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.pets.name")))
                .setLore(colorList(player, "lobby.customization_gui.pets.lore"))
                .build());

        gui.setItem(22, new ItemBuilder(Material.SLIME_BALL)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.gadgets.name")))
                .setLore(colorList(player, "lobby.customization_gui.gadgets.lore"))
                .build());

        gui.setItem(24, new ItemBuilder(Material.CYAN_BANNER)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.banners.name")))
                .setLore(colorList(player, "lobby.customization_gui.banners.lore"))
                .build());

        gui.setItem(40, new ItemBuilder(Material.ARROW)
                .setName(color(LanguageManager.getString(player, "lobby.customization_gui.back")))
                .build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "lobby.customization_gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 11) {
            JoinerGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 13) {
            SuffixGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 15) {
            net.jetluna.api.effect.EffectsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 20) {
            net.jetluna.api.pet.PetsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 22) {
            net.jetluna.api.gadget.GadgetsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 24) {
            net.jetluna.api.cosmetic.CosmeticGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 40) {
            LobbyGui.openProfile(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
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