package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players!");
            return true;
        }

        // --- УСТАНОВКА СПАВНА ---
        if (label.equalsIgnoreCase("setlobbyspawn")) {
            if (!player.hasPermission("last.admin")) {
                LanguageManager.sendMessage(player, "general.no_permission");
                return true;
            }

            plugin.getConfig().set("spawn.world", player.getWorld().getName());
            plugin.getConfig().set("spawn.x", player.getX());
            plugin.getConfig().set("spawn.y", player.getY());
            plugin.getConfig().set("spawn.z", player.getZ());
            plugin.getConfig().set("spawn.yaw", player.getYaw());
            plugin.getConfig().set("spawn.pitch", player.getPitch());
            plugin.saveConfig();

            LanguageManager.sendMessage(player, "lobby.spawn_set");
            return true;
        }

        // --- ТЕЛЕПОРТАЦИЯ ---
        if (label.equalsIgnoreCase("lobby") || label.equalsIgnoreCase("hub") || label.equalsIgnoreCase("l")) {
            teleportToLobby(player);
            return true;
        }

        return false;
    }

    public void teleportToLobby(Player player) {
        if (plugin.getConfig().contains("spawn.world")) {
            World w = Bukkit.getWorld(plugin.getConfig().getString("spawn.world"));
            if (w != null) {
                double x = plugin.getConfig().getDouble("spawn.x");
                double y = plugin.getConfig().getDouble("spawn.y");
                double z = plugin.getConfig().getDouble("spawn.z");
                float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
                float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

                player.teleport(new Location(w, x, y, z, yaw, pitch));

                // Сообщение "Телепортация..."
                LanguageManager.sendMessage(player, "lobby.teleport");
            }
        } else {
            // Если спавн не установлен
            ChatUtil.sendMessage(player, "<red>Spawn not set!");
        }
    }
}