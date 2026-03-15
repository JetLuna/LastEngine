package net.jetluna.api.staff;

import net.jetluna.api.chat.ChatCommands;
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
                String baseMessage = msgIn.readUTF();
                String adminPart = msgIn.readUTF();

                StaffNotifier.broadcastLocal(baseMessage, adminPart);
            }
            // !!! НОВОЕ: ПРИЕМ ГЛОБАЛЬНОГО ЧАТА !!!
            else if (subchannel.equals("GlobalChat")) {
                short len = in.readShort();
                byte[] msgBytes = new byte[len];
                in.readFully(msgBytes);

                DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));

                // Распаковываем данные в том же порядке, в котором запаковали
                String chatType = msgIn.readUTF();
                int minWeight = msgIn.readInt();
                String chatMessage = msgIn.readUTF();

                // Показываем сообщение игрокам на этом сервере
                ChatCommands.broadcastLocal(chatMessage, minWeight, chatType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}