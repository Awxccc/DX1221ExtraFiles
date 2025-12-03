package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;

public class LeaderboardManager
{
    // Variables
    private Context context;
    private SharedPreferences prefs;

    public LeaderboardManager(Context context)
    {
        this.context = context;
        this.prefs = context.getSharedPreferences("LeaderboardPrefs", Context.MODE_PRIVATE);
    }

    // Add new scores to the leaderboard
    public void addScore(String playerName, int score)
    {
        // Get the current scores
        ArrayList<ScoreEntry> scores = getScores();

        // Add the new score entry to the leaderboard
        scores.add(new ScoreEntry(playerName, score));

        // Sort the scores by highest first
        sortScores(scores);

        // Keep only top 10 scores while removing the next lowest
        if (scores.size() > 10)
        {
            while (scores.size() > 10)
            {
                scores.remove(scores.size() - 1);
            }
        }

        // Save the scores
        saveScores(scores);
    }

    // We get the scores to display on the leaderboard
    public ArrayList<ScoreEntry> getScores()
    {
        ArrayList<ScoreEntry> scores = new ArrayList<>();
        String scoresString = prefs.getString("scores", "");

        // If we have something to display
        if (!scoresString.isEmpty())
        {
            String[] entries = scoresString.split(";");

            // Then we loop through each entry
            for (int i = 0; i < entries.length; i++)
            {
                String entry = entries[i];
                String[] parts = entry.split(",");

                if (parts.length == 2)
                {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.add(new ScoreEntry(name, score));
                }
            }
        }

        return scores;
    }

    // Sort scores from highest to lowest
    private void sortScores(ArrayList<ScoreEntry> scores)
    {
        for (int i = 0; i < scores.size(); i++)
        {
            for (int v = 0; v < scores.size() - 1; v++)
            {
                // Bubble sorting magic
                if (scores.get(v).score < scores.get(v + 1).score)
                {
                    ScoreEntry temp = scores.get(v);
                    scores.set(v, scores.get(v + 1));
                    scores.set(v + 1, temp);
                }
            }
        }
    }

    // Save the scores
    private void saveScores(ArrayList<ScoreEntry> scores)
    {
        String scoresString = "";

        // Convert scores to text format first just in case
        for (int i = 0; i < scores.size(); i++)
        {
            ScoreEntry entry = scores.get(i);
            scoresString = scoresString + entry.name + "," + entry.score;

            if (i < scores.size() - 1)
            {
                scoresString = scoresString + ";";
            }
        }

        // Here we actually save the score lolol
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("scores", scoresString);
        editor.apply();
    }

    // Clear all scores (Either for debugging purpose or others)
    public void clearScores()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("scores");
        editor.apply();
    }

    public static class ScoreEntry
    {
        // Variables
        public String name;
        public int score;

        public ScoreEntry(String name, int score)
        {
            this.name = name;
            this.score = score;
        }
    }
}