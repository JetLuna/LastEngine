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

    // --- ХРАНИЛИЩЕ ЗАРЯДОВ ПАУТИНЫ ---
    private final java.util.Map<java.util.UUID, Integer> cobwebCharges = new java.util.HashMap<>();

    public void addCobwebCharge(Player player) {
        cobwebCharges.put(player.getUniqueId(), getCobwebCharges(player) + 1);
    }

    public int getCobwebCharges(Player player) {
        return cobwebCharges.getOrDefault(player.getUniqueId(), 0);
    }
    // Таймер сканирования баз
    private org.bukkit.scheduler.BukkitTask baseScannerTask;
    // Таймеры ожидания перезахода (1 минута)
    private final java.util.Map<java.util.UUID, org.bukkit.scheduler.BukkitTask> disconnectTasks = new java.util.HashMap<>();

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

                    // Выдаем мечи, кирки и топоры с нужным уровнем
                    plugin.getEquipmentManager().giveRespawnEquipment(player);
                    // Надеваем броню и чаруем мечи на остроту
                    plugin.getEquipmentManager().updateEquipment(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
            }
        }

        // 5. ЗАПУСК СКАНЕРА БАЗ (Аура и Ловушки)
        baseScannerTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (GameTeam team : plugin.getTeamManager().getActiveTeams()) {
                    org.bukkit.Location baseLoc = team.getSpawnLocation();
                    if (baseLoc == null || baseLoc.getWorld() == null) continue;

                    java.util.List<Player> allies = new java.util.ArrayList<>();
                    java.util.List<Player> enemies = new java.util.ArrayList<>();

                    // Ищем игроков в радиусе 15 блоков от спавна команды
                    for (Player p : baseLoc.getWorld().getPlayers()) {
                        if (p.getGameMode() != GameMode.SURVIVAL) continue;

                        if (p.getLocation().distanceSquared(baseLoc) <= 225) { // 15 * 15 блоков
                            GameTeam pTeam = plugin.getTeamManager().getTeam(p);
                            if (pTeam == team) {
                                allies.add(p);
                            } else if (pTeam != null) {
                                enemies.add(p);
                            }
                        }
                    }

                    // --- ИСЦЕЛЯЮЩАЯ АУРА ---
                    if (team.hasHealPool()) {
                        for (Player ally : allies) {
                            // Выдаем Регенерацию (60 тиков = 3 сек, чтобы эффект не мигал, но и не висел долго после ухода)
                            ally.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.REGENERATION, 60, 0, false, false
                            ));
                        }
                    }

                    // --- СИСТЕМА ЛОВУШЕК ---
                    if (!enemies.isEmpty() && !team.getTraps().isEmpty()) {
                        // Берем самую первую ловушку из очереди и удаляем её
                        String trapType = team.getTraps().remove(0);

                        // 1. Оповещаем союзников (даже если они на центре карты)
                        for (java.util.UUID uuid : team.getPlayers()) {
                            Player ally = Bukkit.getPlayer(uuid);
                            if (ally != null) {
                                ally.sendTitle("§c§lЛОВУШКА СРАБОТАЛА!", "§7Враг проник на базу!", 5, 40, 5);
                                ally.playSound(ally.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1f, 1f);
                            }
                        }

                        // 2. Караем врагов на базе
                        for (Player enemy : enemies) {
                            if (trapType.equals("FATIGUE")) {
                                // Усталость (Майнинг фатиг) на 10 секунд
                                enemy.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                        org.bukkit.potion.PotionEffectType.MINING_FATIGUE, 200, 0
                                ));
                                enemy.sendMessage("§cВы наступили на ловушку! На вас наложена Усталость.");
                            } else if (trapType.equals("BLINDNESS")) {
                                // Слепота на 5 секунд
                                enemy.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                        org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 0
                                ));
                                enemy.sendMessage("§cВы наступили на ловушку! На вас наложена Слепота.");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Запускаем проверку каждую 1 секунду
    }

    @Override
    public void onDisable() {
        plugin.getGeneratorManager().stopGenerators();

        if (baseScannerTask != null) {
            baseScannerTask.cancel();
        }
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
        // --- БРОНЯ ИЗ ПАУТИНЫ ---
        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth > 0 && finalHealth <= 6.0) { // Если осталось 3 сердца или меньше
            int charges = getCobwebCharges(player);
            if (charges > 0) {
                cobwebCharges.put(player.getUniqueId(), charges - 1);
                player.sendMessage("§f🕸 Броня из паутины спасла вас! (Осталось зарядов: " + (charges - 1) + ")");
                player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1f, 1f);

                org.bukkit.Location loc = player.getLocation();
                // Генерируем куб 3х3 вокруг игрока
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 1; y++) { // Высота 2 блока. y=2 (крыша) не ставим, чтобы можно было выпрыгнуть
                        for (int z = -1; z <= 1; z++) {
                            // Пропускаем центр, где стоит сам игрок
                            if (x == 0 && z == 0) continue;

                            org.bukkit.block.Block b = loc.clone().add(x, y, z).getBlock();
                            // Ставим паутину ТОЛЬКО вместо блоков воздуха
                            if (b.getType().isAir()) {
                                b.setType(org.bukkit.Material.COBWEB);
                                plugin.getBlockManager().addBlock(b); // Заносим в базу, чтобы её можно было сломать мечом
                            }
                        }
                    }
                }
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

                // 1. Убили мечом или рукой
                if (byEntityEvent.getDamager() instanceof Player) {
                    killer = (Player) byEntityEvent.getDamager();
                }
                // 2. Убили стрелой, фаерболом или снежком
                else if (byEntityEvent.getDamager() instanceof org.bukkit.entity.Projectile) {
                    org.bukkit.entity.Projectile proj = (org.bukkit.entity.Projectile) byEntityEvent.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        killer = (Player) proj.getShooter();
                    }
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
    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        GameTeam team = plugin.getTeamManager().getTeam(player);

        if (team != null) {
            // Если кровати уже нет — мгновенная финальная смерть
            if (!team.hasBed()) {
                org.bukkit.Bukkit.broadcastMessage(team.getColor().getChatColor() + player.getName() + " §7покинул игру и был §c§lОКОНЧАТЕЛЬНО §7устранен.");
                team.getPlayers().remove(player.getUniqueId());
                plugin.getDeathManager().checkWinCondition();
                return;
            }

            org.bukkit.Bukkit.broadcastMessage("§eИгрок " + team.getColor().getChatColor() + player.getName() + " §eпокинул игру! У него есть §c1 минута §eна перезаход.");

            // Запускаем таймер на 60 секунд (1200 тиков)
            org.bukkit.scheduler.BukkitTask task = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    disconnectTasks.remove(player.getUniqueId());
                    org.bukkit.Bukkit.broadcastMessage("§cИгрок " + team.getColor().getChatColor() + player.getName() + " §cне вернулся и был исключен.");

                    // Удаляем игрока из команды
                    team.getPlayers().remove(player.getUniqueId());

                    // Проверяем, не остался ли только один игрок/команда на арене
                    plugin.getDeathManager().checkWinCondition();
                }
            }.runTaskLater(plugin, 1200L);

            disconnectTasks.put(player.getUniqueId(), task);
        }
    }
    @org.bukkit.event.EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();

        // Проверяем, есть ли игрок в списке "ждунов"
        if (disconnectTasks.containsKey(player.getUniqueId())) {
            // Отменяем удаление из игры! Он успел!
            disconnectTasks.get(player.getUniqueId()).cancel();
            disconnectTasks.remove(player.getUniqueId());

            GameTeam team = plugin.getTeamManager().getTeam(player);
            if (team != null) {
                org.bukkit.Bukkit.broadcastMessage("§aИгрок " + team.getColor().getChatColor() + player.getName() + " §aуспел вернуться в игру!");

                // Начинаем 10-секундный респавн
                player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                player.teleport(team.getSpawnLocation().clone().add(0, 10, 0)); // Летает над базой
                player.getInventory().clear();

                new org.bukkit.scheduler.BukkitRunnable() {
                    int time = 10;

                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            cancel();
                            return;
                        }

                        if (time <= 0) {
                            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                            player.teleport(team.getSpawnLocation());
                            player.setHealth(20.0);
                            player.setFoodLevel(20);

                            // Выдаем мечи, кирки и топоры с нужным уровнем
                            plugin.getEquipmentManager().giveRespawnEquipment(player);
                            // Надеваем броню и чаруем мечи на остроту
                            plugin.getEquipmentManager().updateEquipment(player);

                            player.sendTitle("§a§lВОЗРОЖДЕНИЕ", "§7Вы вернулись в бой!", 0, 20, 0);
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            cancel();
                            return;
                        }

                        player.sendTitle("§cПерезаход...", "§7Возрождение через §e" + time + " §7сек.", 0, 25, 0);
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
                        time--;
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        } else {
            // Если зашел просто левый зритель (или тот, кто не успел)
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);

            // Если есть лобби ожидания, можно кинуть туда, или на центр арены
            org.bukkit.Location waitingLobby = plugin.getConfig().getLocation("locations.waiting-lobby");
            if (waitingLobby != null) {
                player.teleport(waitingLobby);
            }
        }
    }
}