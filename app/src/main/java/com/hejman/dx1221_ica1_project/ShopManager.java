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
    private static final String KEY_HEADSTART = "headstart_bought";
    private static final int HEADSTART_COST = 500;

    public ShopManager(Context context)
    {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getCurrency() {
        return prefs.getInt(KEY_CURRENCY, 0);
    }

    public void addCurrency(int amount)
    {
        int current = getCurrency();
        prefs.edit().putInt(KEY_CURRENCY, current + amount).apply();
    }

    public boolean spendCurrency(int amount)
    {
        int current = getCurrency();
        if (current >= amount)
        {
            prefs.edit().putInt(KEY_CURRENCY, current - amount).apply();
            return true;
        }
        return false;
    }
    public int getPowerUpLevel(int powerUpType)
    {
        return prefs.getInt("powerup_level_" + powerUpType, 0);
    }

    public void upgradePowerUp(int powerUpType)
    {
        int currentLevel = getPowerUpLevel(powerUpType);
        prefs.edit().putInt("powerup_level_" + powerUpType, currentLevel + 1).apply();
    }
    private int getPriceForLevel(int levelIndex)
    {
        int unlockCost = 500;
        int upgradeCostBase = 1000;

        if (levelIndex == 0)
        {
            return unlockCost;
        }

        return upgradeCostBase * levelIndex;
    }
    public int getUpgradeCost(int powerUpType)
    {
        int currentLevel = getPowerUpLevel(powerUpType);
        return getPriceForLevel(currentLevel);
    }

    public float getSpawnChance(int powerUpType)
    {
        int level = getPowerUpLevel(powerUpType);
        if (level == 0) return 0f;

        // Base 2% chance, +0.5% for every extra level
        return 2.0f + ((level - 1) * 0.5f);
    }

    public int calculateTotalSpent(int powerUpType)
    {
        int currentLevel = getPowerUpLevel(powerUpType);
        int totalSpent = 0;
        for (int i = 0; i < currentLevel; i++)
        {
            totalSpent += getPriceForLevel(i);
        }

        return totalSpent;
    }
    public void resetAllShopProgress()
    {
        int totalRefund = 0;
        //Calculate total refund for ALL items
        for (int type : ALL_POWERUPS)
        {
            totalRefund += calculateTotalSpent(type);
            prefs.edit().putInt("powerup_level_" + type, 0).apply();
        }
        addCurrency(totalRefund);
    }

    public boolean isHeadStartPurchased()
    {
        return prefs.getBoolean(KEY_HEADSTART, false);
    }

    public void setHeadStartPurchased(boolean purchased)
    {
        prefs.edit().putBoolean(KEY_HEADSTART, purchased).apply();
    }

    public int getHeadStartCost()
    {
        return HEADSTART_COST;
    }
    public boolean consumeHeadStart()
    {
        if (isHeadStartPurchased())
        {
            setHeadStartPurchased(false);
            return true;
        }
        return false;
    }
}