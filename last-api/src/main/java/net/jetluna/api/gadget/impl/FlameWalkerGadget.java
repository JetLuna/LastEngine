package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlameWalkerGadget extends LobbyGadget {
    public FlameWalkerGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1f);
        // Выдаем скорость на 10 секунд
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));
    }

    @Override
    public boolean onTick(int tick) {
        if (tick > 200) return true; // Работает 10 секунд

        // Спавним огонь прямо в ногах
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 0.1, 0), 2, 0.2, 0.1, 0.2, 0.01, null);

        if (tick % 10 == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1f);
        }
        return false;
    }

    @Override
    public void onClear() {
        // Если игрок убрал гаджет раньше времени — снимаем скорость
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}