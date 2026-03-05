package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SmashDownGadget extends LobbyGadget {

    public SmashDownGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Подкидываем игрока высоко вверх
        player.setVelocity(new Vector(0, 2.5, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        // Ждем минимум полсекунды (10 тиков), чтобы игрок успел взлететь
        // И как только он снова касается земли — БАБАХ!
        if (tick > 10 && player.isOnGround()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1);

            // Разлетающиеся куски земли (DIRT) для эпичности
            player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 150, 3.0, 0.5, 3.0, Bukkit.createBlockData(Material.DIRT));

            // Раскидываем всех вокруг
            for (org.bukkit.entity.Entity ent : player.getNearbyEntities(5, 5, 5)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue; // Защита NPC!
                    target.setVelocity(new Vector(0, 1.5, 0).add(target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize()));
                } else if (ent instanceof LivingEntity && !(ent instanceof Player) && ent != player) {
                    ent.setVelocity(new Vector(0, 1.5, 0).add(ent.getLocation().toVector().subtract(player.getLocation().toVector()).normalize()));
                }
            }
            return true; // Удар завершен!
        }

        // Предохранитель: если игрок упал в пропасть или завис дольше 5 секунд
        if (tick > 100) return true;

        return false;
    }

    @Override
    public void onClear() {
        // Частицы пропадают сами
    }
}