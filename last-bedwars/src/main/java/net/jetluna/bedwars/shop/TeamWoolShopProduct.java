package net.jetluna.bedwars.shop;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.resource.Resource;
import net.jetluna.bedwars.team.GameTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TeamWoolShopProduct extends ShopProduct {

    private final BedWarsPlugin plugin;
    private final int amount;

    public TeamWoolShopProduct(BedWarsPlugin plugin, int amount, Resource currency, int price) {
        super(new ItemStack(Material.WHITE_WOOL, amount), currency, price);
        this.plugin = plugin;
        this.amount = amount;
    }

    // Выдача шерсти правильного цвета
    @Override
    public void onBuy(Player player) {
        GameTeam team = plugin.getTeamManager().getTeam(player);
        Material woolMat = (team != null) ? team.getColor().getWool() : Material.WHITE_WOOL;
        player.getInventory().addItem(new ItemStack(woolMat, amount));
    }

    // Отрисовка правильного цвета в меню
    @Override
    public ItemStack getIcon(Player player) {
        ItemStack baseIcon = super.getIcon().clone();
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            baseIcon.setType(team.getColor().getWool());
        }
        return baseIcon;
    }
}