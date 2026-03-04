package net.jetluna.api.stream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StreamManager {

    // Теперь храним СПИСОК ссылок (Set) для каждого игрока
    private static final Map<String, Set<String>> linkedChannels = new HashMap<>();

    // Хранит запущенные стримы: Игрок -> Полная ссылка
    private static final Map<String, String> activeStreams = new HashMap<>();

    public static void linkChannel(String playerName, String url) {
        String cleanUrl = url.replace("https://", "").replace("http://", "").toLowerCase();
        // Если у игрока еще нет списка, создаем его и добавляем ссылку
        linkedChannels.computeIfAbsent(playerName.toLowerCase(), k -> new HashSet<>()).add(cleanUrl);
    }

    public static Set<String> getLinkedChannels(String playerName) {
        return linkedChannels.get(playerName.toLowerCase());
    }

    public static void addActiveStream(String playerName, String url) {
        activeStreams.put(playerName, url);
    }

    public static void removeActiveStream(String playerName) {
        activeStreams.remove(playerName);
    }

    public static Map<String, String> getActiveStreams() {
        return activeStreams;
    }
}