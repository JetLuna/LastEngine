package net.jetluna.api.cosmetic;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import org.bukkit.ChatColor;
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

    public static boolean hasPurchased(Player player, String bannerId) {
        if (player.hasPermission("lastengine.cosmetic.all")) return true;
        NamespacedKey key = new NamespacedKey(LastApi.getInstance(), "banner_" + bannerId);
        return player.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static void setPurchased(Player player, String bannerId) {
        NamespacedKey key = new NamespacedKey(LastApi.getInstance(), "banner_" + bannerId);
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }

    // --- БЕСПЛАТНЫЕ БАННЕРЫ ---
    public static ItemStack getPirateBanner(Player player) {
        ItemStack banner = new ItemStack(Material.BLACK_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.pirate.name")));
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.SKULL));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getRoyalBanner(Player player) {
        ItemStack banner = new ItemStack(Material.PURPLE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.royal.name")));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.CROSS));
        meta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.FLOWER));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getLastEngineBanner(Player player) {
        ItemStack banner = new ItemStack(Material.LIGHT_BLUE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.standard.name")));
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.GLOBE));
        meta.addPattern(new Pattern(DyeColor.CYAN, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    // --- ПЛАТНЫЕ БАННЕРЫ ---
    public static ItemStack getUkraineBanner(Player player) {
        ItemStack banner = new ItemStack(Material.YELLOW_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.ukraine.name")));
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.HALF_HORIZONTAL));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getCreeperBanner(Player player) {
        ItemStack banner = new ItemStack(Material.LIME_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.creeper.name")));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.CREEPER));
        meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    public static ItemStack getCrusaderBanner(Player player) {
        ItemStack banner = new ItemStack(Material.WHITE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(toLegacy(LanguageManager.getString(player, "cosmetics.banners.crusader.name")));
        meta.addPattern(new Pattern(DyeColor.RED, PatternType.CROSS));
        meta.addPattern(new Pattern(DyeColor.RED, PatternType.STRAIGHT_CROSS));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        banner.setItemMeta(meta);
        return banner;
    }

    public static void removeCosmetic(Player player) {
        player.getInventory().setHelmet(null);
    }

    public static String toLegacy(String text) {
        if (text == null) return "";
        String legacy = text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }
}