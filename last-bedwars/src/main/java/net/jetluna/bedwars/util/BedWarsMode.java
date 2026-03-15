package net.jetluna.bedwars.util;

public enum BedWarsMode {

    SOLO("Одиночный", 1, 8),
    DOUBLE("Двойной", 2, 8),
    TRIO("Трио", 3, 4),
    QUADRO("Квадро", 4, 4);

    private final String format;
    private final int teamSize;
    private final int teamCount;

    BedWarsMode(String format, int teamSize, int teamCount) {
        this.format = format;
        this.teamSize = teamSize;
        this.teamCount = teamCount;
    }

    public String getFormat() { return format; }
    public int getTeamSize() { return teamSize; }
    public int getTeamCount() { return teamCount; }
}