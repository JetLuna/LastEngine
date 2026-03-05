package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

public class TntGadget extends LobbyGadget {

    private TNTPrimed tnt;

    public TntGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Спавним зажженный ТНТ перед глазами
        tnt = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), TNTPrimed.class);
        tnt.setFuseTicks(30); // Взорвется через 1.5 секунды
        tnt.setVelocity(player.getEyeLocation().getDirection().multiply(0.85)); // Кидаем вперед
    }

    @Override
    public boolean onTick(int tick) {
        if (tnt == null || !tnt.isValid()) return true;

        // Перехватываем ТНТ ровно за 1 тик до ванильного взрыва
        if (tnt.getFuseTicks() <= 1) {

            // Кастомный взрыв (только визуал и звук)
            tnt.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, tnt.getLocation(), 1);
            tnt.getWorld().playSound(tnt.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.4f, 1.5f);

            // Раскидываем только игроков в радиусе 4 блоков
            for (org.bukkit.entity.Entity nearby : tnt.getNearbyEntities(4, 4, 4)) {
                if (nearby instanceof Player target) {
                    if (target.hasMetadata("NPC")) continue; // Защита NPC!

                    Vector dir = target.getLocation().toVector().subtract(tnt.getLocation().toVector()).normalize();
                    target.setVelocity(dir.multiply(1.3).add(new Vector(0, 0.8, 0)));
                }
            }

            // БЕЗОПАСНОЕ УДАЛЕНИЕ: блоки не пострадают!
            tnt.remove();
            return true;
        }
        return false;
    }

    @Override
    public void onClear() {
        // Если игрок ливнул до взрыва — просто удаляем ТНТ
        if (tnt != null && tnt.isValid()) {
            tnt.remove();
        }
    }
}