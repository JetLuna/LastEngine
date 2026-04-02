package net.jetluna.bedwars;

import java.util.*;

// Класс для хранения данных ОДНОЙ текущей игры
public class GameStats {
    // Храним UUID игрока -> количество убийств
    private final Map<UUID, Integer> gameKills = new HashMap<>();

    // Храним UUID игрока -> количество сломанных кроватей
    private final Map<UUID, Integer> gameBeds = new HashMap<>();

    // Лист, куда будем добавлять игроков по мере их вылета (последний выживший будет первым в топе)
    private final List<UUID> survivalOrder = new ArrayList<>();

    // Методы для добавления данных во время эвентов
    public void addKill(UUID playerUUID) { gameKills.merge(playerUUID, 1, Integer::sum); }
    public void addBedBroken(UUID playerUUID) { gameBeds.merge(playerUUID, 1, Integer::sum); }
    public void addEliminated(UUID playerUUID) { survivalOrder.add(playerUUID); } // Добавляем вылетевшего

    // Геттеры для данных (нам понадобятся для расчета топов в конце)
    public Map<UUID, Integer> getGameKills() { return gameKills; }
    public Map<UUID, Integer> getGameBeds() { return gameBeds; }

    // Топ выживших: переворачиваем лист, чтобы последний выживший стал #1
    public List<UUID> getSurvivalTop() {
        List<UUID> reversed = new ArrayList<>(survivalOrder);
        Collections.reverse(reversed);
        return reversed;
    }
    // Метод для получения Топ-5 по убийствам
    public List<Map.Entry<UUID, Integer>> getTopKills() {
        return gameKills.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();
    }

    // Метод для получения Топ-5 по кроватям
    public List<Map.Entry<UUID, Integer>> getTopBeds() {
        return gameBeds.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();
    }
    // Метод для добавления выжившего в конце игры
    public void addSurvivor(UUID playerUUID) {
        if (!survivalOrder.contains(playerUUID)) {
            survivalOrder.add(playerUUID);
        }
    }
}