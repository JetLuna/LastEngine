package net.jetluna.auth;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    // Список тех, кто успешно вошел.
    // Если игрока тут нет — он не может двигаться.
    private final Set<UUID> authorizedPlayers = new HashSet<>();

    public boolean isAuthorized(Player player) {
        return authorizedPlayers.contains(player.getUniqueId());
    }

    public void setAuthorized(Player player) {
        authorizedPlayers.add(player.getUniqueId());
        player.sendMessage(net.jetluna.api.util.ChatUtil.parse("<green>Вы успешно вошли!"));
    }

    public void logout(Player player) {
        authorizedPlayers.remove(player.getUniqueId());
    }
}