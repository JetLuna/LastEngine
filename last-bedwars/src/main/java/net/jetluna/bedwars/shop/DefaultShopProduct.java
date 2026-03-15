package net.jetluna.bedwars.shop;

import net.jetluna.bedwars.resource.Resource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DefaultShopProduct extends ShopProduct {

    private final ItemStack itemToGive;

    public DefaultShopProduct(ItemStack icon, ItemStack itemToGive, Resource currency, int price) {
        super(icon, currency, price);
        this.itemToGive = itemToGive;
    }

    @Override
    public void onBuy(Player player) {
        // Просто выдаем предмет в инвентарь
        player.getInventory().addItem(itemToGive.clone());
    }
}