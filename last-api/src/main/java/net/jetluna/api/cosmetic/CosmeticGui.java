package net.jetluna.api.cosmetic;

import net.jetluna.api.lang.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CosmeticGui {

    public static void open(Player player) {
        String title = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.gui.title"));
        Inventory inv = Bukkit.createInventory(null, 36, title);

        String free = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.free"));
        String equip = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.equip"));
        String bought = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.bought"));
        String buy = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.buy"));

        // --- ВЕРХНИЙ РЯД (Бесплатные) ---
        String stLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.standard.lore"));
        inv.setItem(11, applyLore(CosmeticManager.getLastEngineBanner(player), stLore, "", free, equip));

        String piLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.pirate.lore"));
        inv.setItem(13, applyLore(CosmeticManager.getPirateBanner(player), piLore, "", free, equip));

        String roLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.royal.lore"));
        inv.setItem(15, applyLore(CosmeticManager.getRoyalBanner(player), roLore, "", free, equip));

        // --- НИЖНИЙ РЯД (Платные) ---
        String ukrPrice = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.price").replace("%price%", "500"));
        String ukrStatus = CosmeticManager.hasPurchased(player, "ukraine") ? bought : buy;
        String ukrLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.ukraine.lore"));
        inv.setItem(20, applyLore(CosmeticManager.getUkraineBanner(player), ukrLore, "", ukrPrice, ukrStatus));

        String crpPrice = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.price").replace("%price%", "750"));
        String crpStatus = CosmeticManager.hasPurchased(player, "creeper") ? bought : buy;
        String crpLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.creeper.lore"));
        inv.setItem(22, applyLore(CosmeticManager.getCreeperBanner(player), crpLore, "", crpPrice, crpStatus));

        String crsPrice = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.status.price").replace("%price%", "1000"));
        String crsStatus = CosmeticManager.hasPurchased(player, "crusader") ? bought : buy;
        String crsLore = CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.banners.crusader.lore"));
        inv.setItem(24, applyLore(CosmeticManager.getCrusaderBanner(player), crsLore, "", crsPrice, crsStatus));

        // --- Кнопка снятия ---
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta bMeta = barrier.getItemMeta();
        bMeta.setDisplayName(CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.gui.remove")));
        bMeta.setLore(Arrays.asList(CosmeticManager.toLegacy(LanguageManager.getString(player, "cosmetics.gui.remove_lore"))));
        barrier.setItemMeta(bMeta);
        inv.setItem(31, barrier);

        player.openInventory(inv);
    }

    private static ItemStack applyLore(ItemStack item, String... loreLines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>(Arrays.asList(loreLines));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}