package net.jetluna.api.chat;

import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MsgCommand implements CommandExecutor {

    // Храним, кому отвечать: <Кто пишет, Кому отвечать>
    private static final Map<UUID, UUID> replies = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        Player target = null;
        String message = "";

        // --- КОМАНДА /MSG ---
        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w")) {
            if (args.length < 2) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /msg <ник> <сообщение>");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                ChatUtil.sendMessage(player, "<red>Игрок не найден!");
                return true;
            }
            message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        }

        // --- КОМАНДА /R (ОТВЕТИТЬ) ---
        else if (cmd.equals("r") || cmd.equals("reply")) {
            if (args.length < 1) {
                ChatUtil.sendMessage(player, "<yellow>Использование: /r <сообщение>");
                return true;
            }
            if (!replies.containsKey(player.getUniqueId())) {
                ChatUtil.sendMessage(player, "<red>Вам некому отвечать.");
                return true;
            }
            target = Bukkit.getPlayer(replies.get(player.getUniqueId()));
            if (target == null) {
                ChatUtil.sendMessage(player, "<red>Игрок вышел из сети.");
                return true;
            }
            message = String.join(" ", args);
        }

        // --- ОТПРАВКА ---
        sendPrivateMessage(player, target, message);
        return true;
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        // Запоминаем для /r
        replies.put(sender.getUniqueId(), target.getUniqueId());
        replies.put(target.getUniqueId(), sender.getUniqueId());

        // Получаем префиксы
        String senderPrefix = RankManager.getPrefix(sender);
        String targetPrefix = RankManager.getPrefix(target);

        // --- Формат для ОТПРАВИТЕЛЯ (Я -> Игрок) ---
        // [Я -> Admin JetLuna] сообщение
        // Делаем кликабельным, чтобы можно было быстро дописать еще сообщение этому же игроку
        String senderFormat =
                "<click:suggest_command:'/msg " + target.getName() + " '>" +
                        "<hover:show_text:'<gray>Нажми, чтобы написать еще'>" +
                        "<dark_gray>[<green>Я <dark_gray>-> " + targetPrefix + target.getName() + "<dark_gray>] <white>" + message +
                        "</hover></click>";

        // --- Формат для ПОЛУЧАТЕЛЯ (Игрок -> Я) ---
        // [Admin JetLuna -> Я] сообщение
        // При клике в чат вставится: /msg JetLuna
        String targetFormat =
                "<click:suggest_command:'/msg " + sender.getName() + " '>" +
                        "<hover:show_text:'<gray>Нажми, чтобы ответить'>" +
                        "<dark_gray>[" + senderPrefix + sender.getName() + " <dark_gray>-> <green>Я<dark_gray>] <white>" + message +
                        "</hover></click>";

        ChatUtil.sendMessage(sender, senderFormat);
        ChatUtil.sendMessage(target, targetFormat);

        // Звук
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f);
    }
}