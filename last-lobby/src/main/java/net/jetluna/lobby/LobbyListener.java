package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {

    private final LobbyPlugin plugin;

    public LobbyListener(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    // ВХОД ИГРОКА
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null); // Отключаем стандартное сообщение
        Player player = event.getPlayer();

        // Выдаем предметы
        LobbyItems.giveItems(player);

        // Запускаем скорборд/таб
        LobbyBoard.update(player);
        LobbyTab.update(player);

        // Телепортируем на спавн
        new LobbyCommand(plugin).teleportToLobby(player);
    }

    // ЗАПРЕТ НА ВЫБРАСЫВАНИЕ / ПЕРЕМЕЩЕНИЕ
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().hasPermission("last.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("last.admin")) {
            event.setCancelled(true); // Запрещаем двигать предметы
        }
    }

    // ЛОГИКА КЛИКОВ (Компас, Профиль, Скрытие)
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Реагируем только на клик ПКМ (Воздух или Блок)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // 1. КОМПАС -> Открыть меню игр
        if (item.getType() == Material.COMPASS) {
            LobbyGui.openSelector(player);
            playSound(player);
            return;
        }

        // 2. ГОЛОВА -> Открыть профиль
        if (item.getType() == Material.PLAYER_HEAD) {
            LobbyGui.openProfile(player);
            playSound(player);
            return;
        }

        // 3. КРАСИТЕЛЬ -> Скрыть/Показать игроков
        if (item.getType() == Material.LIME_DYE || item.getType() == Material.GRAY_DYE) {
            toggleVisibility(player, item);
            playSound(player);
        }
    }

    // Метод переключения видимости
    private void toggleVisibility(Player player, ItemStack item) {
        // Если сейчас ЛАЙМОВЫЙ (Видно) -> Скрываем (Делаем СЕРЫМ)
        if (item.getType() == Material.LIME_DYE) {
            // Меняем предмет в руке на Серый
            ItemStack disabled = new ItemBuilder(Material.GRAY_DYE)
                    .setName(LanguageManager.getString(player, "lobby.items.visibility.disabled.name"))
                    .setLore(LanguageManager.getList(player, "lobby.items.visibility.disabled.lore"))
                    .build();
            player.getInventory().setItemInMainHand(disabled);

            // Скрываем всех
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_hide");

        } else {
            // Если сейчас СЕРЫЙ (Скрыто) -> Показываем (Делаем ЛАЙМОВЫМ)
            ItemStack enabled = new ItemBuilder(Material.LIME_DYE)
                    .setName(LanguageManager.getString(player, "lobby.items.visibility.enabled.name"))
                    .setLore(LanguageManager.getList(player, "lobby.items.visibility.enabled.lore"))
                    .build();
            player.getInventory().setItemInMainHand(enabled);

            // Показываем всех
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_show");
        }
    }

    private void playSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    // ЧАТ (на всякий случай, если потерял)
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        String prefix = net.jetluna.api.rank.RankManager.getPrefix(event.getPlayer());
        String arrow = "<dark_gray>»";
        String color = event.getPlayer().hasPermission("last.admin") ? "<white>" : "<gray>";

        String format = prefix + " <white>" + event.getPlayer().getName() + " " + arrow + " " + color + event.getMessage();

        for (Player p : Bukkit.getOnlinePlayers()) {
            ChatUtil.sendMessage(p, format);
        }
    }
}