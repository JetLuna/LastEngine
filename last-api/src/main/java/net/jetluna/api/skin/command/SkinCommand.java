package net.jetluna.api.skin.command;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.skin.SkinManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "api.skin.usage")));
            return true;
        }

        String targetSkin = args[0];
        ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "api.skin.loading")));

        SkinManager.getSkin(targetSkin).thenAccept(skinData -> {
            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> {
                if (skinData == null) {
                    ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "api.skin.not_found")));
                    return;
                }

                // Передаем true в конце, чтобы скин сохранился в историю!
                SkinManager.applySkin(player, targetSkin, skinData[0], skinData[1], true);
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "api.skin.success").replace("%skin%", targetSkin)));
            });
        });

        return true;
    }

    private String color(String text) {
        return text == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}