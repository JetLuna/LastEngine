package net.jetluna.api.punish;

import net.jetluna.api.LastApi;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class PunishmentManager {

    private static File file;
    private static YamlConfiguration config;

    public static void init(LastApi plugin) {
        file = new File(plugin.getDataFolder(), "punishments.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private static void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    // --- ПРОВЕРКИ ---

    public static boolean isMuted(String playerName) {
        String path = "mutes." + playerName.toLowerCase();
        if (!config.contains(path)) return false;
        long expire = config.getLong(path + ".expire");
        if (System.currentTimeMillis() > expire) {
            unmute(playerName, null);
            return false;
        }
        return true;
    }

    public static boolean isBanned(String playerName) {
        String path = "bans." + playerName.toLowerCase();
        if (!config.contains(path)) return false;
        long expire = config.getLong(path + ".expire");
        if (System.currentTimeMillis() > expire) {
            unban(playerName, null);
            return false;
        }
        return true;
    }

    // --- СООБЩЕНИЯ (НОВЫЙ ФОРМАТ) ---

    public static String getMuteMessage(String playerName) {
        String path = "mutes." + playerName.toLowerCase();
        long expire = config.getLong(path + ".expire");
        String reason = config.getString(path + ".reason");
        String admin = config.getString(path + ".admin");

        // Считаем, сколько осталось
        long remaining = expire - System.currentTimeMillis();
        String timeStr = (remaining > 0) ? TimeUtil.formatDuration(remaining) : "Истекает сейчас";

        return "\n<gray>=================================\n" +
                "<gradient:aqua:blue>LastEngine <dark_gray>- <red>Доступ к чату заблокирован!\n" +
                "<gray>Наказание истекает через: <yellow>" + timeStr + "\n" +
                "<gray>Причина: <white>" + reason + "\n" +
                "<gray>Наказание выдал: " + admin + "\n" +
                "<gray>Обжаловать: <green>forum.lastengine.net\n" +
                "<gray>=================================";
    }

    public static String getBanMessage(String playerName) {
        String path = "bans." + playerName.toLowerCase();
        long expire = config.getLong(path + ".expire");
        String reason = config.getString(path + ".reason");
        String admin = config.getString(path + ".admin");

        long remaining = expire - System.currentTimeMillis();
        String timeStr = (remaining > 0) ? TimeUtil.formatDuration(remaining) : "Истекает сейчас";

        return "\n<red>Доступ к серверу заблокирован!\n" +
                "<gray>Наказание истекает через: <yellow>" + timeStr + "\n" +
                "<gray>Причина: <white>" + reason + "\n" +
                "<gray>Наказание выдал: " + admin + "\n" +
                " \n" +
                "<gray>Обжаловать: <green>forum.lastengine.net";
    }

    public static String getKickMessage(String adminDisplay) {
        return "\n<red>Вы были кикнуты с сервера!\n" +
                "<gray>Наказание выдал: " + adminDisplay + "\n" +
                " \n" +
                "<gray>Обжаловать: <green>forum.lastengine.net";
    }

    // --- ДЕЙСТВИЯ ---

    public static void kick(Player target, String adminDisplay, String reason) {
        target.kick(ChatUtil.parse(getKickMessage(adminDisplay)));
        broadcastStaff(adminDisplay, "KICK", target.getName(), reason, "Сейчас");
    }

    public static void mute(String target, String adminDisplay, long time, String reason) {
        String path = "mutes." + target.toLowerCase();
        config.set(path + ".expire", System.currentTimeMillis() + time);
        config.set(path + ".reason", reason);
        config.set(path + ".admin", adminDisplay);

        String durationStr = TimeUtil.formatDuration(time); // Сохраняем длительность для истории
        addToHistory(target, "MUTE", adminDisplay, reason, durationStr);
        save();

        broadcastStaff(adminDisplay, "MUTE", target, reason, durationStr);
    }

    public static void ban(String target, String adminDisplay, long time, String reason) {
        String path = "bans." + target.toLowerCase();
        config.set(path + ".expire", System.currentTimeMillis() + time);
        config.set(path + ".reason", reason);
        config.set(path + ".admin", adminDisplay);

        String durationStr = TimeUtil.formatDuration(time);
        addToHistory(target, "BAN", adminDisplay, reason, durationStr);
        save();

        Player p = Bukkit.getPlayer(target);
        if (p != null) p.kick(ChatUtil.parse(getBanMessage(target)));

        broadcastStaff(adminDisplay, "BAN", target, reason, durationStr);
    }

    public static void unmute(String target, Player sender) {
        config.set("mutes." + target.toLowerCase(), null);
        save();
    }

    public static void unban(String target, Player sender) {
        config.set("bans." + target.toLowerCase(), null);
        save();
    }

    // --- ИСТОРИЯ И ПРОЧЕЕ ---

    private static void addToHistory(String target, String type, String admin, String reason, String duration) {
        // Записываем просто текущую дату для истории (когда выдали)
        // Для этого используем стандартную Java дату, чтобы не тянуть TimeUtil.formatDate
        String date = new java.text.SimpleDateFormat("dd.MM HH:mm").format(new java.util.Date());
        String entry = "<gray>[" + date + "] <gold>" + type + " <gray>(" + duration + ") by " + admin + ": <white>" + reason;

        List<String> history = config.getStringList("history." + target.toLowerCase() + "." + type);
        history.add(entry);
        config.set("history." + target.toLowerCase() + "." + type, history);
    }

    public static List<String> getHistory(String target, String type) {
        return config.getStringList("history." + target.toLowerCase() + "." + type);
    }

    public static Set<String> getActiveBans() {
        ConfigurationSection sec = config.getConfigurationSection("bans");
        return sec == null ? Set.of() : sec.getKeys(false);
    }

    public static boolean canPunish(Player admin, Player target) {
        if (admin.isOp()) return true;
        Rank adminRank = RankManager.getRank(admin);
        Rank targetRank = RankManager.getRank(target);
        return adminRank.getWeight() > targetRank.getWeight();
    }

    private static void broadcastStaff(String admin, String type, String target, String reason, String duration) {
        String msg = "<gray>[<red>Staff<gray>] " + admin +
                " <gray>issued <red><bold>" + type +
                " <gray>to <red>" + target +
                " <gray>for <white>" + reason +
                " <gray>(Duration: <yellow>" + duration + "<gray>)";

        for (Player p : Bukkit.getOnlinePlayers()) {
            Rank rank = RankManager.getRank(p);
            if (p.isOp() || rank.getWeight() >= 7) {
                ChatUtil.sendMessage(p, msg);
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatUtil.parseLegacy(msg));
    }
}