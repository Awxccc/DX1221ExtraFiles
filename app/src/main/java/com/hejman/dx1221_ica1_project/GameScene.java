package com.hejman.dx1221_ica1_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class GameScene extends Activity
{
    // Variables
    private GameLogic gameLogic;
    private TextView scoreText;
    private Handler scoreHandler;
    private Runnable scoreUpdater;
    private LinearLayout gameOverScreen;
    private TextView gameOverTitle;
    private TextView gameOverScoreText;
    private EditText nameInput;
    private Button submitButton;
    private boolean isUpdatingScore = false;
    private View instructionsOverlay;
    private Button closeInstructionsBtn;
    private Button pauseButton;
    private MinigameLogic minigameLogic;

    //Audio
    private MediaPlayer bgmPlayer;
    private SoundPool soundPool;
    private int playerLostId;
    private int playerWonId;
    private int buttonClickId;
    private SettingsManager settingsManager;
    private ShopManager shopManager;
    private TextView milestoneAlertText;
    private final Handler alertHandler = new Handler();
    private boolean isPauseDialogShowing = false;
    private final Runnable hideAlertRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (milestoneAlertText != null)
            {
                milestoneAlertText.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamescene);
        settingsManager = new SettingsManager(this);
        shopManager = new ShopManager(this);
        gameLogic = findViewById(R.id.game_logic);
        minigameLogic = findViewById(R.id.minigame_logic);
        scoreText = findViewById(R.id.score_text);
        pauseButton = findViewById(R.id.pause_button);
        instructionsOverlay = findViewById(R.id.instructions_overlay);
        closeInstructionsBtn = findViewById(R.id.close_instructions_btn);
        milestoneAlertText = findViewById(R.id.milestone_alert_text);
        SoundManager.getInstance(this).playBGM(R.raw.gamescene_bgm);
        closeInstructionsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playSound(SoundManager.SFX_BUTTON_CLICK);
                instructionsOverlay.setVisibility(View.GONE);
            }
        });
        //Pause button
        pauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playSound(SoundManager.SFX_BUTTON_CLICK);
                showPauseDialog();
            }
        });

        createGameOverScreen();
        startScoreUpdates();


    }

    private void createGameOverScreen()
    {
        // Container for game over screen
        gameOverScreen = new LinearLayout(this);
        gameOverScreen.setOrientation(LinearLayout.VERTICAL);
        gameOverScreen.setBackgroundColor(0xE6000000);
        gameOverScreen.setGravity(Gravity.CENTER);
        gameOverScreen.setPadding(60, 100, 60, 100);
        gameOverScreen.setVisibility(View.GONE);

        gameOverTitle = new TextView(this);
        gameOverTitle.setText("GAME OVER");
        gameOverTitle.setTextColor(0xFFFFFFFF);
        gameOverTitle.setTextSize(56);
        gameOverTitle.setGravity(Gravity.CENTER);
        gameOverTitle.setPadding(0, 0, 0, 40);

        gameOverScoreText = new TextView(this);
        gameOverScoreText.setTextColor(0xFFCCCCCC);
        gameOverScoreText.setTextSize(28);
        gameOverScoreText.setGravity(Gravity.CENTER);
        gameOverScoreText.setPadding(0, 0, 0, 60);

        nameInput = new EditText(this);
        nameInput.setHint("Enter your name");
        nameInput.setTextColor(0xFFFFFFFF);
        nameInput.setHintTextColor(0xFF888888);
        nameInput.setBackground(getDrawable(R.drawable.gameover_input_style));
        nameInput.setPadding(30, 25, 30, 25);
        nameInput.setGravity(Gravity.CENTER);
        nameInput.setTextSize(20);

        LinearLayout.LayoutParams inputSize = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputSize.setMargins(0, 0, 0, 30);
        nameInput.setLayoutParams(inputSize);

        submitButton = new Button(this);
        submitButton.setText("SUBMIT SCORE");
        submitButton.setTextColor(0xFFFFFFFF);
        submitButton.setBackground(getDrawable(R.drawable.gameover_button_style));
        submitButton.setPadding(40, 25, 40, 25);
        submitButton.setTextSize(18);
        submitButton.setAllCaps(false);

        gameOverScreen.addView(gameOverTitle);
        gameOverScreen.addView(gameOverScoreText);
        gameOverScreen.addView(nameInput);
        gameOverScreen.addView(submitButton);

        addContentView(gameOverScreen, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    // Start updating the score display
    private void startScoreUpdates()
    {
        if (isUpdatingScore) return;

        scoreHandler = new Handler();
        scoreUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                // Update score text with current score
                if (gameLogic != null && scoreText != null)
                {
                    int currentScore = gameLogic.getScore();
                    scoreText.setText("Distance: "+String.valueOf(currentScore)+"M");
                }

                // Update the score with some delay (i dunno performance reasons)
                if (isUpdatingScore && scoreHandler != null)
                {
                    scoreHandler.postDelayed(this, 100);
                }
            }
        };

        isUpdatingScore = true;
        scoreHandler.post(scoreUpdater);
    }

    // Stop updating the score display
    private void stopScoreUpdates()
    {
        isUpdatingScore = false;
        if (scoreHandler != null && scoreUpdater != null)
        {
            scoreHandler.removeCallbacks(scoreUpdater);
        }
    }

    // Called when the player wins at 2500 distance
    public void onGameWin(int finalScore)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showGameWin(finalScore);
            }
        });
    }

    // Called when game ends by losing
    public void onGameOver(int finalScore)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showGameOver(finalScore);
            }
        });
    }

    // Show the win screen with final score
    private void showGameWin(int finalScore)
    {
        SoundManager.getInstance(this).stopBGM();
        SoundManager.getInstance(this).playSFX(SoundManager.SFX_PLAYER_WON);

        int coinsEarned = finalScore / 10;
        shopManager.addCurrency(coinsEarned);

        gameOverTitle.setText("YOU WIN!");
        gameOverTitle.setTextColor(0xFF00FF00);
        gameOverScoreText.setText("Score: " + finalScore + "\nCoins Earned: " + coinsEarned);
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playSound(SoundManager.SFX_BUTTON_CLICK);
                String playerName = nameInput.getText().toString().trim();
                if (playerName.isEmpty())
                {
                    playerName = "Player";
                }

                // Save score to the leaderboard and go back to the main menu
                MainMenu.addScoreToLeaderboard(GameScene.this, playerName, finalScore);
                Intent mainMenuIntent = new Intent(GameScene.this, MainMenu.class);
                mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainMenuIntent);
                finish();
            }
        });

        // Show the win screen
        gameOverScreen.setVisibility(View.VISIBLE);
    }

    // Show the game over screen with final score
    private void showGameOver(int finalScore)
    {
        SoundManager.getInstance(this).stopBGM();
        SoundManager.getInstance(this).playSFX(SoundManager.SFX_PLAYER_LOST);
        int coinsEarned = finalScore / 10;
        shopManager.addCurrency(coinsEarned);

        gameOverTitle.setText("GAME OVER");
        gameOverTitle.setTextColor(0xFFFFFFFF);
        gameOverScoreText.setText("Score: " + finalScore + "\nCoins Earned: " + coinsEarned);
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playSound(SoundManager.SFX_BUTTON_CLICK);
                String playerName = nameInput.getText().toString().trim();
                if (playerName.isEmpty())
                {
                    playerName = "Player";
                }

                // Save score to the leaderboard and go back to the main menu
                MainMenu.addScoreToLeaderboard(GameScene.this, playerName, finalScore);
                Intent mainMenuIntent = new Intent(GameScene.this, MainMenu.class);
                mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainMenuIntent);
                finish();
            }
        });

        // Show the game over screen
        gameOverScreen.setVisibility(View.VISIBLE);
    }
    public void onMilestoneReached(int metersLeft)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (milestoneAlertText != null) {
                    milestoneAlertText.setText(metersLeft + " meters left!");
                    milestoneAlertText.setVisibility(View.VISIBLE);

                    // Reset timer and hide after 2 seconds
                    alertHandler.removeCallbacks(hideAlertRunnable);
                    alertHandler.postDelayed(hideAlertRunnable, 2000);
                }
            }
        });
    }

    public void enterWormhole()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                gameLogic.pauseGame();
                gameLogic.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                minigameLogic.setVisibility(View.VISIBLE);
                minigameLogic.startMinigame();
            }
        });
    }

    public void exitWormhole(int bonusPoints)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                minigameLogic.stopMinigame();
                minigameLogic.setVisibility(View.GONE);
                gameLogic.addBonusScore(bonusPoints);
                gameLogic.resumeGame();
                gameLogic.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                // Show alert
                if (milestoneAlertText != null)
                {
                    milestoneAlertText.setText("You gained " + bonusPoints + " points!");
                    milestoneAlertText.setVisibility(View.VISIBLE);
                    alertHandler.removeCallbacks(hideAlertRunnable);
                    alertHandler.postDelayed(hideAlertRunnable, 2000);
                }
            }
        });
    }

    private void playSound(int soundId)
    {
        SoundManager.getInstance(this).playSFX(soundId);
    }
    private void showPauseDialog() {
        if (isPauseDialogShowing) return;

        isPauseDialogShowing = true;
        gameLogic.pauseGame();

        new android.app.AlertDialog.Builder(this)
                .setTitle("Pause Game")
                .setMessage("Exit to main menu?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    isPauseDialogShowing = false;
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    isPauseDialogShowing = false;
                    gameLogic.resumeGame();
                    pauseButton.setText("PAUSE");
                })
                .show();
    }
    @Override
    public void onBackPressed()
    {
        showPauseDialog();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (bgmPlayer != null)
        {
            float volume = settingsManager.getMusicVolume() / 100f;
            bgmPlayer.setVolume(volume, volume);

            if (!bgmPlayer.isPlaying())
            {
                bgmPlayer.start();
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (bgmPlayer != null && bgmPlayer.isPlaying())
        {
            bgmPlayer.pause();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopScoreUpdates();
        if (bgmPlayer != null)
        {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (soundPool != null)
        {
            soundPool.release();
            soundPool = null;
        }
    }
}