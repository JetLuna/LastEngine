package net.jetluna.bedwars.team;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameTeam {

    private final TeamColor color;
    private final List<UUID> players = new ArrayList<>();

    // Координаты базы
    private Location spawnLocation;
    private Location generatorLocation;
    private Location bedLocation;

    // Статус
    private boolean hasBed = true;

    // Прокачки (например: "SHARPNESS" -> 1, "PROTECTION" -> 2)
    private final Map<String, Integer> upgrades = new HashMap<>();

    public GameTeam(TeamColor color) {
        this.color = color;
    }

    // --- ИГРОКИ ---
    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    public List<UUID> getPlayers() {
        return players;
    }

    // --- ЛОКАЦИИ ---
    public void setSpawnLocation(Location spawnLocation) { this.spawnLocation = spawnLocation; }
    public Location getSpawnLocation() { return spawnLocation; }

    public void setGeneratorLocation(Location generatorLocation) { this.generatorLocation = generatorLocation; }
    public Location getGeneratorLocation() { return generatorLocation; }

    public void setBedLocation(Location bedLocation) { this.bedLocation = bedLocation; }
    public Location getBedLocation() { return bedLocation; }

    // --- СТАТУС ---
    public boolean hasBed() { return hasBed; }
    public void setHasBed(boolean hasBed) { this.hasBed = hasBed; }
    public TeamColor getColor() { return color; }

    // --- ПРОКАЧКИ ---
    public int getUpgradeLevel(String upgradeName) {
        return upgrades.getOrDefault(upgradeName.toUpperCase(), 0);
    }

    public void setUpgradeLevel(String upgradeName, int level) {
        upgrades.put(upgradeName.toUpperCase(), level);
    }
}