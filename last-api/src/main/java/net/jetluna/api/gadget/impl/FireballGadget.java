package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class FireballGadget extends LobbyGadget {
    private Item fireball;

    public FireballGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        fireball = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.FIRE_CHARGE));
        fireball.setPickupDelay(32767);
        fireball.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (fireball == null || !fireball.isValid()) return true;

        // Шлейф из огня
        fireball.getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 5, 0.2, 0.2, 0.2, 0.05, null);
        fireball.getWorld().spawnParticle(Particle.SMOKE, fireball.getLocation(), 2, 0.1, 0.1, 0.1, 0.05, null);

        if (fireball.isOnGround() || tick > 60) {
            fireball.getWorld().playSound(fireball.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);
            fireball.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, fireball.getLocation(), 1);

            for (org.bukkit.entity.Entity ent : fireball.getNearbyEntities(3, 3, 3)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue;
                    Vector push = target.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize().multiply(1.2).setY(0.6);
                    target.setVelocity(push);
                    target.setFireTicks(40); // Поджигаем на 2 секунды для веселья
                }
            }
            return true;
        }
        return false;
    }

    @Override public void onClear() { if (fireball != null && fireball.isValid()) fireball.remove(); }
}