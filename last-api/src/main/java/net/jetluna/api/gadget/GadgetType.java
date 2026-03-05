package net.jetluna.api.gadget;

import org.bukkit.Material;

public enum GadgetType {
    BLACK_HOLE(Material.BLACK_TERRACOTTA, "Черная дыра", 1500),
    TRAMPOLINE(Material.SLIME_BLOCK, "Батут", 1500),
    DISCO_BALL(Material.BEACON, "Диско-шар", 1500),
    BLIZZARD_BLASTER(Material.BLUE_ICE, "Вьюга", 1500),
    ROCKET(Material.FIREWORK_ROCKET, "Ракета", 1500),
    PORTAL_GUN(Material.END_PORTAL_FRAME, "Портальная пушка", 1500),
    TSUNAMI(Material.WATER_BUCKET, "Цунами", 1500),
    FLESH_HOOK(Material.TRIPWIRE_HOOK, "Мясной крюк", 1500),
    SMASH_DOWN(Material.DIRT, "Удар по земле", 1500),
    TNT(Material.TNT, "Динамит", 1500),
    PAINTBALL_GUN(Material.DIAMOND_HORSE_ARMOR, "Пейнтбол", 1500),
    COLOR_BOMB(Material.CYAN_WOOL, "Цветная бомба", 1500),
    MELON_THROWER(Material.MELON, "Арбузомет", 1500),
    FUN_GUN(Material.BLAZE_ROD, "Веселая пушка", 1500),
    THOR_HAMMER(Material.IRON_AXE, "Молот Тора", 1500),
    EXPLOSIVE_SHEEP(Material.WHITE_WOOL, "Взрывная овца", 1500),
    CHICKENATOR(Material.COOKED_CHICKEN, "Куриная пушка", 1500),
    BAT_BLASTER(Material.BAT_SPAWN_EGG, "Бластер мышей", 1500),
    ENDER_RIDER(Material.ENDER_PEARL, "Эндер-наездник", 1500),
    GRAPPLING_HOOK(Material.FISHING_ROD, "Крюк-кошка", 1500),
    JETPACK(Material.FIRE_CHARGE, "Джетпак", 1500),
    FLAME_WALKER(Material.BLAZE_POWDER, "Огненный след", 1500),
    LASER_POINTER(Material.REDSTONE_TORCH, "Лазер", 1500),
    FAKE_CREEPER(Material.CREEPER_HEAD, "Фейковый крипер", 1500),
    ICE_BOMB(Material.ICE, "Ледяная бомба", 1500),
    FIREBALL(Material.FIRE_CHARGE, "Шар Гаста", 1500),
    ZEUS_STRIKE(Material.LIGHTNING_ROD, "Гнев Зевса", 1500),
    LOVE_AURA(Material.POPPY, "Аура Любви", 1500);

    private final Material icon;
    private final String displayName;
    private final int price;

    GadgetType(Material icon, String displayName, int price) {
        this.icon = icon;
        this.displayName = displayName;
        this.price = price;
    }

    public Material getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public int getPrice() { return price; }
}