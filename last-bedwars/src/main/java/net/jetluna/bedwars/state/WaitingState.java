package net.jetluna.bedwars.state;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WaitingState extends GameState {

    public WaitingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: ОЖИДАНИЕ (Lobby)");
    }

    @Override
    public void onDisable() {
        // Очистка перед началом игры
    }

    @Override
    public String getName() {
        return "WAITING";
    }

    // --- СОБЫТИЯ ---

    @org.bukkit.event.EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        event.joinMessage(null);
        org.bukkit.entity.Player player = event.getPlayer();

        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);

        // --- 1. ТЕЛЕПОРТ В ЛОББИ ОЖИДАНИЯ ---
        org.bukkit.Location spawnLoc = plugin.getConfig().getLocation("locations.waiting-lobby");
        if (spawnLoc != null) {
            // Задержка в 1 тик нужна, чтобы сервер успел прогрузить игрока в мире
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawnLoc);
            }, 1L);
        } else {
            // Предупреждение для админов, если забыли поставить точку
            if (player.hasPermission("bedwars.admin")) {
                player.sendMessage("§c[!] Точка лобби ожидания не установлена! Напишите /bw setspawn");
            }
        }

        // --- 2. ОПОВЕЩЕНИЕ О ЗАХОДЕ ---
        int online = org.bukkit.Bukkit.getOnlinePlayers().size();
        int max = 8; // Допустим макс 8
        org.bukkit.Bukkit.broadcast(
                net.jetluna.api.util.ChatUtil.parse("§7Игрок §a" + player.getName() + " §7подключился! §e(" + online + "/" + max + ")")
        );

        // --- 3. ПРОВЕРКА ЗАПУСКА ---
        if (online >= 2) {
            plugin.getGameManager().setGameState(new StartingState(plugin));
        }
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        event.quitMessage(null);

        int online = org.bukkit.Bukkit.getOnlinePlayers().size() - 1;

        // Если кто-то вышел и игроков стало меньше 2 - возвращаемся в ожидание (если таймер уже идет)
        // Но эту проверку лучше делать внутри StartingState.
        // Пока оставим просто логирование.
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) event.setCancelled(true);
    }

    // --- ЗАЩИТА ЛОББИ ---
    @org.bukkit.event.EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        // Отменяем абсолютно любой урон (от игроков, падения, огня)
        event.setCancelled(true);

        // Бонус: Если игрок случайно выпал за пределы карты в лобби, возвращаем его на спавн
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
            if (event.getEntity() instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();

                // В зависимости от файла (WaitingState или EndingState) бери нужную точку:
                // Для WaitingState: "locations.waiting-lobby"
                // Для EndingState: "locations.win-lobby"
                org.bukkit.Location safeLoc = plugin.getConfig().getLocation("locations.waiting-lobby");

                if (safeLoc != null) {
                    player.teleport(safeLoc);
                }
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}