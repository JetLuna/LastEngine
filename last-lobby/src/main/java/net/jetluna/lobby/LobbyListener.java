package net.jetluna.lobby;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.api.util.NameFormatUtil;
import net.jetluna.lobby.gui.SettingsGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

        LobbyItems.giveItems(player);
        new LobbyCommand(plugin).teleportToLobby(player);

        // --- ДЖОИНЕРЫ (Красивые сообщения при входе) ---
        Rank rank = RankManager.getRank(player);
        PlayerStats stats = StatsManager.getStats(player);
        String suffix = (stats != null && stats.getSuffix() != null) ? color(stats.getSuffix()) : "";

        // 1. Получаем готовый красивый ник (с кастомным цветом)
        String formattedName = NameFormatUtil.getFormattedName(player, rank);

        // 2. Вставляем его в сообщение. Отдельный плейсхолдер %prefix% заменяем на пустоту,
        // так как префикс уже красиво вшит внутри formattedName
        String joinMsg = color(net.jetluna.lobby.gui.JoinerGui.getActiveMessage(player))
                .replace("%prefix%", "")
                .replace("%player%", formattedName)
                .replace("%suffix%", suffix);

        event.setJoinMessage(joinMsg);

        // Включаем полет/прыжки если надо
        SettingsGui.updateFlight(player);

        // Оповещение стаффа
        net.jetluna.api.staff.StaffNotifier.notifyJoin(player, suffix);

        // СПАВН ПИТОМЦА ПРИ ВХОДЕ
        if (stats != null && stats.getActivePet() != null && !stats.getActivePet().isEmpty()) {
            try {
                net.jetluna.api.pet.PetType type = net.jetluna.api.pet.PetType.valueOf(stats.getActivePet());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        net.jetluna.api.pet.PetManager.spawnPet(player, type);
                    }
                }, 10L);
            } catch (IllegalArgumentException ignored) {}
        }
        // Делаем небольшую задержку (5 тиков), чтобы Scoreboard точно успел загрузиться
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            net.jetluna.api.util.NameFormatUtil.refreshNameTags();
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        net.jetluna.api.pet.PetManager.removePet(event.getPlayer());
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
            event.setCancelled(true);
            LobbyGui.openSelector(player);
            playSound(player);
            return;
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            event.setCancelled(true);
            LobbyGui.openProfile(player);
            playSound(player);
            return;
        }

        if (item.getType() == Material.LIME_DYE || item.getType() == Material.GRAY_DYE) {
            event.setCancelled(true);
            toggleVisibility(player, item);
            playSound(player);
        }
    }

    private void toggleVisibility(Player player, ItemStack item) {
        if (item.getType() == Material.LIME_DYE) {
            ItemStack disabled = new ItemBuilder(Material.GRAY_DYE)
                    .setName(color(LanguageManager.getString(player, "lobby.items.visibility.disabled.name")))
                    .setLore(colorList(player, "lobby.items.visibility.disabled.lore"))
                    .build();
            player.getInventory().setItemInMainHand(disabled);
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_hide");
        } else {
            ItemStack enabled = new ItemBuilder(Material.LIME_DYE)
                    .setName(color(LanguageManager.getString(player, "lobby.items.visibility.enabled.name")))
                    .setLore(colorList(player, "lobby.items.visibility.enabled.lore"))
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

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
            if (plugin.getLobbySpawn() != null) {
                player.teleport(plugin.getLobbySpawn());
            } else {
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFood(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityBlockForm(org.bukkit.event.block.EntityBlockFormEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Snowman) {
            if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains("Питомец")) {
                event.setCancelled(true);
            }
        }
    }

    // Вспомогательные методы для парсинга цветов
    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        return list;
    }

    @EventHandler
    public void onLanguageChange(net.jetluna.api.lang.PlayerLanguageChangeEvent event) {
        Player player = event.getPlayer();

        // Запоминаем, были ли скрыты игроки (проверяем наличие серого красителя ДО очистки)
        boolean wasHidden = player.getInventory().contains(org.bukkit.Material.GRAY_DYE);

        // Очищаем старые предметы (на старом языке)
        player.getInventory().clear();

        // Выдаем новые предметы (они уже подтянут новый язык из LanguageManager)
        LobbyItems.giveItems(player);

        // Если игроки были скрыты, заменяем выданный зеленый краситель на серый с правильным переводом
        if (wasHidden) {
            ItemStack disabled = new net.jetluna.api.util.ItemBuilder(org.bukkit.Material.GRAY_DYE)
                    .setName(color(LanguageManager.getString(player, "lobby.items.visibility.disabled.name")))
                    .setLore(colorList(player, "lobby.items.visibility.disabled.lore"))
                    .build();

            // Находим слот с зеленым красителем и меняем его
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                org.bukkit.inventory.ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == org.bukkit.Material.LIME_DYE) {
                    player.getInventory().setItem(i, disabled);
                    break;
                }
            }
        }
    }

    // --- ЗВУК ПРИ СМЕНЕ СЛОТА В ХОТБАРЕ ---
    @EventHandler
    public void onItemHeldChange(org.bukkit.event.player.PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // ENTITY_EXPERIENCE_ORB_PICKUP - звук опыта
        // 0.2f - очень тихая громкость
        // 2.0f - максимальная тональность (делает звук очень коротким и "булькающим")
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 2.0f);
    }
}