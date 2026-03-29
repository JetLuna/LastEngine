package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.resource.Resource;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolShopProduct extends ShopProduct {

    public enum ToolType { PICKAXE, AXE }
    private final ToolType toolType;

    // Конструктор-заглушка (как у мечей)
    public ToolShopProduct(ToolType toolType) {
        super(new ItemStack(Material.WOODEN_PICKAXE), Resource.IRON, 0);
        this.toolType = toolType;
    }

    private int getToolLevel(Player player) {
        int level = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material mat = item.getType();
            if (toolType == ToolType.PICKAXE) {
                if (mat == Material.WOODEN_PICKAXE) level = Math.max(level, 1);
                else if (mat == Material.COPPER_PICKAXE) level = Math.max(level, 2);
                else if (mat == Material.IRON_PICKAXE) level = Math.max(level, 3);
                else if (mat == Material.DIAMOND_PICKAXE) level = Math.max(level, 4);
                else if (mat == Material.NETHERITE_PICKAXE) level = Math.max(level, 5);
            } else if (toolType == ToolType.AXE) {
                if (mat == Material.WOODEN_AXE) level = Math.max(level, 1);
                else if (mat == Material.COPPER_AXE) level = Math.max(level, 2);
                else if (mat == Material.IRON_AXE) level = Math.max(level, 3);
                else if (mat == Material.DIAMOND_AXE) level = Math.max(level, 4);
                else if (mat == Material.NETHERITE_AXE) level = Math.max(level, 5);
            }
        }
        return level;
    }

    private ToolData getNextToolData(int currentLevel) {
        if (toolType == ToolType.PICKAXE) {
            switch (currentLevel) {
                case 0: return new ToolData(Material.WOODEN_PICKAXE, "§eДеревянная кирка", Resource.IRON, 10);
                case 1: return new ToolData(Material.COPPER_PICKAXE, "§6Медная кирка", Resource.IRON, 20);
                case 2: return new ToolData(Material.IRON_PICKAXE, "§fЖелезная кирка", Resource.IRON, 40);
                case 3: return new ToolData(Material.DIAMOND_PICKAXE, "§bАлмазная кирка", Resource.DIAMOND, 6);
                case 4: return new ToolData(Material.NETHERITE_PICKAXE, "§5Незеритовая кирка", Resource.OPAL, 4);
                default: return null;
            }
        } else {
            switch (currentLevel) {
                case 0: return new ToolData(Material.WOODEN_AXE, "§eДеревянный топор", Resource.IRON, 10);
                case 1: return new ToolData(Material.COPPER_AXE, "§6Медный топор", Resource.IRON, 20);
                case 2: return new ToolData(Material.IRON_AXE, "§fЖелезный топор", Resource.IRON, 40);
                case 3: return new ToolData(Material.DIAMOND_AXE, "§bАлмазный топор", Resource.DIAMOND, 6);
                case 4: return new ToolData(Material.NETHERITE_AXE, "§5Незеритовый топор", Resource.OPAL, 4);
                default: return null;
            }
        }
    }

    @Override
    public ItemStack getIcon(Player player) {
        int currentLevel = getToolLevel(player);
        ToolData nextData = getNextToolData(currentLevel);

        if (nextData == null) {
            Material maxMat = toolType == ToolType.PICKAXE ? Material.NETHERITE_PICKAXE : Material.NETHERITE_AXE;
            return new ItemBuilder(maxMat).setName("§aМаксимальный уровень!").setLore("§7Вы уже купили лучший инструмент.").build();
        }

        ItemStack icon = new ItemBuilder(nextData.material).setName(nextData.name).build();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(ChatUtil.parse("§7Цена: " + nextData.resource.getDisplayName() + " §ex" + nextData.price));
            lore.add(ChatUtil.parse("§eНажмите, чтобы купить!"));
            meta.lore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    @Override
    public boolean processPurchase(Player player) {
        int currentLevel = getToolLevel(player);
        ToolData nextData = getNextToolData(currentLevel);

        if (nextData == null) {
            ChatUtil.sendMessage(player, "§cУ вас уже максимальный уровень этого инструмента!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }

        // --- 1. ПРОВЕРКА РЕСУРСОВ ---
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == nextData.resource.getMaterial()) count += item.getAmount();
        }

        if (count < nextData.price) {
            ChatUtil.sendMessage(player, "§cНедостаточно ресурсов!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }

        // --- 2. СПИСАНИЕ РЕСУРСОВ ---
        int needed = nextData.price;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == nextData.resource.getMaterial()) {
                if (item.getAmount() > needed) {
                    item.setAmount(item.getAmount() - needed);
                    break;
                } else {
                    needed -= item.getAmount();
                    item.setAmount(0);
                }
            }
            if (needed <= 0) break;
        }

        // --- 3. ВЫДАЧА ИНСТРУМЕНТА ---
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        ChatUtil.sendMessage(player, "§aВы купили " + nextData.name + "§a!");

        // Удаляем старые инструменты этого типа
        List<Material> allMats = toolType == ToolType.PICKAXE
                ? Arrays.asList(Material.WOODEN_PICKAXE, Material.COPPER_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE)
                : Arrays.asList(Material.WOODEN_AXE, Material.COPPER_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && allMats.contains(item.getType())) {
                player.getInventory().remove(item);
            }
        }

        // Выдаем новый инструмент (без хардкода чар)
        ItemStack newItem = new ItemBuilder(nextData.material).setName(nextData.name).build();
        ItemMeta meta = newItem.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            // УДАЛИЛИ СТРОКУ С ЭФФЕКТИВНОСТЬЮ
            newItem.setItemMeta(meta);
        }
        player.getInventory().addItem(newItem);

        // --- ВАЖНО: Применяем командные чары на новые предметы! ---
        BedWarsPlugin.getInstance().getEquipmentManager().updateEquipment(player);

        ShopGui.open(player, ShopCategory.TOOLS);
        return true;
    }

    @Override
    public void onBuy(Player player) {}

    private static class ToolData {
        Material material; String name; Resource resource; int price;
        public ToolData(Material material, String name, Resource resource, int price) {
            this.material = material; this.name = name; this.resource = resource; this.price = price;
        }
    }
}