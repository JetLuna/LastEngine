package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

public class ExplosiveSheepGadget extends LobbyGadget {
    private Sheep sheep;

    public ExplosiveSheepGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        sheep = player.getWorld().spawn(player.getLocation(), Sheep.class);
        sheep.setColor(DyeColor.WHITE);
        sheep.setVelocity(player.getEyeLocation().getDirection().multiply(1.2)); // Слегка кидаем ее вперед
        player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (sheep == null || !sheep.isValid()) return true;

        // Мигаем цветами как бомба с таймером
        if (tick % 5 == 0) {
            sheep.setColor(sheep.getColor() == DyeColor.WHITE ? DyeColor.RED : DyeColor.WHITE);
            sheep.getWorld().playSound(sheep.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
        }

        // Взрыв через 2.5 секунды (50 тиков)
        if (tick > 50) {
            sheep.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, sheep.getLocation(), 1);
            sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);

            for (org.bukkit.entity.Entity ent : sheep.getNearbyEntities(4, 4, 4)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue;
                    Vector push = target.getLocation().toVector().subtract(sheep.getLocation().toVector()).normalize().multiply(1.2).setY(0.8);
                    target.setVelocity(push);
                }
            }
            return true;
        }
        return false;
    }

    @Override public void onClear() { if (sheep != null) sheep.remove(); }
}