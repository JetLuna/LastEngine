package net.jetluna.api.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rank {

    // --- ИГРОКИ И ДОНАТ ---
    PLAYER("Player", "", 1),
    GO("Go", "§e§lGO ", 2),
    PLUS("Plus", "§6§lPLUS ", 3),
    ULTRA("Ultra", "§d§lULTRA ", 4),
    MAX("Max", "§5§lMAX ", 5),

    // --- МЕДИЯ ---
    MEDIA("Media", "§b§lMEDIA ", 6),

    // --- ПЕРСОНАЛ (STAFF) ---
    JUNIOR("Junior", "§a§lJUNIOR ", 7),
    BUILDER("Builder", "§3§lBUILDER ", 8),
    TESTER("Tester", "§6§lTESTER ", 9),
    HELPER("Helper", "§2§lHELPER ", 10),
    MODER("Moder", "§9§lMODER ", 11),
    ADMIN("Admin", "§c§lADMIN ", 12),

    // --- РАЗРАБОТЧИК (Developer) ---
    DEV("Dev", "§4§lDEV ", 13);

    private final String name;
    private final String prefix;
    private final int weight;
}