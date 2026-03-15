package net.jetluna.api.skin;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
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
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkinHistoryGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "api.skin_history_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 45, title);

        List<String> history = SkinManager.getHistory(player);

        if (history.isEmpty()) {
            gui.setItem(22, new ItemBuilder(Material.GLASS_BOTTLE)
                    .setName(color(LanguageManager.getString(player, "api.skin_history_gui.empty.name")))
                    .setLore(colorList(player, "api.skin_history_gui.empty.lore"))
                    .build());
        } else {
            int slot = 0;
            for (String entry : history) {
                if (slot >= 36) break;

                String[] parts = entry.split(";", 3);
                String skinName = parts[0];
                String textureValue = parts[1];

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatUtil.parseLegacy("&e" + skinName));

                    List<String> lore = colorList(player, "api.skin_history_gui.head.lore");
                    meta.setLore(lore);

                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), skinName);
                    profile.setProperty(new ProfileProperty("textures", textureValue));
                    meta.setPlayerProfile(profile);

                    head.setItemMeta(meta);
                }
                gui.setItem(slot++, head);
            }
        }

        gui.setItem(40, new ItemBuilder(Material.BARRIER).setName(color(LanguageManager.getString(player, "api.skin_history_gui.close"))).build());
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "api.skin_history_gui.title"));
        if (!ChatUtil.strip(event.getView().getTitle()).equals(ChatUtil.strip(expectedTitle))) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 40) {
            player.closeInventory();
            return;
        }

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            String skinName = ChatUtil.strip(event.getCurrentItem().getItemMeta().getDisplayName());

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            player.performCommand("skin " + skinName);
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