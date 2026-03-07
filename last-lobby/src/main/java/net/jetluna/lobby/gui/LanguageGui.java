package net.jetluna.lobby.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class LanguageGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.language_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        // Флаг России (RU)
        gui.setItem(11, getHeadItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZlYWZlZjk4MGQ2MTE3ZGFiZTg5ODJhYzRiNDUwOTg4N2UyYzQ2MjFmNmE4ZmU1YzliNzM1YTgzZDc3NWFkIn19fQ==",
                color(LanguageManager.getString(player, "lobby.language_gui.ru.name")),
                colorList(player, "lobby.language_gui.ru.lore")
        ));

        // Флаг США/Британии (EN)
        gui.setItem(13, getHeadItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhYzk3NzRkYTEyMTcyNDg1MzJjZTE0N2Y3ODMxZjY3YTEyZmRjY2ExY2YwY2I0YjM4NDhkZTZiYzk0YjQifX19",
                color(LanguageManager.getString(player, "lobby.language_gui.en.name")),
                colorList(player, "lobby.language_gui.en.lore")
        ));

        // Флаг Украины (UA)
        gui.setItem(15, getHeadItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjhiOWY1MmUzNmFhNWM3Y2FhYTFlN2YyNmVhOTdlMjhmNjM1ZThlYWM5YWVmNzRjZWM5N2Y0NjVmNWE2YjUxIn19fQ==",
                color(LanguageManager.getString(player, "lobby.language_gui.ua.name")),
                colorList(player, "lobby.language_gui.ua.lore")
        ));

        player.openInventory(gui);
    }

    // Метод для создания голов по Base64 текстуре
    public static ItemStack getHeadItem(String base64, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", base64));
                meta.setPlayerProfile(profile);
            } catch (Exception ignored) {}
            head.setItemMeta(meta);
        }
        return head;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String currentTitle = ChatUtil.strip(event.getView().getTitle());
        String expectedTitle = ChatUtil.strip(color(LanguageManager.getString(player, "lobby.language_gui.title")));

        if (!currentTitle.equals(expectedTitle)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // Форсируем выполнение команды от лица игрока
        if (slot == 11) {
            player.chat("/lang ru");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.closeInventory();
        } else if (slot == 13) {
            player.chat("/lang en");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.closeInventory();
        } else if (slot == 15) {
            player.chat("/lang ua");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.closeInventory();
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