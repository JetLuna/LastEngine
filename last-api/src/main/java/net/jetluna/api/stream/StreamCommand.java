package net.jetluna.api.stream;

import net.jetluna.api.LastApi;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StreamCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Rank rank = RankManager.getRank(player);

        if (label.equalsIgnoreCase("streams")) {
            StreamsGui.open(player);
            return true;
        }

        if (args.length == 0) {
            ChatUtil.sendMessage(player, "&eИспользование:");
            if (rank.getWeight() >= 12) ChatUtil.sendMessage(player, "&e/stream link <ник> <ссылка> &7- Привязать канал");
            if (rank.getWeight() >= 6) {
                ChatUtil.sendMessage(player, "&e/stream add <ссылка> &7- Опубликовать стрим");
                ChatUtil.sendMessage(player, "&e/stream stop &7- Завершить стрим");
            }
            return true;
        }

        // --- ПРИВЯЗКА КАНАЛА ---
        if (args[0].equalsIgnoreCase("link") && rank.getWeight() >= 12) {
            if (args.length < 3) {
                ChatUtil.sendMessage(player, "&cИспользование: /stream link <ник> <ссылка на канал>");
                return true;
            }
            String target = args[1];
            String url = args[2];
            StreamManager.linkChannel(target, url);
            ChatUtil.sendMessage(player, "&aКанал &e" + url + " &aуспешно добавлен игроку &e" + target);
            return true;
        }

        // --- РУЧНАЯ ОСТАНОВКА СТРИМА ---
        if (args[0].equalsIgnoreCase("stop") && rank.getWeight() >= 6) {
            if (StreamManager.getActiveStreams().containsKey(player.getName())) {
                StreamManager.removeActiveStream(player.getName());
                ChatUtil.sendMessage(player, "&aВаш стрим успешно удален из списка активных!");
            } else {
                ChatUtil.sendMessage(player, "&cУ вас нет запущенных стримов.");
            }
            return true;
        }

        // --- ПУБЛИКАЦИЯ СТРИМА ---
        if (args[0].equalsIgnoreCase("add") && rank.getWeight() >= 6) {
            if (args.length < 2) {
                ChatUtil.sendMessage(player, "&cИспользование: /stream add <полная ссылка на стрим>");
                return true;
            }

            Set<String> linkedChannels = StreamManager.getLinkedChannels(player.getName());
            if (linkedChannels == null || linkedChannels.isEmpty()) {
                ChatUtil.sendMessage(player, "&cВаш аккаунт не привязан ни к одному каналу! Обратитесь к администрации.");
                return true;
            }

            String url = args[1].toLowerCase();

            // --- УМНАЯ ПРОВЕРКА ССЫЛОК ---
            boolean isValid = false;
            for (String linked : linkedChannels) {
                // 1. Строгая проверка (для Twitch и кастомных ссылок)
                if (url.contains(linked)) {
                    isValid = true;
                    break;
                }

                // 2. Исключение для YouTube (Разрешаем форматы /live/ и /watch?)
                if (linked.contains("youtube.com") || linked.contains("youtu.be")) {
                    if (url.contains("youtube.com/live/") || url.contains("youtube.com/watch") || url.contains("youtu.be/")) {
                        isValid = true;
                        break;
                    }
                }
            }

            if (!isValid) {
                ChatUtil.sendMessage(player, "&cОшибка! Ссылка не совпадает ни с одним из ваших привязанных каналов.");
                return true;
            }

            StreamManager.addActiveStream(player.getName(), args[1]);

            // АВТО-УДАЛЕНИЕ через 2 часа
            Bukkit.getScheduler().runTaskLaterAsynchronously(LastApi.getInstance(), () -> {
                StreamManager.removeActiveStream(player.getName());
            }, 144000L);

            announceStream(player, args[1]);
            return true;
        }

        return true;
    }

    private void announceStream(Player streamer, String url) {
        String separator = ChatColor.translateAlternateColorCodes('&', "&d================================================");
        String title = ChatColor.translateAlternateColorCodes('&', "  &b&lПРЯМОЙ ЭФИР &8| &f" + streamer.getName());
        String subtitle = ChatColor.translateAlternateColorCodes('&', "  &7Игрок запустил стрим на проекте! Залетай!");

        TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&', "  &e&nНажми, чтобы перейти на стрим ➔"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7Открыть трансляцию"))));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(separator);
            p.sendMessage(title);
            p.sendMessage(subtitle);
            p.sendMessage("");
            p.spigot().sendMessage(link);
            p.sendMessage(separator);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.5f);
        }
    }
}