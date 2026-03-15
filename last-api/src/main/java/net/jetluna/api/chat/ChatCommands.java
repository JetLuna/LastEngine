package net.jetluna.api.chat;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.PlayerSettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ChatCommands implements CommandExecutor {

    private final String type;

    public ChatCommands(String type) {
        this.type = type;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Rank rank = RankManager.getRank(player);

        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? stats.getSuffix().replace("&", "§") : "";

        String formattedName = net.jetluna.api.util.NameFormatUtil.getFormattedName(player, rank);

        // --- STAFF CHAT (/sc) ---
        if (type.equals("sc")) {
            if (rank.getWeight() < 6) {
                LanguageManager.sendMessage(player, "chat.staff.no_permission");
                return true;
            }
            if (args.length == 0) {
                LanguageManager.sendMessage(player, "chat.staff.usage");
                return true;
            }

            String message = String.join(" ", args);
            String format = "&8[&bSC&8] " + formattedName + suffix + "&8: &b" + message;

            // Отправляем и сюда, и на другие серверы
            broadcastLocalAndNetwork(player, format, 6, "sc");
        }
        // --- DONOR CHAT (/dc) ---
        else if (type.equals("dc")) {
            if (rank.getWeight() < 2) {
                LanguageManager.sendMessage(player, "chat.donor.no_permission");
                return true;
            }

            // Запрещаем писать, если чат выключен в настройках!
            if (!PlayerSettingsManager.isDonateChatEnabled(player.getUniqueId())) {
                LanguageManager.sendMessage(player, "chat.donor.disabled_in_settings");
                return true;
            }

            if (args.length == 0) {
                LanguageManager.sendMessage(player, "chat.donor.usage");
                return true;
            }

            String message = String.join(" ", args);
            String format = "&8[&6DC&8] " + formattedName + suffix + "&8: &f" + message;

            // Отправляем и сюда, и на другие серверы
            broadcastLocalAndNetwork(player, format, 2, "dc");
        }

        return true;
    }

    // МЕТОД ОТПРАВКИ СООБЩЕНИЯ В СЕТЬ
    private void broadcastLocalAndNetwork(Player sender, String message, int minWeight, String chatType) {
        // 1. Показываем сообщение игрокам на текущем сервере
        broadcastLocal(message, minWeight, chatType);

        // 2. Отправляем скрытый сигнал на другие серверы через Velocity / BungeeCord
        try {
            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            msgOut.writeUTF(chatType);
            msgOut.writeInt(minWeight);
            msgOut.writeUTF(message);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Forward"); // Команда прокси-серверу: "Перешли это"
            out.writeUTF("ALL");     // Кому: "Всем остальным серверам"
            out.writeUTF("GlobalChat"); // Название нашего кастомного канала

            byte[] data = msgBytes.toByteArray();
            out.writeShort(data.length);
            out.write(data);

            sender.sendPluginMessage(LastApi.getInstance(), "BungeeCord", b.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ЛОКАЛЬНЫЙ БРОДКАСТ (Сделали static, чтобы вызывать из слушателя)
    public static void broadcastLocal(String message, int minWeight, String chatType) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank pRank = RankManager.getRank(p);

            if (pRank.getWeight() >= minWeight) {
                if (chatType.equals("dc") && !PlayerSettingsManager.isDonateChatEnabled(p.getUniqueId())) {
                    continue;
                }
                ChatUtil.sendMessage(p, message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(message));
    }
}