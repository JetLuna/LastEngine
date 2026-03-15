package net.jetluna.bedwars.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum TeamColor {
    RED("Красная", ChatColor.RED, Material.RED_WOOL, Material.RED_BED, Material.RED_STAINED_GLASS, Material.RED_TERRACOTTA),
    BLUE("Синяя", ChatColor.BLUE, Material.BLUE_WOOL, Material.BLUE_BED, Material.BLUE_STAINED_GLASS, Material.BLUE_TERRACOTTA),
    GREEN("Зеленая", ChatColor.GREEN, Material.LIME_WOOL, Material.LIME_BED, Material.LIME_STAINED_GLASS, Material.LIME_TERRACOTTA),
    YELLOW("Желтая", ChatColor.YELLOW, Material.YELLOW_WOOL, Material.YELLOW_BED, Material.YELLOW_STAINED_GLASS, Material.YELLOW_TERRACOTTA),
    AQUA("Голубая", ChatColor.AQUA, Material.LIGHT_BLUE_WOOL, Material.LIGHT_BLUE_BED, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_TERRACOTTA),
    WHITE("Белая", ChatColor.WHITE, Material.WHITE_WOOL, Material.WHITE_BED, Material.WHITE_STAINED_GLASS, Material.WHITE_TERRACOTTA),
    PINK("Розовая", ChatColor.LIGHT_PURPLE, Material.PINK_WOOL, Material.PINK_BED, Material.PINK_STAINED_GLASS, Material.PINK_TERRACOTTA),
    GRAY("Серая", ChatColor.DARK_GRAY, Material.GRAY_WOOL, Material.GRAY_BED, Material.GRAY_STAINED_GLASS, Material.GRAY_TERRACOTTA);

    private final String name;
    private final ChatColor chatColor;
    private final Material wool;
    private final Material bed;
    private final Material glass;
    private final Material terracotta;

    TeamColor(String name, ChatColor chatColor, Material wool, Material bed, Material glass, Material terracotta) {
        this.name = name;
        this.chatColor = chatColor;
        this.wool = wool;
        this.bed = bed;
        this.glass = glass;
        this.terracotta = terracotta;
    }

    public String getName() { return name; }
    public ChatColor getChatColor() { return chatColor; }
    public Material getWool() { return wool; }
    public Material getBed() { return bed; }
    public Material getGlass() { return glass; }
    public Material getTerracotta() { return terracotta; }

    // Удобный метод: передаешь блок кровати, получаешь цвет команды
    public static TeamColor getByBed(Material bedMaterial) {
        for (TeamColor color : values()) {
            if (color.getBed() == bedMaterial) return color;
        }
        return null;
    }

    public static TeamColor getByWool(Material woolMaterial) {
        for (TeamColor color : values()) {
            if (color.getWool() == woolMaterial) return color;
        }
        return null;
    }
}