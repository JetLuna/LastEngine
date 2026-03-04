package net.jetluna.api.effect;

import net.jetluna.api.LastApi;
import net.jetluna.api.stats.PlayerStats;
import net.jetluna.api.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EffectManager {

    private static int ticks = 0;

    private static final boolean[][] WINGS_SHAPE = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {false, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, false, false},
            {false, false, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false},
            {false, false, false, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false},
            {false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false, false, false},
            {false, false, false, false, true, true, true, true, false, true, true, true, true, false, false, false, false, false},
            {false, false, false, false, false, true, true, true, false, true, true, true, false, false, false, false, false, false},
            {false, false, false, false, false, true, true, false, false, false, true, true, false, false, false, false, false, false},
            {false, false, false, false, true, true, false, false, false, false, false, true, true, false, false, false, false, false}
    };

    // Матрица дьявольских крыльев (более широкие и рваные края)
    private static final boolean[][] DEVIL_WINGS_SHAPE = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            {true,  true,  false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,  true },
            {false, true,  true,  true,  false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,  true,  true,  false},
            {false, false, true,  true,  true,  true,  false, false, false, false, false, false, false, false, false, false, true,  true,  true,  true,  false, false},
            {false, false, false, true,  true,  true,  true,  true,  false, false, false, false, false, false, true,  true,  true,  true,  true,  false, false, false},
            {false, false, false, false, true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false, false, false, false},
            {false, false, false, false, false, false, true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false, false, false, false, false, false},
            {false, false, false, false, false, true,  true,  true,  false, false, false, false, false, false, true,  true,  true,  false, false, false, false, false},
            {false, false, false, false, true,  true,  false, false, false, false, false, false, false, false, false, false, true,  true,  false, false, false, false},
            {false, false, false, true,  false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,  false, false, false}
    };

    private static final boolean[][] CAPE_SHAPE = {
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true},
            {true, true, true, true, true}
    };

    public static void startTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(LastApi.getInstance(), () -> {
            ticks++;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isInvisible() || !player.isOnline()) continue;

                PlayerStats stats = StatsManager.getStats(player);
                if (stats == null || stats.getActiveEffect().isEmpty()) continue;

                ParticleEffect effect;
                try {
                    effect = ParticleEffect.valueOf(stats.getActiveEffect());
                } catch (IllegalArgumentException e) {
                    stats.setActiveEffect("");
                    continue;
                }

                try {
                    Location loc = player.getLocation();

                    switch (effect) {
                        case WINGS:
                            drawWings(player, loc, effect.getParticle());
                            break;

                        case DEVIL:
                            drawDevilWings(player, loc);
                            break;

                        case ECLIPSE:
                            // ЗАТМЕНИЕ: Черное Солнце с огненной короной и кровавыми каплями
                            Vector eclipseBack = loc.getDirection().setY(0).normalize().multiply(-0.8);
                            Location sunLoc = loc.clone().add(eclipseBack).add(0, 2.2, 0);

                            // 1. Черное ядро (Сфера дыма)
                            for (int i = 0; i < 15; i++) {
                                Vector randomPos = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).normalize().multiply(Math.random() * 0.4);
                                player.getWorld().spawnParticle(effect.getParticle(), sunLoc.clone().add(randomPos), 1, 0,0,0,0);
                            }

                            // 2. Огненная корона
                            for (int i = 0; i < 10; i++) {
                                Vector randomPos = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).normalize().multiply(0.55);
                                player.getWorld().spawnParticle(Particle.FLAME, sunLoc.clone().add(randomPos), 1, 0,0,0,0);
                            }

                            // 3. Капли крови (лавы) из-под затмения
                            if (Math.random() < 0.4) {
                                player.getWorld().spawnParticle(Particle.FALLING_LAVA, sunLoc.clone().add(0, -0.4, 0), 1, 0.2, 0, 0.2, 0);
                            }
                            break;

                        case HERO:
                            drawCape(player, loc, effect.getParticle());
                            break;

                        case CUBE:
                            double s = 1.5;
                            for (double t = -s; t <= s; t += 0.3) {
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(t, 0.1, -s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(t, 0.1, s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(-s, 0.1, t), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(s, 0.1, t), 1, 0,0,0,0);

                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(t, 3.1, -s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(t, 3.1, s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(-s, 3.1, t), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(s, 3.1, t), 1, 0,0,0,0);

                                double y = 1.6 + t;
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(-s, y, -s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(s, y, -s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(-s, y, s), 1, 0,0,0,0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(s, y, s), 1, 0,0,0,0);
                            }
                            break;

                        case INFERNO:
                            double infSpeed = ticks * 0.15;
                            for (int i = 0; i < 4; i++) {
                                double angle = infSpeed + (i * Math.PI / 2);
                                double h = 1.0 + Math.sin(ticks * 0.2 + (i * Math.PI / 2));
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(angle) * 1.2, h, Math.sin(angle) * 1.2), 2, 0, 0, 0, 0);
                            }
                            break;

                        case SANTA:
                            Location head = player.getEyeLocation().add(0, 0.4, 0);
                            for(double a = 0; a < 2 * Math.PI; a += Math.PI / 4) {
                                player.getWorld().spawnParticle(Particle.CLOUD, head.clone().add(Math.cos(a) * 0.35, 0, Math.sin(a) * 0.35), 1, 0,0,0,0);
                            }
                            double r = 0.25;
                            for(double y = 0.1; y <= 0.4; y += 0.1) {
                                for(double a = 0; a < 2 * Math.PI; a += Math.PI / 3) {
                                    player.getWorld().spawnParticle(effect.getParticle(), head.clone().add(Math.cos(a) * r, y, Math.sin(a) * r), 1, 0,0,0,0);
                                }
                                r -= 0.07;
                            }
                            player.getWorld().spawnParticle(Particle.CLOUD, head.clone().add(0, 0.5, 0), 2, 0.05,0.05,0.05,0);
                            break;

                        case RINGS:
                            double rSpeed = ticks * 0.15;
                            for (int i = 0; i < 6; i++) {
                                double a1 = rSpeed + (i * Math.PI / 3);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(a1) * 1.2, 0.9, Math.sin(a1) * 1.2), 1, 0, 0, 0, 0);

                                double a2 = -rSpeed + (i * Math.PI / 3);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(a2) * 1.0, 0.4, Math.sin(a2) * 1.0), 1, 0, 0, 0, 0);
                            }
                            break;

                        case BLOOD:
                            for (int i = 0; i < 4; i++) {
                                Location bloodLoc = loc.clone().add((Math.random() - 0.5) * 1.8, 0.1 + Math.random() * 1.2, (Math.random() - 0.5) * 1.8);
                                player.getWorld().spawnParticle(effect.getParticle(), bloodLoc, 1, 0, 0, 0, 0);
                            }
                            break;

                        case FLAME:
                            for (double y = 0; y < 1.2; y += 0.2) {
                                double fRadius = 0.4 + (y * 0.4);
                                double fAngle = (y * 4) + (ticks * 0.3);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(fAngle) * fRadius, y, Math.sin(fAngle) * fRadius), 1, 0, 0, 0, 0);
                            }
                            break;

                        case NOTES:
                            for (int i = 0; i < 3; i++) {
                                Location noteLoc = loc.clone().add((Math.random() - 0.5) * 2, 0.5 + Math.random() * 1.5, (Math.random() - 0.5) * 2);
                                player.getWorld().spawnParticle(effect.getParticle(), noteLoc, 0, Math.random(), 0, 0, 1);
                            }
                            break;

                        case SNOW:
                            for (int i = 0; i < 15; i++) {
                                double sx = (Math.random() - 0.5) * 3;
                                double sz = (Math.random() - 0.5) * 3;
                                double sy = Math.random() * 2.5;
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(sx, sy, sz), 1, 0, 0, 0, 0);
                            }
                            break;

                        case VILLAGER:
                            double vRadius = 0.8 + Math.sin(ticks * 0.2) * 0.2;
                            for (double vAngle = 0; vAngle <= 2 * Math.PI; vAngle += Math.PI / 8) {
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(vAngle) * vRadius, 0.1, Math.sin(vAngle) * vRadius), 1, 0, 0, 0, 0);
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(vAngle) * (vRadius - 0.3), 2.2, Math.sin(vAngle) * (vRadius - 0.3)), 1, 0, 0, 0, 0);
                            }
                            break;

                        case MAGIC:
                            double mAngle = ticks * 0.2;
                            player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(mAngle) * 1.2, 1, Math.sin(mAngle) * 1.2), 3, 0.1, 0.1, 0.1, 0);
                            player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(-mAngle) * 1.2, 1, Math.sin(-mAngle) * 1.2), 3, 0.1, 0.1, 0.1, 0);
                            break;

                        case ENDER:
                            player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(0, 1.0, 0), 10, 0.4, 0.4, 0.4, 0.05);
                            break;

                        case ENCHANTED:
                            player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(0, 1.0, 0), 10, 0.6, 0.6, 0.6, 0.1);
                            break;

                        case HALO:
                            Location eyeLoc = player.getEyeLocation().add(0, 0.5, 0);
                            double cAngle = (ticks % 360) * ((2 * Math.PI) / 20);
                            player.getWorld().spawnParticle(effect.getParticle(), eyeLoc.add(Math.cos(cAngle) * 1.1, 0, Math.sin(cAngle) * 1.1), 5, 0.1, 0.1, 0.1, 0);
                            break;

                        case FROST:
                            for (double i = 0; i < 2 * Math.PI; i += Math.PI / 4) {
                                player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(i) * 0.4, 2.3, Math.sin(i) * 0.4), 1, 0, 0, 0, 0);
                            }
                            player.getWorld().spawnParticle(Particle.FIREWORK, loc.clone().add(0, 0.2, 0), 4, 0.5, 0.1, 0.5, 0.02);
                            break;

                        case RAIN:
                            Location cloudLoc = loc.clone().add(0, 2.6, 0);
                            player.getWorld().spawnParticle(Particle.CLOUD, cloudLoc, 8, 0.4, 0.1, 0.4, 0.01);
                            player.getWorld().spawnParticle(effect.getParticle(), cloudLoc.clone().add(0, -0.2, 0), 3, 0.3, 0.1, 0.3, 0);
                            break;

                        case RODS:
                            double rodTime = ticks * 0.1;
                            for (int i = 0; i < 3; i++) {
                                double angle = rodTime + (i * (2 * Math.PI / 3));
                                double x = Math.cos(angle) * 1.2;
                                double z = Math.sin(angle) * 1.2;
                                for (double y = 0.4; y <= 1.8; y += 0.3) {
                                    player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(x, y, z), 2, 0, 0, 0, 0);
                                }
                            }
                            break;

                        case HEART:
                        default:
                            player.getWorld().spawnParticle(effect.getParticle(), loc.clone().add(Math.cos(ticks * 0.2) * 0.8, 2.1, Math.sin(ticks * 0.2) * 0.8), 2, 0.1, 0.1, 0.1, 0);
                            break;
                    }
                } catch (Exception ignored) {
                }
            }
        }, 0L, 5L);
    }

    private static void drawWings(Player player, Location loc, Particle particle) {
        Vector backVector = loc.getDirection().setY(0).normalize().multiply(-0.4);
        Location center = loc.clone().add(backVector);

        double space = 0.2;
        double defX = center.getX() - (space * WINGS_SHAPE[0].length / 2.0) + space;
        double x = defX;
        double y = center.clone().getY() + 1.9;
        double angle = -((player.getLocation().getYaw() + 180) / 60.0) + 3.0;

        for (boolean[] aShape : WINGS_SHAPE) {
            for (boolean anAShape : aShape) {
                if (anAShape) {
                    Vector v = new Vector(x - center.getX(), y - center.getY(), 0);
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double rX = v.getX() * cos + v.getZ() * sin;
                    double rZ = v.getX() * -sin + v.getZ() * cos;
                    v.setX(rX).setZ(rZ);

                    player.getWorld().spawnParticle(particle, center.clone().add(v), 1, 0.02, 0.02, 0.02, 0);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }

    private static void drawDevilWings(Player player, Location loc) {
        Vector backVector = loc.getDirection().setY(0).normalize().multiply(-0.5);
        Location center = loc.clone().add(backVector);

        double space = 0.2;
        double defX = center.getX() - (space * DEVIL_WINGS_SHAPE[0].length / 2.0) + space;
        double x = defX;
        double y = center.clone().getY() + 2.0;
        double angle = -((player.getLocation().getYaw() + 180) / 60.0) + 3.0;

        for (boolean[] aShape : DEVIL_WINGS_SHAPE) {
            for (boolean anAShape : aShape) {
                if (anAShape) {
                    Vector v = new Vector(x - center.getX(), y - center.getY(), 0);
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double rX = v.getX() * cos + v.getZ() * sin;
                    double rZ = v.getX() * -sin + v.getZ() * cos;
                    v.setX(rX).setZ(rZ);

                    // Миксуем черный дым с частицами огня
                    Particle p = Math.random() < 0.2 ? Particle.FLAME : Particle.LARGE_SMOKE;
                    player.getWorld().spawnParticle(p, center.clone().add(v), 1, 0.02, 0.02, 0.02, 0);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }

    private static void drawCape(Player player, Location loc, Particle particle) {
        Vector backVector = loc.getDirection().setY(0).normalize().multiply(-0.3);
        Location center = loc.clone().add(backVector);

        double space = 0.2;
        double defX = center.getX() - (space * CAPE_SHAPE[0].length / 2.0) + (space / 2.0);
        double x = defX;
        double defY = center.clone().getY() + 1.4;
        double y = defY;
        double angle = -((player.getLocation().getYaw() + 180) / 60.0) + 3.0;

        for (int i = 0; i < CAPE_SHAPE.length; i++) {
            for (int j = 0; j < CAPE_SHAPE[i].length; j++) {
                if (CAPE_SHAPE[i][j]) {
                    Vector v = new Vector(x - center.getX(), y - center.getY(), 0);
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double rX = v.getX() * cos + v.getZ() * sin;
                    double rZ = v.getX() * -sin + v.getZ() * cos;
                    v.setX(rX).setZ(rZ);

                    double iT = ((double) i) / 10.0;
                    Vector v2 = loc.getDirection().setY(0).normalize().multiply(-(0.1 + iT));

                    player.getWorld().spawnParticle(particle, center.clone().add(v).add(v2), 1, 0.01, 0.01, 0.01, 0);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }
}