package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.gui.LobbyGui; // Импорт
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SettingsGui implements Listener {

    // Храним UUID тех, кто скрыл чат
    private static final Set<UUID> chatHidden = new HashSet<>();

    public static void open(Player player) {
        String title = LanguageManager.getString(player, "lobby.settings_gui.title");
        Inventory gui = Bukkit.createInventory(player, 27, title);

        boolean playersVisible = !player.hasMetadata("visibility_hidden");

        ItemStack visibilityItem = new ItemBuilder(Material.LIME_DYE)
                .setName(LanguageManager.getString(player, "lobby.settings_gui.visibility.name"))
                .setLore(LanguageManager.getList(player, playersVisible ? "lobby.settings_gui.visibility.lore_on" : "lobby.settings_gui.visibility.lore_off"))
                .setType(playersVisible ? Material.LIME_DYE : Material.GRAY_DYE)
                .build();
        gui.setItem(11, visibilityItem);

        boolean chatVisible = !chatHidden.contains(player.getUniqueId());
        ItemStack chatItem = new ItemBuilder(Material.PAPER)
                .setName(LanguageManager.getString(player, "lobby.settings_gui.chat.name"))
                .setLore(LanguageManager.getList(player, chatVisible ? "lobby.settings_gui.chat.lore_on" : "lobby.settings_gui.chat.lore_off"))
                .setGlow(chatVisible)
                .build();
        gui.setItem(15, chatItem);

        ItemStack back = new ItemBuilder(Material.ARROW)
                .setName(LanguageManager.getString(player, "lobby.settings_gui.back.name"))
                .build();
        gui.setItem(22, back);

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = ChatUtil.strip(event.getView().getTitle());
        String guiTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.settings_gui.title"));

        if (!title.equals(guiTitle)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        if (event.getSlot() == 11) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.sendMessage("§cФункция видимости в разработке...");
        }

        if (event.getSlot() == 15) {
            if (chatHidden.contains(player.getUniqueId())) {
                chatHidden.remove(player.getUniqueId());
            } else {
                chatHidden.add(player.getUniqueId());
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        }

        if (event.getSlot() == 22) {
            LobbyGui.openProfile(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }

    // Вот этот метод нужен для LobbyChat!
    public static boolean isChatHidden(Player player) {
        return chatHidden.contains(player.getUniqueId());
    }
}