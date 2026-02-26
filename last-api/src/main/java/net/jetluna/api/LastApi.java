package net.jetluna.api;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.punish.PunishCommand;
import net.jetluna.api.punish.PunishmentManager;
import net.jetluna.api.rank.RankCommand;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LastApi extends JavaPlugin {
    private static LastApi instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        StatsManager.init(this);
        LanguageManager.init(this);

        // !!! ВОТ ЭТА СТРОКА САМАЯ ВАЖНАЯ !!!
        // Без нее конфиг наказаний не загрузится и будет ошибка
        PunishmentManager.init(this);

        // Ранги
        if (getCommand("setrank") != null) getCommand("setrank").setExecutor(new RankCommand());

        // Команды наказаний
        PunishCommand pc = new PunishCommand();
        // Убедись, что массив команд полный (включая history и banlist)
        String[] cmds = {"kick", "mute", "ban", "unmute", "unban", "history", "banlist"};
        for (String cmd : cmds) {
            if (getCommand(cmd) != null) getCommand(cmd).setExecutor(pc);
        }
    }

    public static LastApi getInstance() { return instance; }
}