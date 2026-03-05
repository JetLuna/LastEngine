package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PortalGunGadget extends LobbyGadget {

    private Item portalItem;

    public PortalGunGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        portalItem = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.END_PORTAL_FRAME));
        portalItem.setPickupDelay(32767);
        portalItem.setVelocity(player.getEyeLocation().getDirection().multiply(1.5)); // Сила броска

        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 2f);
    }

    @Override
    public boolean onTick(int tick) {
        if (portalItem == null || !portalItem.isValid()) return true;

        // Рисуем фиолетовые частицы портала в полете
        portalItem.getWorld().spawnParticle(Particle.PORTAL, portalItem.getLocation(), 10, 0.2, 0.2, 0.2, 0.1, null);

        // Если предмет упал на землю ИЛИ прошло 3 секунды (60 тиков)
        if (portalItem.isOnGround() || tick > 60) {
            Location tpLocation = portalItem.getLocation();

            // Сохраняем направление взгляда игрока (чтобы его не развернуло в случайную сторону)
            tpLocation.setYaw(player.getLocation().getYaw());
            tpLocation.setPitch(player.getLocation().getPitch());

            // Телепортируем!
            player.teleport(tpLocation);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

            return true; // Телепорт успешен, закрываем гаджет
        }

        return false;
    }

    @Override
    public void onClear() {
        if (portalItem != null && portalItem.isValid()) {
            portalItem.remove(); // Удаляем рамку портала с земли
        }
    }
}