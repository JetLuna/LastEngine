package net.jetluna.api.friends;

import net.jetluna.api.LastApi;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendManager {

    private static File file;
    private static YamlConfiguration config;

    // Кэш друзей (UUID игрока -> Список UUID друзей)
    private static final Map<UUID, List<UUID>> friendsCache = new HashMap<>();

    // Заявки в друзья (Кому отправили -> От кого)
    private static final Map<UUID, List<UUID>> requests = new HashMap<>();

    public static void init(LastApi plugin) {
        file = new File(plugin.getDataFolder(), "friends.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("friends")) {
            for (String key : config.getConfigurationSection("friends").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<String> friendStrings = config.getStringList("friends." + key);
                    List<UUID> friendUuids = new ArrayList<>();
                    for (String s : friendStrings) friendUuids.add(UUID.fromString(s));
                    friendsCache.put(uuid, friendUuids);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private static void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    // --- БАЗОВЫЕ ФУНКЦИИ ---
    public static boolean isFriend(UUID player, UUID target) {
        return getFriends(player).contains(target);
    }

    public static List<UUID> getFriends(UUID player) {
        return friendsCache.getOrDefault(player, new ArrayList<>());
    }

    public static void addFriend(UUID player, UUID friend) {
        friendsCache.computeIfAbsent(player, k -> new ArrayList<>()).add(friend);
        friendsCache.computeIfAbsent(friend, k -> new ArrayList<>()).add(player); // Взаимно
        saveToConfig(player);
        saveToConfig(friend);
        save();
    }

    public static void removeFriend(UUID player, UUID friend) {
        if (friendsCache.containsKey(player)) friendsCache.get(player).remove(friend);
        if (friendsCache.containsKey(friend)) friendsCache.get(friend).remove(player);
        saveToConfig(player);
        saveToConfig(friend);
        save();
    }

    private static void saveToConfig(UUID uuid) {
        List<String> list = new ArrayList<>();
        for (UUID f : getFriends(uuid)) list.add(f.toString());
        config.set("friends." + uuid.toString(), list);
    }

    // --- ЗАЯВКИ В ДРУЗЬЯ ---
    public static boolean hasRequest(UUID receiver, UUID sender) {
        return requests.containsKey(receiver) && requests.get(receiver).contains(sender);
    }

    public static void sendRequest(UUID sender, UUID receiver) {
        requests.computeIfAbsent(receiver, k -> new ArrayList<>()).add(sender);
    }

    public static void removeRequest(UUID sender, UUID receiver) {
        if (requests.containsKey(receiver)) requests.get(receiver).remove(sender);
    }

    // Получить список входящих запросов
    public static List<UUID> getIncomingRequests(UUID receiver) {
        return requests.getOrDefault(receiver, new ArrayList<>());
    }

    // Получить список исходящих запросов
    public static List<UUID> getOutgoingRequests(UUID sender) {
        List<UUID> outgoing = new ArrayList<>();
        for (Map.Entry<UUID, List<UUID>> entry : requests.entrySet()) {
            if (entry.getValue().contains(sender)) {
                outgoing.add(entry.getKey());
            }
        }
        return outgoing;
    }
}