package net.jetluna.api.cosmetic;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Получаем переведенный заголовок меню для текущего игрока
        String expectedTitle = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.gui.title"));
        if (!event.getView().getTitle().equals(expectedTitle)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 11:
                player.getInventory().setHelmet(CosmeticManager.getLastEngineBanner(player));
                successEquip(player);
                break;
            case 13:
                player.getInventory().setHelmet(CosmeticManager.getPirateBanner(player));
                successEquip(player);
                break;
            case 15:
                player.getInventory().setHelmet(CosmeticManager.getRoyalBanner(player));
                successEquip(player);
                break;
            case 20:
                handlePurchase(player, "ukraine", 500, CosmeticManager.getUkraineBanner(player));
                break;
            case 22:
                handlePurchase(player, "creeper", 750, CosmeticManager.getCreeperBanner(player));
                break;
            case 24:
                handlePurchase(player, "crusader", 1000, CosmeticManager.getCrusaderBanner(player));
                break;
            case 31:
                CosmeticManager.removeCosmetic(player);
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

    private void handlePurchase(Player player, String id, int price, org.bukkit.inventory.ItemStack banner) {
        if (CosmeticManager.hasPurchased(player, id)) {
            player.getInventory().setHelmet(banner);
            successEquip(player);
            return;
        }

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        if (stats.getCoins() >= price) {
            stats.setCoins(stats.getCoins() - price);
            StatsManager.saveStats(player);

            CosmeticManager.setPurchased(player, id);

            player.getInventory().setHelmet(banner);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            // Динамическая замена цены в сообщении
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