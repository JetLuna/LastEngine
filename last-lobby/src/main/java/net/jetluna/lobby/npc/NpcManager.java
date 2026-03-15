package net.jetluna.lobby.npc;

import eu.decentsoftware.holograms.api.DHAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.jetluna.lobby.LobbyPlugin;
import net.jetluna.lobby.gui.RewardGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NpcManager implements Listener {
    private final LobbyPlugin plugin;
    private final Set<String> holoNames = new HashSet<>();

    public NpcManager(LobbyPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void createNpc(String id, Location loc, String name, String skinName) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) return;

        // 1. Создаем NPC объект (но он еще не появился в мире)
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");

        npc.data().set("nameplate-visible", false);           // Отключаем через строку (для новых версий)
        // ----------------------------------

        npc.spawn(loc); // Теперь спавним уже настроенного "невидимку"

        // --- И СЮДА (ПОСЛЕ СПАВНА) ---
        // Насильно выключаем ванильный ник у сущности, если она создалась
        if (npc.getEntity() != null) {
            npc.getEntity().setCustomNameVisible(false);
        }
        // -----------------------------

        npc.getOrAddTrait(SkinTrait.class).setSkinName(skinName);
        npc.getOrAddTrait(LookClose.class).lookClose(true);
        npc.data().set("lobby_id", id);

        // 2. Создаем голограмму через DecentHolograms (С ПОДДЕРЖКОЙ ПЕРЕВОДА PAPI)
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            String holoName = "npc_holo_" + id;

            // ПОДНИМАЕМ ГОЛОГРАММУ ВЫШЕ: было 2.3, ставим 2.65
            Location holoLoc = loc.clone().add(0, 2.65, 0);

            // Удаляем старую, если она была
            if (DHAPI.getHologram(holoName) != null) {
                DHAPI.removeHologram(holoName);
            }

            // ПЕРЕДАЕМ ПЛЕЙСХОЛДЕРЫ: DH будет сам переводить их для каждого игрока
            List<String> lines = Arrays.asList(
                    "%lastengine_npc_" + id + "_name%",
                    "%lastengine_npc_" + id + "_holo%"
            );

            DHAPI.createHologram(holoName, holoLoc, lines);
            holoNames.add(holoName);
        }
    }

    public void removeAll() {
        // 1. Удаляем всех NPC через Citizens
        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            CitizensAPI.getNPCRegistry().deregisterAll();
        }

        // 2. Удаляем голограммы через DHAPI
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            for (String name : holoNames) {
                if (DHAPI.getHologram(name) != null) {
                    DHAPI.removeHologram(name);
                }
            }
            holoNames.clear();
        }
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if (event.getNPC().data().has("lobby_id")) {
            String id = event.getNPC().data().get("lobby_id");
            Player player = event.getClicker();

            if (id.equals("rewards")) {
                RewardGui.open(player);
            }
            else if (id.equals("bestplayer")) {
                net.jetluna.lobby.gui.BestPlayerGui.open(player);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeSkin(net.jetluna.api.event.PlayerSkinChangeEvent event) {
        // Лобби слышит сигнал: "Кто-то сменил скин!"
        Player player = event.getPlayer();

        // Проверяем, а не Царь ли это переоделся?
        if (net.jetluna.api.bestplayer.BestPlayerManager.getCurrentBest() != null &&
                net.jetluna.api.bestplayer.BestPlayerManager.getCurrentBest().equals(player.getUniqueId())) {

            // Если да — переспавниваем NPC
            updateBestPlayerSkin();
        }
    }
    // Метод для обновления скина NPC лучшего игрока
    public static void updateBestPlayerSkin() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) return;

        for (net.citizensnpcs.api.npc.NPC npc : net.citizensnpcs.api.CitizensAPI.getNPCRegistry()) {
            if (npc.data().has("lobby_id") && npc.data().get("lobby_id").equals("bestplayer")) {
                net.citizensnpcs.trait.SkinTrait skinTrait = npc.getOrAddTrait(net.citizensnpcs.trait.SkinTrait.class);

                // ЖЕСТКО сбрасываем старую текстуру перед применением новой!
                skinTrait.clearTexture();

                if (net.jetluna.api.bestplayer.BestPlayerManager.isExpired()) {
                    skinTrait.setSkinName("MHF_Question");
                } else {
                    String val = net.jetluna.api.bestplayer.BestPlayerManager.getTextureValue();
                    String sig = net.jetluna.api.bestplayer.BestPlayerManager.getTextureSignature();
                    String name = net.jetluna.api.bestplayer.BestPlayerManager.getCurrentName();

                    if (val != null && !val.isEmpty()) {
                        skinTrait.setSkinPersistent(name, sig, val);
                    } else {
                        skinTrait.setSkinName("Steve");
                    }
                }

                // Даем ядру 2 тика (доли секунды) на обработку текстуры перед переспавном
                Bukkit.getScheduler().runTaskLater(net.jetluna.lobby.LobbyPlugin.getInstance(), () -> {
                    if (npc.isSpawned()) {
                        org.bukkit.Location loc = npc.getStoredLocation();
                        npc.despawn();
                        npc.spawn(loc);
                    }
                }, 2L);

                break;
            }
        }
    }

}