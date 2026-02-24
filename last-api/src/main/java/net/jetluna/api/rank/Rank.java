package net.jetluna.api.rank;

import lombok.AllArgsConstructor; // Создает конструктор для ВСЕХ полей
import lombok.Getter;             // Создает геттеры для ВСЕХ полей

@AllArgsConstructor
@Getter
public enum Rank {

    // --- ИГРОКИ И ДОНАТ ---
    PLAYER("Player", "", 1),
    GO("Go", "<yellow><bold>GO ", 2),
    PLUS("Plus", "<gold><bold>PLUS ", 3),
    ULTRA("Ultra", "<light_purple><bold>ULTRA ", 4),
    MAX("Max", "<purple><bold>MAX ", 5),

    // --- ПЕРСОНАЛ (STAFF) ---
    JUNIOR("Junior", "<green><bold>JUNIOR ", 6),
    HELPER("Helper", "<dark_green><bold>HELPER ", 7),
    MODER("Moder", "<blue><bold>MODER ", 8),
    ADMIN("Admin", "<red><bold>ADMIN ", 9),

    // --- РАЗРАБОТЧИК (Developer) ---
    DEV("Dev", "<dark_red><bold>DEV ", 10);

    private final String name;
    private final String prefix;
    private final int weight;

    // ВСЁ! Больше ничего писать не надо. Lombok сам создаст getName(), getPrefix() и конструктор.
}