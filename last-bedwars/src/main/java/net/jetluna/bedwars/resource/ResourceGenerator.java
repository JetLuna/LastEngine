package net.jetluna.bedwars.resource;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class ResourceGenerator {

    private final Location location;
    private final Resource resource;
    private long timeLeft;

    public ResourceGenerator(Location location, Resource resource) {
        this.location = location;
        this.resource = resource;
        this.timeLeft = resource.getDelayMillis();
    }

    // Метод вызывается каждый "тик" менеджера
    public void tick(long millisPassed, double speedMultiplier) {
        timeLeft -= millisPassed;

        if (timeLeft <= 0) {
            spawnResource();
            // Сбрасываем таймер, учитывая множитель скорости режима (Double, Trio и т.д.)
            timeLeft = (long) (resource.getDelayMillis() / speedMultiplier);
        }
    }

    private void spawnResource() {
        if (location.getWorld() == null) return;

        ItemStack item = resource.buildItem();

        // 1.21.11 API: Ищем только игроков (SURVIVAL) в радиусе 1.5 блоков
        Collection<Player> nearbyPlayers = location.getWorld().getNearbyEntitiesByType(
                Player.class,
                location,
                1.5, 1.5, 1.5,
                player -> player.getGameMode() == GameMode.SURVIVAL
        );

        if (nearbyPlayers.isEmpty()) {
            // Если никого нет — бросаем предмет на землю
            location.getWorld().dropItem(location, item);
        } else {
            // Если кто-то стоит в спавнере — кладем предмет прямо в инвентарь всем
            for (Player p : nearbyPlayers) {
                p.getInventory().addItem(item);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 2.0f);
            }
        }
    }

    public Location getLocation() { return location; }
    public Resource getResource() { return resource; }
}