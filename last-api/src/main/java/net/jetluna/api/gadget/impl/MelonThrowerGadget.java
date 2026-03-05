package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MelonThrowerGadget extends LobbyGadget {

    private Item melon;

    public MelonThrowerGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        melon = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.MELON));
        melon.setPickupDelay(32767);
        melon.setVelocity(player.getEyeLocation().getDirection().multiply(1.3));

        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (melon == null || !melon.isValid()) return true;

        // Оставляем красивый шлейф из долек арбуза, пока он летит
        melon.getWorld().spawnParticle(Particle.ITEM, melon.getLocation(), 2, 0.2, 0.2, 0.2, 0.05, new ItemStack(Material.MELON_SLICE));

        if (melon.isOnGround() || tick > 60) {
            // Сочный звук хруста (еда + ломающаяся слизь)
            melon.getWorld().playSound(melon.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.5f, 1f);
            melon.getWorld().playSound(melon.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1f, 1f);

            // Взрыв из кусков целого арбуза и мелких долек
            melon.getWorld().spawnParticle(Particle.BLOCK, melon.getLocation(), 40, 0.5, 0.5, 0.5, Bukkit.createBlockData(Material.MELON));
            melon.getWorld().spawnParticle(Particle.ITEM, melon.getLocation(), 15, 0.4, 0.4, 0.4, 0.1, new ItemStack(Material.MELON_SLICE));

            return true;
        }

        return false;
    }

    @Override
    public void onClear() {
        if (melon != null && melon.isValid()) {
            melon.remove();
        }
    }
}