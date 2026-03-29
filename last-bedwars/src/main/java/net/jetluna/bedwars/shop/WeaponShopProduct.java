package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.resource.Resource;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeaponShopProduct extends ShopProduct {

    // Добавляем конструктор-заглушку, так как ShopProduct требует передачи базовых параметров
    public WeaponShopProduct() {
        super(new ItemStack(Material.WOODEN_SWORD), Resource.IRON, 0);
    }

    // 0 = Деревянный (выдается при спавне), 1 = Медный(Каменный), 2 = Железный, 3 = Алмазный, 4 = Незеритовый
    private int getSwordLevel(Player player) {
        int level = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material mat = item.getType();
            if (mat == Material.WOODEN_SWORD) level = Math.max(level, 0);
            else if (mat == Material.COPPER_SWORD) level = Math.max(level, 1); // Используем STONE как Медный
            else if (mat == Material.IRON_SWORD) level = Math.max(level, 2);
            else if (mat == Material.DIAMOND_SWORD) level = Math.max(level, 3);
            else if (mat == Material.NETHERITE_SWORD) level = Math.max(level, 4);
        }
        return level;
    }

    private SwordData getNextSwordData(int currentLevel) {
        switch (currentLevel) {
            case 0: return new SwordData(Material.COPPER_SWORD, "§6Медный меч", Resource.IRON, 10);
            case 1: return new SwordData(Material.IRON_SWORD, "§fЖелезный меч", Resource.DIAMOND, 7);
            case 2: return new SwordData(Material.DIAMOND_SWORD, "§bАлмазный меч", Resource.OPAL, 4);
            case 3: return new SwordData(Material.NETHERITE_SWORD, "§5Незеритовый меч", Resource.OPAL, 10);
            default: return null; // Максимум
        }
    }

    @Override
    public ItemStack getIcon(Player player) {
        int currentLevel = getSwordLevel(player);
        SwordData nextData = getNextSwordData(currentLevel);

        if (nextData == null) {
            return new ItemBuilder(Material.NETHERITE_SWORD)
                    .setName("§aМаксимальный уровень!")
                    .setLore("§7Вы уже купили лучший меч.")
                    .build();
        }

        ItemStack icon = new ItemBuilder(nextData.material).setName(nextData.name).build();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);

            // Собираем красивый лор в стиле твоего ShopProduct
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(ChatUtil.parse("§7Цена: " + nextData.resource.getDisplayName() + " §ex" + nextData.price));
            lore.add(ChatUtil.parse("§eНажмите, чтобы купить!"));
            meta.lore(lore);

            icon.setItemMeta(meta);
        }
        return icon;
    }

    // Полностью переопределяем процесс покупки для динамической цены
    @Override
    public boolean processPurchase(Player player) {
        int currentLevel = getSwordLevel(player);
        SwordData nextData = getNextSwordData(currentLevel);

        if (nextData == null) {
            ChatUtil.sendMessage(player, "§cУ вас уже максимальный уровень меча!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }

        // --- 1. ПРОВЕРКА РЕСУРСОВ В ИНВЕНТАРЕ ---
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == nextData.resource.getMaterial()) {
                count += item.getAmount();
            }
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
                    item.setAmount(0); // Удаляем стак
                }
            }
            if (needed <= 0) break;
        }

        // --- 3. ВЫДАЧА ПРЕДМЕТА ---
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        ChatUtil.sendMessage(player, "§aВы купили " + nextData.name + "§a!");

        // Удаляем старые мечи
        List<Material> allSwords = Arrays.asList(Material.WOODEN_SWORD, Material.COPPER_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && allSwords.contains(item.getType())) {
                player.getInventory().remove(item);
            }
        }

        // Выдаем новый меч
        ItemStack newSword = new ItemBuilder(nextData.material).setName(nextData.name).build();
        ItemMeta meta = newSword.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            newSword.setItemMeta(meta);
        }
        player.getInventory().addItem(newSword);

        // --- ДОБАВЬ ЭТУ СТРОКУ ---
        BedWarsPlugin.getInstance().getEquipmentManager().updateEquipment(player);

        ShopGui.open(player, ShopCategory.WEAPONS);
        return true;
    }

    @Override
    public void onBuy(Player player) {
        // Оставляем метод пустым, так как всю логику выдачи
        // мы перенесли в processPurchase для доступа к nextData
    }

    private static class SwordData {
        Material material; String name; Resource resource; int price;
        public SwordData(Material material, String name, Resource resource, int price) {
            this.material = material; this.name = name; this.resource = resource; this.price = price;
        }
    }
}