package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LobbyCommand implements CommandExecutor {

    private final LobbyPlugin plugin;

    public LobbyCommand(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("setlobbyspawn")) {
            if (!player.hasPermission("last.admin")) return true;
            plugin.getConfig().set("spawn.world", player.getWorld().getName());
            plugin.getConfig().set("spawn.x", player.getLocation().getX());
            plugin.getConfig().set("spawn.y", player.getLocation().getY());
            plugin.getConfig().set("spawn.z", player.getLocation().getZ());
            plugin.getConfig().set("spawn.yaw", player.getLocation().getYaw());
            plugin.getConfig().set("spawn.pitch", player.getLocation().getPitch());
            plugin.saveConfig();

            // Используем сообщение, которое уже было в ru.yml (раздел messages.spawn_set)
            LanguageManager.sendMessage(player, "messages.spawn_set");
            return true;
        }

        if (command.getName().equalsIgnoreCase("lobby") || command.getName().equalsIgnoreCase("hub")) {
            teleportToLobby(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("setnpc")) {
            if (!player.hasPermission("last.admin")) return true;
            if (args.length < 1) return true;
            plugin.getConfig().set("locations.npc." + args[0].toLowerCase(), player.getLocation());
            plugin.saveConfig();

            String msg = LanguageManager.getString(player, "lobby.commands.npc_set").replace("%name%", args[0]);
            ChatUtil.sendMessage(player, msg);
            return true;
        }

        return false;
    }

    public void teleportToLobby(Player player) {
        Location loc = plugin.getLobbySpawn();
        if (loc != null) {
            player.teleport(loc);
        } else {
            LanguageManager.sendMessage(player, "lobby.commands.spawn_not_set");
        }
    }
}