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

public class MainMenu extends Activity
{
    // Variables
    private LinearLayout mainMenuContainer;
    private LinearLayout creditsContainer;
    private LinearLayout highscoreContainer;
    private LinearLayout settingsContainer;
    private LinearLayout shopContainer;
    private LinearLayout leaderboardEntries;

    private TextView noEntriesMessage;
    private LeaderboardManager leaderboardManager;
    private SettingsManager settingsManager;

    //Tutorial
    private LinearLayout tutorialContainer;
    private Button tutorialButton;

    //Shop
    private ShopManager shopManager;
    private LinearLayout shopEntries;
    private TextView shopCurrencyText;
    private ImageButton shopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        shopManager = new ShopManager(this);

        // Setup leaderboard manager
        leaderboardManager = new LeaderboardManager(this);
        settingsManager = new SettingsManager(this);
        SoundManager.getInstance(this).playBGM(R.raw.mainmenu_bgm);
        setupContainers();
        setupButtons();
        updateLeaderboardDisplay();
    }
    private void playClickSound()
    {
        SoundManager.getInstance(this).playSFX(SoundManager.SFX_BUTTON_CLICK);
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
        tutorialButton = findViewById(R.id.tutorial_button);
        //Shop
        shopButton = findViewById(R.id.shop_button);
        Button backButtonShop = findViewById(R.id.back_button_shop);
        Button resetShopButton = findViewById(R.id.reset_shop_button);

        //Shop buttons
        shopButton.setOnClickListener(v -> {
            playClickSound();
            showShop();
        });

        backButtonShop.setOnClickListener(v -> {
            playClickSound();
            showMainMenu();
        });

        resetShopButton.setOnClickListener(v -> {
            playClickSound();
            shopManager.resetAllShopProgress();
            updateShopDisplay();
        });

        // Play game button
        playButton.setOnClickListener(v -> {
            playClickSound();
            Intent intent = new Intent(MainMenu.this, GameScene.class);
            startActivity(intent);
        });

        //Tutorial buttons
        tutorialButton.setOnClickListener(v -> {
            playClickSound();
            showTutorial();
        });
        Button backButtonTutorial = findViewById(R.id.back_button_tutorial);
        backButtonTutorial.setOnClickListener(v -> {
            playClickSound();
            showMainMenu();
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
        tutorialContainer = findViewById(R.id.tutorial_container);

        //Shop
        shopContainer = findViewById(R.id.shop_container);
        shopEntries = findViewById(R.id.shop_entries);
        shopCurrencyText = findViewById(R.id.shop_currency_text);
    }

    private void showCredits()
    {
        hideAllScreens();
        creditsContainer.setVisibility(View.VISIBLE);
        setMenuButtonsVisible(false);
    }

    private void showHighscore()
    {
        hideAllScreens();
        highscoreContainer.setVisibility(View.VISIBLE);
        updateLeaderboardDisplay();
        setMenuButtonsVisible(false);
    }
    private void showSettings()
    {
        hideAllScreens();
        settingsContainer.setVisibility(View.VISIBLE);
        settingsManager.setButtonVisibility(false);
        setMenuButtonsVisible(false);
    }
    private void showShop()
    {
        hideAllScreens();
        shopContainer.setVisibility(View.VISIBLE);
        settingsManager.setButtonVisibility(false);
        setMenuButtonsVisible(false);
        updateShopDisplay();
    }
    private void showMainMenu()
    {
        hideAllScreens();
        mainMenuContainer.setVisibility(View.VISIBLE);
        settingsManager.setButtonVisibility(true);
        setMenuButtonsVisible(true);
    }

    private void hideAllScreens()
    {
        mainMenuContainer.setVisibility(View.GONE);
        creditsContainer.setVisibility(View.GONE);
        highscoreContainer.setVisibility(View.GONE);
        settingsContainer.setVisibility(View.GONE);
        shopContainer.setVisibility(View.GONE);
        tutorialContainer.setVisibility(View.GONE);
    }
    private void updateShopDisplay()
    {
        if (shopCurrencyText != null)
        {
            shopCurrencyText.setText("Coins: " + shopManager.getCurrency());
        }

        shopEntries.removeAllViews();

        addHeadStartRow();
        addShopItemRow(1, "Tunneller", 0xFF78EBF5);
        addShopItemRow(2, "Range Enhancer", 0xFFA54BE1);
        addShopItemRow(3, "Stabilizer", 0xFFE17341);
        addShopItemRow(5, "Signal Bypass", 0xFFE66E91);

        TextView cosmeticHeader = new TextView(this);
        cosmeticHeader.setText("PLAYER SKINS");
        cosmeticHeader.setTextSize(24);
        cosmeticHeader.setTextColor(0xFFFFFFFF);
        cosmeticHeader.setPadding(0, 60, 0, 20);
        shopEntries.addView(cosmeticHeader);

        for (int i = 0; i < ShopManager.COLOR_VALUES.length; i++)
        {
            addCosmeticRow(i);
        }
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
        SeekBar tiltSlider = findViewById(R.id.tilt_slider);

        settingsManager.bindSliders(musicSlider, sfxSlider, tiltSlider, progress -> {
            SoundManager.getInstance(this).updateMusicVolume();
        });
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        SoundManager.getInstance(this).playBGM(R.raw.mainmenu_bgm);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SoundManager.getInstance(this).pauseBGM();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void addShopItemRow(final int type, final String name, int color)
    {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 40);

        final TextView infoText = new TextView(this);
        infoText.setTextSize(18);
        infoText.setTextColor(color);

        final Button buyBtn = new Button(this);
        buyBtn.setTextColor(0xFFFFFFFF);
        buyBtn.setPadding(20, 10, 20, 10);

        updateRowText(type, name, infoText, buyBtn);

        buyBtn.setOnClickListener(v -> {
            playClickSound();
            int cost = shopManager.getUpgradeCost(type);
            if (shopManager.spendCurrency(cost))
            {
                shopManager.upgradePowerUp(type);
                updateShopDisplay();
            }
        });

        row.addView(infoText);
        row.addView(buyBtn);
        shopEntries.addView(row);
    }

    private void updateRowText(int type, String name, TextView infoView, Button btn)
    {
        int level = shopManager.getPowerUpLevel(type);
        int cost = shopManager.getUpgradeCost(type);

        String status = (level == 0) ? "LOCKED" : "Lvl " + level;
        String desc = (level == 0) ? "Unlock to enable spawning" : "Increases spawn rate";

        infoView.setText(name + " (" + status + ")\n" + desc);

        btn.setText((level == 0 ? "UNLOCK" : "UPGRADE") + " - " + cost + " Coins");

        if (shopManager.getCurrency() < cost)
        {
            btn.setAlpha(0.5f);
            btn.setEnabled(false);
        }
        else
        {
            btn.setAlpha(1.0f);
            btn.setEnabled(true);
        }
    }
    private void addHeadStartRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 40);

        final TextView infoText = new TextView(this);
        infoText.setTextSize(18);
        infoText.setTextColor(0xFFFFFF00); // Yellow/Gold color to make it stand out

        final Button buyBtn = new Button(this);
        buyBtn.setTextColor(0xFFFFFFFF);
        buyBtn.setPadding(20, 10, 20, 10);

        // Update text logic
        boolean isBought = shopManager.isHeadStartPurchased();
        int cost = shopManager.getHeadStartCost();

        if (isBought) {
            infoText.setText("HEAD START (ACTIVE)\nTunneller active for first 200m");
            buyBtn.setText("READY FOR NEXT RUN");
            buyBtn.setEnabled(false); // Cannot buy again until used
            buyBtn.setAlpha(0.5f);
            buyBtn.setTextColor(0xFF00FF00); // Green text to show ready
        } else {
            infoText.setText("HEAD START (Single Use)\nTunneller active for first 200m");
            buyBtn.setText("BUY - " + cost + " Coins");

            if (shopManager.getCurrency() < cost) {
                buyBtn.setEnabled(false);
                buyBtn.setAlpha(0.5f);
            } else {
                buyBtn.setEnabled(true);
                buyBtn.setAlpha(1.0f);
            }
        }

        buyBtn.setOnClickListener(v -> {
            playClickSound();
            if (!shopManager.isHeadStartPurchased() && shopManager.spendCurrency(cost)) {
                shopManager.setHeadStartPurchased(true);
                updateShopDisplay(); // Refresh UI
            }
        });

        row.addView(infoText);
        row.addView(buyBtn);
        shopEntries.addView(row);
    }
    private void setMenuButtonsVisible(boolean visible)
    {
        settingsManager.setButtonVisibility(visible);
        shopButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        tutorialButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    private void showTutorial()
    {
        hideAllScreens();
        tutorialContainer.setVisibility(View.VISIBLE);
        setMenuButtonsVisible(false);
    }
    private void addCosmeticRow(final int colorId)
    {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 40);

        final TextView infoText = new TextView(this);
        infoText.setTextSize(18);
        infoText.setTextColor(ShopManager.COLOR_VALUES[colorId]);

        final Button actionBtn = new Button(this);
        actionBtn.setTextColor(0xFFFFFFFF);
        actionBtn.setPadding(20, 10, 20, 10);

        boolean isUnlocked = shopManager.isColorUnlocked(colorId);
        boolean isEquipped = (shopManager.getEquippedColorId() == colorId);

        infoText.setText(ShopManager.COLOR_NAMES[colorId]);

        if (isEquipped)
        {
            actionBtn.setText("EQUIPPED");
            actionBtn.setEnabled(false);
            actionBtn.setAlpha(0.5f);
            actionBtn.setTextColor(0xFF00FF00);
        }
        else if (isUnlocked)
        {
            actionBtn.setText("EQUIP");
            actionBtn.setEnabled(true);
            actionBtn.setAlpha(1.0f);
            actionBtn.setOnClickListener(v -> {
                playClickSound();
                shopManager.equipColor(colorId);
                updateShopDisplay();
            });
        }
        else
        {
            actionBtn.setText("BUY - " + ShopManager.COSMETIC_COST + " Coins");
            if (shopManager.getCurrency() >= ShopManager.COSMETIC_COST)
            {
                actionBtn.setEnabled(true);
                actionBtn.setAlpha(1.0f);
                actionBtn.setOnClickListener(v -> {
                    playClickSound();
                    if (shopManager.spendCurrency(ShopManager.COSMETIC_COST))
                    {
                        shopManager.unlockColor(colorId);
                        shopManager.equipColor(colorId);
                        updateShopDisplay();
                    }
                });
            }
            else
            {
                actionBtn.setEnabled(false);
                actionBtn.setAlpha(0.5f);
            }
        }

        row.addView(infoText);
        row.addView(actionBtn);
        shopEntries.addView(row);
    }
}