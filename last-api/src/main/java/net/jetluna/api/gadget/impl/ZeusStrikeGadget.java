package net.jetluna.api.gadget.impl;

import net.jetluna.api.gadget.LobbyGadget;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ZeusStrikeGadget extends LobbyGadget {
    public ZeusStrikeGadget(Player player) { super(player); }

    @Override
    public void onUse() {
        // Берем блок, на который смотрит игрок (макс дистанция 50)
        Block target = player.getTargetBlockExact(50);
        Location strikeLoc;

        if (target != null) {
            strikeLoc = target.getLocation();
        } else {
            // Если смотрит в небо, бьем в 50 блоках впереди
            strikeLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(50));
        }

        // Бьем визуальной молнией (без урона и поджогов)
        player.getWorld().strikeLightningEffect(strikeLoc);
    }

    @Override
    public boolean onTick(int tick) {
        return true; // Срабатывает моментально
    }

    @Override public void onClear() {}
}