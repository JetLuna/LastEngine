package net.jetluna.api.staff;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private static final Set<UUID> recentJoins = new HashSet<>();

    public static void notifyJoin(Player player, String suffix) {
        if (recentJoins.contains(player.getUniqueId())) return;

        recentJoins.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(LastApi.getInstance(),
                () -> recentJoins.remove(player.getUniqueId()), 40L);

        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < STAFF_WEIGHT) return;

        String ip = player.getAddress().getAddress().getHostAddress();

        // --- БЕРЕМ КРАСИВЫЙ НИК ---
        String formattedName = NameFormatUtil.getFormattedName(player, rank);

        Bukkit.getScheduler().runTaskAsynchronously(LastApi.getInstance(), () -> {
            String location = getGeo(ip);

            // Передаем отформатированный ник в локальный бродкаст
            broadcastLocal(formattedName, suffix, location, ip);

            // Убираем %prefix% и заменяем %player% на наш готовый ник
            String defaultBase = color(LanguageManager.getString(null, "staff.join_message"))
                    .replace("%prefix%", "")
                    .replace("%player%", formattedName)
                    .replace("%suffix%", suffix);

            String defaultAdmin = color(LanguageManager.getString(null, "staff.admin_info"))
                    .replace("%location%", location)
                    .replace("%ip%", ip);

            sendCrossServer(defaultBase, defaultAdmin);
        });
    }

    private static String getGeo(String ip) {
        if (ip.equals("127.0.0.1") || ip.startsWith("192.168.")) {
            return color(LanguageManager.getString(null, "staff.local_network"));
        }
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

        return color(LanguageManager.getString(null, "staff.unknown_location"));
    }

    // Изменили сигнатуру: теперь принимаем отформатированное имя вместо prefix + playerName
    public static void broadcastLocal(String formattedName, String suffix, String location, String ip) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank rank = RankManager.getRank(p);
            if (rank.getWeight() >= STAFF_WEIGHT) {

                String baseMessage = color(LanguageManager.getString(p, "staff.join_message"))
                        .replace("%prefix%", "")
                        .replace("%player%", formattedName)
                        .replace("%suffix%", suffix);

                if (rank.getWeight() >= ADMIN_WEIGHT) {
                    String adminPart = color(LanguageManager.getString(p, "staff.admin_info"))
                            .replace("%location%", location)
                            .replace("%ip%", ip);
                    ChatUtil.sendMessage(p, baseMessage + adminPart);
                } else {
                    ChatUtil.sendMessage(p, baseMessage);
                }
            }
        }
    }

    public static void broadcastLocal(String baseMessage, String adminPart) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank rank = RankManager.getRank(p);
            if (rank.getWeight() >= STAFF_WEIGHT) {
                if (rank.getWeight() >= ADMIN_WEIGHT) {
                    ChatUtil.sendMessage(p, baseMessage + adminPart);
                } else {
                    ChatUtil.sendMessage(p, baseMessage);
                }
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

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }
}