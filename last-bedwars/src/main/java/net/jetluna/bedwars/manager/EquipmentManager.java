package net.jetluna.bedwars.manager;

import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.shop.ArmorType;
import net.jetluna.bedwars.team.GameTeam;
import net.jetluna.bedwars.team.TeamManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentManager {

    private final TeamManager teamManager;
    // Кэш купленной брони
    private final Map<UUID, ArmorType> playerArmor = new HashMap<>();

    public EquipmentManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public ArmorType getArmor(Player player) {
        return playerArmor.getOrDefault(player.getUniqueId(), ArmorType.LEATHER);
    }

    public void setArmor(Player player, ArmorType type) {
        playerArmor.put(player.getUniqueId(), type);
        updateEquipment(player); // Сразу обновляем экипировку
    }

    // Метод выдачи брони и мечей (вызываем при покупке и респавне)
    public void updateEquipment(Player player) {
        GameTeam team = teamManager.getTeam(player);
        if (team == null) return;

        ArmorType armor = getArmor(player);
        int protectionLevel = team.getUpgradeLevel("PROTECTION");
        int sharpnessLevel = team.getUpgradeLevel("SHARPNESS");

        // 1. НАДЕВАЕМ БРОНЮ
        ItemStack chest = new ItemBuilder(armor.getChestplate()).build();
        ItemStack boots = new ItemBuilder(armor.getBoots()).build();
        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS).build();

        // Делаем неразрушимыми через мету
        org.bukkit.inventory.meta.ItemMeta chestMeta = chest.getItemMeta();
        if (chestMeta != null) { chestMeta.setUnbreakable(true); chest.setItemMeta(chestMeta); }

        org.bukkit.inventory.meta.ItemMeta bootsMeta = boots.getItemMeta();
        if (bootsMeta != null) { bootsMeta.setUnbreakable(true); boots.setItemMeta(bootsMeta); }

        org.bukkit.inventory.meta.LeatherArmorMeta legMeta = (org.bukkit.inventory.meta.LeatherArmorMeta) leggings.getItemMeta();
        if (legMeta != null) {
            legMeta.setColor(getColorFromDye(team.getColor().getWool()));
            legMeta.setUnbreakable(true);
            leggings.setItemMeta(legMeta);
        }

        // Применяем зачарование Защиты (современное название PROTECTION)
        if (protectionLevel > 0) {
            chest.addEnchantment(Enchantment.PROTECTION, protectionLevel);
            leggings.addEnchantment(Enchantment.PROTECTION, protectionLevel);
            boots.addEnchantment(Enchantment.PROTECTION, protectionLevel);
        }

        player.getInventory().setChestplate(chest);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        // 2. ЗАЧАРОВЫВАЕМ МЕЧИ В ИНВЕНТАРЕ (современное название SHARPNESS)
        if (sharpnessLevel > 0) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType().name().endsWith("_SWORD")) {
                    item.addEnchantment(Enchantment.SHARPNESS, sharpnessLevel);
                }
            }
        }
    }

    private Color getColorFromDye(Material wool) {
        // Конвертируем материал шерсти в цвет для брони (упрощенно)
        switch (wool) {
            case RED_WOOL: return Color.RED;
            case BLUE_WOOL: return Color.BLUE;
            case LIME_WOOL: return Color.LIME;
            case YELLOW_WOOL: return Color.YELLOW;
            case LIGHT_BLUE_WOOL: return Color.AQUA;
            case WHITE_WOOL: return Color.WHITE;
            case PINK_WOOL: return Color.FUCHSIA;
            case GRAY_WOOL: return Color.GRAY;
            default: return Color.BLACK;
        }
    }
}