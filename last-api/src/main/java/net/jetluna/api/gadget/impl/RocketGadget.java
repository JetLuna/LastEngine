package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class RocketGadget extends LobbyGadget {

    private ArmorStand rocket;

    public RocketGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        rocket = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        rocket.setVisible(false);
        rocket.setGravity(false); // Отключаем гравитацию для 1-секундной задержки
        rocket.getEquipment().setHelmet(new ItemStack(Material.FIREWORK_ROCKET));

        rocket.addPassenger(player);
    }

    @Override
    public boolean onTick(int tick) {
        if (rocket == null || !rocket.isValid()) return true;

        if (tick < 20) {
            // Подготовка к взлету
            rocket.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, rocket.getLocation(), 5, 0.2, 0.0, 0.2, 0.05, null);
        } else if (tick < 60) {
            // ВАЖНО: Включаем физику обратно, иначе не взлетит!
            if (tick == 20) {
                rocket.setGravity(true);
            }

            // Мощная тяга вверх
            rocket.setVelocity(new Vector(0, 1.2, 0));
            rocket.getWorld().spawnParticle(Particle.FLAME, rocket.getLocation(), 10, 0.2, 0.0, 0.2, 0.1, null);

            if (tick % 5 == 0) {
                rocket.getWorld().playSound(rocket.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1f);
            }
        } else {
            // Взрыв
            rocket.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, rocket.getLocation(), 1, 0.0, 0.0, 0.0, 0.0, null);
            rocket.getWorld().playSound(rocket.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
            return true;
        }
        return false;
    }

    @Override
    public void onClear() {
        if (rocket != null && rocket.isValid()) {
            rocket.remove();
        }
    }
}