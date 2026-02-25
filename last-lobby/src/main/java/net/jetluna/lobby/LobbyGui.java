package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LobbyGui implements Listener {

    // --- МЕНЮ КОМПАСА ---
    public static void openSelector(Player player) {
        String title = LanguageManager.getString(player, "lobby.inventory.title");
        Inventory gui = Bukkit.createInventory(player, 27, title);

        ItemStack bedwars = new ItemBuilder(Material.RED_BED)
                .setName(LanguageManager.getString(player, "lobby.inventory.bedwars.name"))
                .setLore(LanguageManager.getList(player, "lobby.inventory.bedwars.lore"))
                .build();
        gui.setItem(11, bedwars);

        ItemStack vanilla = new ItemBuilder(Material.GRASS_BLOCK)
                .setName("<green><b>Vanilla</b>")
                .addLore("<gray>Классическое выживание")
                .addLore("<gray>Версия: <white>1.21")
                .build();
        gui.setItem(13, vanilla);

        ItemStack duels = new ItemBuilder(Material.IRON_SWORD)
                .setName("<yellow><b>Duels</b>")
                .addLore("<gray>Сражения 1 на 1")
                .build();
        gui.setItem(15, duels);

        player.openInventory(gui);
    }

    // --- МЕНЮ ПРОФИЛЯ ---
    public static void openProfile(Player player) {
        String title = LanguageManager.getString(player, "lobby.profile_gui.title");
        Inventory gui = Bukkit.createInventory(player, 54, title);

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        String rank = player.hasPermission("last.admin") ? "<red>Admin" : "<gray>Игрок";
        String progressBar = getProgressBar(stats.getExp(), 1000);

        // !!! ИСПРАВЛЕНИЕ: Обрабатываем список строк правильно !!!
        List<String> lore = LanguageManager.getList(player, "lobby.profile_gui.info.lore");
        List<String> finalLore = new ArrayList<>();

        for (String line : lore) {
            finalLore.add(line
                    .replace("%rank%", rank)
                    .replace("%level%", String.valueOf(stats.getLevel()))
                    .replace("%progressbar%", progressBar)
                    .replace("%exp%", String.valueOf(stats.getExp()))
                    .replace("%max_exp%", "1000")
                    .replace("%coins%", String.valueOf(stats.getCoins()))
                    .replace("%emeralds%", String.valueOf(stats.getEmeralds()))
            );
        }

        ItemStack info = new ItemBuilder(Material.PLAYER_HEAD)
                .setOwner(player.getName())
                .setName(LanguageManager.getString(player, "lobby.profile_gui.info.name"))
                .setLore(finalLore) // Передаем готовый список
                .build();
        gui.setItem(13, info);

        player.openInventory(gui);
    }

    private static String getProgressBar(int current, int max) {
        int totalBars = 10;
        int filledBars = (int) ((double) current / max * totalBars);
        StringBuilder sb = new StringBuilder("<green>");
        for (int i = 0; i < filledBars; i++) sb.append("■");
        sb.append("<gray>");
        for (int i = filledBars; i < totalBars; i++) sb.append("■");
        return sb.toString();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = ChatUtil.strip(event.getView().getTitle());
        String selectorTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.inventory.title"));
        String profileTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.profile_gui.title"));

        if (title.equals(selectorTitle) || title.equals(profileTitle)) {
            event.setCancelled(true);
        }
    }
}