package net.jetluna.lobby.npc;

public class Skin {
    private final String value;
    private final String signature;

    public Skin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public String getValue() { return value; }
    public String getSignature() { return signature; }

    // Я подготовил для тебя пару скинов (можешь заменить на свои с mineskin.org)
    // Эти значения - просто пример, замени их на реальные длинные строки
    public static final Skin BEDWARS = new Skin(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmN2FhN2Q2N2M2ZDJmN2I0YjQ5YzQ4Y2Q4NGExYmQxN2M4Y2M0M2U3ZTM4YjQ5M2MzY2Q0NTc3In19fQ==",
            "" // Сигнатура не обязательна для пираток, но нужна для лицензии
    );

    public static final Skin VANILLA = new Skin(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y2ZTVhODUyYjQ5ZNTc0M2U5MjQ2OTQ3M2I4OGViNTc2Y2RWZlYzQ3YmQ0OTE1OTQzNGZkOGU4In19fQ==",
            ""
    );

    public static final Skin DUELS = new Skin(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjFkZDRmZTRhNDI5YWJkNjQzOGQ5YjJjZDA1OWFjYjM0ZTU2ZTE4OTQyZjU3ZGRjYTMxMjQ1MzIyIn19fQ==",
            ""
    );

    public static final Skin REWARDS = new Skin(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0MzM0ZTI2MzAyN2RjNjZlOUZiYWU2ZTFjZDBDdnTiJ9fX0=",
            ""
    );
}