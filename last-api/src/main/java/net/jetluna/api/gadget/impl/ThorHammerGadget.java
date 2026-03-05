package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ThorHammerGadget extends LobbyGadget {
    private Item axe;

    public ThorHammerGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        axe = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.IRON_AXE));
        axe.setPickupDelay(32767);
        axe.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (axe == null || !axe.isValid()) return true;

        axe.setRotation(tick * 20f, 0); // Крутим молот
        axe.getWorld().spawnParticle(Particle.ENCHANTED_HIT, axe.getLocation(), 2, 0.1, 0.1, 0.1, 0.05, null);

        // Через 15 тиков молот начинает лететь обратно к тебе
        if (tick > 15) {
            Vector back = player.getEyeLocation().toVector().subtract(axe.getLocation().toVector()).normalize().multiply(1.2);
            axe.setVelocity(back);

            // Если вернулся в руки или летит слишком долго - удаляем
            if (axe.getLocation().distance(player.getEyeLocation()) < 2 || tick > 60) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                return true;
            }
        }

        // Проверка на попадание по игрокам
        for (org.bukkit.entity.Entity ent : axe.getNearbyEntities(1.5, 1.5, 1.5)) {
            if (ent instanceof Player target && ent != player) {
                if (target.hasMetadata("NPC")) continue; // Защита NPC

                // Визуальная молния без урона!
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.setVelocity(new Vector(0, 0.5, 0).add(axe.getVelocity().normalize().multiply(1.5)));
                return true; // Сразу исчезает после удара
            }
        }
        return false;
    }

    @Override public void onClear() { if (axe != null) axe.remove(); }
}