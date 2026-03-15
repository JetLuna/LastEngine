package net.jetluna.bedwars.manager;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class BlockManager {

    // Храним локации (координаты), а не сами изменчивые блоки!
    // HashSet работает в 10 раз быстрее обычного списка при поиске.
    private final Set<Location> placedBlocks = new HashSet<>();

    public void addBlock(Block block) {
        if (block != null) {
            placedBlocks.add(block.getLocation());
        }
    }

    public void removeBlock(Block block) {
        if (block != null) {
            placedBlocks.remove(block.getLocation());
        }
    }

    public boolean isPlayerPlaced(Block block) {
        if (block == null) return false;
        return placedBlocks.contains(block.getLocation());
    }

    // Очищаем список при перезапуске (хотя у нас и так сервер рестартится, но для порядка нужно)
    public void clearBlocks() {
        placedBlocks.clear();
    }
}