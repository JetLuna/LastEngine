package net.jetluna.api.pet; // Исправлен пакет

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@AllArgsConstructor
@Getter
public enum PetType {

    DOG("Собака", Material.BONE, EntityType.WOLF, Material.BONE, 5000),
    CAT("Котик", Material.TROPICAL_FISH, EntityType.CAT, Material.TROPICAL_FISH, 5000),
    BUNNY("Пасхальный Заяц", Material.GOLDEN_CARROT, EntityType.RABBIT, Material.CARROT, 6000),
    PIGGY("Свинка", Material.PORKCHOP, EntityType.PIG, Material.PORKCHOP, 4000),
    CHICK("Цыпленок", Material.EGG, EntityType.CHICKEN, Material.EGG, 3000),
    COW("Коровка", Material.MILK_BUCKET, EntityType.COW, Material.LEATHER, 4000),
    MOOSHROOM("Грибная корова", Material.RED_MUSHROOM, EntityType.MOOSHROOM, Material.RED_MUSHROOM, 5000), // Исправлено для 1.21
    SHEEP("Овечка", Material.WHITE_WOOL, EntityType.SHEEP, Material.WHITE_WOOL, 4500),
    SNOWMAN("Снеговик", Material.SNOWBALL, EntityType.SNOW_GOLEM, Material.SNOWBALL, 6000),
    GOLEM("Железный Голем", Material.IRON_INGOT, EntityType.IRON_GOLEM, Material.IRON_NUGGET, 8000),
    LLAMA("Лама", Material.LEAD, EntityType.LLAMA, Material.WHITE_CARPET, 6500),
    PARROT("Попугай", Material.COOKIE, EntityType.PARROT, Material.FEATHER, 7000),
    BAT("Летучая мышь", Material.COAL, EntityType.BAT, Material.COAL, 5000),
    BEAR("Белый Медведь", Material.COD, EntityType.POLAR_BEAR, Material.SALMON, 7500);

    private final String displayName;
    private final Material icon;
    private final EntityType entityType;
    private final Material dropItem;
    private final int price;
}