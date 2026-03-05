package net.jetluna.api.gadget;

import net.jetluna.api.LastApi;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GadgetManager implements Listener {

    private static final Map<UUID, GadgetType> equipped = new HashMap<>();
    private static final Map<UUID, ActiveData> running = new HashMap<>();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    private static class ActiveData {
        LobbyGadget gadget;
        int ticks = 0;
        ActiveData(LobbyGadget gadget) { this.gadget = gadget; }
    }

    public static void init(LastApi plugin) {
        Bukkit.getPluginManager().registerEvents(new GadgetManager(), plugin);

        // Тот самый двигатель
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            running.entrySet().removeIf(entry -> {
                ActiveData data = entry.getValue();
                data.ticks++;
                if (data.ticks >= 200 || data.gadget.onTick(data.ticks)) { // 10 секунд максимум
                    data.gadget.onClear();
                    return true;
                }
                return false;
            });
        }, 0L, 1L);
    }

    public static void equip(Player player, GadgetType type) {
        equipped.put(player.getUniqueId(), type);
        player.getInventory().setItem(3, new ItemBuilder(type.getIcon())
                .setName("&c" + type.getDisplayName())
                .setLore("", "&7Нажмите ПКМ, чтобы использовать!")
                .setGlow(true)
                .build());
        player.updateInventory();
    }

    public static void remove(Player player) {
        equipped.remove(player.getUniqueId());
        player.getInventory().setItem(3, null);
        stopActive(player);
    }

    private static void stopActive(Player player) {
        ActiveData data = running.remove(player.getUniqueId());
        if (data != null) data.gadget.onClear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        GadgetType type = equipped.get(player.getUniqueId());
        if (type == null) return;

        if (player.getInventory().getHeldItemSlot() != 3) return;
        if (event.getItem() == null || event.getItem().getType() != type.getIcon()) return;

        event.setCancelled(true);

        long time = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) - time) / 1000;
            if (timeLeft > 0) {
                ChatUtil.sendMessage(player, "&cПодождите " + timeLeft + " сек. перед броском!");
                return;
            }
        }
        cooldowns.put(player.getUniqueId(), time + 10000);

        stopActive(player);

        LobbyGadget gadget = GadgetFactory.create(player, type);
        if (gadget != null) {
            gadget.onUse();
            running.put(player.getUniqueId(), new ActiveData(gadget));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }
}