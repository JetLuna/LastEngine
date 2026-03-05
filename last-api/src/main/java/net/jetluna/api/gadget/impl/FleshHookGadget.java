package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FleshHookGadget extends LobbyGadget {

    private Item hook;

    public FleshHookGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        hook = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.TRIPWIRE_HOOK));
        hook.setPickupDelay(32767);
        hook.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (hook == null || !hook.isValid() || hook.isOnGround() || tick > 40) return true;

        hook.getWorld().spawnParticle(Particle.CRIT, hook.getLocation(), 2, 0.0, 0.0, 0.0, 0.05, null);

        for (org.bukkit.entity.Entity ent : hook.getNearbyEntities(1.5, 1.5, 1.5)) {
            if (ent instanceof Player target && ent != player) {
                // Вот она, защита от кражи NPC!
                if (target.hasMetadata("NPC")) continue;

                Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.5).setY(0.6);
                target.setVelocity(pull);

                hook.getWorld().playSound(hook.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 1f);

                return true;
            }
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