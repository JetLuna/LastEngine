package net.jetluna.bedwars.manager;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import net.jetluna.bedwars.team.TeamColor;
import net.jetluna.bedwars.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class ArenaScanner {

    private final BedWarsPlugin plugin;
    private final TeamManager teamManager;

    // Глобальные генераторы ресурсов (как в Плазмиксе)
    private final List<Location> diamondGenerators = new ArrayList<>();
    private final List<Location> emeraldGenerators = new ArrayList<>();

    // Локации для спавна NPC (потом будем их там спавнить)
    private final List<Location> shopNpcLocations = new ArrayList<>();
    private final List<Location> upgradeNpcLocations = new ArrayList<>();

    public ArenaScanner(BedWarsPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    public void scanArena(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return;

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minY = Math.max(world.getMinHeight(), center.getBlockY() - radius);
        int maxY = Math.min(world.getMaxHeight(), center.getBlockY() + radius);

        int blocksScanned = 0;
        int basesFound = 0;

        plugin.getLogger().info("Начинаем сканирование арены...");

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {

                int minZ = center.getBlockZ() - radius;
                int maxZ = center.getBlockZ() + radius;

                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType(); // <--- ВОТ ТА САМАЯ СТРОЧКА

                    if (type == Material.AIR) continue;
                    blocksScanned++;

                    // 1. Ищем кровати
                    if (type.name().endsWith("_BED")) {
                        TeamColor color = TeamColor.getByBed(type);
                        if (color != null) {
                            GameTeam team = teamManager.getOrCreateTeam(color);
                            team.setBedLocation(block.getLocation());
                        }
                        continue; // Кровать не удаляем на воздух!
                    }

                    // 2. Ищем глобальные генераторы
                    if (type == Material.EMERALD_BLOCK) {
                        emeraldGenerators.add(block.getLocation().add(0.5, 1, 0.5));
                        block.setType(Material.AIR);
                        continue;
                    }
                    if (type == Material.DIAMOND_BLOCK) {
                        diamondGenerators.add(block.getLocation().add(0.5, 1, 0.5));
                        block.setType(Material.AIR);
                        continue;
                    }

                    // 3. Ищем маркеры баз (Блок стоит НА шерсти определенного цвета)
                    Block blockBelow = block.getRelative(BlockFace.DOWN);
                    if (blockBelow.getType().name().endsWith("_WOOL")) {
                        TeamColor color = TeamColor.getByWool(blockBelow.getType());
                        if (color != null) {
                            GameTeam team = teamManager.getOrCreateTeam(color);

                            // Железный блок = Генератор базы (Бронза/Железо + Золото)
                            if (type == Material.IRON_BLOCK) {
                                team.setGeneratorLocation(block.getLocation().add(0.5, 0, 0.5));
                                block.setType(Material.AIR);
                                blockBelow.setType(Material.AIR);
                            }
                            // Маяк = Точка спавна игроков команды
                            else if (type == Material.BEACON) {
                                team.setSpawnLocation(block.getLocation().add(0.5, 0, 0.5));
                                block.setType(Material.AIR);
                                blockBelow.setType(Material.AIR);
                                basesFound++;
                            }
                            // Золотой блок = Магазин (Торговец)
                            else if (type == Material.GOLD_BLOCK) {
                                shopNpcLocations.add(block.getLocation().add(0.5, 0, 0.5));
                                block.setType(Material.AIR);
                                blockBelow.setType(Material.AIR);
                            }
                            // Лазуритовый блок = Улучшения
                            else if (type == Material.LAPIS_BLOCK) {
                                upgradeNpcLocations.add(block.getLocation().add(0.5, 0, 0.5));
                                block.setType(Material.AIR);
                                blockBelow.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }

        plugin.getLogger().info("Сканирование завершено! Проверено блоков: " + blocksScanned);
        plugin.getLogger().info("Найдено баз (команд): " + basesFound);
        plugin.getLogger().info("Найдено алмазных генераторов: " + diamondGenerators.size());
        plugin.getLogger().info("Найдено изумрудных генераторов: " + emeraldGenerators.size());
    }

    public List<Location> getDiamondGenerators() { return diamondGenerators; }
    public List<Location> getEmeraldGenerators() { return emeraldGenerators; }
    public List<Location> getShopNpcLocations() { return shopNpcLocations; }
    public List<Location> getUpgradeNpcLocations() { return upgradeNpcLocations; }
    public void setDiamondGenerators(List<Location> locs) {
        this.diamondGenerators.clear();
        if (locs != null) this.diamondGenerators.addAll(locs);
    }

    public void setEmeraldGenerators(List<Location> locs) {
        this.emeraldGenerators.clear();
        if (locs != null) this.emeraldGenerators.addAll(locs);
    }

    public void setShopNpcLocations(List<Location> locs) {
        this.shopNpcLocations.clear();
        if (locs != null) this.shopNpcLocations.addAll(locs);
    }

    public void setUpgradeNpcLocations(List<Location> locs) {
        this.upgradeNpcLocations.clear();
        if (locs != null) this.upgradeNpcLocations.addAll(locs);
    }

}