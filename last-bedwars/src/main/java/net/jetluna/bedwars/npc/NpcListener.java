package net.jetluna.bedwars.npc;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.jetluna.api.util.ChatUtil;
import net.jetluna.bedwars.shop.ShopCategory;
import net.jetluna.bedwars.shop.ShopGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcListener implements Listener {

    @EventHandler
    public void onNpcClick(NPCRightClickEvent event) {
        Player player = event.getClicker();

        // Проверяем, есть ли у этого NPC наша метка
        if (!event.getNPC().data().has("bedwars_npc_type")) return;

        String type = event.getNPC().data().get("bedwars_npc_type");

        if (type.equals("shop")) {
            // Открываем главную вкладку магазина (Блоки)
            ShopGui.open(player, ShopCategory.BLOCKS);

        } else if (type.equals("upgrade")) {
            // Открываем наше новое меню прокачки!
            net.jetluna.bedwars.shop.UpgradeGui.open(player);
        }
    }
}