package net.jetluna.api.gadget;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            running.entrySet().removeIf(entry -> {
                ActiveData data = entry.getValue();
                data.ticks++;
                if (data.ticks >= 200 || data.gadget.onTick(data.ticks)) {
                    data.gadget.onClear();
                    return true;
                }
                return false;
            });
        }, 0L, 1L);
    }

    public static void equip(Player player, GadgetType type) {
        equipped.put(player.getUniqueId(), type);

        String gadgetName = toLegacy(LanguageManager.getString(player, "gadgets.list." + type.name().toLowerCase()));
        String equipName = toLegacy(LanguageManager.getString(player, "gadgets.items.equip_name")).replace("%gadget%", gadgetName);
        List<String> equipLore = getLegacyList(player, "gadgets.items.equip_lore");

        player.getInventory().setItem(3, new ItemBuilder(type.getIcon())
                .setName(equipName)
                .setLore(equipLore)
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
                String msg = toLegacy(LanguageManager.getString(player, "gadgets.messages.cooldown")).replace("%time%", String.valueOf(timeLeft));
                ChatUtil.sendMessage(player, msg);
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

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    private static List<String> getLegacyList(Player player, String key) {
        List<String> list = LanguageManager.getList(player, key);
        List<String> legacyList = new ArrayList<>();
        if (list != null) {
            for (String s : list) {
                legacyList.add(toLegacy(s));
            }
        }
        return legacyList;
    }

    private static String toLegacy(String text) {
        if (text == null) return "";
        String legacy = text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }
}