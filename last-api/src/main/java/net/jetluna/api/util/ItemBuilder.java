package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    // Конструктор для Материала (Кровать, Компас...)
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    // !!! ВОТ ЭТОГО НЕ ХВАТАЛО !!!
    // Конструктор для готового предмета (Голова игрока и т.д.)
    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }
    

    // --- НАЗВАНИЕ ---
    public ItemBuilder setName(String text) {
        if (meta != null) meta.displayName(ChatUtil.parse(text));
        return this;
    }

    // Для совместимости
    public ItemBuilder name(String text) {
        return setName(text);
    }

    // --- ОПИСАНИЕ ---
    // Для конфига (List)
    public ItemBuilder setLore(List<String> lines) {
        if (meta != null && lines != null) {
            List<Component> components = new ArrayList<>();
            for (String line : lines) {
                components.add(ChatUtil.parse(line));
            }
            meta.lore(components);
        }
        return this;
    }

    // Для кода (String...)
    public ItemBuilder setLore(String... lines) {
        return lore(lines);
    }

    // Для совместимости
    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<Component> components = new ArrayList<>();
            for (String line : lines) {
                components.add(ChatUtil.parse(line));
            }
            meta.lore(components);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}