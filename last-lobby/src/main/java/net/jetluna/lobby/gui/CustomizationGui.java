package net.jetluna.lobby.gui;

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
        Inventory gui = Bukkit.createInventory(player, 27, "Кастомизация");

        gui.setItem(11, new ItemBuilder(Material.NAME_TAG)
                .setName("&aСообщения при входе")
                .setLore("&7Удивите всех своим", "&7появлением на сервере!", "", "&eНажмите, чтобы выбрать")
                .build());

        // !!! КНОПКА СУФФИКСОВ !!!
        gui.setItem(13, new ItemBuilder(Material.OAK_SIGN)
                .setName("&bСуффиксы")
                .setLore("&7Крутые титулы рядом", "&7с вашим никнеймом.", "", "&eНажмите, чтобы выбрать")
                .build());

        // !!! НОВАЯ КНОПКА ЭФФЕКТОВ (Вместо Гаджетов) !!!
        gui.setItem(15, new ItemBuilder(Material.BLAZE_POWDER)
                .setName("&dВизуальные эффекты")
                .setLore("&7Красивые партиклы,", "&7которые будут летать вокруг вас.", "", "&eНажмите, чтобы выбрать")
                .build());

        gui.setItem(22, new ItemBuilder(Material.ARROW).setName("&cНазад").build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Кастомизация")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 11) {
            JoinerGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 13) { // Открываем суффиксы
            SuffixGui.openCategories(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 15) { // !!! ОТКРЫВАЕМ ЭФФЕКТЫ !!!
            net.jetluna.api.effect.EffectsGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (slot == 22) {
            LobbyGui.openProfile(player);
        }
    }
}