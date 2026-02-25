package net.jetluna.api.stats;

public class PlayerStats {
    private int coins;
    private int emeralds;
    private long lastRewardTime;
    private int rewardDay;
    private int level;
    private int exp;

    public PlayerStats(int coins, int emeralds, long lastRewardTime, int rewardDay, int level, int exp) {
        this.coins = coins;
        this.emeralds = emeralds;
        this.lastRewardTime = lastRewardTime;
        this.rewardDay = rewardDay;
        this.level = level;
        this.exp = exp;
    }

    // --- Геттеры и Сеттеры ---

    public int getCoins() { return coins; }
    public void setCoins(int coins) {
        this.coins = coins;
        // Автоматическое сохранение при изменении (костыль, но надежно для новичка)
        // Но лучше вызывать save() отдельно. Пока оставим так.
    }

    public int getEmeralds() { return emeralds; }
    public void setEmeralds(int emeralds) { this.emeralds = emeralds; }

    public long getLastRewardTime() { return lastRewardTime; }
    public void setLastRewardTime(long time) { this.lastRewardTime = time; }

    public int getRewardDay() { return rewardDay; }
    public void setRewardDay(int day) { this.rewardDay = day; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
}