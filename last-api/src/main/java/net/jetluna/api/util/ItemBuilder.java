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

    public ItemBuilder setName(String name) {
        if (meta != null) {
            meta.displayName(ChatUtil.parse(name));
        }
        return this;
    }

    // Метод для списка (был раньше)
    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<Component> components = new ArrayList<>();
            for (String line : lore) {
                components.add(ChatUtil.parse(line));
            }
            meta.lore(components);
        }
        return this;
    }

    // !!! НОВЫЙ МЕТОД: Позволяет писать .setLore("Str1", "Str2") !!!
    // Именно его требует AuthItems
    public ItemBuilder setLore(String... lines) {
        return setLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(String line) {
        if (meta != null) {
            List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
            lore.add(ChatUtil.parse(line));
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow && meta != null) {
            // Исправленное зачарование (UNBREAKING вместо DURABILITY)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder setOwner(String playerName) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(playerName);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}