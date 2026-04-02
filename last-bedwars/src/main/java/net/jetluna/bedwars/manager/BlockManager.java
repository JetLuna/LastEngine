package net.jetluna.bedwars.manager;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class BlockManager {

    private final Set<String> placedBlocks = new HashSet<>();

    // Превращаем координаты в строку — yaw/pitch не участвуют
    private String key(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public void addBlock(Block block) {
        if (block != null) placedBlocks.add(key(block));
    }

    public void removeBlock(Block block) {
        if (block != null) placedBlocks.remove(key(block));
    }

    public boolean isPlayerPlaced(Block block) {
        if (block == null) return false;
        return placedBlocks.contains(key(block));
    }

    public void clearBlocks() {
        placedBlocks.clear();
    }
}