package net.jetluna.api.pet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@AllArgsConstructor
@Getter
public enum PetType {

    DOG(Material.BONE, EntityType.WOLF, Material.BONE, 5000),
    CAT(Material.TROPICAL_FISH, EntityType.CAT, Material.TROPICAL_FISH, 5000),
    BUNNY(Material.GOLDEN_CARROT, EntityType.RABBIT, Material.CARROT, 6000),
    PIGGY(Material.PORKCHOP, EntityType.PIG, Material.PORKCHOP, 4000),
    CHICK(Material.EGG, EntityType.CHICKEN, Material.EGG, 3000),
    COW(Material.MILK_BUCKET, EntityType.COW, Material.LEATHER, 4000),
    MOOSHROOM(Material.RED_MUSHROOM, EntityType.MOOSHROOM, Material.RED_MUSHROOM, 5000),
    SHEEP(Material.WHITE_WOOL, EntityType.SHEEP, Material.WHITE_WOOL, 4500),
    SNOWMAN(Material.SNOWBALL, EntityType.SNOW_GOLEM, Material.SNOWBALL, 6000),
    GOLEM(Material.IRON_INGOT, EntityType.IRON_GOLEM, Material.IRON_NUGGET, 8000),
    LLAMA(Material.LEAD, EntityType.LLAMA, Material.WHITE_CARPET, 6500),
    PARROT(Material.COOKIE, EntityType.PARROT, Material.FEATHER, 7000),
    BAT(Material.COAL, EntityType.BAT, Material.COAL, 5000),
    BEAR(Material.COD, EntityType.POLAR_BEAR, Material.SALMON, 7500);

    private final Material icon;
    private final EntityType entityType;
    private final Material dropItem;
    private final int price;
}