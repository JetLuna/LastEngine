package net.jetluna.api.staff;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class BungeeChannelListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();

            if (subchannel.equals("StaffAlert")) {
                short len = in.readShort();
                byte[] msgBytes = new byte[len];
                in.readFully(msgBytes);

                DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));

                // Читаем две переменные по очереди
                String baseMessage = msgIn.readUTF();
                String adminPart = msgIn.readUTF();

                // Отправляем в локальный бродкаст (он сам разберется, кому что показывать)
                StaffNotifier.broadcastLocal(baseMessage, adminPart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}