package net.jetluna.api.stats;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {
    private int coins;
    private int emeralds;
    private long lastRewardTime;
    private int rewardDay;
    @Setter
    private int level;
    private int exp;

    private String suffix = "";

    // !!! НОВОЕ: ЭФФЕКТЫ !!!
    private String activeEffect = ""; // Текущий включенный эффект
    private List<String> unlockedEffects = new ArrayList<>(); // Список купленных

    public PlayerStats(int coins, int emeralds, long lastRewardTime, int rewardDay, int level, int exp) {
        this.coins = coins;
        this.emeralds = emeralds;
        this.lastRewardTime = lastRewardTime;
        this.rewardDay = rewardDay;
        this.level = level;
        this.exp = exp;
    }

    // --- НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ЭФФЕКТОВ ---
    public String getActiveEffect() { return activeEffect == null ? "" : activeEffect; }
    public void setActiveEffect(String activeEffect) { this.activeEffect = activeEffect; }

    public List<String> getUnlockedEffects() {
        if (unlockedEffects == null) unlockedEffects = new ArrayList<>();
        return unlockedEffects;
    }
    public void addUnlockedEffect(String effect) { getUnlockedEffects().add(effect); }

    // --- СТАРЫЕ Геттеры и Сеттеры ---
    public String getSuffix() { return suffix == null ? "" : suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public int getEmeralds() { return emeralds; }
    public void setEmeralds(int emeralds) { this.emeralds = emeralds; }
    public long getLastRewardTime() { return lastRewardTime; }
    public void setLastRewardTime(long time) { this.lastRewardTime = time; }
    public int getRewardDay() { return rewardDay; }
    public void setRewardDay(int day) { this.rewardDay = day; }
    public int getLevel() { return level; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    private List<String> ownedPets = new ArrayList<>(); // Список названий купленных PetType
    private String activePet = ""; // Название текущего питомца (например, "DOG")

    // Геттеры (если не используешь Lombok)
    public List<String> getOwnedPets() { return ownedPets; }
    public String getActivePet() { return activePet; }
    public void setActivePet(String activePet) { this.activePet = activePet; }

    private List<String> ownedGadgets = new ArrayList<>();
    private String activeGadget = "";

    // Методы для получения и записи гаджетов
    public java.util.List<String> getOwnedGadgets() {
        return ownedGadgets;
    }

    public void setOwnedGadgets(java.util.List<String> ownedGadgets) {
        this.ownedGadgets = ownedGadgets;
    }

    public String getActiveGadget() {
        return activeGadget;
    }

    public void setActiveGadget(String activeGadget) {
        this.activeGadget = activeGadget;
    }
}