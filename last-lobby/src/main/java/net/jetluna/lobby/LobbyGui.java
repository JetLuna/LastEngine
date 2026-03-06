package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.gui.SettingsGui; // !!! ВАЖНЫЙ ИМПОРТ
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
        String rankDisplay = (rank.getWeight() == 1) ? "<gray>Player" : rank.getPrefix();
        String progressBar = getProgressBar(stats.getExp(), 1000);

        List<String> lore = LanguageManager.getList(player, "lobby.profile_gui.info.lore");
        List<String> finalLore = new ArrayList<>();

        for (String line : lore) {
            finalLore.add(line
                    .replace("%rank%", rankDisplay)
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
                .setLore(finalLore)
                .build();
        gui.setItem(13, info);

        // !!! КНОПКА НАСТРОЕК (Я ЕЕ ДОБАВИЛ) !!!
        ItemStack settings = new ItemBuilder(Material.COMPARATOR)
                .setName("&eНастройки")
                .setLore("&7Управление чатом,", "&7видимостью и эффектами.")
                .build();
        gui.setItem(10, settings);

        player.openInventory(gui);

        // !!! КНОПКА КАСТОМИЗАЦИИ !!!
        ItemStack customization = new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .setName("&dКастомизация")
                .setLore("&7Выделитесь из толпы!", "&7Сообщения при входе, гаджеты и партиклы.")
                .build();
        gui.setItem(16, customization);
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

        // 1. Получаем все переводы названий
        String currentTitle = ChatUtil.strip(event.getView().getTitle());
        String selectorTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.inventory.title"));
        String profileTitle = ChatUtil.strip(LanguageManager.getString(player, "lobby.profile_gui.title"));

        // 2. ГЛАВНЫЙ ЗАМОК: Если это не Компас и не Профиль — выходим! Шпионов больше нет.
        if (!currentTitle.equals(selectorTitle) && !currentTitle.equals(profileTitle)) {
            return;
        }

        // 3. Если мы здесь, значит это одно из наших меню. Отменяем возможность красть вещи:
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // 4. ОБРАБОТКА МЕНЮ ПРОФИЛЯ
        if (currentTitle.equals(profileTitle)) {
            if (slot == 10) { // Настройки
                SettingsGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (slot == 16) { // Кастомизация (Теперь она надежно заперта внутри Профиля!)
                net.jetluna.lobby.gui.CustomizationGui.open(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }

        // 5. ОБРАБОТКА МЕНЮ КОМПАСА (Выбор режима)
        else if (currentTitle.equals(selectorTitle)) {
            // В будущем тут можно будет добавить телепортацию:
            // if (slot == 11) { player.chat("/server bedwars"); }
        }
    }
}