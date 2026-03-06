package net.jetluna.lobby;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class FlyingPigManager implements Listener {

    private final LobbyPlugin plugin;
    private final NamespacedKey pigTag;
    private final NamespacedKey tntTag;
    private final Random random = new Random();

    private Pig bomberPig;
    private Bat batVehicle;

    public FlyingPigManager(LobbyPlugin plugin) {
        this.plugin = plugin;
        this.pigTag = new NamespacedKey(plugin, "bomber_pig");
        this.tntTag = new NamespacedKey(plugin, "fake_tnt");

        cleanupOldPigs();

        plugin.getServer().getScheduler().runTaskLater(plugin, this::spawnPig, 60L);

        startFlightControlTask();
        scheduleNextBomb();
    }

    public void cleanupOldPigs() {
        Location spawn = plugin.getLobbySpawn();
        if (spawn == null || spawn.getWorld() == null) return;

        // ПРИНУДИТЕЛЬНО ГРУЗИМ ЧАНК, чтобы Bukkit точно увидел старых мобов!
        spawn.getChunk().load();

        for (Entity entity : spawn.getWorld().getEntities()) {
            if (entity.getPersistentDataContainer().has(pigTag, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }

    // НОВЫЙ МЕТОД ДЛЯ УДАЛЕНИЯ ПРИ РЕСТАРТЕ
    public void clearAll() {
        if (bomberPig != null && !bomberPig.isDead()) bomberPig.remove();
        if (batVehicle != null && !batVehicle.isDead()) batVehicle.remove();
        cleanupOldPigs();
    }

    public void spawnPig() {
        Location spawn = plugin.getLobbySpawn();
        if (spawn == null || spawn.getWorld() == null) return;

        spawn.setY(spawn.getY() + 15);

        // Невидимая мышь (Наш двигатель)
        batVehicle = (Bat) spawn.getWorld().spawnEntity(spawn, EntityType.BAT);
        batVehicle.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        batVehicle.setSilent(true);
        batVehicle.setInvulnerable(true);
        batVehicle.setRemoveWhenFarAway(false);
        batVehicle.getPersistentDataContainer().set(pigTag, PersistentDataType.BYTE, (byte) 1);

        // Сама Свинья
        bomberPig = (Pig) spawn.getWorld().spawnEntity(spawn, EntityType.PIG);
        bomberPig.setInvulnerable(true);
        bomberPig.setCustomName("§d§lСвин-Бомбардировщик");
        bomberPig.setCustomNameVisible(true);
        bomberPig.setRemoveWhenFarAway(false);
        bomberPig.getPersistentDataContainer().set(pigTag, PersistentDataType.BYTE, (byte) 1);

        batVehicle.addPassenger(bomberPig);
    }

    private void startFlightControlTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (batVehicle == null || batVehicle.isDead() || bomberPig == null || bomberPig.isDead()) {
                    return;
                }

                Location center = plugin.getLobbySpawn();
                if (center == null || center.getWorld() == null) return;

                double targetY = center.getY() + 15;
                center.setY(targetY);

                Location currentLoc = batVehicle.getLocation();
                if (!currentLoc.getWorld().equals(center.getWorld())) return;

                if (currentLoc.distance(center) > 20) {
                    Vector directionToCenter = center.toVector().subtract(currentLoc.toVector()).normalize();
                    batVehicle.setVelocity(directionToCenter.multiply(0.4));
                } else if (currentLoc.getY() < targetY - 3) {
                    Vector velocity = batVehicle.getVelocity();
                    velocity.setY(0.3);
                    batVehicle.setVelocity(velocity);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void scheduleNextBomb() {
        int delayTicks = 600 + random.nextInt(1201);

        new BukkitRunnable() {
            @Override
            public void run() {
                scheduleNextBomb();

                if (bomberPig == null || bomberPig.isDead() || batVehicle == null || batVehicle.isDead()) {
                    cleanupOldPigs();
                    spawnPig();
                    return;
                }

                Location dropLoc = bomberPig.getLocation();
                World world = dropLoc.getWorld();
                if (world == null) return;

                TNTPrimed tnt = world.spawn(dropLoc, TNTPrimed.class);
                tnt.setFuseTicks(60);
                tnt.getPersistentDataContainer().set(tntTag, PersistentDataType.BYTE, (byte) 1);

                world.playSound(dropLoc, Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            }
        }.runTaskLater(plugin, delayTicks);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getPersistentDataContainer().has(tntTag, PersistentDataType.BYTE)) {
                event.blockList().clear();

                // --- СВИНЬЯ ОРЕТ В ЧАТ ---
                for (org.bukkit.entity.Player p : tnt.getWorld().getPlayers()) {
                    p.sendMessage("§d§lСвин-Бомбардировщик §8» §c§lБАБАХ!!!");
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getDamager();
            if (tnt.getPersistentDataContainer().has(tntTag, PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }
}