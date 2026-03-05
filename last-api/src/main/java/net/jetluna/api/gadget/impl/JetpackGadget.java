package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class JetpackGadget extends LobbyGadget {
    public JetpackGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f);
    }

    @Override
    public boolean onTick(int tick) {
        if (tick > 40) return true; // Полет длится 2 секунды (40 тиков)

        // Толкаем игрока туда, куда он смотрит, но слегка приподнимаем вверх
        Vector dir = player.getLocation().getDirection().setY(0).normalize().multiply(0.6).setY(0.4);
        player.setVelocity(dir);

        // Частицы огня и дыма из-под ног
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 3, 0.2, 0.1, 0.2, 0.05, null);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 2, 0.2, 0.1, 0.2, 0.05, null);

        // Звук турбины
        if (tick % 5 == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.3f, 0.5f);
        }

        return false;
    }

    @Override public void onClear() {}
}