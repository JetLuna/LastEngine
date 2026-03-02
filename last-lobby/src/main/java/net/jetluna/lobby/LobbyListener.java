package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;           // !!! ДОБАВИЛ
import net.jetluna.api.rank.RankManager;    // !!! ДОБАВИЛ
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.gui.SettingsGui;   // !!! ДОБАВИЛ
import org.bukkit.Bukkit;
import org.bukkit.GameMode;                 // !!! ДОБАВИЛ
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;           // !!! Звездочка подключит все события игрока
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

        // Сообщение приветствия
        String header = LanguageManager.getString(player, "lobby.tab.header").replace("%player%", player.getName());
        String footer = LanguageManager.getString(player, "lobby.tab.footer").replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        player.sendPlayerListHeaderAndFooter(ChatUtil.parse(header), ChatUtil.parse(footer));

        // Телепорт на спавн
        new LobbyCommand(plugin).teleportToLobby(player);

        // Включаем полет/прыжки если надо
        SettingsGui.updateFlight(player);
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
        if (!event.getWhoClicked().hasPermission("last.admin")) {
            if (event.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    // ЛОГИКА КЛИКОВ (Компас, Профиль, Скрытие)
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        if (item.getType() == Material.COMPASS) {
            LobbyGui.openSelector(player);
            playSound(player);
            return;
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            LobbyGui.openProfile(player);
            playSound(player);
            return;
        }

        if (item.getType() == Material.LIME_DYE || item.getType() == Material.GRAY_DYE) {
            toggleVisibility(player, item);
            playSound(player);
        }
    }

    private void toggleVisibility(Player player, ItemStack item) {
        if (item.getType() == Material.LIME_DYE) {
            ItemStack disabled = new ItemBuilder(Material.GRAY_DYE)
                    .setName(LanguageManager.getString(player, "lobby.items.visibility.disabled.name"))
                    .setLore(LanguageManager.getList(player, "lobby.items.visibility.disabled.lore"))
                    .build();
            player.getInventory().setItemInMainHand(disabled);
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_hide");
        } else {
            ItemStack enabled = new ItemBuilder(Material.LIME_DYE)
                    .setName(LanguageManager.getString(player, "lobby.items.visibility.enabled.name"))
                    .setLore(LanguageManager.getList(player, "lobby.items.visibility.enabled.lore"))
                    .build();
            player.getInventory().setItemInMainHand(enabled);
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_show");
        }
    }

    private void playSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    // ФИЗИКА
    @EventHandler
    public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
        if (!net.jetluna.lobby.PhysicsCommand.isPhysicsEnabled()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockForm(org.bukkit.event.block.BlockFormEvent event) {
        if (!net.jetluna.lobby.PhysicsCommand.isPhysicsEnabled()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent event) {
        if (!net.jetluna.lobby.PhysicsCommand.isPhysicsEnabled()) event.setCancelled(true);
    }

    // ПОЛЕТЫ И ДВОЙНЫЕ ПРЫЖКИ
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        GameMode gm = player.getGameMode();

        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        Rank rank = RankManager.getRank(player);
        boolean hasFly = rank.getWeight() >= 3 && SettingsGui.isFlyEnabled(player);
        boolean hasDJ = rank.getWeight() >= 2 && SettingsGui.isDoubleJumpEnabled(player);

        if (hasFly) return;

        if (hasDJ) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1.0));
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
            player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            Rank rank = RankManager.getRank(player);
            if (rank.getWeight() >= 2 && SettingsGui.isDoubleJumpEnabled(player) && !SettingsGui.isFlyEnabled(player)) {
                player.setAllowFlight(true);
            }
        }
    }
}