package net.jetluna.bedwars.resource;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum Resource {

    // 2000L = 2 секунды
    IRON("Железо", 2000L, Material.IRON_INGOT, "§f§lЖелезо"),

    // 30000L = 30 секунд
    DIAMOND("Алмаз", 30000L, Material.DIAMOND, "§b§lАлмаз"),

    // 60000L = 60 секунд (1 минута). Предмет: Призмариновый кристалл
    OPAL("Опал", 60000L, Material.PRISMARINE_CRYSTALS, "§a§lОпал");

    private final String id;
    private final long delayMillis;
    private final Material material;
    private final String displayName;

    Resource(String id, long delayMillis, Material material, String displayName) {
        this.id = id;
        this.delayMillis = delayMillis;
        this.material = material;
        this.displayName = displayName;
    }

    public long getDelayMillis() { return delayMillis; }

    public String getDisplayName() { return displayName; }

    public Material getMaterial() { return material; }

    // Создаем красивый предмет, который нельзя будет спутать с ванильным
    public ItemStack buildItem() {
        ItemStack item = new ItemBuilder(material).build();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(ChatUtil.parse(displayName));
            item.setItemMeta(meta);
        }

        return item;
    }
}