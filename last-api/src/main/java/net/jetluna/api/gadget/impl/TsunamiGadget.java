package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TsunamiGadget extends LobbyGadget {

    public TsunamiGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Звук всплеска при запуске
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1f, 0.5f);
    }

    @Override
    public boolean onTick(int tick) {
        // Волна идет 30 тиков (1.5 секунды)
        if (tick > 30) return true;

        Location loc = player.getLocation().add(0, 0.1, 0);
        // Вычисляем, насколько далеко ушла волна
        Vector forward = player.getLocation().getDirection().setY(0).normalize().multiply(tick * 0.4);
        loc.add(forward);

        // Рисуем саму волну из частиц брызг и падающей воды
        loc.getWorld().spawnParticle(Particle.SPLASH, loc, 40, 1.5, 0.5, 1.5, 0.0, null);
        loc.getWorld().spawnParticle(Particle.FALLING_WATER, loc, 40, 1.5, 0.5, 1.5, 0.0, null);

        // Сносим всех мобов и игроков, кроме самого себя
        for (org.bukkit.entity.Entity ent : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
            if (ent != player && ent instanceof LivingEntity) {
                // Подкидываем и толкаем по направлению волны
                ent.setVelocity(new Vector(0, 0.8, 0).add(forward.clone().normalize().multiply(0.8)));
            }
        }

        return false;
    }

    @Override
    public void onClear() {
        // Партиклы исчезают сами
    }
}