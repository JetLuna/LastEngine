package net.jetluna.api.report;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportsGui implements Listener {

    private static final int MAX_PER_PAGE = 45;

    public static void open(Player player, int page) {
        String titleRaw = color(LanguageManager.getString(player, "report.gui.main_title")).replace("%page%", String.valueOf(page));
        Inventory gui = Bukkit.createInventory(player, 54, titleRaw);
        List<String> reportedPlayers = ReportManager.getReportedPlayers();

        if (reportedPlayers.isEmpty()) {
            gui.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName(color(LanguageManager.getString(player, "report.gui.empty_name")))
                    .setLore(colorList(player, "report.gui.empty_lore"))
                    .build());
        } else {
            int startIndex = (page - 1) * MAX_PER_PAGE;
            int endIndex = Math.min(startIndex + MAX_PER_PAGE, reportedPlayers.size());

            int slot = 0;
            for (int i = startIndex; i < endIndex; i++) {
                String targetName = reportedPlayers.get(i);
                int reportCount = ReportManager.getReportsFor(targetName).size();

                String headName = color(LanguageManager.getString(player, "report.gui.head_name")).replace("%player%", targetName);
                List<String> headLore = colorList(player, "report.gui.head_lore");
                headLore.replaceAll(s -> s.replace("%count%", String.valueOf(reportCount)));

                ItemStack headItem = new ItemBuilder(Material.PLAYER_HEAD)
                        .setName(headName)
                        .setLore(headLore).build();

                ItemMeta meta = headItem.getItemMeta();
                if (meta instanceof SkullMeta) {
                    ((SkullMeta) meta).setOwner(targetName);
                    headItem.setItemMeta(meta);
                }

                gui.setItem(slot++, headItem);
            }

            if (page > 1) {
                gui.setItem(45, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "report.gui.btn_prev"))).build());
            }
            if (reportedPlayers.size() > endIndex) {
                gui.setItem(53, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "report.gui.btn_next"))).build());
            }
        }
        player.openInventory(gui);
    }

    public static void openPlayerReports(Player player, String targetName, int page) {
        String titleRaw = color(LanguageManager.getString(player, "report.gui.player_title"))
                .replace("%player%", targetName)
                .replace("%page%", String.valueOf(page));
        Inventory gui = Bukkit.createInventory(player, 54, titleRaw);
        List<ReportManager.Report> reports = ReportManager.getReportsFor(targetName);

        int startIndex = (page - 1) * MAX_PER_PAGE;
        int endIndex = Math.min(startIndex + MAX_PER_PAGE, reports.size());

        int slot = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (int i = startIndex; i < endIndex; i++) {
            ReportManager.Report report = reports.get(i);

            String repName = color(LanguageManager.getString(player, "report.gui.report_name")).replace("%sender%", report.sender);
            List<String> repLore = colorList(player, "report.gui.report_lore");
            repLore.replaceAll(s -> s.replace("%reason%", report.reason).replace("%time%", sdf.format(new Date(report.timestamp))));

            gui.setItem(slot++, new ItemBuilder(Material.PAPER).setName(repName).setLore(repLore).build());
        }

        gui.setItem(49, new ItemBuilder(Material.DARK_OAK_DOOR).setName(color(LanguageManager.getString(player, "report.gui.btn_back_list"))).build());

        if (page > 1) {
            gui.setItem(45, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "report.gui.btn_prev"))).build());
        }
        if (reports.size() > endIndex) {
            gui.setItem(53, new ItemBuilder(Material.ARROW).setName(color(LanguageManager.getString(player, "report.gui.btn_next"))).build());
        }

        player.openInventory(gui);
    }

    public static void openManagePlayer(Player player, String targetName) {
        String titleRaw = color(LanguageManager.getString(player, "report.gui.manage_title")).replace("%player%", targetName);
        Inventory gui = Bukkit.createInventory(player, 27, titleRaw);

        gui.setItem(11, new ItemBuilder(Material.ENDER_PEARL)
                .setName(color(LanguageManager.getString(player, "report.gui.manage_tp")))
                .setLore(colorList(player, "report.gui.manage_tp_lore"))
                .build());

        gui.setItem(13, new ItemBuilder(Material.LIME_DYE)
                .setName(color(LanguageManager.getString(player, "report.gui.manage_guilty")))
                .setLore(colorList(player, "report.gui.manage_guilty_lore"))
                .build());

        gui.setItem(15, new ItemBuilder(Material.RED_DYE)
                .setName(color(LanguageManager.getString(player, "report.gui.manage_innocent")))
                .setLore(colorList(player, "report.gui.manage_innocent_lore"))
                .build());

        gui.setItem(22, new ItemBuilder(Material.DARK_OAK_DOOR).setName(color(LanguageManager.getString(player, "report.gui.btn_back"))).build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player staff = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String mainTitleBase = color(LanguageManager.getString(staff, "report.gui.main_title"));
        String mainPrefix = mainTitleBase.substring(0, mainTitleBase.indexOf("%page%"));

        String playerTitleBase = color(LanguageManager.getString(staff, "report.gui.player_title"));
        String pTitlePrefix = playerTitleBase.substring(0, playerTitleBase.indexOf("%player%"));

        String manageTitleBase = color(LanguageManager.getString(staff, "report.gui.manage_title"));
        String mTitlePrefix = manageTitleBase.substring(0, manageTitleBase.indexOf("%player%"));

        if (!title.startsWith(mainPrefix) && !title.startsWith(pTitlePrefix) && !title.startsWith(mTitlePrefix)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ItemStack item = event.getCurrentItem();
        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        String rawBtnPrev = ChatColor.stripColor(color(LanguageManager.getString(staff, "report.gui.btn_prev")));
        String rawBtnNext = ChatColor.stripColor(color(LanguageManager.getString(staff, "report.gui.btn_next")));
        String rawBtnBackList = ChatColor.stripColor(color(LanguageManager.getString(staff, "report.gui.btn_back_list")));
        String rawBtnBack = ChatColor.stripColor(color(LanguageManager.getString(staff, "report.gui.btn_back")));

        // --- ГЛАВНОЕ МЕНЮ ---
        if (title.startsWith(mainPrefix)) {
            int page = Integer.parseInt(title.substring(mainPrefix.length()));

            if (itemName.equals(rawBtnNext)) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, page + 1); return; }
            if (itemName.equals(rawBtnPrev)) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, page - 1); return; }

            if (item.getType() == Material.PLAYER_HEAD) {
                String headBase = ChatColor.stripColor(color(LanguageManager.getString(staff, "report.gui.head_name")));
                String headPrefix = headBase.substring(0, headBase.indexOf("%player%"));
                String targetName = itemName.replace(headPrefix, "");

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

        // --- МЕНЮ ПРОСМОТРА ЖАЛОБ ---
        if (title.startsWith(pTitlePrefix)) {
            String midSection = title.substring(pTitlePrefix.length());
            String pageDelimiter = playerTitleBase.substring(playerTitleBase.indexOf("%player%") + 8, playerTitleBase.indexOf("%page%"));
            String targetName = midSection.split(pageDelimiter)[0];
            int page = Integer.parseInt(midSection.substring(targetName.length() + pageDelimiter.length()));

            if (itemName.equals(rawBtnNext)) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openPlayerReports(staff, targetName, page + 1); return; }
            if (itemName.equals(rawBtnPrev)) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); openPlayerReports(staff, targetName, page - 1); return; }
            if (itemName.equals(rawBtnBackList)) { staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f); open(staff, 1); return; }
            return;
        }

        // --- МЕНЮ УПРАВЛЕНИЯ ---
        if (title.startsWith(mTitlePrefix)) {
            String targetName = title.substring(mTitlePrefix.length());
            Material type = item.getType();

            if (itemName.equals(rawBtnBack)) {
                staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                open(staff, 1);
                return;
            }

            if (type == Material.ENDER_PEARL) {
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null && target.isOnline()) {
                    staff.teleport(target.getLocation());
                    staff.closeInventory();
                    String msg = LanguageManager.getString(staff, "report.messages.teleport_success").replace("%player%", targetName);
                    ChatUtil.sendMessage(staff, msg);
                    staff.playSound(staff.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                } else {
                    String msg = LanguageManager.getString(staff, "report.messages.teleport_fail").replace("%player%", targetName);
                    ChatUtil.sendMessage(staff, msg);
                    staff.playSound(staff.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
            else if (type == Material.LIME_DYE) {
                List<ReportManager.Report> reports = ReportManager.getReportsFor(targetName);
                Set<String> uniqueSenders = reports.stream().map(r -> r.sender).collect(Collectors.toSet());

                for (String senderName : uniqueSenders) {
                    Player senderPlayer = Bukkit.getPlayerExact(senderName);
                    if (senderPlayer != null && senderPlayer.isOnline()) {
                        PlayerStats stats = StatsManager.getStats(senderPlayer);
                        if (stats != null) {
                            stats.setCoins(stats.getCoins() + 1000);
                            String msg1 = LanguageManager.getString(senderPlayer, "report.messages.reward_1").replace("%player%", targetName);
                            ChatUtil.sendMessage(senderPlayer, msg1);
                            LanguageManager.sendMessage(senderPlayer, "report.messages.reward_2");
                            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        }
                    }
                }

                ReportManager.clearReportsFor(targetName);
                LanguageManager.sendMessage(staff, "report.messages.guilty_staff");
                staff.playSound(staff.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                open(staff, 1);
            }
            else if (type == Material.RED_DYE) {
                ReportManager.clearReportsFor(targetName);
                LanguageManager.sendMessage(staff, "report.messages.innocent_staff");
                staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                open(staff, 1);
            }
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