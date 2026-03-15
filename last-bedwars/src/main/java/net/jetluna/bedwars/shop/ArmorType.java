package net.jetluna.bedwars.shop;

import net.jetluna.bedwars.resource.Resource;
import org.bukkit.Material;

public enum ArmorType {
    LEATHER(Material.LEATHER_CHESTPLATE, Material.LEATHER_BOOTS, Resource.IRON, 0, "§7Кожаная броня"),
    IRON(Material.IRON_CHESTPLATE, Material.IRON_BOOTS, Resource.IRON, 40, "§fЖелезная броня"),
    DIAMOND(Material.DIAMOND_CHESTPLATE, Material.DIAMOND_BOOTS, Resource.DIAMOND, 12, "§f§lАлмазная броня"), // Diamond = Рубин из плазмикса
    NETHERITE(Material.NETHERITE_CHESTPLATE, Material.NETHERITE_BOOTS, Resource.OPAL, 6, "§b§lНезеритовая броня");

    private final Material chestplate;
    private final Material boots;
    private final Resource currency;
    private final int price;
    private final String title;

    ArmorType(Material chestplate, Material boots, Resource currency, int price, String title) {
        this.chestplate = chestplate;
        this.boots = boots;
        this.currency = currency;
        this.price = price;
        this.title = title;
    }

    public Material getChestplate() { return chestplate; }
    public Material getBoots() { return boots; }
    public Resource getCurrency() { return currency; }
    public int getPrice() { return price; }
    public String getTitle() { return title; }
}