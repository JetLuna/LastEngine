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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
        event.getPlayer().getInventory().clear();

        // Пишем сообщение о входе
        int online = org.bukkit.Bukkit.getOnlinePlayers().size();
        int max = 8; // Допустим макс 8
        org.bukkit.Bukkit.broadcast(
                net.jetluna.api.util.ChatUtil.parse("<gray>Игрок <green>" + event.getPlayer().getName() + " <gray>подключился! <yellow>(" + online + "/" + max + ")")
        );

        // --- ВАЖНО: ПРОВЕРКА ЗАПУСКА ---
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

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}