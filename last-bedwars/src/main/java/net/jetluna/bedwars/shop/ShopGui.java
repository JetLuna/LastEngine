package net.jetluna.bedwars.shop;

import net.jetluna.api.util.ItemBuilder;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.item.CustomItem;
import net.jetluna.bedwars.resource.Resource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.citizensnpcs.util.Util.createItem;

public class ShopGui implements Listener {

    private static final Map<UUID, ShopCategory> playerCategories = new HashMap<>();
    private static final Map<ShopCategory, Map<Integer, ShopProduct>> shopItems = new HashMap<>();

    public static void init() {
        BedWarsPlugin plugin = BedWarsPlugin.getInstance();

        for (ShopCategory category : ShopCategory.values()) {
            shopItems.put(category, new HashMap<>());
        }

// ================= БЛОКИ =================
        addProduct(ShopCategory.BLOCKS, 19, new TeamWoolShopProduct(plugin, 16, Resource.IRON, 4)); // <--- ТЕПЕРЬ ШЕРСТЬ УМНАЯ
        addProduct(ShopCategory.BLOCKS, 20, new DefaultShopProduct(new ItemStack(Material.OAK_PLANKS, 16), new ItemStack(Material.OAK_PLANKS, 16), Resource.IRON, 16));
        addProduct(ShopCategory.BLOCKS, 21, new DefaultShopProduct(new ItemStack(Material.END_STONE, 12), new ItemStack(Material.END_STONE, 12), Resource.IRON, 24));

        ItemStack blastGlass = new ItemBuilder(Material.TINTED_GLASS).setName("§bСтекло").build();
        blastGlass.setAmount(8);
        addProduct(ShopCategory.BLOCKS, 22, new DefaultShopProduct(blastGlass, blastGlass, Resource.IRON, 12));

        addProduct(ShopCategory.BLOCKS, 23, new DefaultShopProduct(new ItemStack(Material.OBSIDIAN, 4), new ItemStack(Material.OBSIDIAN, 4), Resource.OPAL, 4));
        addProduct(ShopCategory.BLOCKS, 24, new DefaultShopProduct(new ItemStack(Material.LADDER, 16), new ItemStack(Material.LADDER, 16), Resource.IRON, 4));
        addProduct(ShopCategory.BLOCKS, 25, new DefaultShopProduct(new ItemStack(Material.COBWEB, 8), new ItemStack(Material.COBWEB, 8), Resource.IRON, 12));

// ================= ОРУЖИЕ =================
        addProduct(ShopCategory.WEAPONS, 19, new WeaponShopProduct());

        addProduct(ShopCategory.WEAPONS, 23, new DefaultShopProduct(createUnbreakable(Material.MACE, "§6Булава"), createUnbreakable(Material.MACE, "§6Булава"), Resource.OPAL, 15));

        // Используем новые материалы из 1.21.11! (Если Идея подчеркнет красным, проверь, как точно они записаны в энуме Material вашего ядра, возможно просто SPEAR)
        addProduct(ShopCategory.WEAPONS, 24, new DefaultShopProduct(createUnbreakable(Material.WOODEN_SPEAR, "§6Медное копье"), createUnbreakable(Material.WOODEN_SPEAR, "§6Деревянное копье"), Resource.DIAMOND, 10));
        addProduct(ShopCategory.WEAPONS, 25, new DefaultShopProduct(createUnbreakable(Material.NETHERITE_SPEAR, "§5Незеритовое копье"), createUnbreakable(Material.NETHERITE_SPEAR, "§5Незеритовое копье"), Resource.OPAL, 8));

        // ================= БРОНЯ =================
        addProduct(ShopCategory.ARMOR, 19, new ArmorShopProduct(plugin, ArmorType.IRON));
        addProduct(ShopCategory.ARMOR, 20, new ArmorShopProduct(plugin, ArmorType.DIAMOND));
        addProduct(ShopCategory.ARMOR, 21, new ArmorShopProduct(plugin, ArmorType.NETHERITE));
        addProduct(ShopCategory.ARMOR, 23, new DefaultShopProduct(new ItemBuilder(Material.SHIELD).setName("§eЩит").build(), new ItemBuilder(Material.SHIELD).setName("§eЩит").build(), Resource.IRON, 20));

        // ================= ИНСТРУМЕНТЫ (Пока статические, потом сделаем апгрейд) =================
// ================= ИНСТРУМЕНТЫ =================
        addProduct(ShopCategory.TOOLS, 19, new ToolShopProduct(ToolShopProduct.ToolType.PICKAXE));
        addProduct(ShopCategory.TOOLS, 20, new ToolShopProduct(ToolShopProduct.ToolType.AXE));
        addProduct(ShopCategory.TOOLS, 21, new DefaultShopProduct(createUnbreakable(Material.SHEARS, "§fНожницы"), createUnbreakable(Material.SHEARS, "§fНожницы"), Resource.IRON, 20));

        // ================= ЛУКИ =================
        addProduct(ShopCategory.BOWS, 19, new DefaultShopProduct(createUnbreakable(Material.BOW, "§eЛук"), createUnbreakable(Material.BOW, "§eЛук"), Resource.DIAMOND, 12));

        ItemStack bow1 = createUnbreakable(Material.BOW, "§eЛук I"); bow1.addUnsafeEnchantment(Enchantment.POWER, 1);
        addProduct(ShopCategory.BOWS, 20, new DefaultShopProduct(bow1, bow1, Resource.DIAMOND, 24));

        ItemStack bow2 = createUnbreakable(Material.BOW, "§6ЛукII"); bow2.addUnsafeEnchantment(Enchantment.POWER, 2);
        addProduct(ShopCategory.BOWS, 21, new DefaultShopProduct(bow2, bow2, Resource.OPAL, 5));

        ItemStack bow3 = createUnbreakable(Material.BOW, "§cЛук III"); bow3.addUnsafeEnchantment(Enchantment.POWER, 3);
        addProduct(ShopCategory.BOWS, 22, new DefaultShopProduct(bow3, bow3, Resource.OPAL, 10));

        ItemStack crossbow = createUnbreakable(Material.CROSSBOW, "§dАрбалет");
        crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 3);
        crossbow.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
        addProduct(ShopCategory.BOWS, 23, new DefaultShopProduct(crossbow, crossbow, Resource.OPAL, 6));

        addProduct(ShopCategory.BOWS, 24, new DefaultShopProduct(
                new ItemStack(Material.ARROW, 8),
                new ItemStack(Material.ARROW, 8),
                Resource.IRON, 8
        ));

// ================= ЗЕЛЬЯ (ГОЛОВЫ) =================
        String speedTex = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzUyYWZhNjc5OGYxYTRiOTgxNjRmMzczMDFmODkwY2UxZDViZTNiNjg5ZTBkZDI0YjQ1MDkyN2NlOTk4MmIifX19"; // Голубая колба
        String jumpTex = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE1ZDViZjQ5M2RjNjRlNzljMjlhNTJjMDkwZTRjM2I3NzRkY2JlMWY5ZGE3YTFhMDVjZjczZTk3Zjc3YTg2In19fQ=="; // Салатовая колба
        String regenTex = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQyZjUzNjJkZDJhYWIyOGFmNGViNDEzODllMjhmMjBjNWU2ZDU2Y2JhOWE1MDExNWIyOGVkYTU5YmNlZWYifX19"; // Розовая колба
        String levitationTex = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFjM2E3MmJkZTZhNzhhYTBhMGIwZTU3NDkxZDBkODJhYTFiNjQ0NTUzMTU1YWMxNDA0M2IzMTdmNjdmOTkifX19"; // Белая колба

        addProduct(ShopCategory.POTIONS, 19, new DefaultShopProduct(createPotionHead("§bЗелье Скорости I (1 мин)", speedTex), createPotionHead("§bЗелье Скорости I (1 мин)", speedTex), Resource.OPAL, 1));
        addProduct(ShopCategory.POTIONS, 20, new DefaultShopProduct(createPotionHead("§bЗелье Скорости II (30 сек)", speedTex), createPotionHead("§bЗелье Скорости II (30 сек)", speedTex), Resource.OPAL, 2));
        addProduct(ShopCategory.POTIONS, 21, new DefaultShopProduct(createPotionHead("§bЗелье Скорости IV (10 сек)", speedTex), createPotionHead("§bЗелье Скорости IV (10 сек)", speedTex), Resource.OPAL, 3));

        addProduct(ShopCategory.POTIONS, 22, new DefaultShopProduct(createPotionHead("§aЗелье Прыгучести III (30 сек)", jumpTex), createPotionHead("§aЗелье Прыгучести III (30 сек)", jumpTex), Resource.OPAL, 2));
        addProduct(ShopCategory.POTIONS, 23, new DefaultShopProduct(createPotionHead("§dЗелье Регенерации II", regenTex), createPotionHead("§dЗелье Регенерации II", regenTex), Resource.OPAL, 3));
        addProduct(ShopCategory.POTIONS, 24, new DefaultShopProduct(createPotionHead("§fЗелье Левитации", levitationTex), createPotionHead("§fЗелье Левитации", levitationTex), Resource.OPAL, 4));

// ================= ОСТАЛЬНОЕ =================
        addProduct(ShopCategory.OTHER, 19, new DefaultShopProduct(new ItemBuilder(Material.FIRE_CHARGE).setName("§6Фаербол").build(), new ItemBuilder(Material.FIRE_CHARGE).setName("§6Фаербол").build(), Resource.IRON, 40));
        addProduct(ShopCategory.OTHER, 20, new DefaultShopProduct(new ItemBuilder(Material.TNT).setName("§cДинамит").build(), new ItemBuilder(Material.TNT).setName("§cДинамит").build(), Resource.DIAMOND, 4));

        ItemStack platform = new ItemBuilder(Material.SLIME_BALL).setName("§aСейв-Платформа").setLore("§7Создает 3x3 платформу из", "§7слизи под вашими ногами!").build();
        addProduct(ShopCategory.OTHER, 21, new DefaultShopProduct(platform, platform, Resource.OPAL, 1));

        ItemStack wind = new ItemStack(Material.WIND_CHARGE, 8);
        addProduct(ShopCategory.OTHER, 22, new DefaultShopProduct(wind, wind, Resource.IRON, 20));

        addProduct(ShopCategory.OTHER, 23, new DefaultShopProduct(new ItemBuilder(Material.ENDER_PEARL).setName("§3Эндер-жемчуг").build(), new ItemBuilder(Material.ENDER_PEARL).setName("§3Эндер-жемчуг").build(), Resource.OPAL, 4));

        // Новые предметы для полетов
        addProduct(ShopCategory.OTHER, 24, new DefaultShopProduct(new ItemBuilder(Material.ELYTRA).setName("§dЭлитры").build(), new ItemBuilder(Material.ELYTRA).setName("§dЭлитры").build(), Resource.OPAL, 12));

        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, 3);
        addProduct(ShopCategory.OTHER, 25, new DefaultShopProduct(fireworks, fireworks, Resource.OPAL, 1));

        addProduct(ShopCategory.OTHER, 28, new DefaultShopProduct(
                createItem(Material.EGG, "§eЯйцо строителя"),
                createItem(Material.EGG, "§eЯйцо строителя"),
                Resource.IRON, 4
        ));
    }

    // Хелпер для создания неломающихся инструментов
    private static ItemStack createUnbreakable(Material mat, String name) {
        ItemStack item = new ItemBuilder(mat).setName(name).build();
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Хелпер для создания голов-зелий с текстурами
    private static ItemStack createPotionHead(String name, String base64Texture) {
        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .setName(name)
                .setLore("§7Нажмите ПКМ, чтобы", "§7мгновенно получить эффект!")
                .build();

        if (base64Texture != null && !base64Texture.isEmpty()) {
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            if (meta != null) {
                // Магия Paper: ставим текстуру по коду
                com.destroystokyo.paper.profile.PlayerProfile profile = org.bukkit.Bukkit.createProfile(java.util.UUID.randomUUID());
                profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", base64Texture));
                meta.setPlayerProfile(profile);
                head.setItemMeta(meta);
            }
        }
        return head;
    }

    private static void addProduct(ShopCategory category, int slot, ShopProduct product) {
        shopItems.get(category).put(slot, product);
    }

    public static void open(Player player, ShopCategory category) {
        playerCategories.put(player.getUniqueId(), category);

        // УБРАЛИ ChatUtil.parse, используем §
        Inventory inv = Bukkit.createInventory(player, 54, "§6§lМагазин: §e" + category.getName());

        ShopCategory[] categories = ShopCategory.values();
        int startSlot = 1;
        for (int i = 0; i < categories.length; i++) {
            ShopCategory cat = categories[i];
            inv.setItem(startSlot + i, cat.getIcon(cat == category));
        }

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, filler);
        }

        Map<Integer, ShopProduct> products = shopItems.get(category);
        if (products != null) {
            for (Map.Entry<Integer, ShopProduct> entry : products.entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().getIcon(player)); // <--- ТУТ ИЗМЕНЕНИЕ
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (!org.bukkit.ChatColor.stripColor(title).startsWith("Магазин:")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ShopCategory currentCategory = playerCategories.get(player.getUniqueId());
        if (currentCategory == null) return;

        int slot = event.getSlot();

        if (slot >= 1 && slot <= 7) {
            ShopCategory[] categories = ShopCategory.values();
            int catIndex = slot - 1;
            if (catIndex < categories.length) {
                ShopCategory newCategory = categories[catIndex];
                if (newCategory != currentCategory) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    open(player, newCategory);
                }
            }
            return;
        }

        Map<Integer, ShopProduct> products = shopItems.get(currentCategory);
        if (products != null && products.containsKey(slot)) {
            ShopProduct product = products.get(slot);
            product.processPurchase(player);
        }
    }
}