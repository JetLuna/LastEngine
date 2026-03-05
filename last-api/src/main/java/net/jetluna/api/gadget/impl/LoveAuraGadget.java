package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoveAuraGadget extends LobbyGadget {
    public LoveAuraGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 1f, 1f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.5f);
    }

    @Override
    public boolean onTick(int tick) {
        if (tick > 20) return true; // Работает ровно 1 секунду (20 тиков), расширяясь

        Location loc = player.getLocation().add(0, 1, 0); // Поясница игрока
        double radius = tick * 0.25; // С каждым тиком кольцо становится шире (до 5 блоков)

        // Рисуем круг из сердечек (математика в деле!)
        for (double i = 0; i <= Math.PI * 2; i += Math.PI / 8) {
            double x = radius * Math.cos(i);
            double z = radius * Math.sin(i);
            loc.add(x, 0, z);
            loc.getWorld().spawnParticle(Particle.HEART, loc, 1, 0, 0, 0, 0, null);
            loc.subtract(x, 0, z); // Возвращаем центр на место
        }

        // На последнем тике раздаем баффы
        if (tick == 20) {
            for (org.bukkit.entity.Entity ent : player.getNearbyEntities(5, 5, 5)) {
                if (ent instanceof Player target) {
                    // Даем регенерацию на 5 секунд всем вокруг (и себе тоже)
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                }
            }
        }
        return false;
    }

    @Override public void onClear() {}
}