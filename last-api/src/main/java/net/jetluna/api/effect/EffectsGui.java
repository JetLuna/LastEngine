package net.jetluna.api.effect;

import net.jetluna.api.LastApi; // Добавили доступ к ядру плагина
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

import java.util.Arrays;

public class EffectsGui implements Listener {

    private static final String TITLE = "Визуальные эффекты";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, TITLE);
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        ParticleEffect[] effects = ParticleEffect.values();

        for (int i = 0; i < effects.length; i++) {
            if (i >= slots.length) break;

            ParticleEffect effect = effects[i];
            boolean isActive = stats.getActiveEffect().equals(effect.name());
            boolean isOwned = player.isOp() || isActive;

            ItemBuilder builder = new ItemBuilder(effect.getIcon());

            if (isActive) {
                builder.setName("&a★ &e" + effect.getDisplayName() + " &a★");
                builder.setLore(Arrays.asList("", "&aЭффект уже включен!", "&7Будет сверкать вокруг вас."));
            } else if (isOwned) {
                builder.setName("&e" + effect.getDisplayName());
                builder.setLore(Arrays.asList("", "&7Статус: &aРазблокировано", "", "&eНажмите, чтобы включить!"));
            } else {
                builder.setName("&e" + effect.getDisplayName());
                builder.setLore(Arrays.asList("", "&7Цена: &6" + effect.getPrice() + " монет", "", "&cУ вас не куплено!", "&eНажмите, чтобы купить."));
            }

            gui.setItem(slots[i], builder.build());
        }

        gui.setItem(48, new ItemBuilder(Material.BARRIER)
                .setName("&cВыключить все эффекты")
                .setLore(Arrays.asList("&7Убирает все партиклы вокруг вас."))
                .build());

        gui.setItem(49, new ItemBuilder(Material.ARROW)
                .setName("&cЗакрыть")
                .setLore(Arrays.asList("&7Закрыть меню визуальных эффектов."))
                .build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!ChatUtil.strip(event.getView().getTitle()).equals(TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        int slot = event.getSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot == 48) {
            stats.setActiveEffect("");
            ChatUtil.sendMessage(player, "&cЭффект выключен!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);

            // Запускаем обновление через планировщик
            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
            return;
        }

        for (ParticleEffect effect : ParticleEffect.values()) {
            if (item.getType() == effect.getIcon()) {
                boolean isActive = stats.getActiveEffect().equals(effect.name());
                boolean isOwned = player.isOp() || isActive;

                if (isOwned) {
                    stats.setActiveEffect(effect.name());
                    ChatUtil.sendMessage(player, "&aЭффект " + effect.getDisplayName() + " успешно выбран!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                } else {
                    if (stats.getCoins() >= effect.getPrice()) {
                        stats.setCoins(stats.getCoins() - effect.getPrice());
                        stats.setActiveEffect(effect.name());
                        ChatUtil.sendMessage(player, "&aВы купили эффект " + effect.getDisplayName() + "!");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    } else {
                        ChatUtil.sendMessage(player, "&cНедостаточно монет для покупки!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }

                // РЕШЕНИЕ ПРОБЛЕМЫ ВЫЛЕТА:
                // Открываем обновленное меню с задержкой в 1 тик
                Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
                break;
            }
        }
    }
}