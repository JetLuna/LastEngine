package net.jetluna.auth;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.HeadUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AuthItems {

    // Текстуры (можно найти на minecraft-heads.com)
    private static final String TEXTURE_ARROW = "f2f3a2dfce0c3dab7ee10db385e5229f1a39534a8ba2646178e37c4fa93b";
    private static final String TEXTURE_EMAIL = "d6aaef0120af71ba3b83fbddabc334bc63f2311599698a318243be69f0607da3";
    private static final String TEXTURE_TELEGRAM = "d3d25d55caedfd70ee7f3a806af0025f153d4bf34f41e6efd44ec8fe74198f35";

    // Предметы для инвентаря
    public static void giveAuthItems(Player player) {
        player.getInventory().clear();

        // 1. Компас (Авторизация) - для всех
        ItemStack compass = new ItemBuilder(Material.COMPASS)
                .name("<green><bold>Авторизация")
                .lore("<gray>Нажми ПКМ, чтобы войти")
                .build();
        player.getInventory().setItem(4, compass); // В центр слота

        // 2. Стрелка (Только для лицензии)
        // ВАЖНО: Проверка на лицензию в offline-mode сервере сложная.
        // Пока сделаем заглушку: даем, если у игрока красивый UUID (версия 4) или просто всем для теста.
        boolean isPremium = true; // Тут будет твоя логика проверки

        if (isPremium) {
            ItemStack skipAuth = new ItemBuilder(HeadUtil.getHead(TEXTURE_ARROW))
                    .name("<gold><bold>Быстрый Вход")
                    .lore("<gray>Лицензионный аккаунт обнаружен!", "<yellow>Нажми ПКМ для входа")
                    .build();
            player.getInventory().setItem(8, skipAuth);
        }
    }

    // Меню выбора (ТГ или Почта)
    public static void openAuthGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatUtil.parse("<black>Выберите способ входа"));

        ItemStack telegram = new ItemBuilder(HeadUtil.getHead(TEXTURE_TELEGRAM)) // Замени текстуру на синюю голову
                .name("<aqua><bold>Telegram")
                .lore("<gray>Получить код через бота", "", "<yellow>Нажми для выбора")
                .build();

        ItemStack email = new ItemBuilder(HeadUtil.getHead(TEXTURE_EMAIL))
                .name("<gold><bold>Email")
                .lore("<gray>Получить код на почту", "", "<yellow>Нажми для выбора")
                .build();

        gui.setItem(11, telegram);
        gui.setItem(15, email);

        player.openInventory(gui);
    }
}