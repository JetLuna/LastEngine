package net.jetluna.api.rank;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    // Храним ранги: UUID игрока -> Ранг
    private static final Map<UUID, Rank> playerRanks = new HashMap<>();

    // Установить ранг
    public static void setRank(Player player, Rank rank) {
        playerRanks.put(player.getUniqueId(), rank);
    }

    // Получить ранг (если нет - возвращаем PLAYER)
    public static Rank getRank(Player player) {
        // Временный хак: Если у игрока есть ОПка - он DEV (пока мы не сделали БД)
        if (player.isOp() && !playerRanks.containsKey(player.getUniqueId())) {
            return Rank.DEV;
        }
        return playerRanks.getOrDefault(player.getUniqueId(), Rank.PLAYER);
    }

    // Получить красивый префикс (например для чата)
    public static String getPrefix(Player player) {
        return getRank(player).getPrefix();
    }
}