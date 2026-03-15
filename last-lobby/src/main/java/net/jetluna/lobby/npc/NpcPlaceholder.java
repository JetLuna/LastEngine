package net.jetluna.lobby.npc;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NpcPlaceholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "lastengine"; // Наш префикс %lastengine_...%
    }

    @Override
    public @NotNull String getAuthor() {
        return "JetLuna";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.startsWith("npc_")) {
            String[] parts = params.split("_");

            if (parts.length == 3) {
                String id = parts[1];
                String type = parts[2];

                if (id.equals("bestplayer") && type.equals("holo")) {
                    if (net.jetluna.api.bestplayer.BestPlayerManager.isExpired()) {
                        return ChatUtil.parseLegacy("&7Никто");
                    } else {
                        // Пытаемся найти лучшего игрока на сервере прямо сейчас
                        Player bestPlayer = Bukkit.getPlayer(net.jetluna.api.bestplayer.BestPlayerManager.getCurrentBest());
                        if (bestPlayer != null) {
                            // Если он онлайн - берем самый свежий префикс и цвет!
                            return ChatUtil.parseLegacy(net.jetluna.api.util.NameFormatUtil.getFormattedName(bestPlayer, net.jetluna.api.rank.RankManager.getRank(bestPlayer)));
                        } else {
                            // Если оффлайн - берем сохраненный из базы
                            return ChatUtil.parseLegacy(net.jetluna.api.bestplayer.BestPlayerManager.getCurrentFormattedName());
                        }
                    }
                }

                String path = "lobby.npc." + id + "." + type;
                return ChatUtil.parseLegacy(LanguageManager.getString(player, path));
            }
        }
        return null;
    }
}