package net.jetluna.auth;

import net.jetluna.api.LastApi;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthCommand implements CommandExecutor {

    private final AuthPlugin plugin;
    private final AuthManager authManager;

    public AuthCommand(AuthPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Команды только для игроков
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        // --- ЛОГИКА /REG ---
        if (command.getName().equalsIgnoreCase("reg")) {
            handleRegister(player, args);
            return true;
        }

        // --- ЛОГИКА /L ---
        if (command.getName().equalsIgnoreCase("l")) {
            handleLogin(player, args);
            return true;
        }

        return false;
    }

    // Обработка регистрации
    private void handleRegister(Player player, String[] args) {
        if (authManager.isAuthorized(player)) {
            ChatUtil.sendMessage(player, "<red>Вы уже авторизованы!");
            return;
        }

        if (args.length != 2) {
            ChatUtil.sendMessage(player, "<red>Использование: /reg <пароль> <повтор_пароля>");
            return;
        }

        String pass1 = args[0];
        String pass2 = args[1];

        if (!pass1.equals(pass2)) {
            ChatUtil.sendMessage(player, "<red>Пароли не совпадают!");
            return;
        }

        // Идем в базу данных (Асинхронно!)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection()) {

                // 1. Проверяем, есть ли такой ник
                PreparedStatement check = conn.prepareStatement("SELECT uuid FROM users WHERE username = ?");
                check.setString(1, player.getName());
                if (check.executeQuery().next()) {
                    ChatUtil.sendMessage(player, "<red>Вы уже зарегистрированы! Пишите /l <пароль>");
                    return;
                }

                // 2. Создаем запись
                String sql = "INSERT INTO users (uuid, username, ip, password) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, player.getAddress().getAddress().getHostAddress());
                // ВАЖНО: Хешируем пароль, чтобы не хранить его в открытом виде
                stmt.setString(4, PasswordUtil.hash(pass1));
                stmt.executeUpdate();

                // 3. Возвращаемся в главный поток и пускаем игрока
                Bukkit.getScheduler().runTask(plugin, () -> {
                    authManager.setAuthorized(player);
                    ChatUtil.sendMessage(player, "<green>Успешная регистрация! Приятной игры.");
                    player.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, ChatUtil.parse("<green>Добро пожаловать!"));
                });

            } catch (SQLException e) {
                e.printStackTrace();
                ChatUtil.sendMessage(player, "<red>Ошибка базы данных! Сообщите админу.");
            }
        });
    }

    // Обработка входа
    private void handleLogin(Player player, String[] args) {
        if (authManager.isAuthorized(player)) {
            ChatUtil.sendMessage(player, "<red>Вы уже авторизованы!");
            return;
        }

        if (args.length != 1) {
            ChatUtil.sendMessage(player, "<red>Использование: /l <пароль>");
            return;
        }

        String inputPass = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = LastApi.getInstance().getDatabaseManager().getConnection()) {

                // 1. Достаем пароль из базы
                PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?");
                stmt.setString(1, player.getName());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String realHash = rs.getString("password");

                    // 2. Сравниваем хеши
                    if (realHash.equals(PasswordUtil.hash(inputPass))) {
                        // УРА! Пускаем
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            authManager.setAuthorized(player);
                            ChatUtil.sendMessage(player, "<green>Вы успешно вошли!");
                        });
                    } else {
                        ChatUtil.sendMessage(player, "<red>Неверный пароль!");
                    }
                } else {
                    ChatUtil.sendMessage(player, "<red>Аккаунт не найден. Используйте /reg");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                ChatUtil.sendMessage(player, "<red>Ошибка базы данных!");
            }
        });
    }
}