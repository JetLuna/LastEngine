package net.jetluna.bedwars.listeners;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.endlobby.TopNPCManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HologramClickListener implements Listener {

    private static final String HOLO_ID = "end_game_top";

    @EventHandler
    public void onHologramClick(HologramClickEvent event) {
        Hologram holo = DHAPI.getHologram(HOLO_ID);
        if (holo == null) return;
        if (!event.getHologram().getName().equals(HOLO_ID)) return;

        Player player = event.getPlayer();

        int currentPage = holo.getPlayerPage(player);
        int totalPages = holo.getPages().size();
        int nextPage = (currentPage + 1) % totalPages;

        // Переключаем голограмму только для этого игрока
        holo.show(player, nextPage);

        // Переключаем арморстенды только для этого игрока (строго в главном потоке)
        final int page = nextPage;
        Bukkit.getScheduler().runTask(BedWarsPlugin.getInstance(), () ->
                TopNPCManager.switchPageForPlayer(player, page)
        );
    }
}