package net.jetluna.api.gadget.impl;

import net.jetluna.api.LastApi;
import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ChickenatorGadget extends LobbyGadget {
    private Chicken chicken;

    public ChickenatorGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        chicken = player.getWorld().spawn(player.getEyeLocation(), Chicken.class);
        chicken.setInvulnerable(true);
        chicken.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1.4f, 1.5f);
    }

    @Override
    public boolean onTick(int tick) {
        if (chicken == null || !chicken.isValid()) return true;

        // Курица сыплет яйцами в полете
        chicken.getWorld().spawnParticle(Particle.ITEM, chicken.getLocation(), 2, 0.1, 0.1, 0.1, 0, new ItemStack(Material.EGG));

        if (tick > 30 || chicken.isOnGround()) {
            chicken.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, chicken.getLocation(), 1);
            chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1.4f, 1.5f);

            for (org.bukkit.entity.Entity ent : chicken.getNearbyEntities(3, 3, 3)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue;
                    target.setVelocity(target.getLocation().toVector().subtract(chicken.getLocation().toVector()).normalize().multiply(1.2).setY(0.5));
                }
            }

            // Разлетается жареной курочкой
            for (int i = 0; i < 10; i++) {
                Item drop = chicken.getWorld().dropItem(chicken.getLocation(), new ItemStack(Material.COOKED_CHICKEN));
                drop.setPickupDelay(32767);
                drop.setVelocity(new Vector(Math.random() - 0.5, Math.random() / 2, Math.random() - 0.5));
                Bukkit.getScheduler().runTaskLater(LastApi.getInstance(), drop::remove, 30L); // Исчезнет через 1.5 сек
            }

            return true;
        }
        return false;
    }

    @Override public void onClear() { if (chicken != null) chicken.remove(); }
}