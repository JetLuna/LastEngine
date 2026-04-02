package net.jetluna.bedwars.manager;

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

    // --- ХРАНИЛИЩЕ ЗАРЯДОВ ВАМПИРИЗМА ---
    private final java.util.Map<java.util.UUID, Integer> vampirismCharges = new java.util.HashMap<>();

    public void addVampirismCharge(Player player) {
        vampirismCharges.put(player.getUniqueId(), getVampirismCharges(player) + 1);
    }

    public int getVampirismCharges(Player player) {
        return vampirismCharges.getOrDefault(player.getUniqueId(), 0);
    }

    public DeathManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleDeath(Player player, Player killer) {
        GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        // --- 1. ПЕРЕДАЕМ РЕСУРСЫ УБИЙЦЕ ---
        transferResources(player, killer);

        // --- 2. СОХРАНЯЕМ И ПОНИЖАЕМ УРОВЕНЬ ОРУЖИЯ ---
        plugin.getEquipmentManager().saveAndDowngradeEquipment(player);

        // --- 3. ОЧИЩАЕМ ИНВЕНТАРЬ (чтобы старые блоки и мечи исчезли) ---
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setFallDistance(0);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

        // 4. Анонс в чат и Экономика
        if (killer != null) {
            GameTeam killerTeam = plugin.getTeamManager().getTeam(killer);
            String kColor = killerTeam != null ? killerTeam.getColor().getChatColor().toString() : "§7";

            if (team.hasBed()) {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был убит " + kColor + killer.getName());
                plugin.getGameStats().addKill(killer.getUniqueId());
                plugin.getEconomyManager().addPoints(killer, 1);
                killer.sendMessage("§b+ 1 поинт §7за убийство!");
            } else {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был §c§lОКОНЧАТЕЛЬНО §7убит " + kColor + killer.getName());
                plugin.getEconomyManager().addPoints(killer, 5);
                plugin.getGameStats().addKill(killer.getUniqueId()); // ← добавить сюда
                killer.sendMessage("§b+ 5 поинтов §7за §cфинальное убийство!");
            }

            // --- ВАМПИРИЗМ ---
            int vampCharges = getVampirismCharges(killer);
            if (vampCharges > 0) {
                vampirismCharges.put(killer.getUniqueId(), vampCharges - 1);
                double newHealth = Math.min(20.0, killer.getHealth() + 6.0);
                killer.setHealth(newHealth);
                killer.sendMessage("§c❤ Вампиризм восстановил вам здоровье! (Осталось зарядов: " + (vampCharges - 1) + ")");
                killer.playSound(killer.getLocation(), Sound.ENTITY_WITCH_DRINK, 1f, 1f);
            }
        } else {
            if (team.hasBed()) {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7погиб.");
            } else {
                Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7был §c§lОКОНЧАТЕЛЬНО §7устранен.");
            }
        }

        // 5. Логика возрождения или вылета
        if (team.hasBed()) {
            startRespawn(player, team);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            plugin.getTeamManager().removePlayerFromTeam(player);

            player.sendTitle("§c§lВЫ ПОГИБЛИ", "§7Ваша кровать была разрушена!", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

            checkWinCondition();
        }
    }

    // ТВОЙ МЕТОД: Передача Железа, Алмазов и Опалов убийце
    private void transferResources(Player victim, Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        for (ItemStack item : victim.getInventory().getContents()) {
            if (item == null) continue;
            Material type = item.getType();
            if (type == Material.IRON_INGOT || type == Material.DIAMOND || type == Material.PRISMARINE_CRYSTALS) {
                drops.add(item.clone());
            }
        }

        if (drops.isEmpty()) return;

        if (killer != null) {
            for (ItemStack drop : drops) {
                killer.getInventory().addItem(drop).values().forEach(leftover ->
                        killer.getWorld().dropItem(killer.getLocation(), leftover)
                );
            }
            killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    private void startRespawn(Player player, GameTeam team) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(team.getSpawnLocation().clone().add(0, 10, 0));

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

                    // --- ИСПРАВЛЕНИЕ МЕЧЕЙ И ИНСТРУМЕНТОВ ---
                    // 1. Сначала выдаем меч и инструменты нужного уровня!
                    plugin.getEquipmentManager().giveRespawnEquipment(player);

                    // 2. Затем надеваем броню и чаруем мечи (Sharpness)
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
            GameTeam winner = plugin.getTeamManager().getAliveTeams().isEmpty() ? null : plugin.getTeamManager().getAliveTeams().get(0);

            Bukkit.broadcastMessage("§6§lИГРА ОКОНЧЕНА!");
            if (winner != null) {
                Bukkit.broadcastMessage("§eПобедила " + winner.getColor().getChatColor() + winner.getColor().getName() + " §eкоманда!");

                // --- СТАТИСТИКА: Топ выживших ---
                // Перебираем игроков победившей команды и записываем только живых (не спектаторов)
                for (java.util.UUID uuid : winner.getPlayers()) {
                    org.bukkit.entity.Player survivor = Bukkit.getPlayer(uuid);
                    if (survivor != null && survivor.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                        plugin.getGameStats().addSurvivor(uuid);
                    }
                }
            }

            // setGameState — строго ПОСЛЕ записи выживших!
            plugin.getGameManager().setGameState(new net.jetluna.bedwars.state.EndingState(plugin));
        }
    }
}