package com.hejman.dx1221_ica1_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import java.util.ArrayList;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class MainMenu extends Activity
{
    // Variables
    private LinearLayout mainMenuContainer;
    private LinearLayout creditsContainer;
    private LinearLayout highscoreContainer;
    private LinearLayout settingsContainer;
    private LinearLayout leaderboardEntries;
    private TextView noEntriesMessage;
    private LeaderboardManager leaderboardManager;
    private SettingsManager settingsManager;
    //Audio
    private MediaPlayer menuBgmPlayer;
    private SoundPool soundPool;
    private int buttonClickId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        // Setup leaderboard manager
        leaderboardManager = new LeaderboardManager(this);
        settingsManager = new SettingsManager(this);
        setupAudio();
        setupContainers();
        setupButtons();
        updateLeaderboardDisplay();
    }
    private void setupAudio()
    {
        // 1. Setup SoundPool for SFX
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        buttonClickId = soundPool.load(this, R.raw.button_click, 1);

        // 2. Setup MediaPlayer for BGM
        menuBgmPlayer = MediaPlayer.create(this, R.raw.mainmenu_bgm);
        if (menuBgmPlayer != null)
        {
            menuBgmPlayer.setLooping(true);
        }
    }
    private void playClickSound()
    {
        float volume = settingsManager.getSFXVolume() / 100f;
        if (soundPool != null) {
            soundPool.play(buttonClickId, volume, volume, 1, 0, 1f);
        }
    }
    // Setup all buttons and their click actions
    private void setupButtons()
    {
        // Find all of the buttons
        Button playButton = findViewById(R.id.play_button);
        Button highscoreButton = findViewById(R.id.highscore_button);
        Button creditsButton = findViewById(R.id.credits_button);
        Button quitButton = findViewById(R.id.quit_button);
        Button backButton = findViewById(R.id.back_button);
        Button backButtonHighscore = findViewById(R.id.back_button_highscore);
        Button resetLeaderboardButton = findViewById(R.id.reset_leaderboard_button);
        ImageButton settingsButton = findViewById(R.id.settings_button);
        Button backButtonSettings = findViewById(R.id.back_button_settings);

        // Play game button
        playButton.setOnClickListener(v -> {
            playClickSound();
            Intent intent = new Intent(MainMenu.this, GameScene.class);
            startActivity(intent);
        });

        // Highscore button
        highscoreButton.setOnClickListener(v -> {
            playClickSound();
            showHighscore();
        });

        // Credits button
        creditsButton.setOnClickListener(v -> {
            playClickSound();
            showCredits();
        });

        // Back button from credits
        backButton.setOnClickListener(v -> {
            playClickSound();
            showMainMenu();
        });

        // Back button from highscore
        backButtonHighscore.setOnClickListener(v -> {
            playClickSound();
            showMainMenu();
        });

        // Reset leaderboard button
        resetLeaderboardButton.setOnClickListener(v -> {
            playClickSound();
            leaderboardManager.clearScores();
            updateLeaderboardDisplay();
        });

        // Quit button
        quitButton.setOnClickListener(v -> {
            playClickSound();
            finish();
        });

        // Settings button
        settingsButton.setOnClickListener(v -> {
            playClickSound();
            showSettings();
        });

        backButtonSettings.setOnClickListener(v -> {
            playClickSound();
            showMainMenu();
        });

        setupSliders();

        settingsManager.setSettingsButton(settingsButton);
    }

    // Setting up all of the screen containers
    private void setupContainers()
    {
        mainMenuContainer = findViewById(R.id.main_menu_container);
        creditsContainer = findViewById(R.id.credits_container);
        highscoreContainer = findViewById(R.id.highscore_container);
        leaderboardEntries = findViewById(R.id.leaderboard_entries);
        settingsContainer = findViewById(R.id.settings_container);
        noEntriesMessage = findViewById(R.id.no_entries_message);
    }

    private void showCredits()
    {
        hideAllScreens();
        creditsContainer.setVisibility(View.VISIBLE);
    }

    private void showHighscore()
    {
        hideAllScreens();
        highscoreContainer.setVisibility(View.VISIBLE);
        updateLeaderboardDisplay();
    }
    private void showSettings()
    {
        hideAllScreens();
        settingsContainer.setVisibility(View.VISIBLE);
        settingsManager.setButtonVisibility(false);
    }
    private void showMainMenu()
    {
        hideAllScreens();
        mainMenuContainer.setVisibility(View.VISIBLE);
        settingsManager.setButtonVisibility(true);
    }

    private void hideAllScreens()
    {
        mainMenuContainer.setVisibility(View.GONE);
        creditsContainer.setVisibility(View.GONE);
        highscoreContainer.setVisibility(View.GONE);
        settingsContainer.setVisibility(View.GONE);
    }

    private void updateLeaderboardDisplay()
    {
        leaderboardEntries.removeAllViews();
        ArrayList<LeaderboardManager.ScoreEntry> scores = leaderboardManager.getScores();

        // Check if we have any scores in the leaderboard
        if (scores.isEmpty())
        {
            noEntriesMessage.setVisibility(View.VISIBLE);
        }
        else
        {
            noEntriesMessage.setVisibility(View.GONE);

            // We add the scores to the leaderboard
            for (int i = 0; i < scores.size(); i++)
            {
                LeaderboardManager.ScoreEntry entry = scores.get(i);
                LinearLayout scoreRow = createScoreRow(entry.name, entry.score);
                leaderboardEntries.addView(scoreRow);
            }
        }
    }

    private LinearLayout createScoreRow(String name, int score)
    {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 20);
        row.setLayoutParams(rowParams);

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(18);
        nameText.setTextColor(0xFFFFFFFF);
        nameText.setGravity(android.view.Gravity.START);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        nameText.setLayoutParams(nameParams);

        TextView scoreText = new TextView(this);
        scoreText.setText(String.valueOf(score));
        scoreText.setTextSize(18);
        scoreText.setTextColor(0xFFFFFFFF);
        scoreText.setGravity(android.view.Gravity.END);

        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        scoreText.setLayoutParams(scoreParams);

        row.addView(nameText);
        row.addView(scoreText);

        return row;
    }

    public static void addScoreToLeaderboard(Context context, String playerName, int score)
    {
        LeaderboardManager manager = new LeaderboardManager(context);
        manager.addScore(playerName, score);
    }
    private void setupSliders()
    {
        SeekBar musicSlider = findViewById(R.id.music_slider);
        SeekBar sfxSlider = findViewById(R.id.sfx_slider);

        settingsManager.bindSliders(musicSlider, sfxSlider);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        // Start BGM
        if (menuBgmPlayer != null && !menuBgmPlayer.isPlaying())
        {
            float volume = settingsManager.getMusicVolume() / 100f;
            menuBgmPlayer.setVolume(volume, volume);
            menuBgmPlayer.start();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // Pause BGM
        if (menuBgmPlayer != null && menuBgmPlayer.isPlaying())
        {
            menuBgmPlayer.pause();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Clean up audio
        if (menuBgmPlayer != null)
        {
            menuBgmPlayer.release();
            menuBgmPlayer = null;
        }
        if (soundPool != null)
        {
            soundPool.release();
            soundPool = null;
        }
    }
}