package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LobbyGui {

    // --- МЕНЮ ВЫБОРА РЕЖИМОВ (КОМПАС) ---
    // Раньше этот метод назывался просто open()
    public static void openSelector(Player player) {
        // Заголовок из конфига
        String title = LanguageManager.getString(player, "lobby.inventory.title");

        Inventory inventory = Bukkit.createInventory(null, 27, ChatUtil.parse(title));

        // Предмет БедВарса
        String bwName = LanguageManager.getString(player, "lobby.inventory.bedwars.name");

        ItemStack bedwars = new ItemBuilder(Material.RED_BED)
                .setName(bwName)
                .setLore(LanguageManager.getList(player, "lobby.inventory.bedwars.lore"))
                .build();

        inventory.setItem(13, bedwars);

        player.openInventory(inventory);
    }

    // --- МЕНЮ ПРОФИЛЯ (ГОЛОВА) ---
    // Этого метода не хватало, поэтому LobbyListener ругался
    public static void openProfile(Player player) {
        // Пока сделаем простое меню, позже вынесем в конфиг
        String title = "<black>Профиль";
        Inventory inventory = Bukkit.createInventory(null, 27, ChatUtil.parse(title));

        // Пример статистики
        ItemStack stats = new ItemBuilder(Material.PAPER)
                .setName("<yellow>Ваша статистика")
                .setLore(
                        "<gray>Ранг: " + net.jetluna.api.rank.RankManager.getPrefix(player),
                        "",
                        "<gray>Тут скоро будет статистика!"
                )
                .build();

        inventory.setItem(13, stats);

        player.openInventory(inventory);
    }
}