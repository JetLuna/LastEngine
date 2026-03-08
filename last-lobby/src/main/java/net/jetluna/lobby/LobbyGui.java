package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.gui.SettingsGui;
import org.bukkit.Bukkit;
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

public class LobbyGui implements Listener {

    public static void openSelector(Player player) {
        String title = LanguageManager.getString(player, "lobby.inventory.title");
        Inventory gui = Bukkit.createInventory(player, 27, title);

        ItemStack bedwars = new ItemBuilder(Material.RED_BED)
                .setName(LanguageManager.getString(player, "lobby.inventory.bedwars.name"))
                .setLore(LanguageManager.getList(player, "lobby.inventory.bedwars.lore"))
                .build();
        gui.setItem(11, bedwars);

        ItemStack vanilla = new ItemBuilder(Material.GRASS_BLOCK)
                .setName(LanguageManager.getString(player, "lobby.inventory.vanilla.name"))
                .setLore(LanguageManager.getList(player, "lobby.inventory.vanilla.lore"))
                .build();
        gui.setItem(13, vanilla);

        ItemStack duels = new ItemBuilder(Material.IRON_SWORD)
                .setName(LanguageManager.getString(player, "lobby.inventory.duels.name"))
                .setLore(LanguageManager.getList(player, "lobby.inventory.duels.lore"))
                .build();
        gui.setItem(15, duels);

        player.openInventory(gui);
    }

    public static void openProfile(Player player) {
        String title = LanguageManager.getString(player, "lobby.profile_gui.title");
        Inventory gui = Bukkit.createInventory(player, 54, title);

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        Rank rank = RankManager.getRank(player);

        // --- ПРАВИЛЬНОЕ ОТОБРАЖЕНИЕ РАНГА В LORE (С кастомным цветом) ---
        String rankDisplay = net.jetluna.api.util.NameFormatUtil.getFormattedRank(player, rank);

        String progressBar = getProgressBar(stats.getExp(), 1000);

        List<String> lore = LanguageManager.getList(player, "lobby.profile_gui.info.lore");
        List<String> finalLore = new ArrayList<>();

        for (String line : lore) {
            finalLore.add(color(line)
                    .replace("%rank%", rankDisplay)
                    .replace("%level%", String.valueOf(stats.getLevel()))
                    .replace("%progressbar%", progressBar)
                    .replace("%exp%", String.valueOf(stats.getExp()))
                    .replace("%max_exp%", "1000")
                    .replace("%coins%", String.valueOf(stats.getCoins()))
                    .replace("%emeralds%", String.valueOf(stats.getEmeralds()))
            );
        }

        // --- ИСПОЛЬЗУЕМ ОБЫЧНЫЙ КРАСИВЫЙ ФОРМАТ ДЛЯ ГОЛОВЫ ---
        String guiName = net.jetluna.api.util.NameFormatUtil.getFormattedName(player, rank);
        String headName = color(LanguageManager.getString(player, "lobby.profile_gui.info.name")).replace("%player%", guiName);

        ItemStack info = new ItemBuilder(Material.PLAYER_HEAD)
                .setOwner(player.getName())
                .setName(headName)
                .setLore(finalLore)
                .build();
        gui.setItem(13, info);

        // --- АДАПТИРОВАННАЯ КНОПКА НАСТРОЕК ---
        gui.setItem(10, new ItemBuilder(Material.COMPARATOR)
                .setName(color(LanguageManager.getString(player, "lobby.profile_gui.settings.name")))
                .setLore(colorList(player, "lobby.profile_gui.settings.lore"))
                .build());

        // --- АДАПТИРОВАННАЯ КНОПКА КАСТОМИЗАЦИИ ---
        gui.setItem(16, new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .setName(color(LanguageManager.getString(player, "lobby.profile_gui.cosmetics.name")))
                .setLore(colorList(player, "lobby.profile_gui.cosmetics.lore"))
                .build());

        // --- КНОПКА ЯЗЫКА (Текстура планеты Земля) ---
        gui.setItem(22, net.jetluna.lobby.gui.LanguageGui.getHeadItem(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Y0MDk0MmYzNjRmNmNiY2VmZmNmMTE1MTc5NjQxMDI4NmE0OGIxYWViYTc3MjQzZTIxODAyNmMwOWNkMSJ9fX0=",
                color(LanguageManager.getString(player, "lobby.profile_gui.language.name")),
                colorList(player, "lobby.profile_gui.language.lore")
        ));

        // --- КНОПКА ДОНАТА (С ЛОКАЛИЗАЦИЕЙ) ---
        gui.setItem(31, new ItemBuilder(Material.GOLD_INGOT)
                .setName(color(LanguageManager.getString(player, "lobby.profile_gui.donate.name")))
                .setLore(colorList(player, "lobby.profile_gui.donate.lore"))
                .setGlow(true)
                .build());

        player.openInventory(gui);
    }

    private static String getProgressBar(int current, int max) {
        int totalBars = 10;
        int filledBars = (int) ((double) current / max * totalBars);
        // Используем § вместо тегов <green>, чтобы Bukkit понимал цвета в Lore
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < filledBars; i++) sb.append("■");
        sb.append("§7");
        for (int i = filledBars; i < totalBars; i++) sb.append("■");
        return sb.toString();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String currentTitle = ChatUtil.strip(event.getView().getTitle());
        String selectorTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.inventory.title"));
        String profileTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.profile_gui.title"));

        if (!currentTitle.equals(selectorTitle) && !currentTitle.equals(profileTitle)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // Единый блок обработки кликов в профиле
        if (currentTitle.equals(profileTitle)) {
            if (slot == 10) {
                SettingsGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 16) {
                net.jetluna.lobby.gui.CustomizationGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 22) {
                net.jetluna.lobby.gui.LanguageGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 31) {
                net.jetluna.lobby.gui.DonateGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
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