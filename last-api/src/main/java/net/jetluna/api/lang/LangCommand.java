package net.jetluna.api.lang;

import net.jetluna.api.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LangCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            ChatUtil.sendMessage(player, "<yellow>Использование / Usage: /lang <ru/en/ua>");
            return true;
        }

        String lang = args[0].toLowerCase();
        if (lang.equals("ru") || lang.equals("en") || lang.equals("ua")) {
            LanguageManager.setLang(player, lang);
        } else {
            ChatUtil.sendMessage(player, "<red>Unknown language! Try: ru, en, ua");
        }

        return true;
    }
}