package net.jetluna.bedwars.shop;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.UUID;

public class UpgradeGui implements Listener {

    private static BedWarsPlugin plugin;

    // Конструктор (нужно будет зарегистрировать в BedWarsPlugin)
    public UpgradeGui(BedWarsPlugin plugin) {
        UpgradeGui.plugin = plugin;
    }

    public static void open(Player player) {
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        Inventory inv = Bukkit.createInventory(null, 36, "§b§lУлучшения");

        // --- 1. КОМАНДНЫЕ УЛУЧШЕНИЯ ---
        inv.setItem(10, createItem(Material.IRON_SWORD, "§eОстрота мечей",
                team.hasSharpness() ? "§aУже куплено!" : "§7Цена: §b4 поинта", "§7Навсегда зачаровывает мечи и топоры."));

        int protLevel = team.getProtectionLevel();
        int protPrice = protLevel == 0 ? 4 : (protLevel == 1 ? 6 : (protLevel == 2 ? 8 : 10));
        inv.setItem(11, createItem(Material.IRON_CHESTPLATE, "§eУкрепленная броня " + (protLevel + 1),
                protLevel >= 4 ? "§aМаксимальный уровень!" : "§7Цена: §b" + protPrice + " поинтов", "§7Укрепляет любую броню команды."));

        int hasteLevel = team.getHasteLevel();
        int hastePrice = hasteLevel == 0 ? 4 : 6;
        inv.setItem(12, createItem(Material.GOLDEN_PICKAXE, "§eМастер-шахтер " + (hasteLevel + 1),
                hasteLevel >= 2 ? "§aМаксимальный уровень!" : "§7Цена: §b" + hastePrice + " поинтов", "§7Постоянный эффект Спешки."));

        inv.setItem(13, createItem(Material.BEACON, "§eИсцеляющая аура",
                team.hasHealPool() ? "§aУже куплено!" : "§7Цена: §b4 поинта", "§7Регенерация здоровья на базе."));

        // --- 2. ЛОВУШКИ ---
        inv.setItem(14, createItem(Material.COBWEB, "§eЛовушка: Усталость", "§7Цена: §b2 поинта", "§7Срабатывает, когда враг заходит на базу."));
        inv.setItem(15, createItem(Material.ENDER_EYE, "§eЛовушка: Слепота", "§7Цена: §b2 поинта", "§7Ослепляет врагов на вашей базе."));

        // --- 3. ПЕРСОНАЛЬНЫЕ ЗАРЯДЫ ---
        int vampCharges = plugin.getGameManager().getGameState() instanceof net.jetluna.bedwars.state.IngameState ?
                plugin.getDeathManager().getVampirismCharges(player) : 0;
        inv.setItem(20, createItem(Material.REDSTONE, "§cВампиризм §7(Зарядов: " + vampCharges + ")", "§7Цена: §b5 поинтов", "§7Лечит 3 сердца за убийство."));

        int cobCharges = plugin.getGameManager().getGameState() instanceof net.jetluna.bedwars.state.IngameState ?
                ((net.jetluna.bedwars.state.IngameState)plugin.getGameManager().getGameState()).getCobwebCharges(player) : 0;
        inv.setItem(21, createItem(Material.STRING, "§fБроня из паутины §7(Зарядов: " + cobCharges + ")", "§7Цена: §b10 поинтов", "§7Спасает от смертельного удара."));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§b§lУлучшения")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team == null || event.getCurrentItem() == null) return;

        int slot = event.getRawSlot();

        // Вспомогательный метод для покупки
        if (slot == 10 && !team.hasSharpness() && tryBuy(player, 4)) {
            team.setHasSharpness(true);
            updateTeamGear(team);
        } else if (slot == 11 && team.getProtectionLevel() < 4) {
            int price = team.getProtectionLevel() == 0 ? 4 : (team.getProtectionLevel() == 1 ? 6 : (team.getProtectionLevel() == 2 ? 8 : 10));
            if (tryBuy(player, price)) {
                team.setProtectionLevel(team.getProtectionLevel() + 1);
                updateTeamGear(team);
            }
        } else if (slot == 12 && team.getHasteLevel() < 2) {
            int price = team.getHasteLevel() == 0 ? 4 : 6;
            if (tryBuy(player, price)) {
                team.setHasteLevel(team.getHasteLevel() + 1);
                updateTeamGear(team);
            }
        } else if (slot == 13 && !team.hasHealPool() && tryBuy(player, 4)) {
            team.setHasHealPool(true);
            success(player, "Вы купили Исцеляющую ауру!");
        } else if (slot == 14 && tryBuy(player, 2)) {
            team.addTrap("FATIGUE");
            success(player, "Вы добавили ловушку: Усталость!");
        } else if (slot == 15 && tryBuy(player, 2)) {
            team.addTrap("BLINDNESS");
            success(player, "Вы добавили ловушку: Слепота!");
        } else if (slot == 20 && tryBuy(player, 5)) {
            plugin.getDeathManager().addVampirismCharge(player);
            success(player, "Вы купили заряд Вампиризма!");
        } else if (slot == 21 && tryBuy(player, 10)) {
            if (plugin.getGameManager().getGameState() instanceof net.jetluna.bedwars.state.IngameState) {
                ((net.jetluna.bedwars.state.IngameState)plugin.getGameManager().getGameState()).addCobwebCharge(player);
                success(player, "Вы купили Броню из паутины!");
            }
        }
        // Обновляем GUI после покупки
        open(player);
    }

    private boolean tryBuy(Player player, int price) {
        if (plugin.getEconomyManager().takePoints(player, price)) {
            return true;
        }
        player.sendMessage("§cНедостаточно поинтов!");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        return false;
    }

    private void success(Player player, String message) {
        player.sendMessage("§a" + message);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    private void updateTeamGear(GameTeam team) {
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getEquipmentManager().updateEquipment(p);
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                p.sendMessage("§eУлучшения вашей команды были обновлены!");
            }
        }
    }
}