package net.jetluna.bedwars.team;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamManager {

    private final Map<TeamColor, GameTeam> teams = new HashMap<>();

    public TeamManager() {
        // Команды будут регистрироваться динамически при сканировании карты
    }

    // --- НОВЫЕ МЕТОДЫ ДЛЯ ArenaConfig ---

    // Очистить все активные команды
    public void clearActiveTeams() {
        this.teams.clear();
    }

    // Безопасно добавить новую команду в словарь
    public void addActiveTeam(GameTeam team) {
        if (team != null && team.getColor() != null) {
            this.teams.put(team.getColor(), team);
        }
    }

    // ------------------------------------

    public GameTeam getOrCreateTeam(TeamColor color) {
        return teams.computeIfAbsent(color, GameTeam::new);
    }

    public GameTeam getTeam(Player player) {
        for (GameTeam team : teams.values()) {
            if (team.hasPlayer(player)) return team;
        }
        return null;
    }

    public GameTeam getTeam(TeamColor color) {
        return teams.get(color);
    }

    public List<GameTeam> getActiveTeams() {
        return new ArrayList<>(teams.values());
    }

    public List<GameTeam> getAliveTeams() {
        List<GameTeam> alive = new ArrayList<>();
        for (GameTeam team : teams.values()) {
            // Команда жива, если у неё есть кровать ИЛИ остались живые игроки
            if (team.hasBed() || !team.getPlayers().isEmpty()) {
                alive.add(team);
            }
        }
        return alive;
    }

    public void removePlayerFromTeam(Player player) {
        GameTeam team = getTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }
    }
}