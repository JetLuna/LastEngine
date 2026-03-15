package net.jetluna.bedwars.item;

import net.jetluna.bedwars.BedWarsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomItemListener implements Listener {

    private final BedWarsPlugin plugin;

    public CustomItemListener(BedWarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        String name = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "";

// --- 1. ФАЕРБОЛ ---
        if (item.getType() == Material.FIRE_CHARGE) {
            event.setCancelled(true);
            consumeItem(player, item);
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setYield(2.0F); // Усилили взрыв (было 2.5)
            fireball.setIsIncendiary(false); // ОТКЛЮЧАЕМ ОГОНЬ
            fireball.setVelocity(player.getLocation().getDirection().multiply(1.5)); // Ускоряем полет шара
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
            return;
        }

        // --- 2. СЕЙВ-ПЛАТФОРМА (СЛИЗЬ) ---
        if (item.getType() == Material.SLIME_BALL && name.contains("Сейв-Платформа")) {
            event.setCancelled(true);
            consumeItem(player, item);
            createSlimePlatform(player);
            return;
        }

        // --- 3. ЗЕЛЬЯ (ГОЛОВЫ) ---
        if (item.getType() == Material.PLAYER_HEAD && name.contains("Зелье")) {
            event.setCancelled(true);
            consumeItem(player, item);
            applyPotionEffect(player, name);
            return;
        }
    }

    // --- 4. АВТОВЗРЫВ ДИНАМИТА ---
    // В Бедварсе динамит активируется сразу при установке
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() == Material.TNT) {
            event.setCancelled(true);
            consumeItem(player, event.getItemInHand()); // Забираем 1 TNT из руки

            // Спавним уже зажженный динамит на месте блока
            TNTPrimed tnt = block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
            tnt.setFuseTicks(60); // 3 секунды до взрыва
            player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        }
    }

    // Вспомогательный метод для траты 1 предмета из стака
    private void consumeItem(Player player, ItemStack item) {
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // Вспомогательный метод для выдачи зелий по названию головы
    private void applyPotionEffect(Player player, String name) {
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 1f, 1f);

        if (name.contains("Скорости I")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0)); // 1 мин
        } else if (name.contains("Скорости II")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1)); // 30 сек
        } else if (name.contains("Скорости IV")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 3)); // 10 сек
        } else if (name.contains("Прыгучести III")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 30, 2)); // 30 сек
        } else if (name.contains("Регенерации II")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 15, 1)); // 15 сек
        } else if (name.contains("Левитации")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 1)); // 5 сек
        }
    }

    // Логика спавна сейв-платформы
    private void createSlimePlatform(Player player) {
        Location loc = player.getLocation().subtract(0, 4, 0); // На 4 блока ниже игрока
        player.playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1f, 1f);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = loc.clone().add(x, 0, z).getBlock();
                if (b.getType() == Material.AIR) {
                    b.setType(Material.SLIME_BLOCK);

                    // Добавляем в BlockManager, чтобы платформу можно было сломать
                    plugin.getBlockManager().addBlock(b);

                    // Удаляем слизь автоматически через 10 секунд (чтобы карта не засорялась)
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (b.getType() == Material.SLIME_BLOCK) {
                                b.setType(Material.AIR);
                                plugin.getBlockManager().removeBlock(b);
                            }
                        }
                    }.runTaskLater(plugin, 20L * 10);
                }
            }
        }
    }

    // 1. Запрещаем яйцу спавнить куриц при разбивании
    @org.bukkit.event.EventHandler
    public void onEggHatch(org.bukkit.event.player.PlayerEggThrowEvent event) {
        event.setHatching(false);
    }

    // 2. Главная логика Яйца строителя
    @org.bukkit.event.EventHandler
    public void onBridgeEggLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Egg)) return;
        org.bukkit.entity.Egg egg = (org.bukkit.entity.Egg) event.getEntity();

        if (!(egg.getShooter() instanceof org.bukkit.entity.Player)) return;
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) egg.getShooter();

        // Проверяем предмет в руке (ищем наше Яйцо строителя)
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != org.bukkit.Material.EGG) {
            item = player.getInventory().getItemInOffHand();
        }

        // Если это обычное яйцо (без имени), ничего не делаем
        if (item.getType() != org.bukkit.Material.EGG || !item.hasItemMeta() || !item.getItemMeta().getDisplayName().contains("Яйцо строителя")) {
            return;
        }

        // Получаем цвет команды для моста
        net.jetluna.bedwars.team.GameTeam team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;
        org.bukkit.Material woolMat = team.getColor().getWool();

        // Запускаем умный трекинг полета яйца
        new org.bukkit.scheduler.BukkitRunnable() {
            org.bukkit.Location lastLoc = egg.getLocation();
            int ticks = 0;

            @Override
            public void run() {
                // Если яйцо разбилось или летит дольше 3 секунд (защита от лагов)
                if (egg.isDead() || !egg.isValid() || ticks > 60) {
                    cancel();
                    return;
                }

                org.bukkit.Location currentLoc = egg.getLocation();

                // СУПЕР-ФИЧА: Интерполяция!
                // Вычисляем линию между прошлым тиком и текущим, чтобы застроить всё без дырок
                org.bukkit.util.Vector dir = currentLoc.toVector().subtract(lastLoc.toVector());
                double distance = dir.length();
                dir.normalize();

                for (double d = 0; d <= distance; d += 0.5) {
                    // Берем координаты и опускаем на 2 блока вниз, чтобы мост был под ногами
                    org.bukkit.block.Block b = lastLoc.clone().add(dir.clone().multiply(d)).subtract(0, 2, 0).getBlock();

                    if (b.getType() == org.bukkit.Material.AIR) {
                        b.setType(woolMat);
                        plugin.getBlockManager().addBlock(b); // Добавляем в менеджер!
                        b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_WOOL_PLACE, 0.5f, 1f);
                    }
                }

                lastLoc = currentLoc;
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L); // Обновляем каждый тик (20 раз в секунду)
    }
}