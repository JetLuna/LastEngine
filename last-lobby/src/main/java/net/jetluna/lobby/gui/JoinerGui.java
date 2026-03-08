package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
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

public class JoinerGui implements Listener {

    private static final Map<UUID, Set<String>> unlockedJoiners = new HashMap<>();
    private static final Map<UUID, String> selectedJoiner = new HashMap<>();

    public enum Joiner {
        DEFAULT("classic", 0, Material.OAK_SIGN),
        VIP("classic", 150, Material.GOLD_INGOT),
        MEME_FATHER("memes", 300, Material.EMERALD),
        MEME_CLOWN("memes", 250, Material.SLIME_BALL),
        DARK_GHOUL("dark", 500, Material.WITHER_SKELETON_SKULL),
        DARK_BERSERK("dark", 500, Material.NETHERITE_SWORD);

        public final String categoryId;
        public final int price;
        public final Material icon;

        Joiner(String categoryId, int price, Material icon) {
            this.categoryId = categoryId;
            this.price = price;
            this.icon = icon;
        }
    }

    public static void openCategories(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.joiner_gui.categories_title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        gui.setItem(11, new ItemBuilder(Material.BOOK).setName(color(LanguageManager.getString(player, "lobby.joiner_gui.cat_classic"))).build());
        gui.setItem(13, new ItemBuilder(Material.SLIME_BALL).setName(color(LanguageManager.getString(player, "lobby.joiner_gui.cat_memes"))).build());
        gui.setItem(15, new ItemBuilder(Material.WITHER_SKELETON_SKULL).setName(color(LanguageManager.getString(player, "lobby.joiner_gui.cat_dark"))).build());
        gui.setItem(22, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.joiner_gui.back"))).build());

        player.openInventory(gui);
    }

    public static void openList(Player player, String categoryId) {
        String catName = color(LanguageManager.getString(player, "lobby.joiner_gui.cat_" + categoryId));
        String title = color(LanguageManager.getString(player, "lobby.joiner_gui.list_title")).replace("%category%", ChatColor.stripColor(catName));

        Inventory gui = Bukkit.createInventory(player, 36, title);
        int slot = 0;

        String currentSelected = selectedJoiner.getOrDefault(player.getUniqueId(), "DEFAULT");
        Set<String> owned = unlockedJoiners.getOrDefault(player.getUniqueId(), new HashSet<>(Collections.singletonList("DEFAULT")));

        // --- БЕРЕМ КРАСИВЫЙ НИК ---
        Rank rank = RankManager.getRank(player);
        String formattedName = NameFormatUtil.getFormattedName(player, rank);
        String suffix = SuffixGui.getActiveSuffix(player);

        for (Joiner j : Joiner.values()) {
            if (j.categoryId.equals(categoryId)) {
                String joinerName = color(LanguageManager.getString(player, "lobby.joiner_gui.list." + j.name().toLowerCase() + ".name"));
                String rawFormatFromConfig = LanguageManager.getString(player, "lobby.joiner_gui.list." + j.name().toLowerCase() + ".format");

                ItemBuilder builder = new ItemBuilder(j.icon).setName("&b" + joinerName);
                List<String> lore = new ArrayList<>();

                // --- ПРЕДПРОСМОТР С КРАСИВЫМ НИКОМ ---
                String rawFormat = rawFormatFromConfig
                        .replace("%prefix%", "")
                        .replace("%player%", formattedName)
                        .replace("%suffix%", suffix);

                String formatLore = color(LanguageManager.getString(player, "lobby.joiner_gui.format")).replace("%format%", color(rawFormat));
                lore.add(formatLore);
                lore.add("");

                if (currentSelected.equals(j.name())) {
                    lore.add(color(LanguageManager.getString(player, "lobby.joiner_gui.selected")));
                    builder.setGlow(true);
                } else if (owned.contains(j.name())) {
                    lore.add(color(LanguageManager.getString(player, "lobby.joiner_gui.click_to_select")));
                } else {
                    lore.add(color(LanguageManager.getString(player, "lobby.joiner_gui.price")).replace("%price%", String.valueOf(j.price)));
                    lore.add(color(LanguageManager.getString(player, "lobby.joiner_gui.click_to_buy")));
                }

                builder.setLore(lore);
                gui.setItem(slot++, builder.build());
            }
        }

        gui.setItem(31, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.joiner_gui.back"))).build());
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String expCategoriesTitle = color(LanguageManager.getString(player, "lobby.joiner_gui.categories_title"));
        String expListTitleBase = color(LanguageManager.getString(player, "lobby.joiner_gui.list_title"));
        String expListPrefix = expListTitleBase.substring(0, expListTitleBase.indexOf("%category%"));

        if (ChatColor.stripColor(title).equals(ChatColor.stripColor(expCategoriesTitle))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            int slot = event.getSlot();

            if (slot == 11) openList(player, "classic");
            else if (slot == 13) openList(player, "memes");
            else if (slot == 15) openList(player, "dark");
            else if (slot == 22) CustomizationGui.open(player);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
        else if (title.startsWith(ChatColor.stripColor(expListPrefix))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            if (event.getSlot() == 31) {
                openCategories(player);
                return;
            }

            Material clickedType = event.getCurrentItem().getType();
            Joiner clickedJoiner = null;
            for (Joiner j : Joiner.values()) {
                if (j.icon == clickedType) {
                    clickedJoiner = j;
                    break;
                }
            }
            if (clickedJoiner == null) return;

            Set<String> owned = unlockedJoiners.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>(Collections.singletonList("DEFAULT")));
            String rawJoinerName = LanguageManager.getString(player, "lobby.joiner_gui.list." + clickedJoiner.name().toLowerCase() + ".name");

            if (owned.contains(clickedJoiner.name())) {
                selectedJoiner.put(player.getUniqueId(), clickedJoiner.name());
                String msg = color(LanguageManager.getString(player, "lobby.joiner_gui.messages.selected").replace("%name%", rawJoinerName));
                ChatUtil.sendMessage(player, msg);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                openList(player, clickedJoiner.categoryId);
            } else {
                PlayerStats stats = StatsManager.getStats(player);
                if (stats.getEmeralds() >= clickedJoiner.price) {
                    stats.setEmeralds(stats.getEmeralds() - clickedJoiner.price);
                    owned.add(clickedJoiner.name());

                    String msg = color(LanguageManager.getString(player, "lobby.joiner_gui.messages.purchased").replace("%name%", rawJoinerName));
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    openList(player, clickedJoiner.categoryId);
                } else {
                    String msg = color(LanguageManager.getString(player, "lobby.joiner_gui.messages.not_enough").replace("%price%", String.valueOf(clickedJoiner.price)));
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
        }
    }

    public static String getActiveMessage(Player player) {
        String id = selectedJoiner.getOrDefault(player.getUniqueId(), "DEFAULT");
        return color(LanguageManager.getString(player, "lobby.joiner_gui.list." + id.toLowerCase() + ".format"));
    }

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}