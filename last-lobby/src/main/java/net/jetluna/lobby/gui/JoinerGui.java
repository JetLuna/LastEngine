package net.jetluna.lobby.gui;

import net.jetluna.api.rank.Rank;
import net.jetluna.api.rank.RankManager;
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

public class JoinerGui implements Listener {

    private static final Map<UUID, Set<String>> unlockedJoiners = new HashMap<>();
    private static final Map<UUID, String> selectedJoiner = new HashMap<>();

    public enum Joiner {
        DEFAULT("Классика", "Стандарт", "%prefix%%player%%suffix% &eприсоединился к игре", 0, Material.OAK_SIGN),
        VIP("Классика", "Мажор", "&6%prefix%%player%%suffix% &6почтил нас своим присутствием!", 150, Material.GOLD_INGOT),

        MEME_FATHER("Мемы", "Здарова, отец", "%prefix%%player%%suffix% &eзалетел на сервер. Здарова, отец!", 300, Material.EMERALD),
        MEME_CLOWN("Мемы", "Цирк приехал", "&cКлоун %prefix%%player%%suffix% &eвышел на арену!", 250, Material.SLIME_BALL),

        DARK_GHOUL("Дарк", "1000-7", "&8[&cGhoul&8] %prefix%%player%%suffix% &4вышел из тени. 1000-7...", 500, Material.WITHER_SKELETON_SKULL),
        DARK_BERSERK("Дарк", "Черный мечник", "&8[&4Клеймо&8] %prefix%%player%%suffix% &8обнажил Убийцу Драконов.", 500, Material.NETHERITE_SWORD);

        public final String category;
        public final String name;
        public final String format;
        public final int price;
        public final Material icon;

        Joiner(String category, String name, String format, int price, Material icon) {
            this.category = category;
            this.name = name;
            this.format = format;
            this.price = price;
            this.icon = icon;
        }
    }

    public static void openCategories(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "Категории Джоинеров");

        gui.setItem(11, new ItemBuilder(Material.BOOK).setName("&eКлассика").build());
        gui.setItem(13, new ItemBuilder(Material.SLIME_BALL).setName("&aМемы").build());
        gui.setItem(15, new ItemBuilder(Material.WITHER_SKELETON_SKULL).setName("&4Дарк").build());

        gui.setItem(22, new ItemBuilder(Material.ARROW).setName("&cНазад").build());

        player.openInventory(gui);
    }

    public static void openList(Player player, String category) {
        Inventory gui = Bukkit.createInventory(player, 36, "Джоинеры: " + category);
        int slot = 0;

        String currentSelected = selectedJoiner.getOrDefault(player.getUniqueId(), "DEFAULT");
        Set<String> owned = unlockedJoiners.getOrDefault(player.getUniqueId(), new HashSet<>(Collections.singletonList("DEFAULT")));

        Rank rank = RankManager.getRank(player);
        String rawPrefix = rank.getWeight() == 1 ? "&7" : rank.getPrefix();
        String legacyPrefix = toLegacy(rawPrefix);

        // Получаем суффикс
        String suffix = SuffixGui.getActiveSuffix(player).replace("&", "§");

        for (Joiner j : Joiner.values()) {
            if (j.category.equals(category)) {
                ItemBuilder builder = new ItemBuilder(j.icon).setName("&b" + j.name);

                List<String> lore = new ArrayList<>();
                // !!! ИСПРАВЛЕНИЕ: Теперь мы заменяем %suffix% !!!
                String rawFormat = j.format.replace("%player%", player.getName())
                        .replace("%prefix%", legacyPrefix)
                        .replace("%suffix%", suffix);

                lore.add("&7Формат: " + toLegacy(rawFormat));
                lore.add("");

                if (currentSelected.equals(j.name())) {
                    lore.add("&aВЫБРАНО");
                    builder.setGlow(true);
                } else if (owned.contains(j.name())) {
                    lore.add("&eНажмите, чтобы выбрать");
                } else {
                    lore.add("&cЦена: &a" + j.price + " ❇");
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

        if (title.equals("Категории Джоинеров")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            int slot = event.getSlot();

            if (slot == 11) openList(player, "Классика");
            else if (slot == 13) openList(player, "Мемы");
            else if (slot == 15) openList(player, "Дарк");
            else if (slot == 22) CustomizationGui.open(player);

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (title.startsWith("Джоинеры: ")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            if (event.getSlot() == 31) {
                openCategories(player);
                return;
            }

            String category = title.replace("Джоинеры: ", "");
            String itemName = ChatUtil.strip(event.getCurrentItem().getItemMeta().getDisplayName());

            Joiner clickedJoiner = null;
            for (Joiner j : Joiner.values()) {
                if (j.name.equals(itemName) && j.category.equals(category)) clickedJoiner = j;
            }
            if (clickedJoiner == null) return;

            Set<String> owned = unlockedJoiners.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>(Collections.singletonList("DEFAULT")));

            if (owned.contains(clickedJoiner.name())) {
                selectedJoiner.put(player.getUniqueId(), clickedJoiner.name());
                ChatUtil.sendMessage(player, "&aВы выбрали сообщение: &b" + clickedJoiner.name);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                openList(player, category);
            } else {
                PlayerStats stats = StatsManager.getStats(player);
                if (stats.getEmeralds() >= clickedJoiner.price) {
                    stats.setEmeralds(stats.getEmeralds() - clickedJoiner.price);
                    owned.add(clickedJoiner.name());

                    ChatUtil.sendMessage(player, "&aУспешная покупка! Вы приобрели: &b" + clickedJoiner.name);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    openList(player, category);
                } else {
                    ChatUtil.sendMessage(player, "&cУ вас недостаточно Изумрудов! Нужно: &a" + clickedJoiner.price + " ❇");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
        }
    }

    public static String getActiveMessage(Player player) {
        String id = selectedJoiner.getOrDefault(player.getUniqueId(), "DEFAULT");
        return Joiner.valueOf(id).format;
    }

    public static String toLegacy(String text) {
        if (text == null) return "";
        return text
                .replace("<dark_red>", "&4").replace("</dark_red>", "")
                .replace("<red>", "&c").replace("</red>", "")
                .replace("<gold>", "&6").replace("</gold>", "")
                .replace("<yellow>", "&e").replace("</yellow>", "")
                .replace("<dark_green>", "&2").replace("</dark_green>", "")
                .replace("<green>", "&a").replace("</green>", "")
                .replace("<aqua>", "&b").replace("</aqua>", "")
                .replace("<dark_aqua>", "&3").replace("</dark_aqua>", "")
                .replace("<dark_blue>", "&1").replace("</dark_blue>", "")
                .replace("<blue>", "&9").replace("</blue>", "")
                .replace("<light_purple>", "&d").replace("</light_purple>", "")
                .replace("<dark_purple>", "&5").replace("</dark_purple>", "")
                .replace("<white>", "&f").replace("</white>", "")
                .replace("<gray>", "&7").replace("</gray>", "")
                .replace("<dark_gray>", "&8").replace("</dark_gray>", "")
                .replace("<black>", "&0").replace("</black>", "")
                .replace("<bold>", "&l").replace("</bold>", "")
                .replace("<italic>", "&o").replace("</italic>", "")
                .replace("<strikethrough>", "&m").replace("</strikethrough>", "")
                .replace("<underlined>", "&n").replace("</underlined>", "")
                .replace("<obfuscated>", "&k").replace("</obfuscated>", "")
                .replace("<reset>", "&r").replace("</reset>", "")
                .replaceAll("<[^>]+>", ""); // !!! УБИВАЕТ ВСЕ НЕИЗВЕСТНЫЕ ТЕГИ !!!
    }
}