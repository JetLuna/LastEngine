package net.jetluna.bedwars.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
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

    public void clearGlobalNpcs() {
        CitizensAPI.getNPCRegistry().deregisterAll();
        spawnedNpcs.clear();
        plugin.getLogger().info("База Citizens полностью вычищена!");
    }

    public void spawnShopNpc(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, " ");

        npc.data().setPersistent("nameplate-visible", false);
        npc.data().setPersistent("silent", true);

        // Устанавливаем скин по нику аккаунта
        npc.getOrAddTrait(SkinTrait.class).setSkinName("Villager");

        npc.spawn(location);

        if (npc.getEntity() != null) {
            npc.getEntity().setCustomNameVisible(false);
        }

        npc.setProtected(true);

        net.citizensnpcs.trait.HologramTrait holo = npc.getOrAddTrait(net.citizensnpcs.trait.HologramTrait.class);
        holo.clear();
        holo.addLine("§6Торговец");
        holo.addLine("§eНажмите ПКМ");

        npc.data().setPersistent("bedwars_npc_type", "shop");
        spawnedNpcs.add(npc);
    }

    public void spawnUpgradeNpc(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, " ");

        npc.data().setPersistent("nameplate-visible", false);
        npc.data().setPersistent("silent", true);

        // Устанавливаем скин по нику аккаунта
        npc.getOrAddTrait(SkinTrait.class).setSkinName("Villager");

        npc.spawn(location);

        if (npc.getEntity() != null) {
            npc.getEntity().setCustomNameVisible(false);
        }

        npc.setProtected(true);

        net.citizensnpcs.trait.HologramTrait holo = npc.getOrAddTrait(net.citizensnpcs.trait.HologramTrait.class);
        holo.clear();
        holo.addLine("§bУлучшения");
        holo.addLine("§eНажмите ПКМ");

        npc.data().setPersistent("bedwars_npc_type", "upgrade");
        spawnedNpcs.add(npc);
    }

    public void clearNpcs() {
        CitizensAPI.getNPCRegistry().deregisterAll();
        spawnedNpcs.clear();
    }
}