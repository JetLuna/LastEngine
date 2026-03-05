package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BatBlasterGadget extends LobbyGadget {
    private final List<Bat> bats = new ArrayList<>();
    private Vector direction;

    public BatBlasterGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        direction = player.getEyeLocation().getDirection().multiply(0.8);
        for (int i = 0; i < 15; i++) {
            Bat bat = player.getWorld().spawn(player.getEyeLocation(), Bat.class);
            bat.setAwake(true);
            bats.add(bat);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (bats.isEmpty()) return true;

        bats.removeIf(bat -> {
            if (!bat.isValid()) return true;

            // Заставляем мышей лететь вперед с легким разбросом
            Vector rand = new Vector((Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2);
            bat.setVelocity(direction.clone().add(rand));

            // Если мышь врезалась в игрока
            for (org.bukkit.entity.Entity ent : bat.getNearbyEntities(1.5, 1.5, 1.5)) {
                if (ent instanceof Player target && ent != player) {
                    if (target.hasMetadata("NPC")) continue;

                    target.setVelocity(bat.getLocation().getDirection().multiply(0.5).add(new Vector(0, 0.3, 0)));
                    bat.getWorld().spawnParticle(Particle.LARGE_SMOKE, bat.getLocation(), 5);
                    bat.getWorld().playSound(bat.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 1f);

                    bat.remove();
                    return true;
                }
            }
            return false;

        });

        if (tick > 40) return true; // Мыши живут всего 2 секунды
        return false;
    }

    @Override
    public void onClear() {
        for (Bat bat : bats) {
            if (bat.isValid()) {
                bat.getWorld().spawnParticle(Particle.LARGE_SMOKE, bat.getLocation(), 5);
                bat.remove();
            }
        }
        bats.clear();
    }
}