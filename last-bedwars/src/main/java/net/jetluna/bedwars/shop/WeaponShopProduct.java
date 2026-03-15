package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.resource.Resource;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeaponShopProduct extends ShopProduct {

    private final ItemStack sword;
    private final BedWarsPlugin plugin;

    public WeaponShopProduct(BedWarsPlugin plugin, Material swordMaterial, Resource currency, int price) {
        super(new ItemBuilder(swordMaterial).build(), currency, price);

        ItemStack s = new ItemBuilder(swordMaterial).build();
        org.bukkit.inventory.meta.ItemMeta meta = s.getItemMeta();
        if (meta != null) { meta.setUnbreakable(true); s.setItemMeta(meta); }

        this.sword = s;
        this.plugin = plugin;
    }

    @Override
    public void onBuy(Player player) {
        // Убираем старый деревянный меч, чтобы не засорять инвентарь
        player.getInventory().remove(Material.WOODEN_SWORD);

        // Выдаем новый меч
        player.getInventory().addItem(sword.clone());

        // Обновляем экипировку, чтобы наложилась "Острота" от прокачки базы
        plugin.getEquipmentManager().updateEquipment(player);
    }
}