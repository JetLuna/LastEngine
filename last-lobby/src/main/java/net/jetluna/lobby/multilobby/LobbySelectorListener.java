package net.jetluna.lobby.multilobby;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class LobbySelectorListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack selector = new ItemBuilder(Material.NETHER_STAR)
                .setName(ChatUtil.parseLegacy("&aВыбор лобби &7(Нажмите)"))
                .build();
        player.getInventory().setItem(7, selector); // 8-й слот в хотбаре
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR) {
                LobbySelectorGui.open(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.NETHER_STAR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.NETHER_STAR) {
            if (!ChatUtil.strip(event.getView().getTitle()).equals("Выбор лобби")) {
                event.setCancelled(true);
            }
        }

        if (ChatUtil.strip(event.getView().getTitle()).equals("Выбор лобби")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            int slot = event.getSlot();
            String targetServer = null;

            // Названия серверов должны совпадать с BungeeCord/Velocity
            if (slot == 10) targetServer = "lobby-1";
            else if (slot == 12) targetServer = "lobby-2";
            else if (slot == 14) targetServer = "lobby-3";
            else if (slot == 16) targetServer = "lobby-4";

            if (targetServer != null) {
                ChatUtil.sendMessage(player, "&aПодключение к " + targetServer + "...");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.closeInventory();

                // Перебрасываем игрока!
                BungeeUtil.sendToServer(player, targetServer);
            }
        }
    }
}