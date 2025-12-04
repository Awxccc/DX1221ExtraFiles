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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamescene);
        gameLogic = findViewById(R.id.game_logic);
        scoreText = findViewById(R.id.score_text);
        Button quitButton = findViewById(R.id.quit_button);

        // Quit Button
        quitButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
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
                    scoreText.setText(String.valueOf(currentScore));
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
        gameOverTitle.setText("YOU WIN!");
        gameOverTitle.setTextColor(0xFF00FF00);
        gameOverScoreText.setText("You have travelled " + finalScore + " meters");
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
        gameOverTitle.setText("GAME OVER");
        gameOverTitle.setTextColor(0xFFFFFFFF);
        gameOverScoreText.setText("You have travelled " + finalScore + " meters");
        submitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopScoreUpdates(); // Stop score updates when activity closes
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopScoreUpdates(); // Stop score updates when activity pauses
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        startScoreUpdates(); // Resume score updates when activity resumes
    }
}