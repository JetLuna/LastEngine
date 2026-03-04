package net.jetluna.api.pet;

import net.jetluna.api.LastApi;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Nameable; // ВАЖНО: без этого импорта будет ошибка Nameable
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetManager {

    private static final Map<UUID, ActivePet> activePets = new HashMap<>();
    private static int ticks = 0;

    public static void spawnPet(Player player, PetType type) {
        removePet(player);

        Entity pet = player.getWorld().spawnEntity(player.getLocation(), type.getEntityType());

        pet.setInvulnerable(true);
        pet.setSilent(true);

    // Теперь Nameable будет распознаваться корректно
        if (pet instanceof Nameable) {
            pet.setCustomNameVisible(true);
            // Используем parseLegacy, так как setCustomName ждет String, а не Component
            String formattedName = ChatUtil.parseLegacy("&eПитомец &7" + player.getName());
            ((Nameable) pet).setCustomName(formattedName);
        }

        if (pet instanceof Mob) {
            ((Mob) pet).setAware(false);
        }

        if (pet instanceof Ageable) {
            ((Ageable) pet).setBaby();
            ((Ageable) pet).setAgeLock(true);
        }

        if (pet instanceof Tameable) {
            ((Tameable) pet).setTamed(true);
            ((Tameable) pet).setOwner(player);
        }

        if (pet instanceof Wolf) {
            ((Wolf) pet).setCollarColor(DyeColor.RED);
        }

        if (pet instanceof Snowman) {
            ((Snowman) pet).setDerp(false);
        }

        activePets.put(player.getUniqueId(), new ActivePet(pet, type));
    }

    public static void removePet(Player player) {
        ActivePet activePet = activePets.remove(player.getUniqueId());
        if (activePet != null && activePet.entity != null && activePet.entity.isValid()) {
            activePet.entity.remove();
        }
    }

    public static void startTask() {
        Bukkit.getScheduler().runTaskTimer(LastApi.getInstance(), () -> {
            ticks++;

            for (Player player : Bukkit.getOnlinePlayers()) {
                ActivePet activePet = activePets.get(player.getUniqueId());
                if (activePet == null) continue;

                Entity pet = activePet.entity;
                if (!pet.isValid()) {
                    activePets.remove(player.getUniqueId());
                    continue;
                }

                if (!player.getWorld().equals(pet.getWorld()) || player.getLocation().distanceSquared(pet.getLocation()) > 150) {
                    pet.teleport(player);
                    continue;
                }

                double dist = player.getLocation().distance(pet.getLocation());
                if (dist > 2.5) {
                    Vector dir = player.getLocation().toVector().subtract(pet.getLocation().toVector()).normalize();
                    dir.multiply(0.35);

                    if (pet.getType() == EntityType.BAT || pet.getType() == EntityType.PARROT) {
                        dir.setY((player.getLocation().getY() + 1.5 - pet.getLocation().getY()) * 0.2);
                    } else {
                        if (pet.isOnGround() && dir.getY() > 0.5) dir.setY(0.4);
                        else dir.setY(pet.getVelocity().getY());
                    }

                    pet.setVelocity(dir);

                    Location loc = pet.getLocation();
                    loc.setDirection(dir);
                    pet.setRotation(loc.getYaw(), loc.getPitch());
                }

                if (ticks % 15 == 0) {
                    Item drop = pet.getWorld().dropItem(pet.getLocation().add(0, 0.5, 0), new ItemStack(activePet.type.getDropItem()));
                    drop.setPickupDelay(32767);
                    drop.setVelocity(new Vector(Math.random() - 0.5, 0.3, Math.random() - 0.5).multiply(0.3));

                    Bukkit.getScheduler().runTaskLater(LastApi.getInstance(), drop::remove, 10L);
                }
            }
        }, 0L, 1L);
    }

    private static class ActivePet {
        Entity entity;
        PetType type;

        ActivePet(Entity entity, PetType type) {
            this.entity = entity;
            this.type = type;
        }
    }
}