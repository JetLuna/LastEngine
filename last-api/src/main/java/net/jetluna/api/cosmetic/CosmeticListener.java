package net.jetluna.api.cosmetic;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Также ждем 10 тиков перед загрузкой баннеров
        Bukkit.getScheduler().runTaskLater(LastApi.getInstance(), () -> {
            Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
                CosmeticManager.loadCosmetics(player.getUniqueId());
                String equipped = CosmeticManager.getEquipped(player);

                if (equipped != null && !equipped.isEmpty()) {
                    Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> {
                        player.getInventory().setHelmet(CosmeticManager.getBannerById(player, equipped));
                    });
                }
            });
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Очищаем кэш косметики при выходе
        CosmeticManager.unloadCosmetics(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.gui.title"));
        if (!event.getView().getTitle().equals(expectedTitle)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 11:
                CosmeticManager.setEquipped(player, "standard");
                successEquip(player);
                break;
            case 13:
                CosmeticManager.setEquipped(player, "pirate");
                successEquip(player);
                break;
            case 15:
                CosmeticManager.setEquipped(player, "royal");
                successEquip(player);
                break;
            case 20:
                handlePurchase(player, "ukraine", 500);
                break;
            case 22:
                handlePurchase(player, "creeper", 750);
                break;
            case 24:
                handlePurchase(player, "crusader", 1000);
                break;
            case 31:
                CosmeticManager.setEquipped(player, null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                player.closeInventory();
                LanguageManager.sendMessage(player, "cosmetics.messages.removed");
                break;
        }
    }

    private void successEquip(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f);
        LanguageManager.sendMessage(player, "cosmetics.messages.equipped");
        player.closeInventory();
    }

    private void handlePurchase(Player player, String id, int price) {
        if (CosmeticManager.hasPurchased(player, id)) {
            CosmeticManager.setEquipped(player, id);
            successEquip(player);
            return;
        }

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        if (stats.getCoins() >= price) {
            stats.setCoins(stats.getCoins() - price);
            StatsManager.saveStats(player);

            CosmeticManager.setPurchased(player, id);
            CosmeticManager.setEquipped(player, id);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            String msg = LanguageManager.getString(player, "cosmetics.messages.purchased").replace("%price%", String.valueOf(price));
            ChatUtil.sendMessage(player, msg);

            CosmeticGui.open(player);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);

            String msg = LanguageManager.getString(player, "cosmetics.messages.not_enough_coins").replace("%price%", String.valueOf(price));
            ChatUtil.sendMessage(player, msg);

            player.closeInventory();
        }
    }
}