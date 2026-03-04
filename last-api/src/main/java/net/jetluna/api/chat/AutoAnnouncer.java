package net.jetluna.api.chat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AutoAnnouncer implements Runnable {

    private int index = 0;

    // Структура одного рекламного сообщения
    private static class Announcement {
        String[] text;
        String url;
        String linkText;

        Announcement(String url, String linkText, String... text) {
            this.text = text;
            this.url = url;
            this.linkText = linkText;
        }
    }

    // Список всех наших реклам (они будут идти по очереди)
    private final List<Announcement> messages = Arrays.asList(
            new Announcement("https://forum.lastengine.net", "Перейти на Форум ➔",
                    "&fОбщайся, предлагай свои идеи и подавай",
                    "&fзаявки на нашем официальном форуме!"),

            new Announcement("https://t.me/lastengine", "Перейти в Telegram ➔",
                    "&fПрисоединяйся к нашему Telegram-каналу,",
                    "&fчтобы быть в курсе всех новостей и обновлений!"),

            new Announcement("https://www.lastengine.net", "Перейти на Сайт ➔",
                    "&fПосети наш официальный сайт для получения",
                    "&fподробной информации о проекте!"),

            new Announcement("https://store.lastengine.net", "Открыть Магазин ➔",
                    "&fХочешь выделиться и поддержать проект?",
                    "&fПокупай привилегии и кейсы в нашем магазине!"),

            new Announcement("https://discord.gg/lastengine", "Присоединиться к Discord ➔",
                    "&fЗаходи в наш Discord-сервер!",
                    "&fИщи тиммейтов и общайся с другими игроками.")
    );

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return; // Не спамим в пустой сервер

        // Берем текущее сообщение и двигаем индекс вперед
        Announcement ann = messages.get(index);
        index = (index + 1) % messages.size();

        // Подготавливаем дизайн
        String separator = ChatColor.translateAlternateColorCodes('&', "&8================================================");
        String title = ChatColor.translateAlternateColorCodes('&', "  &a&lLAST ENGINE");

        // Создаем кликабельную ссылку через нативный API
        TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&', "  &e&n" + ann.linkText));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ann.url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7Нажми, чтобы открыть ссылку"))));

        // Рассылаем всем локальным игрокам (это сработает на каждом сервере)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(separator);
            player.sendMessage(title);
            for (String line : ann.text) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "  " + line));
            }
            player.sendMessage(""); // Пустая строка
            player.spigot().sendMessage(link); // Кликабельная ссылка
            player.sendMessage(separator);

            // Легкий звук колокольчика для привлечения внимания
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
        }
    }
}