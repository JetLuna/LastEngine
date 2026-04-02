package net.jetluna.bedwars.endlobby;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.GameStats;
import net.jetluna.bedwars.team.GameTeam;
import net.jetluna.bedwars.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.util.*;

public class TopNPCManager {

    private static final java.util.Random random = new java.util.Random();

    // Храним 3 набора стендов: 0 = киллы, 1 = кровати, 2 = выжившие
    private static final List<ArmorStand> standsPage0 = new ArrayList<>();
    private static final List<ArmorStand> standsPage1 = new ArrayList<>();
    private static final List<ArmorStand> standsPage2 = new ArrayList<>();

    // Текущая страница каждого игрока
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    // Создаём все 3 набора сразу при старте EndingState
    public static void spawnAll(GameStats stats) {
        clearAll();

        World world = Bukkit.getWorld("world");
        if (world == null) return;

        Location[] locations = {
                new Location(world, 0.5,  75.0, 1017.5, 180f, 0f),
                new Location(world, 2.5,  74.0, 1016.5, 180f, 0f),
                new Location(world, -1.5, 74.0, 1016.5, 180f, 0f),
                new Location(world, 4.5,  73.0, 1015.5, 180f, 0f),
                new Location(world, -3.5, 73.0, 1015.5, 180f, 0f)
        };

        // Спавним все 3 страницы
        List<UUID> killsTop = stats.getTopKills().stream().map(Map.Entry::getKey).toList();
        spawnPageStands(killsTop, locations, standsPage0);

        List<UUID> bedsTop = stats.getTopBeds().stream().map(Map.Entry::getKey).toList();
        spawnPageStands(bedsTop, locations, standsPage1);

        spawnPageStands(stats.getSurvivalTop(), locations, standsPage2);

        // Всем игрокам показываем страницу 0, остальные скрыты
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerPages.put(player.getUniqueId(), 0);
            showPageForPlayer(player, 0);
        }
    }

    // Переключить страницу для конкретного игрока
    public static void switchPageForPlayer(Player player, int newPage) {
        int current = playerPages.getOrDefault(player.getUniqueId(), 0);
        playerPages.put(player.getUniqueId(), newPage);
        hidePageForPlayer(player, current);
        showPageForPlayer(player, newPage);
    }

    private static void showPageForPlayer(Player player, int page) {
        for (ArmorStand stand : getStandsForPage(page)) {
            player.showEntity(BedWarsPlugin.getInstance(), stand);
        }
    }

    private static void hidePageForPlayer(Player player, int page) {
        for (ArmorStand stand : getStandsForPage(page)) {
            player.hideEntity(BedWarsPlugin.getInstance(), stand);
        }
    }

    private static List<ArmorStand> getStandsForPage(int page) {
        return switch (page) {
            case 1  -> standsPage1;
            case 2  -> standsPage2;
            default -> standsPage0;
        };
    }

    private static void spawnPageStands(List<UUID> topPlayers, Location[] locations, List<ArmorStand> standsList) {
        for (int i = 0; i < 5; i++) {
            if (i >= topPlayers.size()) break;

            UUID playerUUID = topPlayers.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            Location loc = locations[i];

            loc.getChunk().load(true);

            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setSmall(true);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCanPickupItems(false);

            // --- МАГИЯ ЦВЕТА ---
            GameTeam team = null;
            Bukkit.getLogger().info("[DEBUG] Ищу команду для " + playerUUID);
            for (GameTeam t : BedWarsPlugin.getInstance().getTeamManager().getActiveTeams()) {
                Bukkit.getLogger().info("[DEBUG] Команда: " + t.getColor().name() + " players: " + t.getPlayers());
                if (t.getPlayers().contains(playerUUID)) {
                    team = t;
                    break;
                }
            }
            Bukkit.getLogger().info("[DEBUG] Найдена команда: " + (team != null ? team.getColor().name() : "NULL"));

            String chatColor = team != null ? team.getColor().getChatColor().toString() : "§7";
            Color armorColor = getBukkitColor(team != null ? team.getColor() : null);

            String playerName = offlinePlayer.getName();
            if (playerName == null) playerName = "N/A";

            stand.setCustomName(chatColor + playerName);
            stand.setCustomNameVisible(true);

            // Голова
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            if (headMeta != null) {
                headMeta.setOwningPlayer(offlinePlayer);
                head.setItemMeta(headMeta);
            }
            stand.getEquipment().setHelmet(head);

            // Броня
            stand.getEquipment().setChestplate(getColoredArmor(Material.LEATHER_CHESTPLATE, armorColor));
            stand.getEquipment().setLeggings(getColoredArmor(Material.LEATHER_LEGGINGS, armorColor));
            stand.getEquipment().setBoots(getColoredArmor(Material.LEATHER_BOOTS, armorColor));
            stand.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));

            // Позы
            if (i == 0) {
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-120), Math.toRadians(45), 0));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(-120), Math.toRadians(-45), 0));
            } else {
                int pose = random.nextInt(4);
                switch (pose) {
                    case 0 -> {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-80), 0, 0));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-80), 0, 0));
                    }
                    case 1 -> {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(20), 0, Math.toRadians(-20)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(20), 0, Math.toRadians(20)));
                    }
                    case 2 -> {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-120), 0, Math.toRadians(15)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(15), 0, Math.toRadians(20)));
                    }
                    case 3 -> {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-100), Math.toRadians(20), 0));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-60), Math.toRadians(-20), 0));
                    }
                }
            }

            // Скрываем от всех игроков сразу после спавна
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideEntity(BedWarsPlugin.getInstance(), stand);
            }

            standsList.add(stand);
        }
    }

    public static void clearAll() {
        for (ArmorStand s : standsPage0) if (s.isValid()) s.remove();
        for (ArmorStand s : standsPage1) if (s.isValid()) s.remove();
        for (ArmorStand s : standsPage2) if (s.isValid()) s.remove();
        standsPage0.clear();
        standsPage1.clear();
        standsPage2.clear();
        playerPages.clear();
    }

    private static ItemStack getColoredArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static Color getBukkitColor(TeamColor teamColor) {
        if (teamColor == null) return Color.WHITE;
        return switch (teamColor.name()) {
            case "RED"    -> Color.RED;
            case "BLUE"   -> Color.BLUE;
            case "GREEN"  -> Color.LIME;
            case "YELLOW" -> Color.YELLOW;
            case "AQUA"   -> Color.AQUA;
            case "WHITE"  -> Color.WHITE;
            case "PINK"   -> Color.FUCHSIA;
            case "GRAY"   -> Color.GRAY;
            default       -> Color.WHITE;
        };
    }
}