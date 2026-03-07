package net.jetluna.api.lang;

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
            LanguageManager.sendMessage(player, "lang.usage");
            return true;
        }

        String lang = args[0].toLowerCase();
        if (lang.equals("ru") || lang.equals("en") || lang.equals("ua")) {
            LanguageManager.setLang(player, lang);

            // Отправляем сообщение об успешной смене языка из секции general!
            LanguageManager.sendMessage(player, "general.lang_changed");
        } else {
            LanguageManager.sendMessage(player, "lang.unknown");
        }

        return true;
    }
}