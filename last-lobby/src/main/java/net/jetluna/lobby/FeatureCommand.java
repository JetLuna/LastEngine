package net.jetluna.lobby;

import net.jetluna.lobby.gui.SettingsGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FeatureCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        if (cmd.equals("fly")) {
            SettingsGui.toggleFly(player);
        }
        else if (cmd.equals("doublejump") || cmd.equals("dj")) {
            SettingsGui.toggleDoubleJump(player);
        }

        return true;
    }
}