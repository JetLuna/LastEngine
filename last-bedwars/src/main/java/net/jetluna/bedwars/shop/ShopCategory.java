package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public enum ShopCategory {
    BLOCKS("Блоки", Material.TERRACOTTA),
    WEAPONS("Оружие", Material.GOLDEN_SWORD),
    ARMOR("Броня", Material.CHAINMAIL_BOOTS),
    TOOLS("Инструменты", Material.STONE_PICKAXE),
    BOWS("Луки", Material.BOW),
    POTIONS("Зелья", Material.POTION),
    OTHER("Остальное", Material.TNT);

    private final String name;
    private final Material iconMaterial;

    ShopCategory(String name, Material iconMaterial) {
        this.name = name;
        this.iconMaterial = iconMaterial;
    }

    public String getName() {
        return name;
    }

    // Метод генерирует красивую иконку для меню (с подсветкой, если вкладка открыта)
    public ItemStack getIcon(boolean isSelected) {
        ItemBuilder builder = new ItemBuilder(iconMaterial).setName("§e§l" + name);

        if (isSelected) {
            builder.setLore("§aВыбрано!");
        } else {
            builder.setLore("§7Нажмите, чтобы открыть");
        }

        return builder.build();
    }
}