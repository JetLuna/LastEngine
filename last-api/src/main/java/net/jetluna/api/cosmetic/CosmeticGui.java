package net.jetluna.api.cosmetic;

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
        Inventory inv = Bukkit.createInventory(null, 36, "Косметика: Баннеры");

        // --- ВЕРХНИЙ РЯД (Бесплатные) ---
        inv.setItem(11, applyLore(CosmeticManager.getLastEngineBanner(), "§7Стандартный флаг", "", "§aБесплатно!", "§eКликни, чтобы надеть"));
        inv.setItem(13, applyLore(CosmeticManager.getPirateBanner(), "§7Йо-хо-хо!", "", "§aБесплатно!", "§eКликни, чтобы надеть"));
        inv.setItem(15, applyLore(CosmeticManager.getRoyalBanner(), "§7Для истинных лордов", "", "§aБесплатно!", "§eКликни, чтобы надеть"));

        // --- НИЖНИЙ РЯД (Платные) ---
        String ukrStatus = CosmeticManager.hasPurchased(player, "ukraine") ? "§aКуплено! Кликни, чтобы надеть" : "§eКликни для покупки";
        inv.setItem(20, applyLore(CosmeticManager.getUkraineBanner(), "§7Родной флаг", "", "§7Цена: §6500 Монет", ukrStatus));

        String crpStatus = CosmeticManager.hasPurchased(player, "creeper") ? "§aКуплено! Кликни, чтобы надеть" : "§eКликни для покупки";
        inv.setItem(22, applyLore(CosmeticManager.getCreeperBanner(), "§7Тсссс...", "", "§7Цена: §6750 Монет", crpStatus));

        String crsStatus = CosmeticManager.hasPurchased(player, "crusader") ? "§aКуплено! Кликни, чтобы надеть" : "§eКликни для покупки";
        inv.setItem(24, applyLore(CosmeticManager.getCrusaderBanner(), "§7Deus Vult!", "", "§7Цена: §61000 Монет", crsStatus)); // Сделал за монеты, чтобы точно не было ошибок

        // Кнопка снятия в самом низу
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta bMeta = barrier.getItemMeta();
        bMeta.setDisplayName("§c❌ Снять косметику");
        bMeta.setLore(Arrays.asList("§7Очистить слот шлема"));
        barrier.setItemMeta(bMeta);
        inv.setItem(31, barrier);

        player.openInventory(inv);
    }

    // Умный метод, который добавляет описание к уже готовому флагу
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