package com.hejman.dx1221_ica1_project;

import android.graphics.Canvas;
import android.graphics.Paint;

public class EnemyEntity
{
    private float x, y;
    private boolean isOnScreen = false;
    private Paint enemyPaint;

    public EnemyEntity(float spawnX, float spawnY)
    {
        x = spawnX;
        y = spawnY;

        enemyPaint = new Paint();
        enemyPaint.setColor(0xFFFF0000);
        enemyPaint.setAntiAlias(true);
    }

    // Enemy Updates
    public void update(float playerX, float playerY, float cameraY, int gameAreaHeight)
    {
        // Check if enemy is visible
        float screenY = y - cameraY + (gameAreaHeight / 2f);
        isOnScreen = (screenY >= -100 && screenY <= gameAreaHeight + 100);

        // If visible, Move toward player
        if (isOnScreen)
        {
            float deltaX = playerX - x;
            float deltaY = playerY - y;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > 0)
            {
                x += (deltaX / distance) * 3f;
                y += (deltaY / distance) * 3f;
            }
        }
    }

    // Draws the enemy on the screen
    public void draw(Canvas canvas, float cameraX, float cameraY, int gameAreaWidth, int gameAreaHeight)
    {
        float screenX = x - cameraX + (gameAreaWidth / 2f);
        float screenY = y - cameraY + (gameAreaHeight / 2f);

        if (screenY > -100 && screenY < gameAreaHeight + 100)
        {
            canvas.drawCircle(screenX, screenY, 40f, enemyPaint);
        }
    }

    // Check if the enemy collides with the player
    public boolean checkCollision(float playerX, float playerY)
    {
        if (!isOnScreen) return false;

        float deltaX = x - playerX;
        float deltaY = y - playerY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        return distance <= 100f;
    }

    // Check if the enemy should be deleted
    public boolean shouldRemove(float playerY, int gameAreaHeight)
    {
        return y > playerY + (gameAreaHeight * 2);
    }
}