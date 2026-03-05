package net.jetluna.api.pet;

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

public class PetsGui implements Listener {

    private static final String TITLE = "Ваши питомцы";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, TITLE);
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Те же слоты в центре, что и в эффектах, для идеальной симметрии
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        PetType[] pets = PetType.values();

        for (int i = 0; i < pets.length; i++) {
            if (i >= slots.length) break;

            PetType type = pets[i];

            // Проверяем, активен ли питомец и куплен ли он
            boolean isActive = stats.getActivePet() != null && stats.getActivePet().equals(type.name());
            boolean isOwned = stats.getOwnedPets() != null && stats.getOwnedPets().contains(type.name());

            ItemBuilder builder = new ItemBuilder(type.getIcon());

            if (isActive) {
                builder.setName("&a★ &e" + type.getDisplayName() + " &a★");
                builder.setLore(Arrays.asList("", "&aПитомец призван!", "&7Он бегает где-то рядом с вами."));
            } else if (isOwned) {
                builder.setName("&e" + type.getDisplayName());
                builder.setLore(Arrays.asList("", "&7Статус: &aРазблокировано", "", "&eНажмите, чтобы призвать!"));
            } else {
                builder.setName("&e" + type.getDisplayName());
                builder.setLore(Arrays.asList("", "&7Цена: &6" + type.getPrice() + " монет", "", "&cУ вас не куплено!", "&eНажмите, чтобы купить."));
            }

            gui.setItem(slots[i], builder.build());
        }

        gui.setItem(48, new ItemBuilder(Material.BARRIER)
                .setName("&cОтпустить питомца")
                .setLore(Arrays.asList("&7Ваш спутник вернется в лес."))
                .build());

        gui.setItem(49, new ItemBuilder(Material.ARROW)
                .setName("&cЗакрыть")
                .setLore(Arrays.asList("&7Закрыть меню питомцев."))
                .build());

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
            return;
        }

        if (slot == 48) {
            stats.setActivePet("");
            PetManager.removePet(player);
            ChatUtil.sendMessage(player, "&cВы отпустили своего питомца гулять!");
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);

            Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
            return;
        }

        for (PetType type : PetType.values()) {
            if (item.getType() == type.getIcon()) {
                boolean isOwned = stats.getOwnedPets() != null && stats.getOwnedPets().contains(type.name());

                if (isOwned) {
                    stats.setActivePet(type.name());
                    PetManager.spawnPet(player, type);
                    ChatUtil.sendMessage(player, "&aВы призвали: " + type.getDisplayName());
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                } else {
                    if (stats.getCoins() >= type.getPrice()) {
                        stats.setCoins(stats.getCoins() - type.getPrice());
                        stats.getOwnedPets().add(type.name());
                        stats.setActivePet(type.name());
                        PetManager.spawnPet(player, type);
                        ChatUtil.sendMessage(player, "&aВы купили питомца " + type.getDisplayName() + "!");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    } else {
                        ChatUtil.sendMessage(player, "&cНедостаточно монет для покупки!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                }

                // Переоткрываем меню с задержкой в 1 тик
                Bukkit.getScheduler().runTask(LastApi.getInstance(), () -> open(player));
                break;
            }
        }
    }
}