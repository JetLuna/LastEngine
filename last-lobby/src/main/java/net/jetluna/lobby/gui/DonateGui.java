package net.jetluna.lobby.gui;

import net.jetluna.api.color.PrefixColorGui;
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

public class DonateGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.donate_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        // --- Вкладка: Смена цвета префикса ---
        gui.setItem(13, new ItemBuilder(Material.NAME_TAG)
                .setName(color(LanguageManager.getString(player, "lobby.donate_gui.prefix_color.name")))
                .setLore(colorList(player, "lobby.donate_gui.prefix_color.lore"))
                .build());

        // Кнопка назад (в профиль)
        gui.setItem(22, new ItemBuilder(Material.ARROW)
                .setName(color(LanguageManager.getString(player, "lobby.donate_gui.back")))
                .build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "lobby.donate_gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getSlot();

        // Открываем меню выбора цвета из API
        if (slot == 13) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            PrefixColorGui.open(player);
        }
        // Возвращаемся в профиль
        else if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            LobbyGui.openProfile(player);
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