package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class SuffixGui implements Listener {

    private static final Map<UUID, Set<String>> unlockedSuffixes = new HashMap<>();

    public enum Suffix {
        DEFAULT("titles", 0, Material.BARRIER),
        PRO("titles", 150, Material.DIAMOND_SWORD),
        VETERAN("titles", 500, Material.GOLDEN_APPLE),
        TOXIC("memes", 200, Material.POISONOUS_POTATO),
        CLOWN("memes", 150, Material.SLIME_BALL),
        HOKAGE("anime", 300, Material.BLAZE_POWDER),
        GHOUL("anime", 400, Material.WITHER_SKELETON_SKULL);

        public final String categoryId;
        public final int price;
        public final Material icon;

        Suffix(String categoryId, int price, Material icon) {
            this.categoryId = categoryId;
            this.price = price;
            this.icon = icon;
        }
    }

    public static void openCategories(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.suffix_gui.categories_title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        gui.setItem(11, new ItemBuilder(Material.NAME_TAG).setName(color(LanguageManager.getString(player, "lobby.suffix_gui.cat_titles"))).build());
        gui.setItem(13, new ItemBuilder(Material.SLIME_BALL).setName(color(LanguageManager.getString(player, "lobby.suffix_gui.cat_memes"))).build());
        gui.setItem(15, new ItemBuilder(Material.WITHER_SKELETON_SKULL).setName(color(LanguageManager.getString(player, "lobby.suffix_gui.cat_anime"))).build());
        gui.setItem(22, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.suffix_gui.back"))).build());

        player.openInventory(gui);
    }

    public static void openList(Player player, String categoryId) {
        String catName = color(LanguageManager.getString(player, "lobby.suffix_gui.cat_" + categoryId));
        String title = color(LanguageManager.getString(player, "lobby.suffix_gui.list_title")).replace("%category%", ChatColor.stripColor(catName));
        Inventory gui = Bukkit.createInventory(player, 36, title);
        int slot = 0;

        PlayerStats stats = StatsManager.getStats(player);
        String currentFormat = (stats != null && stats.getSuffix() != null) ? stats.getSuffix() : "";

        String currentSelected = "DEFAULT";
        for (Suffix s : Suffix.values()) {
            String sFormat = color(LanguageManager.getString(player, "lobby.suffix_gui.list." + s.name().toLowerCase() + ".format"));
            if (sFormat.equals(currentFormat)) currentSelected = s.name();
        }

        Set<String> owned = unlockedSuffixes.getOrDefault(player.getUniqueId(), new HashSet<>(Collections.singletonList("DEFAULT")));

        // --- БЕРЕМ КРАСИВЫЙ НИК ---
        String formattedName = NameFormatUtil.getFormattedName(player, RankManager.getRank(player));

        for (Suffix s : Suffix.values()) {
            if (s.categoryId.equals(categoryId)) {
                String suffixName = color(LanguageManager.getString(player, "lobby.suffix_gui.list." + s.name().toLowerCase() + ".name"));
                String rawFormat = color(LanguageManager.getString(player, "lobby.suffix_gui.list." + s.name().toLowerCase() + ".format"));

                ItemBuilder builder = new ItemBuilder(s.icon).setName("&b" + suffixName);
                List<String> lore = new ArrayList<>();

                // ПРЕДПРОСМОТР С КРАСИВЫМ НИКОМ!
                String formatLore = color(LanguageManager.getString(player, "lobby.suffix_gui.format")).replace("%format%", formattedName + rawFormat);
                lore.add(formatLore);
                lore.add("");

                if (currentSelected.equals(s.name())) {
                    lore.add(color(LanguageManager.getString(player, "lobby.suffix_gui.selected")));
                    builder.setGlow(true);
                } else if (owned.contains(s.name())) {
                    lore.add(color(LanguageManager.getString(player, "lobby.suffix_gui.click_to_select")));
                } else {
                    lore.add(color(LanguageManager.getString(player, "lobby.suffix_gui.price")).replace("%price%", String.valueOf(s.price)));
                    lore.add(color(LanguageManager.getString(player, "lobby.suffix_gui.click_to_buy")));
                }

                builder.setLore(lore);
                gui.setItem(slot++, builder.build());
            }
        }

        gui.setItem(31, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.suffix_gui.back"))).build());
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String expCategoriesTitle = color(LanguageManager.getString(player, "lobby.suffix_gui.categories_title"));
        String expListTitleBase = color(LanguageManager.getString(player, "lobby.suffix_gui.list_title"));
        String expListPrefix = expListTitleBase.substring(0, expListTitleBase.indexOf("%category%"));

        if (ChatColor.stripColor(title).equals(ChatColor.stripColor(expCategoriesTitle))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            int slot = event.getSlot();

            if (slot == 11) openList(player, "titles");
            else if (slot == 13) openList(player, "memes");
            else if (slot == 15) openList(player, "anime");
            else if (slot == 22) CustomizationGui.open(player);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (title.startsWith(ChatColor.stripColor(expListPrefix))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            if (event.getSlot() == 31) {
                openCategories(player);
                return;
            }

            Material clickedType = event.getCurrentItem().getType();
            Suffix clickedSuffix = null;
            for (Suffix s : Suffix.values()) {
                if (s.icon == clickedType) {
                    clickedSuffix = s;
                    break;
                }
            }
            if (clickedSuffix == null) return;

            Set<String> owned = unlockedSuffixes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>(Collections.singletonList("DEFAULT")));
            PlayerStats stats = StatsManager.getStats(player);
            if (stats == null) return;

            String rawSuffixName = LanguageManager.getString(player, "lobby.suffix_gui.list." + clickedSuffix.name().toLowerCase() + ".name");
            String suffixFormat = color(LanguageManager.getString(player, "lobby.suffix_gui.list." + clickedSuffix.name().toLowerCase() + ".format"));

            if (owned.contains(clickedSuffix.name())) {
                stats.setSuffix(suffixFormat);
                String msg = color(LanguageManager.getString(player, "lobby.suffix_gui.messages.selected").replace("%name%", rawSuffixName));
                ChatUtil.sendMessage(player, msg);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                openList(player, clickedSuffix.categoryId);
            } else {
                if (stats.getEmeralds() >= clickedSuffix.price) {
                    stats.setEmeralds(stats.getEmeralds() - clickedSuffix.price);
                    owned.add(clickedSuffix.name());

                    String msg = color(LanguageManager.getString(player, "lobby.suffix_gui.messages.purchased").replace("%name%", rawSuffixName));
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    openList(player, clickedSuffix.categoryId);
                } else {
                    String msg = color(LanguageManager.getString(player, "lobby.suffix_gui.messages.not_enough").replace("%price%", String.valueOf(clickedSuffix.price)));
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
        }
    }

    public static String getActiveSuffix(Player player) {
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null || stats.getSuffix() == null) return "";
        return color(stats.getSuffix());
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}