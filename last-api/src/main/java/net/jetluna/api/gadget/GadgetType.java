package net.jetluna.api.gadget;

import org.bukkit.Material;

public enum GadgetType {
    BLACK_HOLE(Material.BLACK_TERRACOTTA, 1500),
    TRAMPOLINE(Material.SLIME_BLOCK, 1500),
    DISCO_BALL(Material.BEACON, 1500),
    BLIZZARD_BLASTER(Material.BLUE_ICE, 1500),
    ROCKET(Material.FIREWORK_ROCKET, 1500),
    PORTAL_GUN(Material.END_PORTAL_FRAME, 1500),
    TSUNAMI(Material.WATER_BUCKET, 1500),
    FLESH_HOOK(Material.TRIPWIRE_HOOK, 1500),
    SMASH_DOWN(Material.DIRT, 1500),
    TNT(Material.TNT, 1500),
    PAINTBALL_GUN(Material.DIAMOND_HORSE_ARMOR, 1500),
    COLOR_BOMB(Material.CYAN_WOOL, 1500),
    MELON_THROWER(Material.MELON, 1500),
    FUN_GUN(Material.BLAZE_ROD, 1500),
    THOR_HAMMER(Material.IRON_AXE, 1500),
    EXPLOSIVE_SHEEP(Material.WHITE_WOOL, 1500),
    CHICKENATOR(Material.COOKED_CHICKEN, 1500),
    BAT_BLASTER(Material.BAT_SPAWN_EGG, 1500),
    ENDER_RIDER(Material.ENDER_PEARL, 1500),
    GRAPPLING_HOOK(Material.FISHING_ROD, 1500),
    JETPACK(Material.FIRE_CHARGE, 1500),
    FLAME_WALKER(Material.BLAZE_POWDER, 1500),
    LASER_POINTER(Material.REDSTONE_TORCH, 1500),
    FAKE_CREEPER(Material.CREEPER_HEAD, 1500),
    ICE_BOMB(Material.ICE, 1500),
    FIREBALL(Material.FIRE_CHARGE, 1500),
    ZEUS_STRIKE(Material.LIGHTNING_ROD, 1500),
    LOVE_AURA(Material.POPPY, 1500);

    private final Material icon;
    private final int price;

    GadgetType(Material icon, int price) {
        this.icon = icon;
        this.price = price;
    }

    public Material getIcon() { return icon; }
    public int getPrice() { return price; }
}