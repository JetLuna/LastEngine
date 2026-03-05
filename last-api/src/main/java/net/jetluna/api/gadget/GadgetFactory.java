package net.jetluna.api.gadget;

import net.jetluna.api.gadget.impl.*;
import org.bukkit.entity.Player;

public class GadgetFactory {
    public static LobbyGadget create(Player player, GadgetType type) {
        switch (type) {
            case BLACK_HOLE: return new BlackHoleGadget(player);
            case TRAMPOLINE: return new TrampolineGadget(player);
            case DISCO_BALL: return new DiscoBallGadget(player);
            case BLIZZARD_BLASTER: return new BlizzardBlasterGadget(player);
            case ROCKET: return new RocketGadget(player);
            case PORTAL_GUN: return new PortalGunGadget(player);
            case TSUNAMI: return new TsunamiGadget(player);
            case FLESH_HOOK: return new FleshHookGadget(player);
            case SMASH_DOWN: return new SmashDownGadget(player);
            case TNT: return new TntGadget(player);
            case PAINTBALL_GUN: return new PaintballGunGadget(player);
            case COLOR_BOMB: return new ColorBombGadget(player);
            case MELON_THROWER: return new MelonThrowerGadget(player);
            case FUN_GUN: return new FunGunGadget(player);
            case THOR_HAMMER: return new ThorHammerGadget(player);
            case EXPLOSIVE_SHEEP: return new ExplosiveSheepGadget(player);
            case CHICKENATOR: return new ChickenatorGadget(player);
            case BAT_BLASTER: return new BatBlasterGadget(player);
            case ENDER_RIDER: return new EnderRiderGadget(player);
            case GRAPPLING_HOOK: return new GrapplingHookGadget(player);
            case JETPACK: return new JetpackGadget(player);
            case FLAME_WALKER: return new FlameWalkerGadget(player);
            case LASER_POINTER: return new LaserPointerGadget(player);
            case FAKE_CREEPER: return new FakeCreeperGadget(player);
            case ICE_BOMB: return new IceBombGadget(player);
            case FIREBALL: return new FireballGadget(player);
            case ZEUS_STRIKE: return new ZeusStrikeGadget(player);
            case LOVE_AURA: return new LoveAuraGadget(player);
            default: return null;
        }
    }
}