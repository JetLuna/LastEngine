package net.jetluna.bedwars.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class NpcManager {

    private final BedWarsPlugin plugin;
    private final List<NPC> spawnedNpcs = new ArrayList<>();

    public NpcManager(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnShopNpc(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, "§6§lТорговец");

        // Отключаем звуки жителя навсегда
        // Отключаем звуки жителя навсегда
        npc.data().set("silent", true);

        npc.spawn(location);
        npc.setProtected(true); // Делаем бессмертным

        npc.data().setPersistent("nameplate-visible", true);
        npc.data().setPersistent("bedwars_npc_type", "shop");

        spawnedNpcs.add(npc);
    }

    public void spawnUpgradeNpc(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, "§b§lУлучшения");

        // Отключаем звуки жителя навсегда
        // Отключаем звуки жителя навсегда
        npc.data().set("silent", true);

        npc.spawn(location);
        npc.setProtected(true);
        npc.data().setPersistent("nameplate-visible", true);

        // Метка для улучшений
        npc.data().setPersistent("bedwars_npc_type", "upgrade");

        spawnedNpcs.add(npc);
    }

    // Удаляем всех NPC после завершения игры
    public void clearNpcs() {
        for (NPC npc : spawnedNpcs) {
            if (npc != null) {
                npc.destroy();
            }
        }
        spawnedNpcs.clear();
    }
}