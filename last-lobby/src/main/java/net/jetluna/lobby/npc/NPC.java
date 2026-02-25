package net.jetluna.lobby.npc;

import net.jetluna.api.lang.LanguageManager;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NPC implements Listener {

    private final String id;
    private final Location location;
    private final String name;
    private final Skin skin;
    private final List<String> hologramLines;

    // Технические сущности
    private Interaction clickBox; // Хитбокс (1.19.4+)
    private List<ArmorStand> holograms = new ArrayList<>();

    public NPC(String id, Location location, String name, Skin skin, List<String> hologramLines) {
        this.id = id;
        this.location = location;
        this.name = name;
        this.skin = skin;
        this.hologramLines = hologramLines;
    }

    public void spawn() {
        // 1. СПАВНИМ ВИЗУАЛ (ПОКА БУДЕТ ЗАГЛУШКА)
        // Чтобы спавнить Скин Игрока без NMS, нужен ProtocolLib.
        // Без ProtocolLib и без NMS это невозможно.
        // Я сделаю пока красивый АрморСтенд с флагом/предметом,
        // А ты потом решишь: или добавляем ProtocolLib, или лезем в дебри NMS.

        // ВРЕМЕННОЕ РЕШЕНИЕ: Спавним Вилладжера (Жителя) - они живые и на них можно кликать.
        // Или Скелета.
        // Давай сделаем "Бедного Студента" - Скелета в броне :)

        // ШУТКА. Давай сделаем НПС на базе CitizensAPI, который ты подключил в прошлом шаге.
        // Ты же САМ его подключил в pom.xml!
        // Если ты подключил Citizens API, мы можем управлять ими через код, не касаясь команд /npc.
    }
}