package net.jetluna.lobby.gui;

import net.jetluna.api.bestplayer.BestPlayerManager;
import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.api.util.ItemBuilder;
import net.jetluna.api.util.NameFormatUtil;
import net.jetluna.lobby.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BestPlayerGui implements Listener {

    public static void open(Player player) {
        String title = color(LanguageManager.getString(player, "lobby.best_player_gui.title"));
        Inventory gui = Bukkit.createInventory(player, 27, title);

        int nextPrice = BestPlayerManager.getNextPrice();
        boolean expired = BestPlayerManager.isExpired();

        // --- ДИНАМИЧЕСКИЙ ПРЕФИКС ---
        String currentBestName = BestPlayerManager.getCurrentFormattedName();
        if (!expired && BestPlayerManager.getCurrentBest() != null) {
            Player onlineBest = Bukkit.getPlayer(BestPlayerManager.getCurrentBest());
            if (onlineBest != null) {
                // Если лучший игрок онлайн, генерируем свежий префикс с актуальным цветом!
                currentBestName = NameFormatUtil.getFormattedName(onlineBest, net.jetluna.api.rank.RankManager.getRank(onlineBest));
            }
        }
        // -----------------------------

        String statusKey = expired ? "lobby.best_player_gui.status_empty" : "lobby.best_player_gui.status_taken";
        List<String> lore = colorList(player, statusKey + ".lore");
        List<String> finalLore = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String expireDate = expired ? "-" : sdf.format(new Date(BestPlayerManager.getExpireTime()));

        for (String line : lore) {
            finalLore.add(line
                    .replace("%player%", currentBestName) // Подставляем свежую переменную
                    .replace("%price%", String.valueOf(nextPrice))
                    .replace("%time%", expireDate)
            );
        }

        gui.setItem(13, new ItemBuilder(Material.NETHER_STAR)
                .setName(color(LanguageManager.getString(player, statusKey + ".name")))
                .setLore(finalLore)
                .setGlow(true)
                .build());

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String expectedTitle = color(LanguageManager.getString(player, "lobby.best_player_gui.title"));
        if (!ChatUtil.strip(event.getView().getTitle()).equals(ChatUtil.strip(expectedTitle))) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getSlot() != 13) return;

        if (BestPlayerManager.getCurrentBest() != null && BestPlayerManager.getCurrentBest().equals(player.getUniqueId()) && !BestPlayerManager.isExpired()) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.best_player_gui.already_best")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        PlayerStats stats = StatsManager.getStats(player);
        if (stats == null) return;

        int price = BestPlayerManager.getNextPrice();

        if (stats.getEmeralds() < price) {
            ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.best_player_gui.not_enough")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Списываем изумруды
        stats.setEmeralds(stats.getEmeralds() - price);

        // --- ЖЕЛЕЗОБЕТОННО ДОСТАЕМ ТЕКУЩИЙ СКИН ИГРОКА ---
        String texValue = "";
        String texSignature = "";

        // 1. Сначала берем из нашей надежной истории SkinManager
        java.util.List<String> history = net.jetluna.api.skin.SkinManager.getHistory(player);
        if (history != null && !history.isEmpty()) {
            String lastEntry = history.get(history.size() - 1);
            String[] parts = lastEntry.split(";", 3);
            if (parts.length == 3) {
                texValue = parts[1];
                texSignature = parts[2];
            }
        }

        // 2. Если в истории почему-то пусто, пробуем достать из профиля ядра
        if (texValue.isEmpty()) {
            for (com.destroystokyo.paper.profile.ProfileProperty prop : player.getPlayerProfile().getProperties()) {
                if (prop.getName().equals("textures")) {
                    texValue = prop.getValue();
                    texSignature = prop.getSignature() != null ? prop.getSignature() : "";
                    break;
                }
            }
        }

        // Передаем 100% найденную текстуру в менеджер
        BestPlayerManager.setBestPlayer(player, price, texValue, texSignature);

        // Уведомляем об успехе
        ChatUtil.sendMessage(player, color(LanguageManager.getString(player, "lobby.best_player_gui.success")));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.closeInventory();

        String broadcastMsg = color(LanguageManager.getString(player, "lobby.best_player_gui.broadcast")
                .replace("%player%", NameFormatUtil.getFormattedName(player, net.jetluna.api.rank.RankManager.getRank(player))));
        Bukkit.broadcastMessage(broadcastMsg);

        // ВЫЗЫВАЕМ ОБНОВЛЕНИЕ NPC (так как импорт уже есть в шапке, пишем напрямую)
        NpcManager.updateBestPlayerSkin();
    }

    private static String color(String text) {
        return text == null ? "" : ChatUtil.parseLegacy(text);
    }

    private static List<String> colorList(Player p, String key) {
        List<String> list = LanguageManager.getList(p, key);
        if (list == null) return new ArrayList<>();
        list.replaceAll(s -> ChatUtil.parseLegacy(s));
        return list;
    }
}