package net.jetluna.api.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

@AllArgsConstructor
@Getter
public enum ParticleEffect {

    HEART("Сердца", Material.RED_TULIP, Particle.HEART, 5000),
    FLAME("Огненный дух", Material.BLAZE_POWDER, Particle.FLAME, 3000),
    MAGIC("Магическая аура", Material.ENCHANTED_BOOK, Particle.WITCH, 4500),
    NOTES("Мелодия", Material.NOTE_BLOCK, Particle.NOTE, 6000),
    SNOW("Снежная буря", Material.SNOWBALL, Particle.SNOWFLAKE, 2000),
    VILLAGER("Изумрудный блеск", Material.EMERALD, Particle.HAPPY_VILLAGER, 7500),

    WINGS("Крылья Ангела", Material.FEATHER, Particle.FIREWORK, 15000),
    DEVIL("Крылья Дьявола", Material.PHANTOM_MEMBRANE, Particle.LARGE_SMOKE, 15500),
    RINGS("Огненные кольца", Material.MAGMA_CREAM, Particle.FLAME, 8000),

    ENDER("Аура Эндермена", Material.ENDER_EYE, Particle.PORTAL, 4000),
    ENCHANTED("Древние руны", Material.ENCHANTING_TABLE, Particle.ENCHANT, 5000),
    BLOOD("Аура Берсерка", Material.REDSTONE_BLOCK, Particle.DAMAGE_INDICATOR, 7000),
    HALO("Осколки надежды", Material.GHAST_TEAR, Particle.FIREWORK, 4000),

    FROST("Владыка холода", Material.PACKED_ICE, Particle.SNOWFLAKE, 6500),
    RAIN("Дождевая туча", Material.WATER_BUCKET, Particle.DRIPPING_WATER, 4000),
    RODS("Магические сферы", Material.BLAZE_ROD, Particle.ENCHANT, 7500),

    HERO("Плащ Героя", Material.RED_BANNER, Particle.FLAME, 10000),
    SANTA("Шапка Санты", Material.RED_WOOL, Particle.FLAME, 5000),
    INFERNO("Инферно", Material.MAGMA_CREAM, Particle.FLAME, 8500),
    CUBE("Огненный Куб", Material.FIRE_CHARGE, Particle.FLAME, 9000),
    ECLIPSE("Затмение", Material.WITHER_SKELETON_SKULL, Particle.LARGE_SMOKE, 16000);

    private final String displayName;
    private final Material icon;
    private final Particle particle;
    private final int price;
}