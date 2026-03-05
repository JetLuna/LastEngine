package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LaserPointerGadget extends LobbyGadget {
    public LaserPointerGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        // Звук лазера
        player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 0.5f, 2f);

        Location loc = player.getEyeLocation();
        // Делаем шаг луча короче (0.25 блока вместо 0.5), чтобы он был намного плотнее
        Vector dir = loc.getDirection().normalize().multiply(0.25);

        // УВЕЛИЧИЛИ РАЗМЕР! Было 1.0f, стало 2.5f — теперь это реально жирный луч
        Particle.DustOptions laserColor = new Particle.DustOptions(Color.RED, 2.5f);

        // Так как шаг стал меньше, увеличиваем количество точек (120 * 0.25 = те же 30 блоков длины)
        for (int i = 0; i < 120; i++) {
            loc.add(dir);
            // Если луч врезался в стену — обрываем его
            if (!loc.getBlock().isPassable()) break;

            // Спавним по 2 частицы в одной точке, чтобы цвет был насыщенно-красным без просветов
            loc.getWorld().spawnParticle(Particle.DUST, loc, 2, 0, 0, 0, 0, laserColor);
        }
    }

    @Override
    public boolean onTick(int tick) {
        return true; // Срабатывает моментально
    }

    @Override public void onClear() {}
}