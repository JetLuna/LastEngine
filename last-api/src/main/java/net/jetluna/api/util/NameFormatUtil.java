package net.jetluna.api.util;

import net.jetluna.api.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NameFormatUtil {

    // --- УНИВЕРСАЛЬНЫЙ ФОРМАТЕР НИКА (Для чата, таба, оповещений) ---
    public static String getFormattedName(Player player, Rank rank) {
        String defaultPrefix = rank.getPrefix();

        // 1. Находим базовый цвет ранга
        String color = extractColor(defaultPrefix);

        // 2. Проверяем кастомный цвет (если ранг MAX или выше)
        if (rank.getWeight() >= Rank.MAX.getWeight()) {
            String customColor = PrefixColorManager.getPlayerColor(player.getUniqueId());
            if (customColor != null) {
                color = ChatColor.translateAlternateColorCodes('&', customColor);
            }
        }

        // 3. Вычищаем префикс от цветов и пробелов
        String cleanPrefix = ChatColor.stripColor(defaultPrefix).trim();

        // Если это обычный игрок без префикса (Rank.PLAYER)
        if (cleanPrefix.isEmpty()) {
            return color + player.getName();
        }

        // 4. Собираем финальную строку
        return color + "§l" + cleanPrefix + " " + color + player.getName();
    }

    // --- ПОЛУЧИТЬ ТОЛЬКО НАЗВАНИЕ РАНГА С ЦВЕТОМ (Для Скорборда и Lore) ---
    public static String getFormattedRank(Player player, Rank rank) {
        if (rank.getWeight() == 1) return "§7Player";

        String defaultPrefix = rank.getPrefix();
        String color = extractColor(defaultPrefix);

        // Проверяем кастомный цвет для MAX
        if (rank.getWeight() >= Rank.MAX.getWeight()) {
            String customColor = PrefixColorManager.getPlayerColor(player.getUniqueId());
            if (customColor != null) {
                color = ChatColor.translateAlternateColorCodes('&', customColor);
            }
        }

        return color + "§l" + ChatColor.stripColor(defaultPrefix).trim();
    }

    // Метод, который вытаскивает именно ЦВЕТ (игнорируя жирность)
    private static String extractColor(String prefix) {
        if (prefix == null || prefix.isEmpty()) return "§7";
        String lastColors = ChatColor.getLastColors(prefix);
        for (int i = 0; i < lastColors.length() - 1; i++) {
            if (lastColors.charAt(i) == '§') {
                char code = lastColors.charAt(i + 1);
                // Проверяем, является ли код именно цветом
                if ("0123456789AaBbCcDdEeFf".indexOf(code) != -1) {
                    return "§" + code;
                }
            }
        }
        return "§7"; // Серый по умолчанию
    }
    // --- ПОЛУЧЕНИЕ ПРЕФИКСА ДЛЯ ТАБА И НАД ГОЛОВОЙ ---
    public static String getNameTagPrefix(org.bukkit.entity.Player player, net.jetluna.api.rank.Rank rank) {
        if (rank.getWeight() == 1) return "§7"; // Для обычных игроков просто серый цвет

        String defaultPrefix = rank.getPrefix();
        String color = "§f";

        // Достаем родной цвет из префикса (например, из "§c§lADMIN" возьмем "§c")
        if (defaultPrefix.length() >= 2 && defaultPrefix.contains("§")) {
            int index = defaultPrefix.indexOf("§");
            color = defaultPrefix.substring(index, index + 2);
        }

        // Если это ранг, который может менять цвет (MAX и выше)
        if (rank.getWeight() >= net.jetluna.api.rank.Rank.MAX.getWeight()) {
            String customColor = PrefixColorManager.getPlayerColor(player.getUniqueId());
            if (customColor != null) {
                color = org.bukkit.ChatColor.translateAlternateColorCodes('&', customColor);
            }
        }

        String cleanPrefix = org.bukkit.ChatColor.stripColor(defaultPrefix).trim();
        return color + "§l" + cleanPrefix + " " + color;
    }

    // --- МАГИЧЕСКОЕ ОБНОВЛЕНИЕ НИКОВ НАД ГОЛОВОЙ ДЛЯ ВСЕХ ---
    public static void refreshNameTags() {
        for (org.bukkit.entity.Player viewer : org.bukkit.Bukkit.getOnlinePlayers()) {
            org.bukkit.scoreboard.Scoreboard board = viewer.getScoreboard();
            if (board == null) continue;

            for (org.bukkit.entity.Player target : org.bukkit.Bukkit.getOnlinePlayers()) {
                net.jetluna.api.rank.Rank targetRank = net.jetluna.api.rank.RankManager.getRank(target);

                String rawTeamName = String.format("%04d", 1000 - targetRank.getWeight()) + target.getName();
                String teamName = rawTeamName.substring(0, Math.min(rawTeamName.length(), 16));

                org.bukkit.scoreboard.Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                // Устанавливаем префикс
                team.setPrefix(getNameTagPrefix(target, targetRank));

                // НОВОЕ: ЖЕСТКО КРАСИМ САМ НИК ИГРОКА!
                org.bukkit.ChatColor teamColor = getNameTagColor(target, targetRank);
                if (teamColor != null) {
                    try {
                        team.setColor(teamColor); // Работает на 1.13+
                    } catch (NoSuchMethodError ignored) {} // Защита от ошибок на старых ядрах
                }

                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }
            }
        }
    }
    // --- ПОЛУЧЕНИЕ ЧИСТОГО ЦВЕТА ДЛЯ КОМАНДЫ ---
    public static org.bukkit.ChatColor getNameTagColor(org.bukkit.entity.Player player, net.jetluna.api.rank.Rank rank) {
        if (rank.getWeight() == 1) return org.bukkit.ChatColor.GRAY;

        String defaultPrefix = rank.getPrefix();
        String colorStr = "§f";

        if (defaultPrefix.length() >= 2 && defaultPrefix.contains("§")) {
            int index = defaultPrefix.indexOf("§");
            colorStr = defaultPrefix.substring(index, index + 2);
        }

        if (rank.getWeight() >= net.jetluna.api.rank.Rank.MAX.getWeight()) {
            String customColor = PrefixColorManager.getPlayerColor(player.getUniqueId());
            if (customColor != null) {
                colorStr = org.bukkit.ChatColor.translateAlternateColorCodes('&', customColor);
            }
        }

        if (colorStr.length() >= 2) {
            return org.bukkit.ChatColor.getByChar(colorStr.charAt(1));
        }
        return org.bukkit.ChatColor.WHITE;
    }
}