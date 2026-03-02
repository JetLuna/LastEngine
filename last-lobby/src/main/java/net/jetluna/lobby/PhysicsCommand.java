package net.jetluna.lobby;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PhysicsCommand implements CommandExecutor {

    // По умолчанию физика включена (блоки падают/ломаются как обычно)
    private static boolean physicsEnabled = true;

    public static boolean isPhysicsEnabled() {
        return physicsEnabled;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("last.admin")) {
            ChatUtil.sendMessage(sender, "<red>Нет прав!");
            return true;
        }

        if (args.length != 1) {
            ChatUtil.sendMessage(sender, "<yellow>Использование: /physics <on|off>");
            return true;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("on")) {
            physicsEnabled = true;
            ChatUtil.sendMessage(sender, "<green>Физика блоков ВКЛЮЧЕНА.");
        } else if (arg.equals("off")) {
            physicsEnabled = false;
            ChatUtil.sendMessage(sender, "<red>Физика блоков ВЫКЛЮЧЕНА.");
            ChatUtil.sendMessage(sender, "<gray>(Цветы висят в воздухе, песок не падает)");
        } else {
            ChatUtil.sendMessage(sender, "<yellow>Использование: /physics <on|off>");
        }

        return true;
    }
}