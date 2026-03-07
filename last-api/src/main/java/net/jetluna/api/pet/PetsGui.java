package net.jetluna.api.pet;

import net.jetluna.api.LastApi;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PetsGui implements Listener {

    public static void open(Player player) {
        String title = toLegacy(LanguageManager.getString(player, "pets.gui.title"));
        Inventory gui = Bukkit.createInventory(player, 54, title);

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        PetType[] pets = PetType.values();

        for (int i = 0; i < pets.length; i++) {
            if (i >= slots.length) break;

            PetType type = pets[i];

            boolean isActive = stats.getActivePet() != null && stats.getActivePet().equals(type.name());
            boolean isOwned = stats.getOwnedPets() != null && stats.getOwnedPets().contains(type.name());

            ItemBuilder builder = new ItemBuilder(type.getIcon());
            String petName = toLegacy(LanguageManager.getString(player, "pets.list." + type.name().toLowerCase()));

            if (isActive) {
                builder.setName(toLegacy(LanguageManager.getString(player, "pets.status.active_name")).replace("%pet%", petName));
                builder.setLore(getLegacyList(player, "pets.status.active_lore"));
            } else if (isOwned) {
                builder.setName(toLegacy(LanguageManager.getString(player, "pets.status.owned_name")).replace("%pet%", petName));
                builder.setLore(getLegacyList(player, "pets.status.owned_lore"));
            } else {
                builder.setName(toLegacy(LanguageManager.getString(player, "pets.status.buy_name")).replace("%pet%", petName));
                List<String> lore = getLegacyList(player, "pets.status.buy_lore");
                lore.replaceAll(s -> s.replace("%price%", String.valueOf(type.getPrice())));
                builder.setLore(lore);
            }

            gui.setItem(slots[i], builder.build());
        }

        String removeName = toLegacy(LanguageManager.getString(player, "pets.gui.remove"));
        List<String> removeLore = getLegacyList(player, "pets.gui.remove_lore");
        gui.setItem(48, new ItemBuilder(Material.BARRIER).setName(removeName).setLore(removeLore).build());

        String closeName = toLegacy(LanguageManager.getString(player, "pets.gui.close"));
        List<String> closeLore = getLegacyList(player, "pets.gui.close_lore");
        gui.setItem(49, new ItemBuilder(Material.ARROW).setName(closeName).setLore(closeLore).build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = toLegacy(LanguageManager.getString(player, "pets.gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        int slot = event.getSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot == 48) {
            stats.setActivePet("");
            PetManager.removePet(player);

            String msg = toLegacy(LanguageManager.getString(player, "pets.messages.removed"));
            ChatUtil.sendMessage(player, msg);
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);

            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
            return;
        }

        for (PetType type : PetType.values()) {
            if (item.getType() == type.getIcon()) {
                boolean isOwned = stats.getOwnedPets() != null && stats.getOwnedPets().contains(type.name());
                String petName = toLegacy(LanguageManager.getString(player, "pets.list." + type.name().toLowerCase()));

                if (isOwned) {
                    stats.setActivePet(type.name());
                    PetManager.spawnPet(player, type);

                    String msg = toLegacy(LanguageManager.getString(player, "pets.messages.spawned")).replace("%pet%", petName);
                    ChatUtil.sendMessage(player, msg);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                } else {
                    if (stats.getCoins() >= type.getPrice()) {
                        stats.setCoins(stats.getCoins() - type.getPrice());
                        stats.getOwnedPets().add(type.name());
                        stats.setActivePet(type.name());
                        PetManager.spawnPet(player, type);

                        String msg = toLegacy(LanguageManager.getString(player, "pets.messages.purchased")).replace("%pet%", petName);
                        ChatUtil.sendMessage(player, msg);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    } else {
                        String msg = toLegacy(LanguageManager.getString(player, "pets.messages.not_enough_coins"));
                        ChatUtil.sendMessage(player, msg);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }

                Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
                break;
            }
        }
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