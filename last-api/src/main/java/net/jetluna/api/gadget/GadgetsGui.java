package net.jetluna.api.gadget;

import net.jetluna.api.LastApi;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class GadgetsGui implements Listener {

    private static final String TITLE = "Ваши гаджеты";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, TITLE);
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 54; i++) gui.setItem(i, filler);

        int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };
        GadgetType[] gadgets = GadgetType.values();

        for (int i = 0; i < gadgets.length; i++) {
            if (i >= slots.length) break;
            GadgetType type = gadgets[i];

            boolean isActive = stats.getActiveGadget() != null && stats.getActiveGadget().equals(type.name());
            boolean isOwned = stats.getOwnedGadgets() != null && stats.getOwnedGadgets().contains(type.name());

            ItemBuilder builder = new ItemBuilder(type.getIcon());

            if (isActive) {
                builder.setName("&a★ &c" + type.getDisplayName() + " &a★").setLore(Arrays.asList("", "&aЭкипировано!", "&7Гаджет находится в вашем инвентаре.")).setGlow(true);
            } else if (isOwned) {
                builder.setName("&c" + type.getDisplayName()).setLore(Arrays.asList("", "&7Статус: &aРазблокировано", "", "&eНажмите, чтобы взять!"));
            } else {
                builder.setName("&c" + type.getDisplayName()).setLore(Arrays.asList("", "&7Цена: &6" + type.getPrice() + " монет", "", "&cУ вас не куплено!", "&eНажмите, чтобы купить."));
            }
            gui.setItem(slots[i], builder.build());
        }

        gui.setItem(48, new ItemBuilder(Material.BARRIER).setName("&cСнять гаджет").setLore(Arrays.asList("&7Убрать гаджет из инвентаря.")).build());
        gui.setItem(49, new ItemBuilder(Material.ARROW).setName("&cЗакрыть").setLore(Arrays.asList("&7Вернуться назад.")).build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!ChatUtil.strip(event.getView().getTitle()).equals(TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        int slot = event.getSlot();

        if (slot == 49) {
            player.closeInventory();
            player.performCommand("profile");
            return;
        }

        if (slot == 48) {
            stats.setActiveGadget("");
            GadgetManager.remove(player);
            ChatUtil.sendMessage(player, "&cВы убрали гаджет в рюкзак!");
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
            return;
        }

        for (GadgetType type : GadgetType.values()) {
            if (item.getType() == type.getIcon() && ChatUtil.strip(item.getItemMeta().getDisplayName()).contains(type.getDisplayName())) {
                boolean isOwned = stats.getOwnedGadgets() != null && stats.getOwnedGadgets().contains(type.name());

                if (isOwned) {
                    stats.setActiveGadget(type.name());
                    GadgetManager.equip(player, type);
                    ChatUtil.sendMessage(player, "&aВы взяли в руки: " + type.getDisplayName());
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1.5f);
                } else {
                    if (stats.getCoins() >= type.getPrice()) {
                        stats.setCoins(stats.getCoins() - type.getPrice());
                        stats.getOwnedGadgets().add(type.name());
                        stats.setActiveGadget(type.name());
                        GadgetManager.equip(player, type);
                        ChatUtil.sendMessage(player, "&aВы успешно купили гаджет " + type.getDisplayName() + "!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        ChatUtil.sendMessage(player, "&cНедостаточно монет для покупки!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }
                player.closeInventory();
                break;
            }
        }
    }
}