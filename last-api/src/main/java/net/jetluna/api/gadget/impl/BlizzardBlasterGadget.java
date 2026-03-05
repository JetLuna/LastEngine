package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BlizzardBlasterGadget extends LobbyGadget {

    public BlizzardBlasterGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Звуки перенесены в onTick для 10-секундного зацикливания
    }

    @Override
    public boolean onTick(int tick) {
        if (tick > 200) {
            // Принудительно глушим звук ветра в ушах игрока в самом конце
            player.stopSound(Sound.ITEM_ELYTRA_FLYING);
            return true;
        }

        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();

        for (int i = 0; i < 15; i++) {
            loc.add(dir.clone().multiply(0.4));
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.5, 0.5, 0.5, 0.01, null);

            for (org.bukkit.entity.Entity ent : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                if (ent != player && ent instanceof LivingEntity) {

                    // !!! ИГНОРИРУЕМ НПС !!!
                    if (ent.hasMetadata("NPC")) continue;

                    ent.setVelocity(dir.clone().multiply(0.5).setY(0.2));
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2, false, false));
                }
            }
        }

        // Создаем непрерывный звук мощной вьюги (Элитры + дождь) раз в секунду
        if (tick % 20 == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.4f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.WEATHER_RAIN, 0.1f, 0.5f);
        }

        return false;
    }

    @Override
    public void onClear() {
        // Защита от зависания звука, если игрок переключил предмет раньше времени
        player.stopSound(Sound.ITEM_ELYTRA_FLYING);
    }
}