package net.jetluna.bedwars.manager;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    // Храним баланс поинтов для каждого игрока
    private final Map<UUID, Integer> points = new HashMap<>();

    public int getPoints(Player player) {
        return points.getOrDefault(player.getUniqueId(), 0);
    }

    public void addPoints(Player player, int amount) {
        points.put(player.getUniqueId(), getPoints(player) + amount);
    }

    public boolean takePoints(Player player, int amount) {
        int current = getPoints(player);
        if (current >= amount) {
            points.put(player.getUniqueId(), current - amount);
            return true;
        }
        return false;
    }

    public void clearEconomy() {
        points.clear();
    }
}