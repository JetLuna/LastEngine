package net.jetluna.bedwars.item;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum CustomItem {
    FIREBALL(Material.FIRE_CHARGE, "§6Огненный шар"),
    AUTO_BRIDGE(Material.EGG, "§eАвтоматический мост"),
    TNT_BOW(Material.BOW, "§cДинамитный лук"),
    VAMPIRE_SWORD(Material.DIAMOND_SWORD, "§cМеч вампира"),
    WITCHER_SWORD(Material.IRON_SWORD, "§dМеч ведьмака");

    private final Material material;
    private final String displayName;

    CustomItem(Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;
    }

    public ItemStack build(BedWarsPlugin plugin) {
        // 1. Создаем предмет БЕЗ .setName(), чтобы не было конфликта Component и String
        ItemStack item = new ItemBuilder(material).build();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 2. Устанавливаем имя напрямую через Paper API (он принимает Component)
            meta.displayName(ChatUtil.parse(displayName));

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

            // 3. Вешаем невидимую бирку
            NamespacedKey key = new NamespacedKey(plugin, "custom_item");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.name());

            item.setItemMeta(meta);
        }
        return item;
    }

    // Удобный метод проверки предмета
    public static CustomItem getCustomType(BedWarsPlugin plugin, ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        if (!item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) return null;

        try {
            return CustomItem.valueOf(item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING));
        } catch (Exception e) {
            return null;
        }
    }
}