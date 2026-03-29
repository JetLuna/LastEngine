package net.jetluna.bedwars.state;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EndingState extends GameState {

    public EndingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: ОКОНЧАНИЕ (Ending)");

        // Получаем точку лобби победителей из конфига
        Location winLobby = plugin.getConfig().getLocation("locations.win-lobby");

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Очищаем игрока полностью
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

            // Ставим режим приключений (чтобы не ломали лобби)
            player.setGameMode(GameMode.ADVENTURE);

            // Телепортируем, если точка установлена
            if (winLobby != null) {
                player.teleport(winLobby);
            } else {
                player.sendMessage("§cОшибка: Лобби победителей не установлено админом!");
            }

            // Выдаем фейерверки или запускаем красивый тайтл
            player.sendTitle("§6§lИГРА ОКОНЧЕНА", "§7Спасибо за игру!", 10, 80, 10);
        }

        // Тут запускаем таймер на 10-15 секунд до перезагрузки арены или кика игроков в главное лобби сервера
        startRestartTimer();
    }

    @Override
    public void onDisable() {
        // Очистка при выключении (например, удаление NPC)
        plugin.getNpcManager().clearNpcs();
    }

    @Override
    public String getName() {
        return "ENDING";
    }

    private void startRestartTimer() {
        new org.bukkit.scheduler.BukkitRunnable() {
            int time = 10;
            @Override
            public void run() {
                if (time <= 0) {
                    Bukkit.broadcastMessage("§cСервер перезагружается...");
                    Bukkit.shutdown(); // Или отправка игроков на BungeeCord сервер
                    cancel();
                    return;
                }
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
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

    @org.bukkit.event.EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        // Отключаем потерю голода в лобби
        event.setCancelled(true);
    }
}