package net.jetluna.lobby.gui;

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

import java.util.*;

public class SuffixGui implements Listener {

    private static final Map<UUID, Set<String>> unlockedSuffixes = new HashMap<>();

    public enum Suffix {
        DEFAULT("Титулы", "Отключить", "", 0, Material.BARRIER),
        PRO("Титулы", "Про", " &8[&bPRO&8]", 150, Material.DIAMOND_SWORD),
        VETERAN("Титулы", "Ветеран", " &8[&6Ветеран&8]", 500, Material.GOLDEN_APPLE),

        TOXIC("Мемы", "Токсик", " &8[&dТоксик&8]", 200, Material.POISONOUS_POTATO),
        CLOWN("Мемы", "Клоун", " &8[&7Клоун&8]", 150, Material.SLIME_BALL),

        HOKAGE("Аниме", "Хокаге", " &8[&eХокаге&8]", 300, Material.BLAZE_POWDER),
        GHOUL("Аниме", "Гуль", " &8[&4Ghoul&8]", 400, Material.WITHER_SKELETON_SKULL);

        public final String category;
        public final String name;
        public final String format;
        public final int price;
        public final Material icon;

        Suffix(String category, String name, String format, int price, Material icon) {
            this.category = category;
            this.name = name;
            this.format = format;
            this.price = price;
            this.icon = icon;
        }
    }

    public static void openCategories(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "Категории Суффиксов");
        gui.setItem(11, new ItemBuilder(Material.NAME_TAG).setName("&eТитулы").build());
        gui.setItem(13, new ItemBuilder(Material.SLIME_BALL).setName("&aМемы").build());
        gui.setItem(15, new ItemBuilder(Material.WITHER_SKELETON_SKULL).setName("&4Аниме").build());
        gui.setItem(22, new ItemBuilder(Material.ARROW).setName("&cНазад").build());
        player.openInventory(gui);
    }

    public static void openList(Player player, String category) {
        Inventory gui = Bukkit.createInventory(player, 36, "Суффиксы: " + category);
        int slot = 0;

        // ПОЛУЧАЕМ ТЕКУЩИЙ СУФФИКС ИЗ ГЛОБАЛЬНОЙ СТАТИСТИКИ
        PlayerStats stats = StatsManager.getStats(player);
        String currentFormat = (stats != null && stats.getSuffix() != null) ? stats.getSuffix() : "";

        String currentSelected = "DEFAULT";
        for (Suffix s : Suffix.values()) {
            if (s.format.equals(currentFormat)) currentSelected = s.name();
        }

        Set<String> owned = unlockedSuffixes.getOrDefault(player.getUniqueId(), new HashSet<>(Collections.singletonList("DEFAULT")));

        for (Suffix s : Suffix.values()) {
            if (s.category.equals(category)) {
                ItemBuilder builder = new ItemBuilder(s.icon).setName("&b" + s.name);
                List<String> lore = new ArrayList<>();
                lore.add("&7Отображение: " + player.getName() + toLegacy(s.format));
                lore.add("");

                if (currentSelected.equals(s.name())) {
                    lore.add("&aВЫБРАНО");
                    builder.setGlow(true);
                } else if (owned.contains(s.name())) {
                    lore.add("&eНажмите, чтобы выбрать");
                } else {
                    lore.add("&cЦена: &a" + s.price + " ❇");
                    lore.add("&7Нажмите, чтобы купить");
                }

                builder.setLore(lore);
                gui.setItem(slot++, builder.build());
            }
        }

        gui.setItem(31, new ItemBuilder(Material.ARROW).setName("&cНазад").build());
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("Категории Суффиксов")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            int slot = event.getSlot();
            if (slot == 11) openList(player, "Титулы");
            else if (slot == 13) openList(player, "Мемы");
            else if (slot == 15) openList(player, "Аниме");
            else if (slot == 22) CustomizationGui.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
        else if (title.startsWith("Суффиксы: ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getSlot() == 31) {
                openCategories(player);
                return;
            }

            String category = title.replace("Суффиксы: ", "");
            String itemName = ChatUtil.strip(event.getCurrentItem().getItemMeta().getDisplayName());

            Suffix clickedSuffix = null;
            for (Suffix s : Suffix.values()) {
                if (s.name.equals(itemName) && s.category.equals(category)) clickedSuffix = s;
            }
            if (clickedSuffix == null) return;

            Set<String> owned = unlockedSuffixes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>(Collections.singletonList("DEFAULT")));
            PlayerStats stats = StatsManager.getStats(player);
            if (stats == null) return;

            if (owned.contains(clickedSuffix.name())) {
                // СОХРАНЯЕМ СУФФИКС В ГЛОБАЛЬНУЮ СТАТИСТИКУ!
                stats.setSuffix(clickedSuffix.format);
                ChatUtil.sendMessage(player, "&aВы выбрали суффикс: &b" + clickedSuffix.name);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                openList(player, category);
            } else {
                if (stats.getEmeralds() >= clickedSuffix.price) {
                    stats.setEmeralds(stats.getEmeralds() - clickedSuffix.price);
                    owned.add(clickedSuffix.name());
                    ChatUtil.sendMessage(player, "&aУспешная покупка! Вы приобрели: &b" + clickedSuffix.name);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    openList(player, category);
                } else {
                    ChatUtil.sendMessage(player, "&cНедостаточно Изумрудов! Нужно: &a" + clickedSuffix.price + " ❇");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
        }
    }

    public static String getActiveSuffix(Player player) {
        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null || stats.getSuffix() == null) return "";
        return toLegacy(stats.getSuffix());
    }

    private static String toLegacy(String text) {
        if (text == null) return "";
        return text.replace("<dark_red>", "&4").replace("</dark_red>", "").replace("<red>", "&c").replace("</red>", "").replace("<gold>", "&6").replace("</gold>", "").replace("<yellow>", "&e").replace("</yellow>", "").replace("<dark_green>", "&2").replace("</dark_green>", "").replace("<green>", "&a").replace("</green>", "").replace("<aqua>", "&b").replace("</aqua>", "").replace("<dark_aqua>", "&3").replace("</dark_aqua>", "").replace("<dark_blue>", "&1").replace("</dark_blue>", "").replace("<blue>", "&9").replace("</blue>", "").replace("<light_purple>", "&d").replace("</light_purple>", "").replace("<dark_purple>", "&5").replace("</dark_purple>", "").replace("<white>", "&f").replace("</white>", "").replace("<gray>", "&7").replace("</gray>", "").replace("<dark_gray>", "&8").replace("</dark_gray>", "").replace("<black>", "&0").replace("</black>", "").replace("<bold>", "&l").replace("</bold>", "").replace("<italic>", "&o").replace("</italic>", "").replace("<strikethrough>", "&m").replace("</strikethrough>", "").replace("<underlined>", "&n").replace("</underlined>", "").replace("<obfuscated>", "&k").replace("</obfuscated>", "").replace("<reset>", "&r").replace("</reset>", "").replaceAll("<[^>]+>", "");
    }
}