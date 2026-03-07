package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PhysicsCommand implements CommandExecutor {

    // По умолчанию физика включена (блоки падают/ломаются как обычно)
    private static boolean physicsEnabled = true;

    public static boolean isPhysicsEnabled() {
        return physicsEnabled;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission("last.admin")) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "general.no_permission"));
            return true;
        }

        if (args.length != 1) {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "lobby.commands.physics_usage"));
            return true;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("on")) {
            physicsEnabled = true;
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "lobby.commands.physics_on"));
        } else if (arg.equals("off")) {
            physicsEnabled = false;
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "lobby.commands.physics_off"));
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "lobby.commands.physics_off_desc"));
        } else {
            ChatUtil.sendMessage(sender, LanguageManager.getString(p, "lobby.commands.physics_usage"));
        }

        return true;
    }
}