package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TrampolineGadget extends LobbyGadget {

    private final List<BlockState> savedBlocks = new ArrayList<>();
    private Location center;

    public TrampolineGadget(Player player) {
        super(player);
    }

    @Override
    public void onUse() {
        // Подкидываем игрока перед спавном батута
        player.setVelocity(new Vector(0, 1.5, 0));
        center = player.getLocation().getBlock().getLocation();

        // Строим батут 5x5
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                org.bukkit.block.Block b = center.clone().add(x, 0, z).getBlock();
                savedBlocks.add(b.getState()); // Запоминаем старые блоки

                // Синяя обводка
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    b.setType(Material.BLUE_WOOL);
                } else {
                    b.setType(Material.SLIME_BLOCK); // Центр из слизи

                    // Кладем черный ковер поверх слизи (Y + 1)
                    org.bukkit.block.Block carpet = center.clone().add(x, 1, z).getBlock();
                    savedBlocks.add(carpet.getState());
                    carpet.setType(Material.BLACK_CARPET);
                }
            }
        }

        // Ставим ножки (заборы по углам)
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] corner : corners) {
            org.bukkit.block.Block leg = center.clone().add(corner[0], -1, corner[1]).getBlock();
            savedBlocks.add(leg.getState());
            leg.setType(Material.OAK_FENCE);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 1f, 1f);
    }

    @Override
    public boolean onTick(int tick) {
        if (center != null) {
            // Сканируем всех, кто находится на батуте
            for (org.bukkit.entity.Entity ent : center.getWorld().getNearbyEntities(center.clone().add(0, 1.5, 0), 2.5, 2.0, 2.5)) {
                if (ent instanceof Player p) {
                    // Ванильный прыжок (пробел) дает скорость по Y примерно 0.42.
                    // Если мы видим такую скорость, значит игрок прыгнул!
                    if (p.getVelocity().getY() > 0.1 && p.getVelocity().getY() < 0.5) {
                        // Перехватываем прыжок и отправляем в стратосферу!
                        p.setVelocity(p.getVelocity().setY(1.5));
                        p.playSound(p.getLocation(), Sound.ENTITY_SLIME_JUMP, 1f, 1f);
                    }
                }
            }
        }
        return false; // Гаджет ждет свои 10 секунд
    }

    @Override
    public void onClear() {
        // Чтобы ковры не выпадали как предметы при исчезновении слизи,
        // мы восстанавливаем блоки в ОБРАТНОМ порядке (сначала удалятся ковры, потом сама слизь)
        for (int i = savedBlocks.size() - 1; i >= 0; i--) {
            savedBlocks.get(i).update(true, false);
        }
        savedBlocks.clear();

        if (center != null) {
            center.getWorld().playSound(center, Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
        }
    }
}