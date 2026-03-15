package net.jetluna.bedwars.manager;

import net.jetluna.bedwars.manager.EconomyManager;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DeathManager {

    private final BedWarsPlugin plugin;

    public DeathManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleDeath(Player player, Player killer) {
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        // --- ОЧИСТКА И ПЕРЕДАЧА ЛУТА ---
        transferResources(player, killer);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFallDistance(0); // Чтобы не разбился при телепорте
        player.setFireTicks(0);    // Тушим огонь
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType())); // Снимаем эффекты зелий
        // -------------------------------

        // 1. Анонс в чат и Экономика
        if (killer != null) {
            GameTeam killerTeam = plugin.getTeamManager().getTeam(killer);
            String kColor = killerTeam != null ? killerTeam.getColor().getChatColor().toString() : "§7";

            if (team.hasBed()) {
                // Обычное убийство
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был убит " + kColor + killer.getName());
                plugin.getEconomyManager().addPoints(killer, 1);
                killer.sendMessage("§b+ 1 поинт §7за убийство!");
            } else {
                // ФИНАЛЬНОЕ УБИЙСТВО
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был §c§lОКОНЧАТЕЛЬНО §7убит " + kColor + killer.getName());
                plugin.getEconomyManager().addPoints(killer, 5);
                killer.sendMessage("§b+ 5 поинтов §7за §cфинальное убийство!");
            }
        } else {
            // Если игрок умер сам (например, упал)
            if (team.hasBed()) {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7погиб.");
            } else {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был §c§lОКОНЧАТЕЛЬНО §7устранен.");
            }
        }

        // 2. Логика возрождения или вылета
        if (team.hasBed()) {
            startRespawn(player, team);
        } else {
            // ФИНАЛЬНАЯ СМЕРТЬ
            player.setGameMode(GameMode.SPECTATOR);
            plugin.getTeamManager().removePlayerFromTeam(player);

            player.sendTitle("§c§lВЫ ПОГИБЛИ", "§7Ваша кровать была разрушена!", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

            checkWinCondition(); // Проверяем, не закончилась ли игра
        }
    }

    // Метод для передачи Железа, Алмазов и Опалов убийце
    private void transferResources(Player victim, Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // Ищем ресурсы в инвентаре жертвы
        for (ItemStack item : victim.getInventory().getContents()) {
            if (item == null) continue;
            Material type = item.getType();
            // ВАЖНО: Заменили EMERALD на PRISMARINE_CRYSTALS
            if (type == Material.IRON_INGOT || type == Material.DIAMOND || type == Material.PRISMARINE_CRYSTALS) {
                drops.add(item.clone());
            }
        }

        if (drops.isEmpty()) return;

        // Если есть убийца - отдаем ему
        if (killer != null) {
            for (ItemStack drop : drops) {
                // Если инвентарь убийцы полон, ресурсы выпадут рядом с ним
                killer.getInventory().addItem(drop).values().forEach(leftover ->
                        killer.getWorld().dropItem(killer.getLocation(), leftover)
                );
            }
            killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
        // Если убился сам (упал в бездну), ресы просто испаряются
    }

    private void startRespawn(Player player, GameTeam team) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(team.getSpawnLocation().clone().add(0, 10, 0)); // Отправляем летать над базой

        new BukkitRunnable() {
            int time = 5;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (time <= 0) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(team.getSpawnLocation());
                    player.setHealth(20.0);
                    player.setFoodLevel(20);

                    // Возвращаем броню и деревянный меч
                    plugin.getEquipmentManager().updateEquipment(player);

                    player.sendTitle("§a§lВОЗРОЖДЕНИЕ", "", 0, 20, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    cancel();
                    return;
                }

                player.sendTitle("§cВы погибли!", "§7Возрождение через §e" + time + " §7сек.", 0, 25, 0);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void checkWinCondition() {
        if (plugin.getTeamManager().getAliveTeams().size() <= 1) {
            // ОСТАЛАСЬ ОДНА КОМАНДА!
            GameTeam winner = plugin.getTeamManager().getAliveTeams().isEmpty() ? null : plugin.getTeamManager().getAliveTeams().get(0);

            Bukkit.broadcastMessage("§6§lИГРА ОКОНЧЕНА!");
            if (winner != null) {
                Bukkit.broadcastMessage("§eПобедила " + winner.getColor().getChatColor() + winner.getColor().getName() + " §eкоманда!");
            }

            // ЗАПУСКАЕМ РЕСТАРТ АРЕНЫ:
            plugin.getGameManager().setGameState(new net.jetluna.bedwars.state.EndingState(plugin));
        }
    }
}