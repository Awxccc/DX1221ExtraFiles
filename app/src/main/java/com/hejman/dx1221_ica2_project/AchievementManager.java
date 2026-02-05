package com.hejman.dx1221_ica2_project;

import android.content.Context;
import android.content.SharedPreferences;
//Done by Hejy
public class AchievementManager
{
    private static final String PREF_NAME = "GameAchievements";
    private static final String KEY_ACHIEVEMENT = "achievement_";
    private final SharedPreferences prefs;

    // Achievement names
    public static final String[] ACHIEVEMENT_NAMES = {
            "One Small Step", "Who are those..?", "One Giant Leap", "The End in Sight",
            "Tactical Gameplay", "What a Hoarder...",
            "I Gotta Go!", "I REALLY Gotta Go!", "I Like 'Em Far!", "ENHAAAANCE!",
            "Quit Moving!", "Not a Glitch, I Promise", "Yoink!", "No, My Node Now!",
            "I Am Speed", "WooOooah!", "Shiny Stars!", "Just Lucky, That's All",
            "Very, Very Close Call", "Absolute Troll", "So Far, Yet So Close"
    };

    // Achievement descriptions
    public static final String[] ACHIEVEMENT_DESCRIPTIONS = {
            "Reach 200 meters", "Reach 500 meters", "Reach 1000 meters", "Reach 2500 meters",
            "Collect 10 power-ups in one run", "Collect 25 power-ups in one run",
            "Collect 5 Tunnellers", "Collect 10 Tunnellers",
            "Collect 5 Range Enhancers", "Collect 10 Range Enhancers",
            "Collect 5 Stabilizers", "Collect 10 Stabilizers",
            "Collect 5 Signal Bypasses", "Collect 10 Signal Bypasses",
            "Use the headstart for the first time!", "Enter a wormhole for the first time!",
            "Earn 1000 points in the wormhole!", "Earn 2500 points in the wormhole!",
            "Jump to a node with less than 10 pixels of click range left!",
            "Die on the first node...", "Die with 10 meters left to win!"
    };

    public AchievementManager(Context context)
    {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Unlock the achievement by ID
    public void unlockAchievement(int achievementId)
    {
        if (achievementId >= 0 && achievementId < ACHIEVEMENT_NAMES.length)
        {
            prefs.edit().putBoolean(KEY_ACHIEVEMENT + achievementId, true).apply();
        }
    }

    // Check if the achievement is unlocked
    public boolean isAchievementUnlocked(int achievementId)
    {
        return prefs.getBoolean(KEY_ACHIEVEMENT + achievementId, false);
    }

    // Reset all of the achievements
    public void resetAllAchievements()
    {
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < ACHIEVEMENT_NAMES.length; i++)
        {
            editor.remove(KEY_ACHIEVEMENT + i);
        }
        editor.apply();
    }

    public int getTotalAchievements()
    {
        return ACHIEVEMENT_NAMES.length;
    }

    public String getAchievementName(int achievementId)
    {
        if (achievementId >= 0 && achievementId < ACHIEVEMENT_NAMES.length)
        {
            return ACHIEVEMENT_NAMES[achievementId];
        }
        return "";
    }

    public String getAchievementDescription(int achievementId)
    {
        if (achievementId >= 0 && achievementId < ACHIEVEMENT_DESCRIPTIONS.length)
        {
            return ACHIEVEMENT_DESCRIPTIONS[achievementId];
        }
        return "";
    }

    public int getUnlockedCount()
    {
        int count = 0;
        for (int i = 0; i < ACHIEVEMENT_NAMES.length; i++)
        {
            if (isAchievementUnlocked(i))
            {
                count++;
            }
        }
        return count;
    }
}