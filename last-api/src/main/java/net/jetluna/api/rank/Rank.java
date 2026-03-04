package net.jetluna.api.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rank {

    // --- ИГРОКИ И ДОНАТ ---
    PLAYER("Player", "", 1),
    GO("Go", "<yellow><bold>GO ", 2),
    PLUS("Plus", "<gold><bold>PLUS ", 3),
    ULTRA("Ultra", "<light_purple><bold>ULTRA ", 4),
    MAX("Max", "<dark_purple><bold>MAX ", 5),

    // --- МЕДИЯ ---
    MEDIA("Media", "<aqua><bold>MEDIA ", 6),

    // --- ПЕРСОНАЛ (STAFF) ---
    JUNIOR("Junior", "<green><bold>JUNIOR ", 7),
    BUILDER("Builder", "<yellow><bold>BUILDER ", 8),
    TESTER("Tester", "<gold><bold>TESTER ", 9),
    HELPER("Helper", "<dark_green><bold>HELPER ", 10),
    MODER("Moder", "<blue><bold>MODER ", 11),
    ADMIN("Admin", "<red><bold>ADMIN ", 12),

    // --- РАЗРАБОТЧИК (Developer) ---
    DEV("Dev", "<dark_red><bold>DEV ", 13);

    private final String name;
    private final String prefix;
    private final int weight;
}