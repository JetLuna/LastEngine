package net.jetluna.lobby.multilobby;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class LobbySelectorGui {

    // Названия серверов ТОЧНО КАК В КОНФИГЕ BungeeCord/Velocity
    private static final String[] SERVERS = {"lobby-1", "lobby-2", "lobby-3", "lobby-4"};

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatUtil.parseLegacy("&8Выбор лобби"));

        for (int i = 0; i < SERVERS.length; i++) {
            int lobbyNum = i + 1;

            ItemStack item = new ItemBuilder(Material.EMERALD_BLOCK)
                    .setName(ChatUtil.parseLegacy("&aЛобби #" + lobbyNum))
                    .setLore(Arrays.asList(
                            "",
                            ChatUtil.parseLegacy("&e▸ Нажмите для перехода!")
                    ))
                    .build();

            // Расставляем по центру (10, 12, 14, 16 слоты)
            int slot = 10 + (i * 2);
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
    }
}