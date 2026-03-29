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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentManager {

    private final TeamManager teamManager;
    // Кэш купленной брони
    private final Map<UUID, ArmorType> playerArmor = new HashMap<>();

    // --- ХРАНИЛИЩЕ СОХРАНЕННЫХ ПРЕДМЕТОВ ПОСЛЕ СМЕРТИ ---
    private final Map<UUID, SavedEquipment> savedEquipment = new HashMap<>();

    private static class SavedEquipment {
        int swordLevel;
        int pickaxeLevel;
        int axeLevel;
        boolean hasShears;
    }

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

    // --- ЛОГИКА СОХРАНЕНИЯ И ПОНИЖЕНИЯ УРОВНЯ ПРИ СМЕРТИ ---
    public void saveAndDowngradeEquipment(Player player) {
        SavedEquipment saved = new SavedEquipment();

        // 1. Анализируем текущий инвентарь игрока
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material mat = item.getType();

            // Мечи (0=Дерево, 1=Медь(Камень), 2=Железо, 3=Алмаз, 4=Незерит)
            if (mat == Material.STONE_SWORD) saved.swordLevel = Math.max(saved.swordLevel, 1);
            else if (mat == Material.IRON_SWORD) saved.swordLevel = Math.max(saved.swordLevel, 2);
            else if (mat == Material.DIAMOND_SWORD) saved.swordLevel = Math.max(saved.swordLevel, 3);
            else if (mat == Material.NETHERITE_SWORD) saved.swordLevel = Math.max(saved.swordLevel, 4);

            // Кирки (1=Дерево, 2=Медь, 3=Железо, 4=Алмаз, 5=Незерит)
            if (mat == Material.WOODEN_PICKAXE) saved.pickaxeLevel = Math.max(saved.pickaxeLevel, 1);
            else if (mat == Material.STONE_PICKAXE) saved.pickaxeLevel = Math.max(saved.pickaxeLevel, 2);
            else if (mat == Material.IRON_PICKAXE) saved.pickaxeLevel = Math.max(saved.pickaxeLevel, 3);
            else if (mat == Material.DIAMOND_PICKAXE) saved.pickaxeLevel = Math.max(saved.pickaxeLevel, 4);
            else if (mat == Material.NETHERITE_PICKAXE) saved.pickaxeLevel = Math.max(saved.pickaxeLevel, 5);

            // Топоры
            if (mat == Material.WOODEN_AXE) saved.axeLevel = Math.max(saved.axeLevel, 1);
            else if (mat == Material.STONE_AXE) saved.axeLevel = Math.max(saved.axeLevel, 2);
            else if (mat == Material.IRON_AXE) saved.axeLevel = Math.max(saved.axeLevel, 3);
            else if (mat == Material.DIAMOND_AXE) saved.axeLevel = Math.max(saved.axeLevel, 4);
            else if (mat == Material.NETHERITE_AXE) saved.axeLevel = Math.max(saved.axeLevel, 5);

            // Ножницы
            if (mat == Material.SHEARS) saved.hasShears = true;
        }

        // 2. ПОНИЖАЕМ УРОВНИ (Downgrade)
        saved.swordLevel = Math.max(0, saved.swordLevel - 1);
        saved.pickaxeLevel = Math.max(0, saved.pickaxeLevel - 1);
        saved.axeLevel = Math.max(0, saved.axeLevel - 1);

        savedEquipment.put(player.getUniqueId(), saved);
    }

    // --- ВЫДАЧА ИНСТРУМЕНТОВ И МЕЧА ПРИ ВОЗРОЖДЕНИИ ---
    public void giveRespawnEquipment(Player player) {
        SavedEquipment saved = savedEquipment.getOrDefault(player.getUniqueId(), new SavedEquipment());

        // 1. Выдаем меч в зависимости от сохраненного уровня
        Material swordMat = Material.WOODEN_SWORD; // По умолчанию (старт игры)
        String swordName = "§eДеревянный меч";
        if (saved.swordLevel == 1) { swordMat = Material.STONE_SWORD; swordName = "§6Медный меч"; }
        else if (saved.swordLevel == 2) { swordMat = Material.IRON_SWORD; swordName = "§fЖелезный меч"; }
        else if (saved.swordLevel == 3) { swordMat = Material.DIAMOND_SWORD; swordName = "§bАлмазный меч"; }
        else if (saved.swordLevel == 4) { swordMat = Material.NETHERITE_SWORD; swordName = "§5Незеритовый меч"; }

        player.getInventory().addItem(createUnbreakableTool(swordMat, swordName));

        // 2. Выдаем кирки и топоры
        if (saved.pickaxeLevel > 0) player.getInventory().addItem(getToolByLevel(saved.pickaxeLevel, true));
        if (saved.axeLevel > 0) player.getInventory().addItem(getToolByLevel(saved.axeLevel, false));

        // 3. Сохраняем ножницы
        if (saved.hasShears) player.getInventory().addItem(createUnbreakableTool(Material.SHEARS, "§fНожницы"));
    }

    private ItemStack getToolByLevel(int level, boolean isPickaxe) {
        Material mat; String name;
        if (isPickaxe) {
            if (level == 1) { mat = Material.WOODEN_PICKAXE; name = "§eДеревянная кирка"; }
            else if (level == 2) { mat = Material.STONE_PICKAXE; name = "§6Медная кирка"; }
            else if (level == 3) { mat = Material.IRON_PICKAXE; name = "§fЖелезная кирка"; }
            else if (level == 4) { mat = Material.DIAMOND_PICKAXE; name = "§bАлмазная кирка"; }
            else { mat = Material.NETHERITE_PICKAXE; name = "§5Незеритовая кирка"; }
        } else {
            if (level == 1) { mat = Material.WOODEN_AXE; name = "§eДеревянный топор"; }
            else if (level == 2) { mat = Material.STONE_AXE; name = "§6Медный топор"; }
            else if (level == 3) { mat = Material.IRON_AXE; name = "§fЖелезный топор"; }
            else if (level == 4) { mat = Material.DIAMOND_AXE; name = "§bАлмазный топор"; }
            else { mat = Material.NETHERITE_AXE; name = "§5Незеритовый топор"; }
        }
        ItemStack tool = createUnbreakableTool(mat, name);
        return tool;
    }

    private ItemStack createUnbreakableTool(Material mat, String name) {
        ItemStack item = new ItemBuilder(mat).setName(name).build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setUnbreakable(true); item.setItemMeta(meta); }
        return item;
    }

    // Метод выдачи брони, и эффектов (вызываем при покупке и респавне)
    public void updateEquipment(Player player) {
        GameTeam team = teamManager.getTeam(player);
        if (team == null) return;

        ArmorType armor = getArmor(player);
        int protectionLevel = team.getProtectionLevel();
        boolean hasSharpness = team.hasSharpness();
        int hasteLevel = team.getHasteLevel();

        // 1. НАДЕВАЕМ БРОНЮ
        ItemStack chest = new ItemBuilder(armor.getChestplate()).build();
        ItemStack boots = new ItemBuilder(armor.getBoots()).build();
        ItemStack leggings = new ItemBuilder(Material.LEATHER_LEGGINGS).build();

        ItemMeta chestMeta = chest.getItemMeta();
        if (chestMeta != null) { chestMeta.setUnbreakable(true); chest.setItemMeta(chestMeta); }

        ItemMeta bootsMeta = boots.getItemMeta();
        if (bootsMeta != null) { bootsMeta.setUnbreakable(true); boots.setItemMeta(bootsMeta); }

        LeatherArmorMeta legMeta = (LeatherArmorMeta) leggings.getItemMeta();
        if (legMeta != null) {
            legMeta.setColor(getColorFromDye(team.getColor().getWool()));
            legMeta.setUnbreakable(true);
            leggings.setItemMeta(legMeta);
        }

        if (protectionLevel > 0) {
            if (chest.getType() != Material.AIR) chest.addUnsafeEnchantment(Enchantment.PROTECTION, protectionLevel);
            leggings.addUnsafeEnchantment(Enchantment.PROTECTION, protectionLevel);
            if (boots.getType() != Material.AIR) boots.addUnsafeEnchantment(Enchantment.PROTECTION, protectionLevel);
        }

        player.getInventory().setChestplate(chest);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);

        // 2. ЗАЧАРОВЫВАЕМ МЕЧИ И ИНСТРУМЕНТЫ
        int efficiencyLevel = team.getEfficiencyLevel(); // Берем уровень прокачки из команды

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            String itemName = item.getType().name();

            // Острота на мечи (и топоры, если нужно)
            if (hasSharpness && (itemName.endsWith("_SWORD") || itemName.endsWith("_AXE"))) {
                item.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
            }

            // Эффективность на кирки, топоры и ножницы (если уровень > 0)
            if (efficiencyLevel > 0 && (itemName.endsWith("_PICKAXE") || itemName.endsWith("_AXE") || itemName.equals("SHEARS"))) {
                item.addUnsafeEnchantment(Enchantment.EFFICIENCY, efficiencyLevel);
            }
        }

        // 3. ВЫДАЕМ ЭФФЕКТ СПЕШКИ (HASTE)
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.HASTE);
        if (hasteLevel > 0) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.HASTE, Integer.MAX_VALUE, hasteLevel - 1, false, false
            ));
        }
    }

    private Color getColorFromDye(Material wool) {
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