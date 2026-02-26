package net.jetluna.api.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    // !!! ВОТ ЭТОТ МЕТОД, КОТОРОГО НЕ ХВАТАЛО !!!
    public ItemBuilder setType(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemBuilder setName(String name) {
        if (meta != null) {
            meta.displayName(ChatUtil.parse(name));
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (meta != null && lore != null) {
            List<Component> components = new ArrayList<>();
            for (String line : lore) {
                components.add(ChatUtil.parse(line));
            }
            meta.lore(components);
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(String line) {
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(ChatUtil.parse(line));
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder setOwner(String owner) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            for (Enchantment enchant : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchant);
            }
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}