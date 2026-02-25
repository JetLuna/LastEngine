package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.lobby.gui.LobbyGui;
import net.jetluna.lobby.gui.RewardGui;
import net.jetluna.lobby.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyPlugin extends JavaPlugin {

    private static LobbyPlugin instance;
    private NpcManager npcManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.npcManager = new NpcManager(this);
        this.npcManager.removeAll(); // Чистим старых

        // Команды
        LobbyCommand cmdExecutor = new LobbyCommand(this);
        if (getCommand("setlobbyspawn") != null) getCommand("setlobbyspawn").setExecutor(cmdExecutor);
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(cmdExecutor);
        if (getCommand("hub") != null) getCommand("hub").setExecutor(cmdExecutor);
        if (getCommand("setnpc") != null) getCommand("setnpc").setExecutor(cmdExecutor);

        // События
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new RewardGui(), this);
        getServer().getPluginManager().registerEvents(new LobbyGui(), this);
        getServer().getPluginManager().registerEvents(new LobbyChat(), this);

        // Спавн НПС
        getServer().getScheduler().runTaskLater(this, this::spawnLobbyNpcs, 60L);

        // !!! ИСПРАВЛЕНИЕ: ЗАПУСКАЕМ ЗАДАЧУ ОБНОВЛЕНИЯ (TAB + SCOREBOARD) !!!
        // Запускаем каждую секунду (20 тиков)
        new LobbyTask(this).runTaskTimer(this, 20L, 20L);

        getLogger().info("LobbyPlugin успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.removeAll();
        }
    }

    private void spawnLobbyNpcs() {
        if (npcManager == null) return;
        npcManager.removeAll();

        spawnOneNpc("bedwars", "lobby.npc.bedwars.name", "Agrael99");
        spawnOneNpc("vanilla", "lobby.npc.vanilla.name", "Notch");
        spawnOneNpc("duels", "lobby.npc.duels.name", "Huahwi");
        spawnOneNpc("rewards", "lobby.npc.rewards.name", "Santa");
    }

    private void spawnOneNpc(String id, String langKey, String skin) {
        Location loc = getConfig().getLocation("locations.npc." + id);
        if (loc != null) {
            if (loc.getWorld() != null) loc.getWorld().getChunkAt(loc).load();
            String name = LanguageManager.getString(null, langKey);
            npcManager.createNpc(id, loc, name, skin);
        }
    }

    public Location getLobbySpawn() {
        FileConfiguration config = getConfig();
        if (!config.contains("spawn.world")) {
            return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        World world = Bukkit.getWorld(config.getString("spawn.world"));
        if (world == null) return null;

        return new Location(world,
                config.getDouble("spawn.x"),
                config.getDouble("spawn.y"),
                config.getDouble("spawn.z"),
                (float) config.getDouble("spawn.yaw"),
                (float) config.getDouble("spawn.pitch"));
    }

    public static LobbyPlugin getInstance() {
        return instance;
    }
}