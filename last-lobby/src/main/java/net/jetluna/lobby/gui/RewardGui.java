package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
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

public class RewardGui implements Listener {

    private static final long ONE_DAY = 86400000L; // 24 часа

    public static void open(Player player) {
        // LanguageManager.getString возвращает String с цветами (§), это то, что нужно для createInventory
        String title = LanguageManager.getString(player, "lobby.rewards.title");

        // !!! ЗДЕСЬ БЫЛА ОШИБКА !!!
        // Мы передаем title напрямую. Не надо ChatUtil.parse()
        Inventory gui = Bukkit.createInventory(player, 54, title);

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        long lastTime = stats.getLastRewardTime();
        int currentDay = stats.getRewardDay();
        if (currentDay == 0) currentDay = 1;

        long diff = System.currentTimeMillis() - lastTime;
        boolean canClaim = diff >= ONE_DAY;

        // Генерация 28 дней
        for (int i = 0; i < 28; i++) {
            int day = i + 1;
            int slot = 10 + (i / 7) * 9 + (i % 7);

            ItemStack item;
            if (day < currentDay) {
                // Получено
                item = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                        .setName(LanguageManager.getString(player, "lobby.rewards.items.day_name").replace("%day%", String.valueOf(day)))
                        .addLore(LanguageManager.getString(player, "lobby.rewards.items.received_lore"))
                        .build();
            } else if (day == currentDay) {
                // Сегодня
                if (canClaim) {
                    item = new ItemBuilder(Material.CHEST_MINECART)
                            .setName(LanguageManager.getString(player, "lobby.rewards.items.current_name").replace("%day%", String.valueOf(day)))
                            .addLore("")
                            .addLore(LanguageManager.getString(player, "lobby.rewards.items.reward_coins").replace("%amount%", String.valueOf(100 + (day * 50))))
                            .addLore("")
                            .addLore(LanguageManager.getString(player, "lobby.rewards.items.click_to_claim"))
                            .setGlow(true)
                            .build();
                } else {
                    item = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                            .setName(LanguageManager.getString(player, "lobby.rewards.items.locked_name").replace("%day%", String.valueOf(day)))
                            .addLore(LanguageManager.getString(player, "lobby.rewards.messages.cooldown").replace("%time%", formatTime(ONE_DAY - diff)))
                            .build();
                }
            } else {
                // Будущее
                item = new ItemBuilder(Material.GRAY_DYE)
                        .setName(LanguageManager.getString(player, "lobby.rewards.items.future_name").replace("%day%", String.valueOf(day)))
                        .addLore(LanguageManager.getString(player, "lobby.rewards.items.future_lore"))
                        .build();
            }
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
    }

    private static String formatTime(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        return String.format("%02dч %02dм", hours, minutes);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = ChatUtil.strip(event.getView().getTitle());
        String expected = ChatUtil.strip(LanguageManager.getString(player, "lobby.rewards.title"));

        if (!title.equals(expected)) return;
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.CHEST_MINECART) {
            PlayerStats stats = StatsManager.getStats(player);

            long lastTime = stats.getLastRewardTime();
            if (System.currentTimeMillis() - lastTime < ONE_DAY) {
                player.sendMessage(ChatUtil.parse("<red>Вы уже получали награду!"));
                player.closeInventory();
                return;
            }

            int day = stats.getRewardDay();
            if (day == 0) day = 1;
            int amount = 100 + (day * 50);

            stats.setCoins(stats.getCoins() + amount);
            stats.setLastRewardTime(System.currentTimeMillis());
            stats.setRewardDay(day + 1);
            StatsManager.saveStats(player);

            String msg = LanguageManager.getString(player, "lobby.rewards.messages.received_coins")
                    .replace("%amount%", String.valueOf(amount));
            player.sendMessage(ChatUtil.parse(msg));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            open(player);
        }
    }
}