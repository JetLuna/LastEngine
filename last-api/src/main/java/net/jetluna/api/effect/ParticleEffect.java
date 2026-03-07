package net.jetluna.api.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

@AllArgsConstructor
@Getter
public enum ParticleEffect {

    HEART(Material.RED_TULIP, Particle.HEART, 5000),
    FLAME(Material.BLAZE_POWDER, Particle.FLAME, 3000),
    MAGIC(Material.ENCHANTED_BOOK, Particle.WITCH, 4500),
    NOTES(Material.NOTE_BLOCK, Particle.NOTE, 6000),
    SNOW(Material.SNOWBALL, Particle.SNOWFLAKE, 2000),
    VILLAGER(Material.EMERALD, Particle.HAPPY_VILLAGER, 7500),

    WINGS(Material.FEATHER, Particle.FIREWORK, 15000),
    DEVIL(Material.PHANTOM_MEMBRANE, Particle.LARGE_SMOKE, 15500),
    RINGS(Material.MAGMA_CREAM, Particle.FLAME, 8000),

    ENDER(Material.ENDER_EYE, Particle.PORTAL, 4000),
    ENCHANTED(Material.ENCHANTING_TABLE, Particle.ENCHANT, 5000),
    BLOOD(Material.REDSTONE_BLOCK, Particle.DAMAGE_INDICATOR, 7000),
    HALO(Material.GHAST_TEAR, Particle.FIREWORK, 4000),

    FROST(Material.PACKED_ICE, Particle.SNOWFLAKE, 6500),
    RAIN(Material.WATER_BUCKET, Particle.DRIPPING_WATER, 4000),
    RODS(Material.BLAZE_ROD, Particle.ENCHANT, 7500),

    HERO(Material.RED_BANNER, Particle.FLAME, 10000),
    SANTA(Material.RED_WOOL, Particle.FLAME, 5000),
    INFERNO(Material.MAGMA_CREAM, Particle.FLAME, 8500),
    CUBE(Material.FIRE_CHARGE, Particle.FLAME, 9000),
    ECLIPSE(Material.WITHER_SKELETON_SKULL, Particle.LARGE_SMOKE, 16000);

    private final Material icon;
    private final Particle particle;
    private final int price;
}