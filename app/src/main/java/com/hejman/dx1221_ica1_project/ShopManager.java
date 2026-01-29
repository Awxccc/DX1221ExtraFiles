package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.content.SharedPreferences;

public class ShopManager {
    private static final String PREF_NAME = "GameShop";
    private static final String KEY_CURRENCY = "currency";

    // Base costs
    private static final int UNLOCK_COST = 1;
    private static final int UPGRADE_COST_BASE = 2;
    private final int[] ALL_POWERUPS = {1, 2, 3, 5};
    private final SharedPreferences prefs;

    public ShopManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- Currency Methods ---
    public int getCurrency() {
        return prefs.getInt(KEY_CURRENCY, 0);
    }

    public void addCurrency(int amount) {
        int current = getCurrency();
        prefs.edit().putInt(KEY_CURRENCY, current + amount).apply();
    }

    public boolean spendCurrency(int amount) {
        int current = getCurrency();
        if (current >= amount) {
            prefs.edit().putInt(KEY_CURRENCY, current - amount).apply();
            return true;
        }
        return false;
    }

    // --- PowerUp Level Methods ---
    // Level 0 = Locked, Level 1 = Unlocked, Level 2+ = Upgraded
    public int getPowerUpLevel(int powerUpType) {
        return prefs.getInt("powerup_level_" + powerUpType, 0);
    }

    public void upgradePowerUp(int powerUpType) {
        int currentLevel = getPowerUpLevel(powerUpType);
        prefs.edit().putInt("powerup_level_" + powerUpType, currentLevel + 1).apply();
    }
    private int getPriceForLevel(int levelIndex) {
        int unlockCost = 500;
        int upgradeCostBase = 1000;

        if (levelIndex == 0) {
            return unlockCost;
        }

        return upgradeCostBase * levelIndex;
    }
    public int getUpgradeCost(int powerUpType) {
        int currentLevel = getPowerUpLevel(powerUpType);
        return getPriceForLevel(currentLevel);
    }

    // Returns the spawn chance percentage (e.g., 2 for 2%)
    public float getSpawnChance(int powerUpType) {
        int level = getPowerUpLevel(powerUpType);
        if (level == 0) return 0f;

        // Base 2% chance, +0.5% for every extra level
        return 2.0f + ((level - 1) * 0.5f);
    }

    // In ShopManager.java

    public int calculateTotalSpent(int powerUpType) {
        int currentLevel = getPowerUpLevel(powerUpType);
        int totalSpent = 0;

        // If I am Level 3, I paid for Level 0, Level 1, and Level 2.
        // Loop from 0 up to (currentLevel - 1)
        for (int i = 0; i < currentLevel; i++) {
            totalSpent += getPriceForLevel(i);
        }

        return totalSpent;
    }
    public void resetAllShopProgress() {
        int totalRefund = 0;

        // 1. Calculate total refund for ALL items
        for (int type : ALL_POWERUPS) {
            totalRefund += calculateTotalSpent(type);
            // 2. Reset level to 0
            prefs.edit().putInt("powerup_level_" + type, 0).apply();
        }

        // 3. Give back the money
        addCurrency(totalRefund);
    }
}