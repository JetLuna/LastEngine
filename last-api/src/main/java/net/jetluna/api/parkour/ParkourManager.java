package net.jetluna.api.parkour;

import net.jetluna.api.LastApi;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
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
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ParkourManager implements Listener {

    private static final Map<UUID, ParkourSession> activeSessions = new HashMap<>();
    private static final Random random = new Random();

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
            ChatUtil.sendMessage(player, "&cВы уже в паркуре! Прыгайте или упадите, чтобы отменить.");
            return;
        }

        startParkour(player);
    }

    public static void startParkour(Player player) {
        Location startLoc = player.getLocation().clone();
        startLoc.setY(150);
        startLoc.setPitch(0);

        Block current = startLoc.getBlock();
        current.setType(Material.LIME_CONCRETE);

        ParkourSession session = new ParkourSession(player.getLocation());
        session.currentBlock = current.getLocation();
        session.distance = 2;

        generateNextBlock(session);
        activeSessions.put(player.getUniqueId(), session);

        // ЖЕСТКО ВЫКЛЮЧАЕМ ФЛАЙ И ДВОЙНОЙ ПРЫЖОК
        if (player.isFlying()) player.setFlying(false);
        player.setAllowFlight(false);

        player.teleport(startLoc.clone().add(0.5, 1, 0.5));
        ChatUtil.sendMessage(player, "&aПаркур начался! Способности отключены.");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ParkourSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        // Защита: если игрок как-то включил флай - вырубаем
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

            // ИСПРАВЛЕННЫЙ Action Bar
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§eСчет: §6" + session.score + " §8| §eДистанция: §6" + session.distance));

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

        if (session.score <= 10) {
            session.distance = 2;
        } else if (session.score <= 35) {
            session.distance = 3;
        } else {
            session.distance = 4;
        }

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
        }
    }

    private static void generateNextBlock(ParkourSession session) {
        Location curLoc = session.currentBlock.clone();

        double angle = random.nextDouble() * 2 * Math.PI;
        int dx = (int) Math.round(Math.cos(angle) * session.distance);
        int dz = (int) Math.round(Math.sin(angle) * session.distance);
        int dy = random.nextInt(3) - 1;

        if (dx == 0 && dz == 0) dx = session.distance;

        Location nextLoc = curLoc.add(dx, dy, dz);
        nextLoc.getBlock().setType(Material.LIGHT_BLUE_CONCRETE);
        session.nextBlock = nextLoc;
    }

    // НОВЫЙ МЕТОД: Возвращаем флай по рангу (без SettingsGui)
    private static void restoreFlight(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank != null) {
            if (rank.getWeight() >= 3) { // Флай для PLUS и выше
                player.setAllowFlight(true);
            } else if (rank.getWeight() >= 2) { // Двойной прыжок для GO и выше
                player.setAllowFlight(true);
            }
        }
    }

    private static void failParkour(Player player, ParkourSession session) {
        activeSessions.remove(player.getUniqueId());
        session.currentBlock.getBlock().setType(Material.AIR);
        session.nextBlock.getBlock().setType(Material.AIR);

        player.teleport(session.returnLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        ChatUtil.sendMessage(player, "&cВы упали! Ваш счет: &e" + session.score);

        restoreFlight(player);
    }

    private static void winParkour(Player player, ParkourSession session) {
        activeSessions.remove(player.getUniqueId());
        session.currentBlock.getBlock().setType(Material.AIR);
        session.nextBlock.getBlock().setType(Material.AIR);

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
            ChatUtil.sendMessage(player, "&a&lПОЗДРАВЛЯЕМ! &7Вы прошли паркур и получили &6500 монет&7!");
        } else {
            long timeLeft = ONE_DAY - (currentTime - lastTime);
            long hours = timeLeft / 3600000;
            long minutes = (timeLeft % 3600000) / 60000;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            ChatUtil.sendMessage(player, "&a&lОТЛИЧНО! &7Вы снова покорили паркур!");
            ChatUtil.sendMessage(player, "&7Награда будет доступна через &e" + hours + "ч " + minutes + "м&7.");
        }
    }

    private static class ParkourSession {
        Location returnLocation;
        Location currentBlock;
        Location nextBlock;
        int score = 0;
        int distance;

        ParkourSession(Location returnLocation) {
            this.returnLocation = returnLocation;
        }
    }
}