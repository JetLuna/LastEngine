package net.jetluna.api.color;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ColorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            // Для консоли можно оставить базовый вывод
            ChatUtil.sendMessage(sender, "&cКоманда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;
        PrefixColorGui.open(player);

        return true;
    }
}