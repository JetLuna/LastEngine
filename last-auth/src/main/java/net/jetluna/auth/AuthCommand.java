package net.jetluna.auth;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.auth.manager.CodeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuthCommand implements CommandExecutor {

    private final AuthPlugin plugin;
    private final AuthManager authManager;
    private final CodeManager codeManager;

    public AuthCommand(AuthPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.codeManager = new CodeManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        // --- 1. КОМАНДА: ЛЕЧЕНИЕ (/heal) ---
        if (label.equalsIgnoreCase("heal")) {
            if (!player.isOp()) {
                ChatUtil.sendMessage(player, "<red>У вас нет прав!");
                return true;
            }
            // Восстанавливаем всё
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.setFireTicks(0);

            ChatUtil.sendMessage(player, "<green>Вы исцелены!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            return true;
        }

        // --- 2. КОМАНДА: УСТАНОВКА ТОЧКИ СПАВНА ---
        if (label.equalsIgnoreCase("setauthspawn")) {
            if (!player.isOp()) {
                ChatUtil.sendMessage(player, "<red>У вас нет прав!");
                return true;
            }
            plugin.getConfig().set("spawn.world", player.getWorld().getName());
            plugin.getConfig().set("spawn.x", player.getX());
            plugin.getConfig().set("spawn.y", player.getY());
            plugin.getConfig().set("spawn.z", player.getZ());
            plugin.getConfig().set("spawn.yaw", player.getYaw());
            plugin.getConfig().set("spawn.pitch", player.getPitch());
            plugin.saveConfig();

            ChatUtil.sendMessage(player, "<green>Точка спавна авторизации установлена!");
            return true;
        }

        // --- 3. РЕГИСТРАЦИЯ / ВХОД ---
        if (label.equalsIgnoreCase("reg") || label.equalsIgnoreCase("login")) {
            if (authManager.isAuthorized(player)) {
                ChatUtil.sendMessage(player, "<red>Вы уже авторизованы!");
                return true;
            }
            if (args.length != 1) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /reg <почта>");
                return true;
            }
            String email = args[0];
            String code = codeManager.generateCode(player.getUniqueId(), email);

            System.out.println("[LastAuth] КОД ДЛЯ " + player.getName() + ": " + code);

            AuthSession session = authManager.getSession(player);
            session.setTempContact(email);
            session.setState(AuthSession.State.AWAITING_CODE);

            ChatUtil.sendMessage(player, "<green>Код отправлен (см. консоль)!");
            ChatUtil.sendMessage(player, "<yellow>Введите: /code <код>");
            return true;
        }

        // --- 4. ВВОД КОДА ---
        if (label.equalsIgnoreCase("code")) {
            if (authManager.isAuthorized(player)) {
                ChatUtil.sendMessage(player, "<red>Вы уже авторизованы!");
                return true;
            }
            if (args.length != 1) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /code <код>");
                return true;
            }

            if (codeManager.checkCode(player.getUniqueId(), args[0])) {
                authManager.setAuthorized(player);
                player.getInventory().clear();

                ChatUtil.sendMessage(player, "<green>Успешная авторизация!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            } else {
                ChatUtil.sendMessage(player, "<red>Неверный код!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
            return true;
        }
        return false;
    }
}