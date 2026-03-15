package net.jetluna.lobby.gui;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.api.util.PlayerSettingsManager;
import net.jetluna.lobby.LobbyGui;
import net.jetluna.lobby.LobbyPlugin;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SettingsGui implements Listener {

    // Сеты с локальными настройками лобби
    public static final Set<UUID> visibilityHidden = new HashSet<>();
    private static final Set<UUID> chatHidden = new HashSet<>();
    private static final Set<UUID> flyDisabled = new HashSet<>();
    private static final Set<UUID> doubleJumpDisabled = new HashSet<>();

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.settings_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 36, title);
        Rank rank = RankManager.getRank(player);

        // --- ВИДИМОСТЬ ИГРОКОВ (Слот 10) ---
        boolean playersVisible = !visibilityHidden.contains(player.getUniqueId());
        String visNamePath = playersVisible ? "lobby.settings_gui.visibility.on" : "lobby.settings_gui.visibility.off";
        gui.setItem(10, new ItemBuilder(playersVisible ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(color(LanguageManager.getString(player, visNamePath)))
                .setLore(colorList(player, "lobby.settings_gui.visibility.lore"))
                .build());

        // --- ЧАТ ЛОББИ (Слот 12) ---
        boolean chatVisible = !chatHidden.contains(player.getUniqueId());
        String chatNamePath = chatVisible ? "lobby.settings_gui.chat.on" : "lobby.settings_gui.chat.off";
        gui.setItem(12, new ItemBuilder(Material.PAPER)
                .setName(color(LanguageManager.getString(player, chatNamePath)))
                .setLore(colorList(player, "lobby.settings_gui.chat.lore"))
                .setGlow(chatVisible)
                .build());

        boolean inParkour = net.jetluna.api.parkour.ParkourManager.isInParkour(player);

        // --- ДВОЙНОЙ ПРЫЖОК (Слот 14) ---
        if (inParkour) {
            gui.setItem(14, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.settings_gui.doublejump.disabled_parkour")))
                    .setLore(colorList(player, "lobby.settings_gui.doublejump.disabled_parkour_lore"))
                    .build());
        } else if (rank.getWeight() >= 2) {
            boolean djEnabled = !doubleJumpDisabled.contains(player.getUniqueId());
            String djNamePath = djEnabled ? "lobby.settings_gui.doublejump.on" : "lobby.settings_gui.doublejump.off";
            gui.setItem(14, new ItemBuilder(Material.SLIME_BALL)
                    .setName(color(LanguageManager.getString(player, djNamePath)))
                    .setLore(colorList(player, "lobby.settings_gui.doublejump.lore"))
                    .setGlow(djEnabled)
                    .build());
        } else {
            gui.setItem(14, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.settings_gui.doublejump.unavailable")))
                    .setLore(colorList(player, "lobby.settings_gui.doublejump.unavailable_lore"))
                    .build());
        }

        // --- ПОЛЕТ (Слот 16) ---
        if (inParkour) {
            gui.setItem(16, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.settings_gui.fly.disabled_parkour")))
                    .setLore(colorList(player, "lobby.settings_gui.fly.disabled_parkour_lore"))
                    .build());
        } else if (rank.getWeight() >= 3) {
            boolean flyEnabled = !flyDisabled.contains(player.getUniqueId());
            String flyNamePath = flyEnabled ? "lobby.settings_gui.fly.on" : "lobby.settings_gui.fly.off";
            gui.setItem(16, new ItemBuilder(Material.FEATHER)
                    .setName(color(LanguageManager.getString(player, flyNamePath)))
                    .setLore(colorList(player, "lobby.settings_gui.fly.lore"))
                    .setGlow(flyEnabled)
                    .build());
        } else {
            gui.setItem(16, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "lobby.settings_gui.fly.unavailable")))
                    .setLore(colorList(player, "lobby.settings_gui.fly.unavailable_lore"))
                    .build());
        }

        // --- ЛИЧНЫЕ СООБЩЕНИЯ (Слот 21) ---
        int pmState = PlayerSettingsManager.getPMSetting(player.getUniqueId());
        String pmKey = (pmState == 0) ? "lore_all" : (pmState == 1) ? "lore_friends" : "lore_none";

        gui.setItem(21, new ItemBuilder(Material.WRITABLE_BOOK)
                .setName(color(LanguageManager.getString(player, "lobby.settings_gui.pm_setting.name")))
                .setLore(colorList(player, "lobby.settings_gui.pm_setting." + pmKey))
                .build());

        // --- ДОНАТ ЧАТ (Слот 23) ---
        boolean donateChat = PlayerSettingsManager.isDonateChatEnabled(player.getUniqueId());
        String dcKey = donateChat ? "lore_enabled" : "lore_disabled";

        gui.setItem(23, new ItemBuilder(Material.DIAMOND)
                .setName(color(LanguageManager.getString(player, "lobby.settings_gui.donate_chat.name")))
                .setLore(colorList(player, "lobby.settings_gui.donate_chat." + dcKey))
                .build());

        // --- НАЗАД (Слот 31) ---
        gui.setItem(31, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "lobby.settings_gui.back"))).build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "lobby.settings_gui.title"));
        if (!ChatColor.stripColor(event.getView().getTitle()).equals(ChatColor.stripColor(expectedTitle))) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        if (slot == 10) {
            toggleVisibility(player);
            open(player);
        } else if (slot == 12) {
            if (chatHidden.contains(player.getUniqueId())) chatHidden.remove(player.getUniqueId());
            else chatHidden.add(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        } else if (slot == 14) {
            if (net.jetluna.api.parkour.ParkourManager.isInParkour(player)) {
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.parkour_disabled")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            toggleDoubleJump(player);
            open(player);
        } else if (slot == 16) {
            if (net.jetluna.api.parkour.ParkourManager.isInParkour(player)) {
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.parkour_disabled")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            toggleFly(player);
            open(player);
        } else if (slot == 21) {
            PlayerSettingsManager.cyclePMSetting(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        } else if (slot == 23) {
            PlayerSettingsManager.toggleDonateChat(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            open(player);
        } else if (slot == 31) {
            LobbyGui.openProfile(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }

    // --- НОВЫЙ НАДЕЖНЫЙ МЕТОД СКРЫТИЯ ИГРОКОВ ---
    public static void toggleVisibility(Player player) {
        LobbyPlugin plugin = LobbyPlugin.getPlugin(LobbyPlugin.class);
        boolean isHidden = visibilityHidden.contains(player.getUniqueId());

        if (isHidden) {
            // Включаем видимость
            visibilityHidden.remove(player.getUniqueId());
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_show");
            updateVisibilityItem(player, true);
        } else {
            // Выключаем видимость
            visibilityHidden.add(player.getUniqueId());
            for (Player target : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(plugin, target);
            }
            LanguageManager.sendMessage(player, "lobby.messages.visibility_hide");
            updateVisibilityItem(player, false);
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    // Метод для авто-замены красителя в хотбаре
    private static void updateVisibilityItem(Player player, boolean show) {
        Material targetMat = show ? Material.GRAY_DYE : Material.LIME_DYE;
        Material newMat = show ? Material.LIME_DYE : Material.GRAY_DYE;
        String nameKey = show ? "lobby.items.visibility.enabled.name" : "lobby.items.visibility.disabled.name";
        String loreKey = show ? "lobby.items.visibility.enabled.lore" : "lobby.items.visibility.disabled.lore";

        ItemStack newItem = new ItemBuilder(newMat)
                .setName(color(LanguageManager.getString(player, nameKey)))
                .setLore(colorList(player, loreKey))
                .build();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == targetMat) {
                player.getInventory().setItem(i, newItem);
                break;
            }
        }
    }

    // ... (Методы toggleFly, toggleDoubleJump и updateFlight оставляем без изменений, они работают отлично) ...
    public static void toggleFly(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < 3) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.fly_req")));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (flyDisabled.contains(player.getUniqueId())) {
            flyDisabled.remove(player.getUniqueId());
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.fly_on")));

            if (!doubleJumpDisabled.contains(player.getUniqueId())) {
                doubleJumpDisabled.add(player.getUniqueId());
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.dj_auto_off")));
            }
        } else {
            flyDisabled.add(player.getUniqueId());
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.fly_off")));
        }
        updateFlight(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public static void toggleDoubleJump(Player player) {
        Rank rank = RankManager.getRank(player);
        if (rank.getWeight() < 2) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.dj_req")));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (doubleJumpDisabled.contains(player.getUniqueId())) {
            doubleJumpDisabled.remove(player.getUniqueId());
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.dj_on")));

            if (!flyDisabled.contains(player.getUniqueId())) {
                flyDisabled.add(player.getUniqueId());
                ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.fly_auto_off")));
            }
        } else {
            doubleJumpDisabled.add(player.getUniqueId());
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.settings_gui.messages.dj_off")));
        }
        updateFlight(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

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

    private static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        return list;
    }
}