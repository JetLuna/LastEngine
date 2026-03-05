package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DiscoBallGadget extends LobbyGadget {

    private ArmorStand armorStand;

    public DiscoBallGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        Location loc = player.getLocation().add(0, 3, 0); // Спавним над головой
        armorStand = player.getWorld().spawn(loc, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.getEquipment().setHelmet(new ItemStack(Material.BEACON));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (armorStand == null || !armorStand.isValid()) return true;

        armorStand.setRotation(tick * 10f, 0); // Крутим шар

        // Разбрасываем ноты во все стороны
        for (int i = 0; i < 3; i++) {
            armorStand.getWorld().spawnParticle(Particle.NOTE,
                    armorStand.getLocation().add(Math.random() * 4 - 2, Math.random() * 2 - 1, Math.random() * 4 - 2),
                    1, 0.0, 0.0, 0.0, 1.0, null);
        }

        // Музыка дискотеки
        if (tick % 10 == 0) {
            armorStand.getWorld().playSound(armorStand.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, (float) (Math.random() + 0.5));
        }
        return false;
    }

    @Override
    public void onClear() {
        if (armorStand != null) {
            armorStand.remove(); // Удаляем шар в конце
        }
    }
}