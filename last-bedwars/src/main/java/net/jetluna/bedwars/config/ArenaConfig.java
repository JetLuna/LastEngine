package net.jetluna.bedwars.config;

import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import net.jetluna.bedwars.team.TeamColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ArenaConfig {

    public static void save(BedWarsPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        config.set("arena", null); // Очищаем старые данные

        // Сохраняем генераторы и NPC
        config.set("arena.diamond", plugin.getArenaScanner().getDiamondGenerators());
        config.set("arena.emerald", plugin.getArenaScanner().getEmeraldGenerators());
        config.set("arena.shop", plugin.getArenaScanner().getShopNpcLocations());
        config.set("arena.upgrade", plugin.getArenaScanner().getUpgradeNpcLocations());

        List<GameTeam> teams = plugin.getTeamManager().getActiveTeams();
        plugin.getLogger().info("[ДЕБАГ-SAVE] Найдено команд в памяти для сохранения: " + teams.size());

        for (GameTeam team : teams) {
            String path = "arena.teams." + team.getColor().name();
            config.set(path + ".spawn", team.getSpawnLocation());
            config.set(path + ".bed", team.getBedLocation());
            config.set(path + ".generator", team.getGeneratorLocation());
            plugin.getLogger().info("[ДЕБАГ-SAVE] Команда " + team.getColor().name() + " записана в конфиг.");
        }

        plugin.saveConfig();
        plugin.getLogger().info("Координаты арены успешно сохранены в config.yml! Сохранено команд: " + teams.size());
    }

    public static void load(BedWarsPlugin plugin) {
        // Принудительно перезагружаем файл с диска
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("arena.teams")) {
            plugin.getLogger().warning("Арена не настроена! (Секция arena.teams не найдена в файле)");
            return;
        }

        plugin.getTeamManager().clearActiveTeams();

        ConfigurationSection teamsSection = config.getConfigurationSection("arena.teams");
        if (teamsSection != null) {
            for (String key : teamsSection.getKeys(false)) {
                try {
                    plugin.getLogger().info("[ДЕБАГ-LOAD] Пытаюсь загрузить команду: " + key);
                    TeamColor color = TeamColor.valueOf(key.toUpperCase());
                    String path = "arena.teams." + key;

                    GameTeam team = new GameTeam(color);
                    team.setSpawnLocation(config.getLocation(path + ".spawn"));
                    team.setBedLocation(config.getLocation(path + ".bed"));
                    team.setGeneratorLocation(config.getLocation(path + ".generator"));
                    team.setHasBed(true);

                    plugin.getTeamManager().addActiveTeam(team);
                    plugin.getLogger().info("[ДЕБАГ-LOAD] Команда " + key + " успешно добавлена!");
                } catch (Exception e) {
                    plugin.getLogger().warning("[ДЕБАГ-ОШИБКА] Сбой при загрузке команды " + key + ": " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("[ДЕБАГ-ОШИБКА] teamsSection = null (Конфиг видит ключ, но не может прочитать раздел)");
        }

        // Загружаем списки
        plugin.getArenaScanner().setDiamondGenerators((List<Location>) config.getList("arena.diamond", new ArrayList<>()));
        plugin.getArenaScanner().setEmeraldGenerators((List<Location>) config.getList("arena.emerald", new ArrayList<>()));
        plugin.getArenaScanner().setShopNpcLocations((List<Location>) config.getList("arena.shop", new ArrayList<>()));
        plugin.getArenaScanner().setUpgradeNpcLocations((List<Location>) config.getList("arena.upgrade", new ArrayList<>()));

        plugin.getLogger().info("Данные арены успешно загружены! Найдено команд: " + plugin.getTeamManager().getActiveTeams().size());
    }
}