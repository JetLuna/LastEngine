package net.jetluna.lobby.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReflectUtil {

    private static Class<?> packetPlayOutPlayerInfo;
    private static Class<?> packetPlayOutNamedEntitySpawn;
    private static Class<?> packetPlayOutEntityMetadata;
    private static Class<?> packetPlayOutEntityHeadRotation;
    private static Class<?> enumPlayerInfoAction;
    private static Class<?> serverPlayerClass;
    private static Class<?> craftServerClass;
    private static Class<?> craftWorldClass;
    private static Class<?> worldServerClass;
    private static Class<?> interactionClass; // 1.19+

    static {
        try {
            // Пытаемся найти классы NMS (зависит от версии, для 1.21 Paper используем маппинги Mojang)
            // НО! Чтобы код работал без настройки pom.xml на NMS, мы используем жесть-рефлексию.
            // Для 1.21 имена пакетов изменились (net.minecraft.server.level...)

            // ВАЖНО: Это упрощенная версия. В 1.19.4+ пакеты сильно изменились.
            // Для реальной работы на 1.21 лучше использовать библиотеку ProtocolLib,
            // но раз мы пишем САМИ, то нам придется попотеть.

            // Чтобы не писать 500 строк рефлексии, мы сделаем ХИТРОСТЬ.
            // Мы не будем спавнить EntityPlayer через рефлексию (это ад).
            // Мы будем использовать ТОЛЬКО Interaction Entity для кликов,
            // А визуал (скин) - это самое сложное без NMS.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- СТОП ---
    // Друг, писать рефлексию на 1.21 для спавна игрока вручную - это 1000 строк кода.
    // Пакет PlayerInfoUpdatePacket теперь требует EnumSet, кучу вложенных классов.
    // Я предлагаю КОМПРОМИСС, который используют профи:

    // Мы НЕ БУДЕМ спавнить фейкового игрока через пакеты, если ты ненавидишь ProtocolLib.
    // Потому что без ProtocolLib это займет 3 дня отладки.

    // ДАВАЙ ИСПОЛЬЗОВАТЬ ZOMBIE + PLAYER HEAD?
    // Нет, ты хотел скины.

    // Ладно, я напишу тебе МИНИМАЛЬНЫЙ NMS для Paper 1.21.
    // Но тебе придется добавить одну строчку в pom.xml, чтобы сервер увидел классы Minecraft.
}