package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import java.util.Random;

public class PaintballGunGadget extends LobbyGadget {

    private Snowball snowball;
    private final Random random = new Random();

    public PaintballGunGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(2)); // Быстрый полет

        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.5f, 1.2f);
    }

    @Override
    public boolean onTick(int tick) {
        if (snowball == null) return true;

        // Если снежок пропал (во что-то врезался) или коснулся земли
        if (!snowball.isValid() || snowball.isOnGround()) {

            // Генерируем случайный цвет
            Color randomColor = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            Particle.DustOptions dust = new Particle.DustOptions(randomColor, 1.5f);

            // Рисуем красивое цветное пятно
            snowball.getWorld().spawnParticle(Particle.DUST, snowball.getLocation(), 40, 0.5, 0.5, 0.5, dust);
            snowball.getWorld().playSound(snowball.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1f, 1f);

            return true;
        }

        // Предохранитель: если снежок летит в пустоту дольше 5 секунд
        if (tick > 100) return true;

        return false;
    }

    @Override
    public void onClear() {
        if (snowball != null && snowball.isValid()) {
            snowball.remove();
        }
    }
}