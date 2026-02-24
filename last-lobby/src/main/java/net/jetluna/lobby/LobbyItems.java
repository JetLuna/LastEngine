package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class LobbyItems {

    public static void giveItems(Player player) {
        player.getInventory().clear();

        // 1. КОМПАС (Навигатор) - Слот 4 (Центр)
        ItemStack compass = new ItemBuilder(Material.COMPASS)
                .setName(LanguageManager.getString(player, "lobby.items.navigator.name"))
                .setLore(LanguageManager.getList(player, "lobby.items.navigator.lore"))
                .build();
        player.getInventory().setItem(4, compass);

        // 2. ГОЛОВА (Профиль) - Слот 0 (Слева)
        ItemStack profile = new ItemBuilder(Material.PLAYER_HEAD)
                .setName(LanguageManager.getString(player, "lobby.items.profile.name"))
                .setLore(LanguageManager.getList(player, "lobby.items.profile.lore"))
                .build();

        // Ставим скин игрока на голову
        SkullMeta meta = (SkullMeta) profile.getItemMeta();
        meta.setOwningPlayer(player);
        profile.setItemMeta(meta);

        player.getInventory().setItem(0, profile);

        // 3. КРАСИТЕЛЬ (Скрытие игроков) - Слот 8 (Справа)
        // По умолчанию выдаем Лаймовый (Всех видно)
        ItemStack visibility = new ItemBuilder(Material.LIME_DYE)
                .setName(LanguageManager.getString(player, "lobby.items.visibility.enabled.name"))
                .setLore(LanguageManager.getList(player, "lobby.items.visibility.enabled.lore"))
                .build();

        player.getInventory().setItem(8, visibility);
    }
}