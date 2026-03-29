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
        npc.spawn(location);

        // --- ГЛУШИМ ЖИТЕЛЯ НАМЕРТВО ---
        if (npc.getEntity() != null) {
            npc.getEntity().setSilent(true);
        }
        npc.setProtected(true); // Делаем бессмертным

        // Отключаем забагованную ванильную табличку
        npc.data().setPersistent("nameplate-visible", false);

        // Создаем нормальную парящую голограмму
        net.citizensnpcs.trait.HologramTrait holo = npc.getOrAddTrait(net.citizensnpcs.trait.HologramTrait.class);
        holo.clear();
        holo.addLine("§6§lТорговец");
        holo.addLine("§eНажмите ПКМ"); // Бонус: теперь можно делать 2 строчки!
        npc.data().setPersistent("bedwars_npc_type", "shop");

        spawnedNpcs.add(npc);
    }

    public void spawnUpgradeNpc(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, "§b§lУлучшения");

        // Отключаем звуки жителя навсегда
        // Отключаем звуки жителя навсегда
        npc.data().set("silent", true);

        npc.spawn(location);
        npc.spawn(location);

        // --- ГЛУШИМ ЖИТЕЛЯ НАМЕРТВО ---
        if (npc.getEntity() != null) {
            npc.getEntity().setSilent(true);
        }
        npc.setProtected(true);
        npc.data().setPersistent("nameplate-visible", false);

        net.citizensnpcs.trait.HologramTrait holo = npc.getOrAddTrait(net.citizensnpcs.trait.HologramTrait.class);
        holo.clear();
        holo.addLine("§b§lУлучшения");
        holo.addLine("§eНажмите ПКМ");

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