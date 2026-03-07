package net.jetluna.api.parkour;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ParkourManager implements Listener {

    private static final Map<UUID, ParkourSession> activeSessions = new HashMap<>();
    private static final Random random = new Random();

    // Храним координаты занятых ячеек
    private static final Set<String> occupiedGrids = new HashSet<>();

    public static boolean isInParkour(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.SLIME_BALL) return;

        event.setCancelled(true);

        if (isInParkour(player)) {
            LanguageManager.sendMessage(player, "parkour.already_in");
            return;
        }

        startParkour(player);
    }

    public static void startParkour(Player player) {
        int playerGridX = (int) Math.round(player.getLocation().getX() / 32.0);
        int playerGridZ = (int) Math.round(player.getLocation().getZ() / 32.0);

        ParkourArena arena = null;

        outer:
        for (int radius = 0; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        int checkX = playerGridX + dx;
                        int checkZ = playerGridZ + dz;
                        String gridKey = checkX + "," + checkZ;

                        if (!occupiedGrids.contains(gridKey)) {
                            occupiedGrids.add(gridKey);
                            int startY = ((Math.abs(checkX) + Math.abs(checkZ)) % 2 == 0) ? 170 : 190;
                            arena = new ParkourArena(gridKey, checkX, checkZ, startY);
                            break outer;
                        }
                    }
                }
            }
        }

        if (arena == null) {
            LanguageManager.sendMessage(player, "parkour.server_full");
            return;
        }

        Location startLoc = player.getLocation().clone();
        startLoc.setX(arena.centerX);
        startLoc.setY(arena.startY);
        startLoc.setZ(arena.centerZ);
        startLoc.setPitch(0);

        Block current = startLoc.getBlock();
        current.setType(Material.LIME_CONCRETE);

        ParkourSession session = new ParkourSession(player.getLocation(), arena);
        session.currentBlock = current.getLocation();
        session.distance = 2;

        generateNextBlock(session);
        activeSessions.put(player.getUniqueId(), session);

        if (player.isFlying()) player.setFlying(false);
        player.setAllowFlight(false);

        Location tpLoc = current.getLocation().add(0.5, 1.0, 0.5);
        tpLoc.setYaw(player.getLocation().getYaw());
        tpLoc.setPitch(0f);

        player.teleport(tpLoc);

        LanguageManager.sendMessage(player, "parkour.started");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ParkourSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        if (player.isFlying() || player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        Location to = event.getTo();
        if (to == null) return;

        Block blockUnder = to.getBlock().getRelative(BlockFace.DOWN);
        if (blockUnder.getLocation().equals(session.nextBlock)) {

            session.currentBlock.getBlock().setType(Material.AIR);
            session.currentBlock = session.nextBlock;
            session.score++;

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);

            updateDistance(session);

            // --- ЛОКАЛИЗАЦИЯ ACTIONBAR ---
            String abRaw = LanguageManager.getString(player, "parkour.actionbar")
                    .replace("%score%", String.valueOf(session.score))
                    .replace("%distance%", String.valueOf(session.distance));

            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(toLegacy(abRaw)));

            if (session.score >= 50) {
                winParkour(player, session);
                return;
            }

            generateNextBlock(session);
            return;
        }

        if (player.getLocation().getY() < session.currentBlock.getY() - 3) {
            failParkour(player, session);
        }
    }

    private static void updateDistance(ParkourSession session) {
        int oldDistance = session.distance;
        if (session.score <= 10) session.distance = 2;
        else if (session.score <= 35) session.distance = 3;
        else session.distance = 4;

        if (session.distance > oldDistance) {
            Player p = Bukkit.getPlayer(session.returnLocation.getWorld().getUID());
            if (p != null) p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ParkourSession session = activeSessions.remove(event.getPlayer().getUniqueId());
        if (session != null) {
            session.currentBlock.getBlock().setType(Material.AIR);
            session.nextBlock.getBlock().setType(Material.AIR);
            occupiedGrids.remove(session.arena.gridKey);
        }
    }

    private static void generateNextBlock(ParkourSession session) {
        Location curLoc = session.currentBlock.clone();

        int dx = 0;
        int dz = 0;
        boolean foundValidSpot = false;

        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            dx = (int) Math.round(Math.cos(angle) * session.distance);
            dz = (int) Math.round(Math.sin(angle) * session.distance);

            if (dx == 0 && dz == 0) dx = session.distance;

            int testX = curLoc.getBlockX() + dx;
            int testZ = curLoc.getBlockZ() + dz;

            if (testX >= session.arena.minX && testX <= session.arena.maxX &&
                    testZ >= session.arena.minZ && testZ <= session.arena.maxZ) {
                foundValidSpot = true;
                break;
            }
        }

        if (!foundValidSpot) {
            dx = Integer.compare(session.arena.centerX, curLoc.getBlockX()) * session.distance;
            dz = Integer.compare(session.arena.centerZ, curLoc.getBlockZ()) * session.distance;
            if (dx == 0 && dz == 0) dx = session.distance;
        }

        Material nextMaterial = Material.LIGHT_BLUE_CONCRETE;
        int chance = random.nextInt(100);
        if (chance < 10) nextMaterial = Material.OAK_FENCE;
        else if (chance < 20) nextMaterial = Material.COBBLESTONE_WALL;

        boolean isFence = (nextMaterial == Material.OAK_FENCE || nextMaterial == Material.COBBLESTONE_WALL);
        int dy;

        if (session.distance >= 4) {
            if (isFence) dy = -1;
            else dy = random.nextInt(2) - 1;
        } else {
            if (isFence) dy = random.nextInt(2) - 1;
            else dy = random.nextInt(3) - 1;
        }

        if (curLoc.getY() + dy < session.arena.minY) {
            dy = 0;
            if (isFence && session.distance >= 4) {
                nextMaterial = Material.LIGHT_BLUE_CONCRETE;
            }
        }

        Location nextLoc = curLoc.add(dx, dy, dz);
        nextLoc.getBlock().setType(nextMaterial);
        session.nextBlock = nextLoc;
    }

    private static void restoreFlight(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank != null) {
            if (rank.getWeight() >= 3) player.setAllowFlight(true);
            else if (rank.getWeight() >= 2) player.setAllowFlight(true);
        }
    }

    private static void failParkour(Player player, ParkourSession session) {
        activeSessions.remove(player.getUniqueId());
        session.currentBlock.getBlock().setType(Material.AIR);
        session.nextBlock.getBlock().setType(Material.AIR);

        occupiedGrids.remove(session.arena.gridKey);

        player.teleport(session.returnLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);

        String msg = LanguageManager.getString(player, "parkour.failed").replace("%score%", String.valueOf(session.score));
        ChatUtil.sendMessage(player, msg);

        restoreFlight(player);
    }

    private static void winParkour(Player player, ParkourSession session) {
        activeSessions.remove(player.getUniqueId());
        session.currentBlock.getBlock().setType(Material.AIR);
        session.nextBlock.getBlock().setType(Material.AIR);

        occupiedGrids.remove(session.arena.gridKey);

        player.teleport(session.returnLocation);
        restoreFlight(player);

        NamespacedKey key = new NamespacedKey(LastApi.getInstance(), "parkour_cooldown");
        long lastTime = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG, 0L);
        long currentTime = System.currentTimeMillis();
        long ONE_DAY = 86400000L;

        if (currentTime - lastTime >= ONE_DAY) {
            PlayerStats stats = StatsManager.getStats(player);
            if (stats != null) {
                stats.setCoins(stats.getCoins() + 500);
                StatsManager.saveStats(player);
            }
            player.getPersistentDataContainer().set(key, PersistentDataType.LONG, currentTime);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

            LanguageManager.sendMessage(player, "parkour.win_first");
        } else {
            long timeLeft = ONE_DAY - (currentTime - lastTime);
            long hours = timeLeft / 3600000;
            long minutes = (timeLeft % 3600000) / 60000;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            LanguageManager.sendMessage(player, "parkour.win_again");
            String msg = LanguageManager.getString(player, "parkour.cooldown")
                    .replace("%hours%", String.valueOf(hours))
                    .replace("%minutes%", String.valueOf(minutes));
            ChatUtil.sendMessage(player, msg);
        }
    }

    private static class ParkourArena {
        String gridKey;
        int centerX, startY, centerZ, minY;
        int minX, maxX, minZ, maxZ;

        ParkourArena(String gridKey, int gridX, int gridZ, int startY) {
            this.gridKey = gridKey;
            this.centerX = gridX * 32;
            this.centerZ = gridZ * 32;
            this.startY = startY;
            this.minY = startY - 10;

            this.minX = centerX - 8;
            this.maxX = centerX + 7;
            this.minZ = centerZ - 8;
            this.maxZ = centerZ + 7;
        }
    }

    private static class ParkourSession {
        Location returnLocation;
        ParkourArena arena;
        Location currentBlock;
        Location nextBlock;
        int score = 0;
        int distance;

        ParkourSession(Location returnLocation, ParkourArena arena) {
            this.returnLocation = returnLocation;
            this.arena = arena;
        }
    }

    // --- Метод для конвертации MiniMessage в Legacy для ActionBar ---
    private static String toLegacy(String text) {
        if (text == null) return "";
        String legacy = text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }
}