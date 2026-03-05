package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

public class EnderRiderGadget extends LobbyGadget {
    private EnderPearl pearl;

    public EnderRiderGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Спавним жемчуг и задаем ему скорость
        pearl = player.launchProjectile(EnderPearl.class);
        pearl.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));

        // Магия: сажаем игрока на летящий жемчуг!
        pearl.addPassenger(player);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (pearl == null || !pearl.isValid()) return true;

        // Красивый фиолетовый след в полете
        pearl.getWorld().spawnParticle(Particle.PORTAL, pearl.getLocation(), 5, 0.1, 0.1, 0.1, 0.1, null);

        // Если игрок слез (нажал Shift) или жемчуг врезался в блок
        if (pearl.getPassengers().isEmpty() || pearl.isOnGround()) {
            pearl.remove(); // Удаляем жемчуг, чтобы не было стандартного урона от телепорта
            return true;
        }

        return false;
    }

    @Override
    public void onClear() {
        if (pearl != null && pearl.isValid()) {
            pearl.remove();
        }
    }
}