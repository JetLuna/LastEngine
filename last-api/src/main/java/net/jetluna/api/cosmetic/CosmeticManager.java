package net.jetluna.api.cosmetic;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CosmeticManager {

    // Кэши для оперативной памяти
    private static final Map<UUID, List<String>> purchasedCache = new HashMap<>();
    private static final Map<UUID, String> equippedCache = new HashMap<>();

    public static void init(LastApi plugin) {
        // Создаем отдельную таблицу для косметики
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_cosmetics (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "purchased_banners MEDIUMTEXT, " +
                             "equipped_banner VARCHAR(64)" +
                             ");")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadCosmetics(UUID uuid) {
        List<String> purchased = new ArrayList<>();
        String equipped = null;

        try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT purchased_banners, equipped_banner FROM player_cosmetics WHERE uuid = ?")) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String rawPurchased = rs.getString("purchased_banners");
                if (rawPurchased != null && !rawPurchased.isEmpty()) {
                    purchased.addAll(Arrays.asList(rawPurchased.split(",")));
                }
                equipped = rs.getString("equipped_banner");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        purchasedCache.put(uuid, purchased);
        equippedCache.put(uuid, equipped);
    }

    public static void unloadCosmetics(UUID uuid) {
        purchasedCache.remove(uuid);
        equippedCache.remove(uuid);
    }

    private static void saveToDatabase(UUID uuid) {
        List<String> purchased = purchasedCache.getOrDefault(uuid, new ArrayList<>());
        String equipped = equippedCache.get(uuid);
        String joinedPurchased = String.join(",", purchased);

        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO player_cosmetics (uuid, purchased_banners, equipped_banner) VALUES (?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE purchased_banners = VALUES(purchased_banners), equipped_banner = VALUES(equipped_banner)")) {

                ps.setString(1, uuid.toString());
                ps.setString(2, joinedPurchased);
                ps.setString(3, equipped);
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean hasPurchased(Player player, String bannerId) {
        if (player.hasPermission("lastengine.cosmetic.all")) return true;
        return purchasedCache.getOrDefault(player.getUniqueId(), new ArrayList<>()).contains(bannerId);
    }

    public static void setPurchased(Player player, String bannerId) {
        UUID uuid = player.getUniqueId();
        List<String> purchased = purchasedCache.getOrDefault(uuid, new ArrayList<>());
        if (!purchased.contains(bannerId)) {
            purchased.add(bannerId);
            purchasedCache.put(uuid, purchased);
            saveToDatabase(uuid);
        }
    }

    public static String getEquipped(Player player) {
        return equippedCache.get(player.getUniqueId());
    }

    public static void setEquipped(Player player, String bannerId) {
        UUID uuid = player.getUniqueId();

        if (bannerId == null || bannerId.isEmpty()) {
            equippedCache.remove(uuid);
            removeCosmetic(player);
        } else {
            equippedCache.put(uuid, bannerId);
            player.getInventory().setHelmet(getBannerById(player, bannerId));
        }

        saveToDatabase(uuid);
    }

    public static ItemStack getBannerById(Player player, String id) {
        if (id == null) return null;
        switch (id.toLowerCase()) {
            case "standard": return getLastEngineBanner(player);
            case "pirate": return getPirateBanner(player);
            case "royal": return getRoyalBanner(player);
            case "ukraine": return getUkraineBanner(player);
            case "creeper": return getCreeperBanner(player);
            case "crusader": return getCrusaderBanner(player);
            default: return null;
        }
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