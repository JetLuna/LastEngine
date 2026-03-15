package net.jetluna.api;

import net.jetluna.api.pet.PetManager;
import net.jetluna.api.pet.PetsGui;
import net.jetluna.api.chat.MsgCommand;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.punish.PunishCommand;
import net.jetluna.api.punish.PunishmentListener;
import net.jetluna.api.punish.PunishmentManager;
import net.jetluna.api.rank.RankCommand;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {
    private static LastApi instance;
    private net.jetluna.api.database.DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // !!! БАЗА ДАННЫХ ИНИЦИАЛИЗИРУЕТСЯ В ПЕРВУЮ ОЧЕРЕДЬ !!!
        databaseManager = new net.jetluna.api.database.DatabaseManager("127.0.0.1", "3306", "last_engine", "root", "");

        if (!getDataFolder().exists()) getDataFolder().mkdir();

        StatsManager.init(this);
        getServer().getPluginManager().registerEvents(new net.jetluna.api.stats.StatsListener(), this);
        LanguageManager.init(this);

        // !!! ВОТ ЭТА СТРОКА САМАЯ ВАЖНАЯ !!!
        // Без нее конфиг наказаний не загрузится и будет ошибка
        PunishmentManager.init(this);
        getServer().getPluginManager().registerEvents(new PunishmentListener(), this);

        // Ранги
        if (getCommand("setrank") != null) getCommand("setrank").setExecutor(new RankCommand());

        // Команды наказаний
        PunishCommand pc = new PunishCommand();
        // Убедись, что массив команд полный (включая history и banlist)
        String[] cmds = {"kick", "mute", "ban", "unmute", "unban", "history", "banlist"};
        for (String cmd : cmds) {
            if (getCommand(cmd) != null) getCommand(cmd).setExecutor(pc);
        }

        // Чаты
        if (getCommand("sc") != null) getCommand("sc").setExecutor(new net.jetluna.api.chat.ChatCommands("sc"));
        if (getCommand("dc") != null) getCommand("dc").setExecutor(new net.jetluna.api.chat.ChatCommands("dc"));

        MsgCommand msgCmd = new MsgCommand();
        if (getCommand("msg") != null) getCommand("msg").setExecutor(msgCmd);
        if (getCommand("r") != null) getCommand("r").setExecutor(msgCmd);

        if (getCommand("eco") != null) getCommand("eco").setExecutor(new net.jetluna.api.stats.EcoCommand());

        // --- НОВОЕ: Стримы ---
        if (getCommand("stream") != null) getCommand("stream").setExecutor(new net.jetluna.api.stream.StreamCommand());
        if (getCommand("streams") != null) getCommand("streams").setExecutor(new net.jetluna.api.stream.StreamCommand());

        // Слушатели стримов
        getServer().getPluginManager().registerEvents(new net.jetluna.api.stream.StreamsGui(), this);
        getServer().getPluginManager().registerEvents(new net.jetluna.api.stream.StreamListener(), this); // <--- ДОБАВИЛИ ЭТУ СТРОКУ

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new net.jetluna.api.staff.BungeeChannelListener());

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new net.jetluna.api.chat.AutoAnnouncer(), 12000L, 12000L);

        // --- Жалобы ---
        if (getCommand("report") != null) getCommand("report").setExecutor(new net.jetluna.api.report.ReportCommand());
        if (getCommand("reports") != null) getCommand("reports").setExecutor(new net.jetluna.api.report.ReportCommand());
        getServer().getPluginManager().registerEvents(new net.jetluna.api.report.ReportsGui(), this);

        // --- НОВОЕ: Эффекты ---
        getServer().getPluginManager().registerEvents(new net.jetluna.api.effect.EffectsGui(), this);
        net.jetluna.api.effect.EffectManager.startTask();

        PetManager.startTask(); // Запускаем цикл движения питомцев
        Bukkit.getPluginManager().registerEvents(new PetsGui(), this);

        net.jetluna.api.gadget.GadgetManager.init(this);
        Bukkit.getPluginManager().registerEvents(new net.jetluna.api.gadget.GadgetsGui(), this);

        net.jetluna.api.cosmetic.CosmeticManager.init(this);
        getServer().getPluginManager().registerEvents(new net.jetluna.api.cosmetic.CosmeticListener(), this);

        getServer().getPluginManager().registerEvents(new net.jetluna.api.parkour.ParkourManager(), this);

        if (getCommand("lang") != null) getCommand("lang").setExecutor(new net.jetluna.api.lang.LangCommand());

        getServer().getPluginManager().registerEvents(new net.jetluna.api.color.PrefixColorGui(), this);

        net.jetluna.api.util.PlayerSettingsManager.init(this);

        getCommand("friend").setExecutor(new net.jetluna.api.friends.FriendCommand());

        net.jetluna.api.friends.FriendManager.init(this);

        // Инициализация базы данных скинов
        net.jetluna.api.skin.SkinManager.init(this);

        // Регистрация команд
        if (getCommand("skin") != null) getCommand("skin").setExecutor(new net.jetluna.api.skin.command.SkinCommand());
        if (getCommand("skins") != null) getCommand("skins").setExecutor(new net.jetluna.api.skin.command.SkinHistoryCommand());

        // Регистрация меню истории
        getServer().getPluginManager().registerEvents(new net.jetluna.api.skin.SkinHistoryGui(), this);
        getServer().getPluginManager().registerEvents(new net.jetluna.api.skin.SkinListener(), this);

        // Инициализация менеджера лучшего игрока
        net.jetluna.api.bestplayer.BestPlayerManager.init(this);

        // Остальные инициализации...
        net.jetluna.api.skin.SkinManager.init(this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static LastApi getInstance() { return instance; }

    public net.jetluna.api.database.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}