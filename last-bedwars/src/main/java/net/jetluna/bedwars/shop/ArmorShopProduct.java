package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArmorShopProduct extends ShopProduct {

    private final ArmorType armorType;
    private final BedWarsPlugin plugin;

    public ArmorShopProduct(BedWarsPlugin plugin, ArmorType armorType) {
        // Вызываем наш метод для создания иконки без конфликта Component/String
        super(buildIcon(armorType), armorType.getCurrency(), armorType.getPrice());
        this.armorType = armorType;
        this.plugin = plugin;
    }

    // Вспомогательный метод для правильного создания предмета
    private static ItemStack buildIcon(ArmorType armorType) {
        ItemStack item = new ItemBuilder(armorType.getBoots()).build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtil.parse(armorType.getTitle()));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean processPurchase(Player player) {
        // Проверяем, не купил ли игрок эту броню (или лучше) ранее
        ArmorType currentArmor = plugin.getEquipmentManager().getArmor(player);
        if (currentArmor.ordinal() >= armorType.ordinal()) {
            ChatUtil.sendMessage(player, "§cУ вас уже есть эта броня (или лучше)!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }

        // Если все ок - вызываем стандартную покупку из ShopProduct
        return super.processPurchase(player);
    }

    @Override
    public void onBuy(Player player) {
        // Сообщаем менеджеру, что игрок купил новую броню
        plugin.getEquipmentManager().setArmor(player, armorType);
    }
}