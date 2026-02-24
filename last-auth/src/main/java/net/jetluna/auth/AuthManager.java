package net.jetluna.auth;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    // Кто успешно вошел
    private final Set<UUID> authorizedPlayers = new HashSet<>();

    // Кто сейчас в процессе (вводит почту/код)
    private final Map<UUID, AuthSession> sessions = new HashMap<>();

    public boolean isAuthorized(Player player) {
        return authorizedPlayers.contains(player.getUniqueId());
    }

    public void setAuthorized(Player player) {
        authorizedPlayers.add(player.getUniqueId());
        sessions.remove(player.getUniqueId()); // Удаляем сессию, он уже вошел
    }

    // --- НОВЫЕ МЕТОДЫ ДЛЯ СЕССИЙ ---

    public AuthSession getSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new AuthSession());
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
        authorizedPlayers.remove(player.getUniqueId());
    }
}