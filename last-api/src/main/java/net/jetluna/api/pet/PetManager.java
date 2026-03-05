package net.jetluna.api.pet;

import net.jetluna.api.LastApi;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Nameable;
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

        // !!! ФИКС ЗАСТЫВШИХ ПИТОМЦЕВ !!!
        // Запрещаем серверу сохранять эту сущность в файлы мира при выходе/рестарте
        pet.setPersistent(false);

        if (pet instanceof Nameable) {
            pet.setCustomNameVisible(true);
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

        if (pet instanceof Bat) {
            ((Bat) pet).setAwake(true);
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

                if (pet instanceof Bat) {
                    ((Bat) pet).setAwake(true);
                }

                Location pLoc = player.getLocation();
                Location eLoc = pet.getLocation();

                // !!! ФИКС ПОЛЕТА !!!
                // Считаем дистанцию ТОЛЬКО по горизонтали (X и Z). Игнорируем высоту.
                double dist2D = Math.sqrt(Math.pow(pLoc.getX() - eLoc.getX(), 2) + Math.pow(pLoc.getZ() - eLoc.getZ(), 2));

                // Телепортируем, если игрок улетел по плоскости дальше 20 блоков или сменил мир
                if (!player.getWorld().equals(pet.getWorld()) || dist2D > 20.0) {
                    pet.teleport(player);
                    continue;
                }

                boolean isFlyingPet = (pet.getType() == EntityType.BAT || pet.getType() == EntityType.PARROT);

                if (dist2D > 2.5) {
                    Vector dir;
                    if (isFlyingPet) {
                        // Летающие питомцы целятся игроку в плечо
                        dir = pLoc.clone().add(0, 1.5, 0).toVector().subtract(eLoc.toVector()).normalize().multiply(0.35);
                    } else {
                        // Наземные питомцы создают вектор только по плоскости
                        dir = new Vector(pLoc.getX() - eLoc.getX(), 0, pLoc.getZ() - eLoc.getZ()).normalize().multiply(0.35);

                        if (pet.isOnGround()) {
                            // Сканируем блок перед питомцем
                            Location front = eLoc.clone().add(dir.clone().normalize().multiply(0.8));
                            if (front.add(0, 0.5, 0).getBlock().getType().isSolid()) {
                                dir.setY(0.5); // Автопрыжок
                            } else {
                                dir.setY(pet.getVelocity().getY()); // Обычный бег
                            }
                        } else {
                            dir.setY(pet.getVelocity().getY()); // Падение
                        }
                    }

                    pet.setVelocity(dir);

                    // Голова питомца всегда смотрит по направлению движения
                    Location loc = pet.getLocation();
                    loc.setDirection(new Vector(dir.getX(), isFlyingPet ? dir.getY() : 0, dir.getZ()));
                    pet.setRotation(loc.getYaw(), loc.getPitch());
                }

                if (ticks % 15 == 0) {
                    Item drop = pet.getWorld().dropItem(eLoc.clone().add(0, 0.5, 0), new ItemStack(activePet.type.getDropItem()));
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