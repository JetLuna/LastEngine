package net.jetluna.api.staff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jetluna.api.LastApi;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffNotifier {

    private static final int STAFF_WEIGHT = 6;
    private static final int ADMIN_WEIGHT = 9;

    // АНТИ-ДУБЛИКАТ: Храним тех, кто только что зашел
    private static final Set<UUID> recentJoins = new HashSet<>();

    public static void notifyJoin(Player player, String suffix) {
        if (recentJoins.contains(player.getUniqueId())) return;

        // Блокируем повторный вызов на 2 секунды
        recentJoins.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(LastApi.getInstance(),
                () -> recentJoins.remove(player.getUniqueId()), 40L);

        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < STAFF_WEIGHT) return;

        String ip = player.getAddress().getAddress().getHostAddress();

        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            String location = getGeo(ip);

            String prefix = rank.getPrefix()
                    .replace("<dark_red>", "&4").replace("<bold>", "&l")
                    .replaceAll("<[^>]+>", "");

            String baseMessage = (prefix + player.getName() + suffix + " &eзашел на сервер.").replace("&", "§");
            String adminPart = (" &7[" + location + "] &8(" + ip + ")").replace("&", "§");

            broadcastLocal(baseMessage, adminPart);
            sendCrossServer(baseMessage, adminPart);
        });
    }

    private static String getGeo(String ip) {
        if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")) return "Локальная сеть";
        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,country,city");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            if (json.get("status").getAsString().equals("success")) {
                return json.get("city").getAsString() + "/" + json.get("country").getAsString();
            }
        } catch (Exception ignored) {}
        return "Неизвестно";
    }

    public static void broadcastLocal(String baseMessage, String adminPart) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank rank = RankManager.getRank(p);
            if (rank.getWeight() >= ADMIN_WEIGHT) {
                ChatUtil.sendMessage(p, baseMessage + adminPart);
            }
            else if (rank.getWeight() >= STAFF_WEIGHT) {
                ChatUtil.sendMessage(p, baseMessage);
            }
        }
    }

    private static void sendCrossServer(String baseMessage, String adminPart) {
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("StaffAlert");
            java.io.ByteArrayOutputStream msgBytes = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream msgOut = new java.io.DataOutputStream(msgBytes);
            msgOut.writeUTF(baseMessage);
            msgOut.writeUTF(adminPart);
            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());

            Player sender = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            if (sender != null) {
                sender.sendPluginMessage(LastApi.getInstance(), "BungeeCord", b.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}