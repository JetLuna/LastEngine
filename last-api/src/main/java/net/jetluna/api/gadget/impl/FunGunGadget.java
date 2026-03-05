package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FunGunGadget extends LobbyGadget {

    // Теперь мы запоминаем все снежки, чтобы подорвать их в конце
    private final List<Snowball> snowballs = new ArrayList<>();

    public FunGunGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // БАХ в самом начале (выстрел из пушки)
        player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getEyeLocation(), 1);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.4f, 1.5f);

        // Выпускаем 25 снежков
        for (int i = 0; i < 25; i++) {
            Snowball sb = player.launchProjectile(Snowball.class);
            // Делаем разброс (дробь)
            Vector randomSpread = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(0.6);
            sb.setVelocity(player.getEyeLocation().getDirection().add(randomSpread).multiply(1.5));
            snowballs.add(sb);
        }
    }

    @Override
    public boolean onTick(int tick) {
        if (snowballs.isEmpty()) return true;

        // Следим за снежками. Если снежок врезался — делаем БАХ в конце!
        snowballs.removeIf(sb -> {
            if (!sb.isValid() || sb.isOnGround()) {
                sb.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, sb.getLocation(), 1);
                sb.getWorld().playSound(sb.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f); // Звук взрыва (высокий тон)
                return true;
            }
            return false;
        });

        // Гаджет работает, пока все снежки не взорвутся (максимум 4 секунды)
        if (tick > 80) return true;

        return false;
    }

    @Override
    public void onClear() {
        for (Snowball sb : snowballs) {
            if (sb.isValid()) sb.remove();
        }
        snowballs.clear();
    }
}