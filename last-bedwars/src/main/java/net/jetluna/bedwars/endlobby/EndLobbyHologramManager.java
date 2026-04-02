package net.jetluna.bedwars.endlobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import net.jetluna.bedwars.GameStats;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndLobbyHologramManager {

    private static final String HOLO_ID = "end_game_top";

    public static void createGameTops(GameStats stats) {
        org.bukkit.World world = Bukkit.getWorld("world");
        if (world == null) return;
        Location holoLocation = new Location(world, 0.5, 75.0, 1015.5);

        Hologram oldHolo = DHAPI.getHologram(HOLO_ID);
        if (oldHolo != null) oldHolo.delete();

        Hologram holo = DHAPI.createHologram(HOLO_ID, holoLocation);

        // Страница 0 — убийства
        setKillsPage(holo.getPage(0), stats);

        // Страница 1 — кровати
        HologramPage bedsPage = holo.addPage();
        setBedsPage(bedsPage, stats);

        // Страница 2 — выжившие
        HologramPage survivalPage = holo.addPage();
        setSurvivalPage(survivalPage, stats);
    }

    private static void setKillsPage(HologramPage page, GameStats stats) {
        DHAPI.addHologramLine(page, "§6BedWars §eTeam");
        DHAPI.addHologramLine(page, "§eТоп игроков по §cУБИЙСТВАМ");
        DHAPI.addHologramLine(page, "§7за ОДНУ ИГРУ");
        DHAPI.addHologramLine(page, " ");

        List<Map.Entry<UUID, Integer>> top = stats.getTopKills();

        for (int i = 0; i < 5; i++) {
            String color = getPositionColor(i);

            if (i < top.size()) {
                UUID playerUUID = top.get(i).getKey();
                int kills = top.get(i).getValue();
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (name == null) name = "Неизвестно";

                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7" + name + " §8(" + kills + " киллов)");
            } else {
                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7Никого");
            }
        }
    }

    private static void setBedsPage(HologramPage page, GameStats stats) {
        DHAPI.addHologramLine(page, "§6BedWars §eTeam");
        DHAPI.addHologramLine(page, "§eТоп игроков по §cСЛОМАННЫМ КРОВАТЯМ");
        DHAPI.addHologramLine(page, "§7за ОДНУ ИГРУ");
        DHAPI.addHologramLine(page, " ");

        List<Map.Entry<UUID, Integer>> top = stats.getTopBeds();

        for (int i = 0; i < 5; i++) {
            String color = getPositionColor(i);

            if (i < top.size()) {
                UUID playerUUID = top.get(i).getKey();
                int beds = top.get(i).getValue();
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (name == null) name = "Неизвестно";

                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7" + name + " §8(" + beds + " кроватей)");
            } else {
                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7Никого");
            }
        }
    }

    private static void setSurvivalPage(HologramPage page, GameStats stats) {
        DHAPI.addHologramLine(page, "§6BedWars §eTeam");
        DHAPI.addHologramLine(page, "§eТоп §cВЫЖИВШИХ §eигроков");
        DHAPI.addHologramLine(page, "§7за ОДНУ ИГРУ");
        DHAPI.addHologramLine(page, " ");

        List<UUID> top = stats.getSurvivalTop();

        for (int i = 0; i < 5; i++) {
            String color = getPositionColor(i);

            if (i < top.size()) {
                UUID playerUUID = top.get(i);
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (name == null) name = "Неизвестно";

                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7" + name);
            } else {
                DHAPI.addHologramLine(page, color + (i + 1) + " место §f- §7Никого");
            }
        }
    }

    private static String getPositionColor(int position) {
        return switch (position) {
            case 0 -> "§e";
            case 1 -> "§f";
            case 2 -> "§6";
            default -> "§7";
        };
    }
}