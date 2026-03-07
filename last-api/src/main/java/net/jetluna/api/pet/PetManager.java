package net.jetluna.api.pet;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        pet.setPersistent(false);

        if (pet instanceof Nameable) {
            pet.setCustomNameVisible(true);

            // --- ЛОКАЛИЗАЦИЯ ИМЕНИ ПИТОМЦА ---
            String rawName = LanguageManager.getString(player, "pets.entity_name").replace("%player%", player.getName());
            String formattedName = ChatColor.translateAlternateColorCodes('&', toLegacy(rawName));
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

                double dist2D = Math.sqrt(Math.pow(pLoc.getX() - eLoc.getX(), 2) + Math.pow(pLoc.getZ() - eLoc.getZ(), 2));

                if (!player.getWorld().equals(pet.getWorld()) || dist2D > 20.0) {
                    pet.teleport(player);
                    continue;
                }

                boolean isFlyingPet = (pet.getType() == EntityType.BAT || pet.getType() == EntityType.PARROT);

                if (dist2D > 2.5) {
                    Vector dir;
                    if (isFlyingPet) {
                        dir = pLoc.clone().add(0, 1.5, 0).toVector().subtract(eLoc.toVector()).normalize().multiply(0.35);
                    } else {
                        dir = new Vector(pLoc.getX() - eLoc.getX(), 0, pLoc.getZ() - eLoc.getZ()).normalize().multiply(0.35);

                        if (pet.isOnGround()) {
                            Location front = eLoc.clone().add(dir.clone().normalize().multiply(0.8));
                            if (front.add(0, 0.5, 0).getBlock().getType().isSolid()) {
                                dir.setY(0.5);
                            } else {
                                dir.setY(pet.getVelocity().getY());
                            }
                        } else {
                            dir.setY(pet.getVelocity().getY());
                        }
                    }

                    pet.setVelocity(dir);

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

    // Вспомогательный метод перевода
    private static String toLegacy(String text) {
        if (text == null) return "";
        String legacy = text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
        return legacy;
    }
}