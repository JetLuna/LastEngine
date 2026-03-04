package net.jetluna.api.report;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportsGui implements Listener {

    private static final int MAX_PER_PAGE = 45; // Первые 5 рядов для элементов, последний для кнопок

    // 1. ГЛАВНОЕ МЕНЮ (Со страницами)
    public static void open(Player player, int page) {
        Inventory gui = Bukkit.createInventory(player, 54, "Список жалоб | Страница " + page);
        List<String> reportedPlayers = ReportManager.getReportedPlayers();

        if (reportedPlayers.isEmpty()) {
            gui.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("&aЖалоб нет!")
                    .setLore("&7Игроки ведут себя хорошо.")
                    .build());
        } else {
            int startIndex = (page - 1) * MAX_PER_PAGE;
            int endIndex = Math.min(startIndex + MAX_PER_PAGE, reportedPlayers.size());

            int slot = 0;
            for (int i = startIndex; i < endIndex; i++) {
                String targetName = reportedPlayers.get(i);
                int reportCount = ReportManager.getReportsFor(targetName).size();

                ItemStack headItem = new ItemBuilder(Material.PLAYER_HEAD)
                        .setName("&cПодозреваемый: &f" + targetName)
                        .setLore(
                                "&7Всего жалоб: &e" + reportCount,
                                "",
                                "&aЛКМ &8- &7Смотреть жалобы",
                                "&cПКМ &8- &7Управление нарушителем"
                        ).build();

                ItemMeta meta = headItem.getItemMeta();
                if (meta instanceof SkullMeta) {
                    ((SkullMeta) meta).setOwner(targetName);
                    headItem.setItemMeta(meta);
                }

                gui.setItem(slot++, headItem);
            }

            // ПАГИНАЦИЯ (Нижний ряд)
            if (page > 1) {
                gui.setItem(45, new ItemBuilder(Material.ARROW).setName("&e⬅ Предыдущая страница").build());
            }
            if (reportedPlayers.size() > endIndex) {
                gui.setItem(53, new ItemBuilder(Material.ARROW).setName("&eСледующая страница ➡").build());
            }
        }
        player.openInventory(gui);
    }

    // 2. МЕНЮ ПРОСМОТРА ЖАЛОБ (ЛКМ) (Со страницами и кнопкой Назад)
    public static void openPlayerReports(Player player, String targetName, int page) {
        Inventory gui = Bukkit.createInventory(player, 54, "Жалобы: " + targetName + " | Стр. " + page);
        List<ReportManager.Report> reports = ReportManager.getReportsFor(targetName);

        int startIndex = (page - 1) * MAX_PER_PAGE;
        int endIndex = Math.min(startIndex + MAX_PER_PAGE, reports.size());

        int slot = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (int i = startIndex; i < endIndex; i++) {
            ReportManager.Report report = reports.get(i);
            gui.setItem(slot++, new ItemBuilder(Material.PAPER)
                    .setName("&eЖалоба от: &f" + report.sender)
                    .setLore(
                            "&7Причина: &f" + report.reason,
                            "&7Время: &8" + sdf.format(new Date(report.timestamp))
                    ).build());
        }

        // НАВИГАЦИЯ (Нижний ряд)
        gui.setItem(49, new ItemBuilder(Material.DARK_OAK_DOOR).setName("&c⬅ Вернуться к списку нарушителей").build());

        if (page > 1) {
            gui.setItem(45, new ItemBuilder(Material.ARROW).setName("&e⬅ Пред. страница").build());
        }
        if (reports.size() > endIndex) {
            gui.setItem(53, new ItemBuilder(Material.ARROW).setName("&eСлед. страница ➡").build());
        }

        player.openInventory(gui);
    }

    // 3. МЕНЮ УПРАВЛЕНИЯ (ПКМ) (С кнопкой Назад)
    public static void openManagePlayer(Player player, String targetName) {
        Inventory gui = Bukkit.createInventory(player, 27, "Управление: " + targetName);

        gui.setItem(11, new ItemBuilder(Material.ENDER_PEARL)
                .setName("&bТелепорт к игроку")
                .setLore("&7Нажмите, чтобы переместиться", "&7к подозреваемому в ванише.")
                .build());

        gui.setItem(13, new ItemBuilder(Material.LIME_DYE)
                .setName("&aВиновен (Наказать)")
                .setLore("&7Закрыть жалобы и", "&7выдать &e1000 монет", "&7всем, кто кинул репорт.")
                .build());

        gui.setItem(15, new ItemBuilder(Material.RED_DYE)
                .setName("&cНе виновен (Оправдать)")
                .setLore("&7Просто удалить все", "&7жалобы на этого игрока.")
                .build());

        // КНОПКА НАЗАД
        gui.setItem(22, new ItemBuilder(Material.DARK_OAK_DOOR).setName("&c⬅ Вернуться к списку").build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("Список жалоб") && !title.startsWith("Жалобы: ") && !title.startsWith("Управление: ")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player staff = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // --- ОБРАБОТКА: ГЛАВНОЕ МЕНЮ ---
        if (title.startsWith("Список жалоб")) {
            int page = Integer.parseInt(title.split("Страница ")[1]);

            if (itemName.equals("Следующая страница ➡")) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, page + 1); return; }
            if (itemName.equals("⬅ Предыдущая страница")) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, page - 1); return; }

            if (item.getType() == Material.PLAYER_HEAD) {
                String targetName = itemName.replace("Подозреваемый: ", "");
                if (event.isLeftClick()) {
                    staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    openPlayerReports(staff, targetName, 1);
                } else if (event.isRightClick()) {
                    staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    openManagePlayer(staff, targetName);
                }
            }
            return;
        }

        // --- ОБРАБОТКА: МЕНЮ ПРОСМОТРА ЖАЛОБ ---
        if (title.startsWith("Жалобы: ")) {
            String targetName = title.substring(8).split(" \\|")[0];
            int page = Integer.parseInt(title.split("Стр. ")[1]);

            if (itemName.equals("След. страница ➡")) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openPlayerReports(staff, targetName, page + 1); return; }
            if (itemName.equals("⬅ Пред. страница")) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openPlayerReports(staff, targetName, page - 1); return; }
            if (itemName.equals("⬅ Вернуться к списку нарушителей")) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, 1); return; }

            return;
        }

        // --- ОБРАБОТКА: МЕНЮ УПРАВЛЕНИЯ ---
        if (title.startsWith("Управление: ")) {
            String targetName = title.replace("Управление: ", "");
            Material type = item.getType();

            if (itemName.equals("⬅ Вернуться к списку")) {
                staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                open(staff, 1);
                return;
            }

            if (type == Material.ENDER_PEARL) { // ТЕЛЕПОРТ
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null && target.isOnline()) {
                    staff.teleport(target.getLocation());
                    staff.closeInventory();
                    ChatUtil.sendMessage(staff, "&aВы телепортировались к &e" + targetName);
                    staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                } else {
                    ChatUtil.sendMessage(staff, "&cИгрок &e" + targetName + " &cне найден на сервере.");
                    staff.playSound(staff.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
            else if (type == Material.LIME_DYE) { // ВИНОВЕН
                List<ReportManager.Report> reports = ReportManager.getReportsFor(targetName);
                Set<String> uniqueSenders = reports.stream().map(r -> r.sender).collect(Collectors.toSet());

                for (String senderName : uniqueSenders) {
                    Player senderPlayer = Bukkit.getPlayerExact(senderName);
                    if (senderPlayer != null && senderPlayer.isOnline()) {
                        PlayerStats stats = StatsManager.getStats(senderPlayer);
                        if (stats != null) {
                            stats.setCoins(stats.getCoins() + 1000);
                            ChatUtil.sendMessage(senderPlayer, "&aИгрок &e" + targetName + " &aбыл наказан по вашей жалобе!");
                            ChatUtil.sendMessage(senderPlayer, "&e+1000 монет &aза помощь проекту!");
                            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        }
                    }
                }

                ReportManager.clearReportsFor(targetName);
                ChatUtil.sendMessage(staff, "&aИгрок признан виновным. Репортеры получили по 1000 монет!");
                staff.playSound(staff.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                open(staff, 1); // Возвращаем админа на 1 страницу
            }
            else if (type == Material.RED_DYE) { // НЕ ВИНОВЕН
                ReportManager.clearReportsFor(targetName);
                ChatUtil.sendMessage(staff, "&aИгрок оправдан. Жалобы удалены.");
                staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                open(staff, 1); // Возвращаем админа на 1 страницу
            }
        }
    }
}