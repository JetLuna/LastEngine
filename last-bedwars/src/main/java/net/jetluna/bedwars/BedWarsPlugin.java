package net.jetluna.bedwars;

import net.jetluna.bedwars.manager.*;
import net.jetluna.bedwars.npc.NpcListener;
import net.jetluna.bedwars.npc.NpcManager;
import net.jetluna.bedwars.resource.GeneratorManager;
import net.jetluna.bedwars.shop.ShopGui;
import net.jetluna.bedwars.state.WaitingState;
import net.jetluna.bedwars.team.TeamManager;
import net.jetluna.bedwars.util.BedWarsMode;
import org.bukkit.plugin.java.JavaPlugin;

public class BedWarsPlugin extends JavaPlugin {

    private static BedWarsPlugin instance;
    private BedWarsMode bedWarsMode;

    private GameManager gameManager;
    private TeamManager teamManager;
    private GeneratorManager generatorManager;
    private EquipmentManager equipmentManager;
    private BlockManager blockManager;
    private DeathManager deathManager;
    private NpcManager npcManager;
    private ArenaScanner arenaScanner;
    private ScoreboardManager scoreboardManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;

        // Отключаем автосохранение миров. Теперь любые разрушения и лут исчезнут при рестарте!
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            world.setAutoSave(false);
        }

        // Временно ставим режим SOLO (потом можно вынести в конфиг)
        this.bedWarsMode = BedWarsMode.SOLO;

        // 1. Инициализация всех менеджеров
        this.teamManager = new TeamManager();
        this.arenaScanner = new ArenaScanner(this, this.teamManager); // <--- ВОСКРЕСИЛИ ИНИЦИАЛИЗАЦИЮ
        this.equipmentManager = new EquipmentManager(teamManager);
        this.generatorManager = new GeneratorManager(this);
        this.blockManager = new BlockManager();
        this.deathManager = new DeathManager(this);
        this.npcManager = new NpcManager(this);
        this.gameManager = new GameManager(this);

        // 2. Регистрация магазинов и слушателей
        ShopGui.init();
        getServer().getPluginManager().registerEvents(new ShopGui(), this);
        getServer().getPluginManager().registerEvents(new NpcListener(), this);
        getServer().getPluginManager().registerEvents(new net.jetluna.bedwars.item.CustomItemListener(this), this);

        // 3. Запуск состояния ожидания
        this.gameManager.setGameState(new WaitingState(this));

        // Отключаем ванильные достижения в чате во всех мирах
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            world.setGameRule(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        getLogger().info("LastBedWars успешно запущен!");

        getCommand("bw").setExecutor(new net.jetluna.bedwars.command.BedWarsCommand(this));

        saveDefaultConfig(); // Создает файл, если его нет
        net.jetluna.bedwars.config.ArenaConfig.load(this);

        this.scoreboardManager = new ScoreboardManager(this);

        this.economyManager = new EconomyManager();
        getServer().getPluginManager().registerEvents(new net.jetluna.bedwars.shop.UpgradeGui(this), this);
    }

    public static BedWarsPlugin getInstance() { return instance; }

    public BedWarsMode getBedWarsMode() { return bedWarsMode; }
    public GameManager getGameManager() { return gameManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public GeneratorManager getGeneratorManager() { return generatorManager; }
    public EquipmentManager getEquipmentManager() { return equipmentManager; }
    public BlockManager getBlockManager() { return blockManager; }
    public DeathManager getDeathManager() { return deathManager; }
    public NpcManager getNpcManager() { return npcManager; }
    public ArenaScanner getArenaScanner() { return arenaScanner; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
}