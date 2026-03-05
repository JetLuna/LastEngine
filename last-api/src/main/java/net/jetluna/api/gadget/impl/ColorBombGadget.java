package net.jetluna.api.gadget.impl;

import net.jetluna.api.LastApi;
import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class ColorBombGadget extends LobbyGadget {

    private Item bomb;
    private final Random random = new Random();

    public ColorBombGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        bomb = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.CYAN_WOOL));
        bomb.setPickupDelay(32767);
        bomb.setVelocity(player.getEyeLocation().getDirection().multiply(0.8));

        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (bomb == null || !bomb.isValid()) return true;

        // Взрываемся при падении на землю ИЛИ через 3 секунды
        if (bomb.isOnGround() || tick > 60) {
            bomb.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, bomb.getLocation(), 1);
            bomb.getWorld().playSound(bomb.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);

            // Цвета для выпавшей шерсти
            Material[] colors = {Material.RED_WOOL, Material.BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.MAGENTA_WOOL};

            // Разбрасываем 15 кусков цветной шерсти
            for (int i = 0; i < 15; i++) {
                Item drop = bomb.getWorld().dropItem(bomb.getLocation(), new ItemStack(colors[random.nextInt(colors.length)]));
                drop.setPickupDelay(32767);
                drop.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5));

                // Убиваем этот кусочек шерсти через 2 секунды (40 тиков)
                Bukkit.getScheduler().runTaskLater(LastApi.getInstance(), () -> {
                    if (drop.isValid()) drop.remove();
                }, 40L);
            }

            // Откидываем игроков вокруг
            for (org.bukkit.entity.Entity ent : bomb.getNearbyEntities(3, 3, 3)) {
                if (ent instanceof Player target && ent != player) {
                    // Игнорируем NPC!
                    if (target.hasMetadata("NPC")) continue;

                    target.setVelocity(new Vector(0, 0.8, 0).add(target.getLocation().toVector().subtract(bomb.getLocation().toVector()).normalize().multiply(0.5)));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void onClear() {
        if (bomb != null && bomb.isValid()) {
            bomb.remove();
        }
    }
}