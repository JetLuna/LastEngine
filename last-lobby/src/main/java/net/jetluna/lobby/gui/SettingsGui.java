package net.jetluna.lobby.gui;

import net.jetluna.api.parkour.ParkourManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
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
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SettingsGui implements Listener {

    private static final Set<UUID> chatHidden = new HashSet<>();
    private static final Set<UUID> flyDisabled = new HashSet<>();
    private static final Set<UUID> doubleJumpDisabled = new HashSet<>();

    public static void open(Player player) {
        String title = "Настройки";
        Inventory gui = Bukkit.createInventory(player, 36, title);
        Rank rank = RankManager.getRank(player);

        // 1. Видимость
        boolean playersVisible = !player.hasMetadata("visibility_hidden");
        gui.setItem(10, new ItemBuilder(playersVisible ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(playersVisible ? "&aИгроки: Видимы" : "&cИгроки: Скрыты")
                .setLore("&7Нажмите, чтобы переключить")
                .build());

        // 2. Чат
        boolean chatVisible = !chatHidden.contains(player.getUniqueId());
        gui.setItem(12, new ItemBuilder(Material.PAPER)
                .setName(chatVisible ? "&aЧат: Виден" : "&cЧат: Скрыт")
                .setLore("&7Нажмите, чтобы переключить")
                .setGlow(chatVisible)
                .build());

        boolean inParkour = net.jetluna.api.parkour.ParkourManager.isInParkour(player);

        // 3. Двойной Прыжок
        if (inParkour) {
            gui.setItem(14, new ItemBuilder(Material.BARRIER)
                    .setName("&cДвойной прыжок")
                    .setLore("&7Способности отключены", "&cво время паркура!")
                    .build());
        } else if (rank.getWeight() >= 2) {
            boolean djEnabled = !doubleJumpDisabled.contains(player.getUniqueId());
            gui.setItem(14, new ItemBuilder(Material.SLIME_BALL)
                    .setName(djEnabled ? "&aДвойной прыжок: ВКЛ" : "&cДвойной прыжок: ВЫКЛ")
                    .setLore("&7Нажмите, чтобы переключить", "&eТребуется: GO")
                    .setGlow(djEnabled)
                    .build());
        } else {
            gui.setItem(14, new ItemBuilder(Material.BARRIER)
                    .setName("&cДвойной прыжок")
                    .setLore("&7Недоступно", "&eКупите привилегию GO")
                    .build());
        }

        // 4. Флай
        if (inParkour) {
            gui.setItem(16, new ItemBuilder(Material.BARRIER)
                    .setName("&cФлай")
                    .setLore("&7Способности отключены", "&cво время паркура!")
                    .build());
        } else if (rank.getWeight() >= 3) {
            boolean flyEnabled = !flyDisabled.contains(player.getUniqueId());
            gui.setItem(16, new ItemBuilder(Material.FEATHER)
                    .setName(flyEnabled ? "&aФлай: ВКЛ" : "&cФлай: ВЫКЛ")
                    .setLore("&7Нажмите, чтобы переключить", "&bТребуется: PLUS")
                    .setGlow(flyEnabled)
                    .build());
        } else {
            gui.setItem(16, new ItemBuilder(Material.BARRIER)
                    .setName("&cФлай")
                    .setLore("&7Недоступно", "&bКупите привилегию PLUS")
                    .build());
        }

        gui.setItem(31, new ItemBuilder(Material.ARROW).setName("&cНазад").build());
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().equals("Настройки")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 10) {
            player.performCommand("lobby:hideplayers");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        } else if (slot == 12) {
            if (chatHidden.contains(player.getUniqueId())) chatHidden.remove(player.getUniqueId());
            else chatHidden.add(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        } else if (slot == 14) {
            if (net.jetluna.api.parkour.ParkourManager.isInParkour(player)) {
                ChatUtil.sendMessage(player, "&cВо время паркура способности недоступны!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            toggleDoubleJump(player);
            open(player);
        } else if (slot == 16) {
            if (net.jetluna.api.parkour.ParkourManager.isInParkour(player)) {
                ChatUtil.sendMessage(player, "&cВо время паркура способности недоступны!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            toggleFly(player);
            open(player);
        } else if (slot == 31) {
            LobbyGui.openProfile(player);
        }
    }

    // --- МЕТОДЫ ДЛЯ КОМАНД ---

    public static void toggleFly(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < 3) {
            ChatUtil.sendMessage(player, "&cВам нужна привилегия PLUS!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (flyDisabled.contains(player.getUniqueId())) {
            flyDisabled.remove(player.getUniqueId());
            ChatUtil.sendMessage(player, "&aФлай включен!");

            if (!doubleJumpDisabled.contains(player.getUniqueId())) {
                doubleJumpDisabled.add(player.getUniqueId());
                ChatUtil.sendMessage(player, "&eДвойной прыжок был автоматически выключен.");
            }
        } else {
            flyDisabled.add(player.getUniqueId());
            ChatUtil.sendMessage(player, "&cФлай выключен!");
        }
        updateFlight(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public static void toggleDoubleJump(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < 2) {
            ChatUtil.sendMessage(player, "&cВам нужна привилегия GO!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (doubleJumpDisabled.contains(player.getUniqueId())) {
            doubleJumpDisabled.remove(player.getUniqueId());
            ChatUtil.sendMessage(player, "&aДвойной прыжок включен!");

            if (!flyDisabled.contains(player.getUniqueId())) {
                flyDisabled.add(player.getUniqueId());
                ChatUtil.sendMessage(player, "&eФлай был автоматически выключен.");
            }
        } else {
            doubleJumpDisabled.add(player.getUniqueId());
            ChatUtil.sendMessage(player, "&cДвойной прыжок выключен!");
        }
        updateFlight(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    // --- ГЕТТЕРЫ И ЛОГИКА ---
    public static boolean isChatHidden(Player p) { return chatHidden.contains(p.getUniqueId()); }
    public static boolean isFlyEnabled(Player p) { return !flyDisabled.contains(p.getUniqueId()); }
    public static boolean isDoubleJumpEnabled(Player p) { return !doubleJumpDisabled.contains(p.getUniqueId()); }

    public static void updateFlight(Player p) {
        Rank rank = RankManager.getRank(p);
        boolean canFly = rank.getWeight() >= 3 && isFlyEnabled(p);
        boolean canDJ = rank.getWeight() >= 2 && isDoubleJumpEnabled(p);

        if (canFly) {
            p.setAllowFlight(true);
            p.setFlying(true);
        } else if (canDJ) {
            p.setAllowFlight(true);
            p.setFlying(false);
        } else {
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }
}