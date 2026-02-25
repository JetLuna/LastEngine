package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.gui.LobbyGui; // ВОТ ЭТОГО НЕ ХВАТАЛО
import net.jetluna.lobby.gui.RewardGui;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {

    private final LobbyPlugin plugin;

    public LobbyListener(LobbyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Очищаем чат и инвентарь
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);

        // Выдаем предметы (Компас, Профиль и т.д.)
        LobbyItems.giveItems(player);

        // Сообщение приветствия (Таб и прочее обновляются через LobbyTask, но можно и тут)
        String header = LanguageManager.getString(player, "lobby.tab.header").replace("%player%", player.getName());
        String footer = LanguageManager.getString(player, "lobby.tab.footer").replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        // Телепорт на спавн
        new LobbyCommand(plugin).teleportToLobby(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
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
        // Запрещаем двигать предметы в своем инвентаре, если не админ
        if (!event.getWhoClicked().hasPermission("last.admin")) {
            // Если игрок в режиме креатива - можно, иначе нет
            if (event.getWhoClicked().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
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
}