package net.jetluna.api.cosmetic;

import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Косметика: Баннеры")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case 11:
                player.getInventory().setHelmet(CosmeticManager.getLastEngineBanner());
                successEquip(player);
                break;
            case 13:
                player.getInventory().setHelmet(CosmeticManager.getPirateBanner());
                successEquip(player);
                break;
            case 15:
                player.getInventory().setHelmet(CosmeticManager.getRoyalBanner());
                successEquip(player);
                break;
            case 20:
                handlePurchase(player, "ukraine", 500, CosmeticManager.getUkraineBanner());
                break;
            case 22:
                handlePurchase(player, "creeper", 750, CosmeticManager.getCreeperBanner());
                break;
            case 24:
                handlePurchase(player, "crusader", 1000, CosmeticManager.getCrusaderBanner());
                break;
            case 31:
                CosmeticManager.removeCosmetic(player);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                player.closeInventory();
                player.sendMessage("§cКосметика снята.");
                break;
        }
    }

    // Метод для бесплатного надевания
    private void successEquip(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f);
        player.sendMessage("§aБаннер успешно надет!");
        player.closeInventory();
    }

    // Универсальный метод покупки
    private void handlePurchase(Player player, String id, int price, org.bukkit.inventory.ItemStack banner) {
        // Если уже куплено — просто надеваем
        if (CosmeticManager.hasPurchased(player, id)) {
            player.getInventory().setHelmet(banner);
            successEquip(player);
            return;
        }

        // Если не куплено — лезем в базу за монетами
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        if (stats.getCoins() >= price) {
            // Списываем монеты
            stats.setCoins(stats.getCoins() - price);
            StatsManager.saveStats(player);

            // Сохраняем покупку в скрытую дату игрока!
            CosmeticManager.setPurchased(player, id);

            player.getInventory().setHelmet(banner);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.sendMessage("§aВы успешно купили баннер за §6" + price + " монет§a!");

            // Переоткрываем меню, чтобы обновить статус на "Куплено"
            CosmeticGui.open(player);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage("§cНедостаточно монет для покупки! Нужно: §6" + price);
            player.closeInventory();
        }
    }
}