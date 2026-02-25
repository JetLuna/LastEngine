package net.jetluna.lobby.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
// ИСПРАВЛЕННЫЕ ИМПОРТЫ:
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.lobby.LobbyPlugin;
import net.jetluna.lobby.gui.RewardGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcManager implements Listener {
    private final LobbyPlugin plugin;

    public NpcManager(LobbyPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void createNpc(String id, Location loc, String name, String skinName) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) return;
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(loc);
        npc.getOrAddTrait(SkinTrait.class).setSkinName(skinName);
        npc.getOrAddTrait(LookClose.class).lookClose(true);
        npc.data().set("lobby_id", id);
    }

    public void removeAll() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) return;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            npc.destroy();
        }
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if (event.getNPC().data().has("lobby_id")) {
            String id = event.getNPC().data().get("lobby_id");
            if (id.equals("rewards")) RewardGui.open(event.getClicker());
            else event.getClicker().sendMessage(ChatUtil.parse("<yellow>Скоро..."));
        }
    }
}