package net.jetluna.bedwars.state;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.bedwars.BedWarsPlugin;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class StartingState extends GameState {

    private BukkitRunnable timerTask;
    private int timeLeft = 10; // Время до начала (секунды)

    public StartingState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: ЗАПУСК (Starting)");

        // Запускаем таймер
        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                handleTimerTick();
            }
        };
        // 0L - задержка перед стартом, 20L - повторять каждые 20 тиков (1 секунда)
        this.timerTask.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(); // Обязательно убиваем таймер при смене статуса
        }

        // Очищаем уровень опыта игрокам (визуальный таймер)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(0);
            player.setExp(0);
        }
    }

    @Override
    public String getName() {
        return "STARTING";
    }

    private void handleTimerTick() {
        // Логика каждую секунду

        // Визуальный отсчет уровнями (XP bar)
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(timeLeft);
            player.setExp((float) timeLeft / 10f); // Полоска опыта убывает
        }

        // Сообщения в чат и звуки на важных отметках
        if (timeLeft <= 5 || timeLeft == 10) {
            ChatUtil.sendMessage(Bukkit.getConsoleSender(), "<yellow>Игра начнется через <gold>" + timeLeft + " <yellow>сек!");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                // Титул на экран
                player.showTitle(Title.title(
                        ChatUtil.parse("<green>" + timeLeft),
                        ChatUtil.parse("<gray>Приготовься!"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ZERO)
                ));
            }
        }

        if (timeLeft <= 0) {
            // ВРЕМЯ ВЫШЛО -> НАЧИНАЕМ ИГРУ!
            // plugin.getGameManager().setGameState(new IngameState(plugin));
            // Пока просто напишем в чат, так как IngameState еще нет
            Bukkit.broadcast(ChatUtil.parse("<red><bold>ИГРА НАЧАЛАСЬ! (Пока что это конец демки)"));
            timerTask.cancel();
        }

        timeLeft--;
    }

    @org.bukkit.event.EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        int online = Bukkit.getOnlinePlayers().size() - 1;
        if (online < 2) {
            Bukkit.broadcast(ChatUtil.parse("<red>Недостаточно игроков! Запуск отменен."));
            plugin.getGameManager().setGameState(new WaitingState(plugin)); // Возврат в лобби
        }
    }
}