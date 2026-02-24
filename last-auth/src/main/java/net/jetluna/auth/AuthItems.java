package net.jetluna.auth;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.HeadUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AuthItems {

    // Текстуры для голов
    private static final String TEXTURE_TELEGRAM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4YWExM2FiODg1NzJmYTE1ZDNkZTU5YmM1ZTY2NTE5ZThhMzEwMGI1ZTY5MDY2NGE0NmQ1M2UyY2Y5MyJ9fX0=";
    private static final String TEXTURE_EMAIL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Q2YmY5MzY1LED1M2Y5ZTRiYjY2MGQ4ODFjYTcxM2M5YmFjN2Y2ODc2NzdhYjQxMjM1ODMyMGQ0NzM5cCJ9fX0=";
    private static final String TEXTURE_ARROW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM3Y3ZTRlY2E1YzU0N2MzM2Q1ZGI4MWQ1YjM4OTg5OTQzYjVlN2E4Mzc0YTNhRU1MTYwYjUzNTY3YjIifX19";

    // Выдача предметов при входе
    public static void giveAuthItems(Player player) {
        player.getInventory().clear();

        // Компас (выбор метода)
        ItemStack selector = new ItemBuilder(Material.COMPASS)
                .setName(LanguageManager.getString(player, "auth.items.selector.name"))
                .setLore(LanguageManager.getList(player, "auth.items.selector.lore"))
                .build();

        player.getInventory().setItem(4, selector);

        // Стрелка (быстрый вход, если нужно)
        boolean isPremium = true;
        if (isPremium) {
            ItemStack skipAuth = new ItemBuilder(HeadUtil.getHead(TEXTURE_ARROW))
                    .setName("<gold><bold>Быстрый Вход")
                    .setLore("<gray>Лицензия обнаружена!", "", "<yellow>▶ Нажми для входа")
                    .build();

            player.getInventory().setItem(8, skipAuth);
        }
    }

    // --- ВОТ ЭТОГО МЕТОДА НЕ ХВАТАЛО ---
    public static void openAuthGui(Player player) {
        String title = LanguageManager.getString(player, "auth.gui.title");
        Inventory gui = Bukkit.createInventory(null, 27, ChatUtil.parse(title));

        // Telegram
        ItemStack telegram = new ItemBuilder(HeadUtil.getHead(TEXTURE_TELEGRAM))
                .setName(LanguageManager.getString(player, "auth.gui.telegram.name"))
                .setLore(LanguageManager.getList(player, "auth.gui.telegram.lore"))
                .build();

        // Email
        ItemStack email = new ItemBuilder(HeadUtil.getHead(TEXTURE_EMAIL))
                .setName(LanguageManager.getString(player, "auth.gui.email.name"))
                .setLore(LanguageManager.getList(player, "auth.gui.email.lore"))
                .build();

        gui.setItem(11, telegram);
        gui.setItem(15, email);

        player.openInventory(gui);
    }
}