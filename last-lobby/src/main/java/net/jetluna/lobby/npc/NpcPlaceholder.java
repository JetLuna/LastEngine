package net.jetluna.lobby.npc;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
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

        // Если DH запрашивает, например: %lastengine_npc_bedwars_name%
        if (params.startsWith("npc_")) {
            String[] parts = params.split("_"); // Разбиваем на [npc, bedwars, name]

            if (parts.length == 3) {
                String id = parts[1]; // bedwars
                String type = parts[2]; // name или holo

                // Формируем путь к конфигу: lobby.npc.bedwars.name
                String path = "lobby.npc." + id + "." + type;

                // Отдаем текст на языке ЭТОГО игрока!
                return ChatUtil.parseLegacy(LanguageManager.getString(player, path));
            }
        }
        return null;
    }
}