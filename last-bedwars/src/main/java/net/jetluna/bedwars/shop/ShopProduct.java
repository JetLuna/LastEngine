package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.bedwars.resource.Resource;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class ShopProduct {

    private final ItemStack icon;
    private final Resource currency;
    private final int price;

    public ShopProduct(ItemStack item, Resource currency, int price) {
        this.currency = currency;
        this.price = price;

        // Автоматически добавляем красивое описание с ценой к иконке товара
        ItemStack finalItem = item.clone();
        ItemMeta meta = finalItem.getItemMeta();

        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            // Если у предмета уже был лор, копируем его
            if (meta.hasLore() && meta.lore() != null) {
                lore.addAll(meta.lore());
            }

            // Добавляем пустую строку и цену
            lore.add(Component.empty());
            lore.add(ChatUtil.parse("§7>Цена: " + currency.getDisplayName() + " §ex" + price));
            lore.add(ChatUtil.parse("§eНажмите, чтобы купить!"));

            meta.lore(lore);
            finalItem.setItemMeta(meta);
        }

        this.icon = finalItem;
    }

    // 1. Стандартный метод (просто возвращает предмет)
    public ItemStack getIcon() {
        return icon;
    }

    // 2. "Умный" метод, который можно переопределять под игрока
    public ItemStack getIcon(Player player) {
        return getIcon(); // По умолчанию просто вызывает метод выше
    }

    // Метод вызывается, когда игрок кликает по товару в меню
    public boolean processPurchase(Player player) {
        if (!hasEnoughCurrency(player)) {
            ChatUtil.sendMessage(player, "§cНедостаточно ресурсов!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }

        takeCurrency(player);
        onBuy(player); // Вызываем логику выдачи предмета (у каждого товара она своя)

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        return true;
    }

    // Метод проверки валюты в инвентаре (замена старому PlayerItemTransaction)
    private boolean hasEnoughCurrency(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency.getMaterial()) {
                count += item.getAmount();
            }
        }
        return count >= price;
    }

    // Метод изъятия валюты
    private void takeCurrency(Player player) {
        int needed = price;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency.getMaterial()) {
                if (item.getAmount() > needed) {
                    item.setAmount(item.getAmount() - needed);
                    break;
                } else {
                    needed -= item.getAmount();
                    item.setAmount(0); // Удаляем стак
                }
            }
            if (needed <= 0) break;
        }
    }

    // Этот метод мы будем переопределять для блоков, брони, мечей и т.д.
    public abstract void onBuy(Player player);
}