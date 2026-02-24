package net.jetluna.auth;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.auth.manager.CodeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class AuthListener implements Listener {

    private final AuthPlugin plugin;
    private final AuthManager authManager;
    private final CodeManager codeManager;

    public AuthListener(AuthPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.codeManager = new CodeManager();
    }

    // --- ВХОД И ВЫХОД ---

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player player = event.getPlayer();

        // Телепортация на спавн при входе
        teleportToSpawn(player);

        // Выдаем предметы
        AuthItems.giveAuthItems(player);

        ChatUtil.sendMessage(player, "<yellow>Используйте компас для входа!");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        authManager.removeSession(event.getPlayer());
    }

    // --- ЗАЩИТА ИГРОКА (УРОН И ГОЛОД) ---

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true); // Отключаем любой урон
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        if (event.getEntity() instanceof Player player) {
            player.setFoodLevel(20); // Всегда сыт
        }
    }

    // --- ЗАЩИТА МИРА (БЛОКИ И ТАБЛИЧКИ) ---

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) { // Разрешаем ломать только админам
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp()) { // Разрешаем строить только админам
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!authManager.isAuthorized(event.getPlayer())) {
            event.setCancelled(true); // Нельзя выбрасывать вещи, пока не вошел
        }
    }

    // --- ПРОВЕРКА ПАДЕНИЯ В ПУСТОТУ ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Оптимизация: проверяем только если изменился блок по Y
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        if (event.getTo().getY() <= 30) {
            teleportToSpawn(event.getPlayer());
        }
    }

    // --- ЛОГИКА АВТОРИЗАЦИИ (КЛИКИ И ЧАТ) ---

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (authManager.isAuthorized(event.getPlayer())) return;

        // Защита от взаимодействия с миром (сундуки, кнопки и т.д.)
        if (event.getAction() == Action.PHYSICAL) { // Например, топтание грядок
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item == null) return;

            event.setCancelled(true); // Отменяем использование предмета

            if (item.getType() == Material.COMPASS) {
                AuthItems.openAuthGui(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (authManager.isAuthorized(player)) return;

        if (!event.getView().title().toString().contains("Выберите")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        AuthSession session = authManager.getSession(player);
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Email") || item.getType() == Material.PLAYER_HEAD) {
            player.closeInventory();
            session.setState(AuthSession.State.AWAITING_EMAIL);
            ChatUtil.sendMessage(player, "<green>Введите вашу почту в чат:");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (authManager.isAuthorized(player)) return;

        AuthSession session = authManager.getSession(player);
        String message = event.getMessage().trim();

        if (session.getState() == AuthSession.State.AWAITING_EMAIL) {
            event.setCancelled(true);
            String code = codeManager.generateCode(player.getUniqueId(), message);

            // Лог в консоль
            System.out.println("=========================================");
            System.out.println("[LastAuth] КОД ДЛЯ " + player.getName() + ": " + code);
            System.out.println("=========================================");

            session.setTempContact(message);
            session.setState(AuthSession.State.AWAITING_CODE);
            ChatUtil.sendMessage(player, "<yellow>Код отправлен! (Проверь консоль). Введите его в чат:");
            return;
        }

        if (session.getState() == AuthSession.State.AWAITING_CODE) {
            event.setCancelled(true);

            if (codeManager.checkCode(player.getUniqueId(), message)) {
                authManager.setAuthorized(player);
                player.getInventory().clear();
                ChatUtil.sendMessage(player, "<green>Успешная авторизация!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            } else {
                ChatUtil.sendMessage(player, "<red>Неверный код! Попробуйте еще раз.");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    // Вспомогательный метод для телепортации
    private void teleportToSpawn(Player player) {
        if (plugin.getConfig().contains("spawn.world")) {
            try {
                World w = Bukkit.getWorld(plugin.getConfig().getString("spawn.world"));
                double x = plugin.getConfig().getDouble("spawn.x");
                double y = plugin.getConfig().getDouble("spawn.y");
                double z = plugin.getConfig().getDouble("spawn.z");
                float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
                float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

                if (w != null) {
                    player.teleport(new Location(w, x, y, z, yaw, pitch));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка телепортации на спавн!");
            }
        }
    }
}