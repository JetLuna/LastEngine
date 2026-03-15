package net.jetluna.bedwars.resource;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GeneratorManager {

    private final BedWarsPlugin plugin;
    private final List<ResourceGenerator> generators = new ArrayList<>();
    private BukkitRunnable task;
    private double currentMultiplier = 1.0;

    public GeneratorManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void addGenerator(ResourceGenerator generator) {
        generators.add(generator);
    }

    // Запускаем работу всех спавнеров
    public void startGenerators(double multiplier) {
        this.currentMultiplier = multiplier;

        if (task != null) task.cancel();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                // Вызываем tick каждые 50 миллисекунд (1 тик сервера)
                for (ResourceGenerator generator : generators) {
                    generator.tick(50, currentMultiplier);
                }
            }
        };

        // Запускаем каждую 1 секунду игрового времени (1 тик)
        task.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopGenerators() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void clearGenerators() {
        stopGenerators();
        generators.clear();
    }
}