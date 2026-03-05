package net.jetluna.lobby.gui;

import net.jetluna.api.cosmetic.CosmeticGui;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.lobby.LobbyGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class CustomizationGui implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(player, 45, "Кастомизация");

        // --- ВЕРХНИЙ РЯД (3 элемента) ---
        gui.setItem(11, new ItemBuilder(Material.NAME_TAG)
                .setName("&aСообщения при входе")
                .setLore("&7Удивите всех своим", "&7появлением на сервере!", "", "&eНажмите, чтобы выбрать")
                .build());

        gui.setItem(13, new ItemBuilder(Material.OAK_SIGN)
                .setName("&bСуффиксы")
                .setLore("&7Крутые титулы рядом", "&7с вашим никнеймом.", "", "&eНажмите, чтобы выбрать")
                .build());

        gui.setItem(15, new ItemBuilder(Material.BLAZE_POWDER)
                .setName("&dВизуальные эффекты")
                .setLore("&7Красивые партиклы,", "&7которые будут летать вокруг вас.", "", "&eНажмите, чтобы выбрать")
                .build());

        // --- НИЖНИЙ РЯД (3 элемента) ---
        gui.setItem(20, new ItemBuilder(Material.LEAD)
                .setName("&6Питомцы")
                .setLore("&7Верные спутники, которые", "&7будут бегать за вами по пятам.", "", "&eНажмите, чтобы выбрать")
                .build());

        gui.setItem(22, new ItemBuilder(Material.SLIME_BALL)
                .setName("&cГаджеты")
                .setLore("&7Игрушки для безумного", "&7веселья в лобби.", "", "&eНажмите, чтобы выбрать")
                .build());

        // !!! НОВАЯ КНОПКА КОСМЕТИКИ (БАННЕРЫ) !!!
        gui.setItem(24, new ItemBuilder(Material.CYAN_BANNER)
                .setName("&3Баннеры")
                .setLore("&7Эпичные флаги и баннеры,", "&7которые носятся за спиной.", "", "&eНажмите, чтобы выбрать")
                .build());

        // Кнопка назад в самом низу по центру
        gui.setItem(40, new ItemBuilder(Material.ARROW).setName("&cНазад").build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("lobby.items.profile.name")) return;
        if (!event.getView().getTitle().equals("Кастомизация")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 11) {
            JoinerGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 13) {
            SuffixGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 15) {
            net.jetluna.api.effect.EffectsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 20) {
            net.jetluna.api.pet.PetsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 22) {
            net.jetluna.api.gadget.GadgetsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 24) { // ОТКРЫВАЕМ МЕНЮ БАННЕРОВ
            net.jetluna.api.cosmetic.CosmeticGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 40) {
            LobbyGui.openProfile(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }
}