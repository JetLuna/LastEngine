package net.jetluna.api.gadget.impl;

import net.jetluna.api.LastApi;
import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class FakeCreeperGadget extends LobbyGadget {
    private Creeper creeper;
    private final Random random = new Random();

    public FakeCreeperGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        // Спавним крипера в паре блоков от игрока
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(2));
        spawnLoc.setY(player.getLocation().getY());

        creeper = (Creeper) player.getWorld().spawnEntity(spawnLoc, EntityType.CREEPER);
        creeper.setAI(false); // Замораживаем его
        creeper.setInvulnerable(true);

        // Включаем жуткое шипение!
        player.playSound(creeper.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (creeper == null || !creeper.isValid()) return true;

        // Раздуваем крипера частицами дыма для эффекта
        if (tick % 5 == 0) {
            creeper.getWorld().spawnParticle(Particle.LARGE_SMOKE, creeper.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05, null);
        }

        if (tick > 30) { // Через 1.5 секунды фейк-взрыв!
            creeper.getWorld().playSound(creeper.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
            creeper.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, creeper.getLocation(), 1);

            // Выбрасываем безобидные цветочки вместо урона
            Material[] flowers = {Material.DANDELION, Material.POPPY, Material.CORNFLOWER};
            for (int i = 0; i < 8; i++) {
                Item drop = creeper.getWorld().dropItem(creeper.getLocation(), new ItemStack(flowers[random.nextInt(flowers.length)]));
                drop.setPickupDelay(32767);
                drop.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5));

                // Удаляем цветочки через 2 секунды
                Bukkit.getScheduler().runTaskLater(LastApi.getInstance(), drop::remove, 40L);
            }

            creeper.remove();
            return true;
        }
        return false;
    }

    @Override
    public void onClear() {
        if (creeper != null && creeper.isValid()) creeper.remove();
    }
}