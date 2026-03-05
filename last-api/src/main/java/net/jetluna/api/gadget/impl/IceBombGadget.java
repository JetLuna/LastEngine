package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceBombGadget extends LobbyGadget {
    private Item ice;

    public IceBombGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        ice = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.ICE));
        ice.setPickupDelay(32767);
        ice.setVelocity(player.getEyeLocation().getDirection().multiply(1.2));
        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (ice == null || !ice.isValid()) return true;

        ice.getWorld().spawnParticle(Particle.SNOWFLAKE, ice.getLocation(), 2, 0.2, 0.2, 0.2, 0.05, null);

        if (ice.isOnGround() || tick > 60) {
            ice.getWorld().playSound(ice.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.8f);
            ice.getWorld().spawnParticle(Particle.BLOCK, ice.getLocation(), 50, 1.5, 1.5, 1.5, Bukkit.createBlockData(Material.ICE));
            ice.getWorld().spawnParticle(Particle.SNOWFLAKE, ice.getLocation(), 30, 1.5, 1.5, 1.5, 0.1, null);

            for (org.bukkit.entity.Entity ent : ice.getNearbyEntities(4, 4, 4)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue;

                    // Замораживаем! (Эффект инея на экране + замедление)
                    target.setFreezeTicks(100); // 5 секунд инея (работает на версиях 1.17+)
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
                    target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
                }
            }
            return true;
        }
        return false;
    }

    @Override public void onClear() { if (ice != null && ice.isValid()) ice.remove(); }
}