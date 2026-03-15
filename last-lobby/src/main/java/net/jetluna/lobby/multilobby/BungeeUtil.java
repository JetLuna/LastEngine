package net.jetluna.lobby.multilobby;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.jetluna.lobby.LobbyPlugin;
import org.bukkit.entity.Player;

public class BungeeUtil {

    // Метод для отправки игрока на другой сервер
    public static void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        // Отправляем пакет прокси-серверу
        player.sendPluginMessage(LobbyPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }
}