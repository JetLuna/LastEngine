package net.jetluna.api.cosmetic;

import net.jetluna.api.LastApi;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

public class CosmeticManager {

    // --- СИСТЕМА СОХРАНЕНИЯ ПОКУПОК (Без Базы Данных!) ---
    public static boolean hasPurchased(Player player, String bannerId) {
        if (player.hasPermission("lastengine.cosmetic.all")) return true; // Опам и донатерам можно выдать все
        NamespacedKey key = new NamespacedKey(LastApi.getInstance(), "banner_" + bannerId);
        return player.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static void setPurchased(Player player, String bannerId) {
        NamespacedKey key = new NamespacedKey(LastApi.getInstance(), "banner_" + bannerId);
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }

    // --- БЕСПЛАТНЫЕ БАННЕРЫ ---
    public static ItemStack getPirateBanner() {
        ItemStack banner = new ItemStack(Material.BLACK_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§c☠ Пиратский флаг");
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.SKULL));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getRoyalBanner() {
        ItemStack banner = new ItemStack(Material.PURPLE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§6♛ Королевский штандарт");
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.CROSS)); // ORANGE вместо GOLD
        meta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.FLOWER));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getLastEngineBanner() {
        ItemStack banner = new ItemStack(Material.LIGHT_BLUE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§b⚡ Флаг LastEngine");
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.GLOBE));
        meta.addPattern(new Pattern(DyeColor.CYAN, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    // --- ПЛАТНЫЕ БАННЕРЫ ---
    public static ItemStack getUkraineBanner() {
        ItemStack banner = new ItemStack(Material.YELLOW_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§eФлаг §bУкраины");
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.HALF_HORIZONTAL));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getCreeperBanner() {
        ItemStack banner = new ItemStack(Material.LIME_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§aЛицо Крипера");
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.CREEPER));
        meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getCrusaderBanner() {
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName("§fЩит Крестоносца");
        meta.addPattern(new Pattern(DyeColor.RED, PatternType.CROSS));
        meta.addPattern(new Pattern(DyeColor.RED, PatternType.STRAIGHT_CROSS));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    // --- СНЯТИЕ ---
    public static void removeCosmetic(Player player) {
        player.getInventory().setHelmet(null);
    }
}