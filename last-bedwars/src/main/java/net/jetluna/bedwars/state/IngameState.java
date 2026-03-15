package net.jetluna.bedwars.state;

import net.jetluna.api.util.ChatUtil;
import net.jetluna.bedwars.BedWarsPlugin;
import net.jetluna.bedwars.team.GameTeam;
import net.jetluna.bedwars.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class IngameState extends GameState {

    public IngameState(BedWarsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getLogger().info("Статус игры: В ИГРЕ (Ingame)");

        // 1. Спавним NPC (Торговцев и Улучшения) там, где нашел сканер
        for (org.bukkit.Location loc : plugin.getArenaScanner().getShopNpcLocations()) {
            plugin.getNpcManager().spawnShopNpc(loc);
        }
        for (org.bukkit.Location loc : plugin.getArenaScanner().getUpgradeNpcLocations()) {
            plugin.getNpcManager().spawnUpgradeNpc(loc);
        }

        // 2. Регистрируем генераторы ресурсов
        plugin.getGeneratorManager().clearGenerators(); // Очищаем старые на всякий случай

        // Генераторы на базах команд (Железо)
        for (GameTeam team : plugin.getTeamManager().getActiveTeams()) {
            if (team.getGeneratorLocation() != null) {
                plugin.getGeneratorManager().addGenerator(new net.jetluna.bedwars.resource.ResourceGenerator(team.getGeneratorLocation(), net.jetluna.bedwars.resource.Resource.IRON));
            }
        }

        // Генераторы алмазов (с защитой от дубликатов)
        java.util.Set<org.bukkit.Location> uniqueDiamonds = new java.util.HashSet<>(plugin.getArenaScanner().getDiamondGenerators());
        for (org.bukkit.Location loc : uniqueDiamonds) {
            plugin.getGeneratorManager().addGenerator(new net.jetluna.bedwars.resource.ResourceGenerator(loc, net.jetluna.bedwars.resource.Resource.DIAMOND));
        }

        // Генераторы изумрудов/опалов (с защитой от дубликатов)
        java.util.Set<org.bukkit.Location> uniqueOpals = new java.util.HashSet<>(plugin.getArenaScanner().getEmeraldGenerators());
        for (org.bukkit.Location loc : uniqueOpals) {
            plugin.getGeneratorManager().addGenerator(new net.jetluna.bedwars.resource.ResourceGenerator(loc, net.jetluna.bedwars.resource.Resource.OPAL));
        }

        // 3. Запускаем спавн ресурсов (1.0 = обычная скорость)
        plugin.getGeneratorManager().startGenerators(1.0);

        // 4. Раскидываем игроков по базам и выдаем броню
        for (GameTeam team : plugin.getTeamManager().getActiveTeams()) {
            for (java.util.UUID uuid : team.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.teleport(team.getSpawnLocation());
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.setHealth(20.0);
                    player.setFoodLevel(20);

                    plugin.getEquipmentManager().updateEquipment(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        plugin.getGeneratorManager().stopGenerators();
    }

    @Override
    public String getName() {
        return "INGAME";
    }

    // --- ЛОГИКА БЛОКОВ ---
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL) {
            plugin.getBlockManager().addBlock(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

// 1. ПРОВЕРКА КРОВАТИ
        if (block.getType().name().endsWith("_BED")) {
            TeamColor bedColor = TeamColor.getByBed(block.getType());
            GameTeam bedTeam = plugin.getTeamManager().getTeam(bedColor);
            GameTeam playerTeam = plugin.getTeamManager().getTeam(player);

            if (bedTeam != null) {
                if (bedTeam == playerTeam) {
                    ChatUtil.sendMessage(player, "§cВы не можете сломать свою кровать!");
                    event.setCancelled(true);
                    return;
                }

                // --- НОВЫЙ ФИКС: Если кровать УЖЕ сломана (событие вызвано второй половинкой кровати) - игнорируем! ---
                if (!bedTeam.hasBed()) {
                    return;
                }
                // -------------------------------------------------------------------------------------------------

                // Ломаем кровать
                event.setDropItems(false);
                bedTeam.setHasBed(false);

                // --- НОВАЯ ЭКОНОМИКА ---
                plugin.getEconomyManager().addPoints(player, 3);
                player.sendMessage("§b+ 3 поинта §7за разрушение кровати!");

                // (И не забудь оставить добавление в стату скорборда)
                plugin.getScoreboardManager().addBrokenBed(player);

                // Эпичный анонс
                Bukkit.broadcast(ChatUtil.parse(""));
                Bukkit.broadcast(ChatUtil.parse("§f§lКРОВАТЬ СЛОМАНА!"));
                Bukkit.broadcast(ChatUtil.parse("§7Кровать " + bedTeam.getColor().getChatColor() + bedTeam.getColor().getName() + " §7команды была разрушена игроком " + playerTeam.getColor().getChatColor() + player.getName() + "§7!"));
                Bukkit.broadcast(ChatUtil.parse(""));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                }

                plugin.getDeathManager().checkWinCondition(); // Проверяем, не убил ли он последнюю команду
                return;
            }
        }

        // 2. ЗАЩИТА КАРТЫ
        if (player.getGameMode() == GameMode.SURVIVAL) {
            if (!plugin.getBlockManager().isPlayerPlaced(block)) {
                ChatUtil.sendMessage(player, "§cВы можете ломать только те блоки, которые поставили игроки!");
                event.setCancelled(true);
            } else {
                plugin.getBlockManager().removeBlock(block); // Убираем из кэша
            }
        }
    }

    // --- ЛОГИКА УРОНА ---
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        // --- ЗАЩИТА ОТ УРОНА ПРИ ПАДЕНИИ (После Фаербола) ---
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (player.hasMetadata("fireball_jump")) {
                event.setCancelled(true); // Отменяем урон
                player.removeMetadata("fireball_jump", plugin); // Снимаем метку
                return;
            }
        }

        // --- ФАЕРБОЛ: Кастомное откидывание ---
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
            if (byEntityEvent.getDamager() instanceof org.bukkit.entity.Fireball) {
                event.setDamage(0.0); // Урон отключаем

                // Вычисляем вектор откидывания
                org.bukkit.Location explosionLoc = byEntityEvent.getDamager().getLocation();
                org.bukkit.Location playerLoc = player.getLocation();

                org.bukkit.util.Vector knockback = playerLoc.toVector().subtract(explosionLoc.toVector());

                if (knockback.lengthSquared() == 0) {
                    knockback = new org.bukkit.util.Vector(0, 1, 0);
                } else {
                    knockback.normalize();
                }

                // НОВЫЕ НАСТРОЙКИ СИЛЫ (Ниже, но дальше):
                double horizontalForce = 1.5; // Было 1.7. Теперь толкает вперед как из пушки
                double verticalForce = 0.3;   // Было 1.0. Меньше реагирует на угол

                knockback.setX(knockback.getX() * horizontalForce);
                // Базовый подкид уменьшен с +0.6 до +0.4, чтобы летел полого
                knockback.setY((knockback.getY() * verticalForce) + 0.4);
                knockback.setZ(knockback.getZ() * horizontalForce);

                player.setVelocity(player.getVelocity().add(knockback));

                // Вешаем метку бессмертия от падения
                player.setMetadata("fireball_jump", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

                // Страховка: если игрок упал в воду (урона нет), метка снимается через 5 секунд (100 тиков),
                // чтобы он не сохранил бессмертие навсегда
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.hasMetadata("fireball_jump")) {
                        player.removeMetadata("fireball_jump", plugin);
                    }
                }, 100L);
            }
        }

        // Если урон смертельный — перехватываем
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            player.setHealth(20.0);
            player.setFireTicks(0);

            Player killer = null;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent byEntityEvent = (EntityDamageByEntityEvent) event;
                if (byEntityEvent.getDamager() instanceof Player) {
                    killer = (Player) byEntityEvent.getDamager();
                }
            }

            // Передаем в менеджер смертей
            plugin.getDeathManager().handleDeath(player, killer);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true); // Отключаем голод
    }

    // --- ЛОГИКА ВЗРЫВОВ (TNT и Фаерболы) ---
    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        java.util.Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            // Если блок НЕ поставлен игроком ИЛИ это наше бронестекло - защищаем его от взрыва
            if (!plugin.getBlockManager().isPlayerPlaced(block) || block.getType() == org.bukkit.Material.TINTED_GLASS) {
                it.remove();
            } else {
                // Если блок взорвался, удаляем его из кэша
                plugin.getBlockManager().removeBlock(block);
            }
        }
    }

    // --- ПАДЕНИЕ В БЕЗДНУ ---
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();

        // Проверяем только живых игроков (наблюдатели могут летать ниже карты)
        if (player.getGameMode() == GameMode.SURVIVAL) {
            // Как только игрок пересекает высоту 10...
            if (event.getTo().getY() <= 10.0) {
                // Передаем его в наш менеджер смерти
                // (Пока передаем null вместо убийцы. Логику "кто последний ударил перед падением" можно дописать позже)
                plugin.getDeathManager().handleDeath(player, null);
            }
        }
    }
}