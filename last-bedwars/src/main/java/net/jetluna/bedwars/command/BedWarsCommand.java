package net.jetluna.bedwars.command;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedWarsCommand implements CommandExecutor {

    private final BedWarsPlugin plugin;

    public BedWarsCommand(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage("§cУ вас нет прав!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eИспользование: /bw scan <радиус>");
            return true;
        }

        if (args[0].equalsIgnoreCase("scan")) {
            int radius = 100; // По умолчанию ищем в радиусе 100 блоков
            if (args.length > 1) {
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }

            player.sendMessage("§aЗапускаем сканирование арены в радиусе " + radius + " блоков...");
            plugin.getArenaScanner().scanArena(player.getLocation(), radius);

            net.jetluna.bedwars.config.ArenaConfig.save(plugin);
            player.sendMessage("§aСканирование завершено! Проверь консоль для деталей.");
            return true;
        }

        return true;
    }
}