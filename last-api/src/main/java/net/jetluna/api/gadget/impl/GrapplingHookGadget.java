package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GrapplingHookGadget extends LobbyGadget {

    private Item hook;

    public GrapplingHookGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // И вот этот кусок тоже потерялся!
        hook = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.TRIPWIRE_HOOK));
        hook.setPickupDelay(32767);
        hook.setVelocity(player.getEyeLocation().getDirection().multiply(2.0));

        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (hook == null || !hook.isValid()) return true;

        // Короткий и чистый вызов партикла
        hook.getWorld().spawnParticle(Particle.CRIT, hook.getLocation(), 1);

        if (hook.isOnGround() || tick > 40) {
            Location hookLoc = hook.getLocation();
            Location playerLoc = player.getLocation();

            Vector pull = hookLoc.toVector().subtract(playerLoc.toVector()).normalize().multiply(1.8);
            pull.setY(pull.getY() + 0.6);

            player.setVelocity(pull);
            player.playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1.5f, 1f);

            hook.remove();
            return true;
        }

        return false;
    }

    @Override
    public void onClear() {
        if (hook != null && hook.isValid()) {
            hook.remove();
        }
    }
}