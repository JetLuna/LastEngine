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

        // Выдача поинтов: /bw givepoints <игрок> <количество>
        if (args[0].equalsIgnoreCase("givepoints")) {
            if (!player.hasPermission("bedwars.admin")) {
                player.sendMessage("§cУ вас нет прав!");
                return true;
            }

            if (args.length < 3) {
                player.sendMessage("§eИспользование: /bw givepoints <игрок> <количество>");
                return true;
            }

            Player target = org.bukkit.Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cИгрок не найден!");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cКоличество должно быть числом!");
                return true;
            }

            plugin.getEconomyManager().addPoints(target, amount);
            player.sendMessage("§a[BedWars] Вы выдали §e" + amount + " §aпоинтов игроку §e" + target.getName() + "§a!");
            target.sendMessage("§a[BedWars] Администратор выдал вам §e" + amount + " §aпоинтов!");
            target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
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
        // Установка лобби ожидания
        if (args[0].equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("bedwars.admin")) {
                player.sendMessage("§cУ вас нет прав!");
                return true;
            }

            plugin.getConfig().set("locations.waiting-lobby", player.getLocation());
            plugin.saveConfig();

            player.sendMessage("§a[BedWars] Точка лобби ожидания успешно установлена!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;
        }

        // Установка лобби победителей
        if (args[0].equalsIgnoreCase("setwinlobby")) {
            if (!player.hasPermission("bedwars.admin")) {
                player.sendMessage("§cУ вас нет прав!");
                return true;
            }

            plugin.getConfig().set("locations.win-lobby", player.getLocation());
            plugin.saveConfig();

            player.sendMessage("§a[BedWars] Точка лобби Победителей успешно установлена!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;
        }
        return true;

    }
}