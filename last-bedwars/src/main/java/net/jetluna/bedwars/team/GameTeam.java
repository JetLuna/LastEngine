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

    // === СИСТЕМА ПРОКАЧЕК (УЛУЧШЕНИЯ КОМАНДЫ) ===
    private boolean hasSharpness = false;
    private int protectionLevel = 0; // 0, 1, 2, 3, 4
    private int hasteLevel = 0; // 0, 1, 2
    private boolean hasHealPool = false;

    private int efficiencyLevel = 0;
    private final java.util.List<String> traps = new java.util.ArrayList<>();

    // Геттеры и сеттеры для прокачек
    public boolean hasSharpness() { return hasSharpness; }
    public void setHasSharpness(boolean hasSharpness) { this.hasSharpness = hasSharpness; }

    public int getProtectionLevel() { return protectionLevel; }
    public void setProtectionLevel(int protectionLevel) { this.protectionLevel = protectionLevel; }

    public int getHasteLevel() { return hasteLevel; }
    public void setHasteLevel(int hasteLevel) { this.hasteLevel = hasteLevel; }

    public boolean hasHealPool() { return hasHealPool; }
    public void setHasHealPool(boolean hasHealPool) { this.hasHealPool = hasHealPool; }

    public java.util.List<String> getTraps() { return traps; }
    public void addTrap(String trapType) { this.traps.add(trapType); }

    public int getEfficiencyLevel() { return efficiencyLevel; }
    public void setEfficiencyLevel(int efficiencyLevel) { this.efficiencyLevel = efficiencyLevel; }
}