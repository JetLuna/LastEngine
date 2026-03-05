package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BlackHoleGadget extends LobbyGadget {

    private Item blackHoleItem;

    public BlackHoleGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        blackHoleItem = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.BLACK_TERRACOTTA));
        blackHoleItem.setPickupDelay(32767);
        blackHoleItem.setVelocity(player.getEyeLocation().getDirection().multiply(1.3));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1f, 0.5f);
    }

    @Override
    public boolean onTick(int tick) {
        if (blackHoleItem == null || !blackHoleItem.isValid()) return true;

        if (blackHoleItem.isOnGround()) {
            blackHoleItem.setVelocity(new Vector(0, 0, 0));

            // Воспроизводим долгий жуткий звук только 1 раз в самом начале (на 20-й тик)
            if (tick == 20) {
                blackHoleItem.getWorld().playSound(blackHoleItem.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.8f, 0.5f);
            }
            // Короткие звуки пульсации раз в секунду (20 тиков), чтобы не забивать уши
            if (tick % 20 == 0) {
                blackHoleItem.getWorld().playSound(blackHoleItem.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.5f, 2.0f);
            }

            for (double t = 0; t <= 2 * Math.PI; t += Math.PI / 4) {
                blackHoleItem.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                        blackHoleItem.getLocation().clone().add(Math.cos(t) * 2.5, 0.2, Math.sin(t) * 2.5),
                        2, 0, 0, 0, 0.05, null);
                blackHoleItem.getWorld().spawnParticle(Particle.PORTAL,
                        blackHoleItem.getLocation().clone().add(Math.cos(t) * 1.5, 0.5, Math.sin(t) * 1.5),
                        5, 0.5, 0.5, 0.5, 0.1, null);
            }

            for (org.bukkit.entity.Entity ent : blackHoleItem.getNearbyEntities(8, 8, 8)) {
                if (ent instanceof LivingEntity && ent != player) {

                    // !!! ИГНОРИРУЕМ НПС !!!
                    if (ent.hasMetadata("NPC")) continue;

                    Vector pull = blackHoleItem.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.3);
                    ent.setVelocity(ent.getVelocity().add(pull));
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false));
                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
                }
            }
        }
        return false;
    }

    @Override
    public void onClear() {
        if (blackHoleItem != null && blackHoleItem.isValid()) {
            // ПРИНУДИТЕЛЬНО ВЫКЛЮЧАЕМ долгие звуки у всех игроков рядом, чтобы они не играли после исчезновения
            for (org.bukkit.entity.Entity ent : blackHoleItem.getNearbyEntities(15, 15, 15)) {
                if (ent instanceof Player) {
                    ((Player) ent).stopSound(Sound.ENTITY_ENDERMAN_STARE);
                    ((Player) ent).stopSound(Sound.BLOCK_BEACON_AMBIENT);
                }
            }

            blackHoleItem.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, blackHoleItem.getLocation(), 1);
            blackHoleItem.getWorld().playSound(blackHoleItem.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            blackHoleItem.remove();
        }
    }
}