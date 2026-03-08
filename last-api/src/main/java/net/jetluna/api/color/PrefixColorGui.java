package net.jetluna.api.color;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.api.util.PrefixColorManager;
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

public class PrefixColorGui implements Listener {

    public enum PColor {
        DARK_RED("dark_red", "&4", Material.RED_SHULKER_BOX, 12),
        GOLD("gold", "&6", Material.ORANGE_SHULKER_BOX, 13),
        YELLOW("yellow", "&e", Material.YELLOW_SHULKER_BOX, 14),

        GREEN("green", "&a", Material.LIME_SHULKER_BOX, 21),
        BLUE("blue", "&9", Material.BLUE_SHULKER_BOX, 22),
        AQUA("aqua", "&b", Material.LIGHT_BLUE_SHULKER_BOX, 23),

        DARK_PURPLE("dark_purple", "&5", Material.PURPLE_SHULKER_BOX, 30),
        LIGHT_PURPLE("light_purple", "&d", Material.PINK_SHULKER_BOX, 31),
        WHITE("white", "&f", Material.WHITE_SHULKER_BOX, 32);

        public final String langKey;
        public final String code;
        public final Material material;
        public final int slot;

        PColor(String langKey, String code, Material material, int slot) {
            this.langKey = langKey;
            this.code = code;
            this.material = material;
            this.slot = slot;
        }
    }

    public static void open(Player player) {
        Rank rank = RankManager.getRank(player);

        if (rank != Rank.MAX && rank != Rank.ADMIN && rank != Rank.DEV) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "color_gui.no_permission")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String title = color(LanguageManager.getString(player, "color_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        String currentColorCode = PrefixColorManager.getPlayerColor(player.getUniqueId());
        if (currentColorCode == null) currentColorCode = "&7";

        String exampleText = color(LanguageManager.getString(player, "color_gui.example_text"));
        String statusSelected = color(LanguageManager.getString(player, "color_gui.status_selected"));
        String statusUnselected = color(LanguageManager.getString(player, "color_gui.status_unselected"));

        String cleanPrefix = ChatColor.stripColor(rank.getPrefix()).trim();

        for (PColor pColor : PColor.values()) {
            boolean isSelected = pColor.code.equals(currentColorCode);
            String rawColorName = LanguageManager.getString(player, "color_gui.colors." + pColor.langKey);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(exampleText);

            String preview = color(LanguageManager.getString(player, "color_gui.preview_format"))
                    .replace("%color%", color(pColor.code))
                    .replace("%prefix%", cleanPrefix)
                    .replace("%player%", player.getName());

            lore.add(preview);
            lore.add("");

            if (isSelected) {
                lore.add(statusSelected);
            } else {
                lore.add(statusUnselected);
            }

            ItemBuilder builder = new ItemBuilder(pColor.material)
                    .setName(color(pColor.code + "§l" + rawColorName))
                    .setLore(lore);

            if (isSelected) builder.setGlow(true);

            gui.setItem(pColor.slot, builder.build());
        }

        gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .setName(color(LanguageManager.getString(player, "color_gui.reset_name")))
                .setLore(colorList(player, "color_gui.reset_lore"))
                .build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "color_gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 49) {
            PrefixColorManager.removePlayerColor(player.getUniqueId());
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "color_gui.success_reset")));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            player.closeInventory();

            // ОБНОВЛЯЕМ НИКИ НАД ГОЛОВОЙ!
            net.jetluna.api.util.NameFormatUtil.refreshNameTags();
            return;
        }

        for (PColor pColor : PColor.values()) {
            if (pColor.slot == slot) {
                // Сохраняем цвет в базу
                PrefixColorManager.setPlayerColor(player.getUniqueId(), pColor.code);

                String rawColorName = LanguageManager.getString(player, "color_gui.colors." + pColor.langKey);
                String msg = color(LanguageManager.getString(player, "color_gui.success_change"))
                        .replace("%color%", color(pColor.code) + rawColorName);

                // === ВОТ ТВОЙ КУСОК КОДА ===
                ChatUtil.sendMessage(player, msg);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);

                // ОБНОВЛЯЕМ НИКИ НАД ГОЛОВОЙ!
                net.jetluna.api.util.NameFormatUtil.refreshNameTags();

                open(player);
                return;
            }
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