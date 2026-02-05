package com.hejman.dx1221_ica2_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;
import android.media.AudioAttributes;
import android.media.SoundPool;
//Done by Hejy
public class MinigameLogic extends View implements SensorEventListener
{
    // Variables
    private float playerX;
    private final float playerSize = 150f;
    private int screenWidth, screenHeight, bonusPoints = 0;
    private long startTime = 0;
    private static final long MINIGAME_DURATION = 30000; // 30 Seconds
    private boolean isActive = false;
    private final Random random = new Random();

    // Accelerometer ( for the player )
    private SensorManager sensorManager;
    private Sensor accelerometer;
    float tiltSensitivity;

    // Spawn Variables
    private long lastObstacleSpawn = 0, lastCollectibleSpawn = 0;
    private static final long OBSTACLE_SPAWN_INTERVAL = 250;
    private static final long COLLECTIBLE_SPAWN_INTERVAL = 1000;

    // Graphics & Animation
    private Bitmap bmpPlayer, bmpObstacle, bmpCollectible;
    private final Matrix rotationMatrix = new Matrix();
    private float currentRotation = 0f, obstacleRotation = 0f;

    // Paint Colours
    private Paint playerPaint, timerPaint, instructionPaint, pointsPaint, bgPaint, particlePaint;

    // Audio
    private SoundPool soundPool;
    private int starPickupId, explosionId;
    private float sfxVolume = 1.0f;

    private static class Particle
    {
        float x, y, speed;

        Particle(float x, float y, float speed)
        {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
    private final ArrayList<Particle> particles = new ArrayList<>();

    private static class Obstacle
    {
        float x, y, speed;

        Obstacle(float x, float y, float speed)
        {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();

    private static class Collectible
    {
        float x, y, speed;

        Collectible(float x, float y, float speed)
        {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
    private final ArrayList<Collectible> collectibles = new ArrayList<>();

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (!isActive) return;

        long currentTime = System.currentTimeMillis();
        canvas.drawRect(0, 0, screenWidth, screenHeight, bgPaint);

        currentRotation = (currentRotation + 2) % 360;
        obstacleRotation = (obstacleRotation - 1) % 360;

        drawSpeedLineParticles(canvas);
        drawObstacles(canvas);
        drawCollectibles(canvas);
        drawPlayer(canvas);
        drawUI(canvas, currentTime);

        updateGameLogic(currentTime);
        invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (!isActive) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            playerX -= event.values[0] * tiltSensitivity;

            float halfSize = playerSize / 2f;
            if (playerX < halfSize) playerX = halfSize;
            if (playerX > screenWidth - halfSize) playerX = screenWidth - halfSize;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        screenWidth = width;
        screenHeight = height;
        playerX = screenWidth / 2f;

        // Load and Scale Sprites
        Bitmap rawHexagon = BitmapFactory.decodeResource(getResources(), R.drawable.hexagon);
        Bitmap rawObstacle = BitmapFactory.decodeResource(getResources(), R.drawable.img_clumped);
        Bitmap rawCollectible = BitmapFactory.decodeResource(getResources(), R.drawable.img_star);

        int hexHeight = (int) (playerSize * 0.8453f);
        bmpPlayer = Bitmap.createScaledBitmap(rawHexagon, (int) playerSize, hexHeight, true);
        bmpObstacle = Bitmap.createScaledBitmap(rawObstacle, 120, 120, true);
        bmpCollectible = Bitmap.createScaledBitmap(rawCollectible, 120, 120, true);

        createInitialParticles();
    }

    public MinigameLogic(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupColours();
        setupSensor(context);
        setupAudio(context);
        SettingsManager settings = new SettingsManager(context);
        tiltSensitivity = settings.getTiltSensitivityValue();
    }

    private void updateGameLogic(long currentTime)
    {
        spawnObjects(currentTime);
        updateObstacles();
        updateCollectibles();

        // Check if the time is up ( send the player out of the mini-game )
        if (currentTime - startTime >= MINIGAME_DURATION)
        {
            endMinigameSuccess();
        }
    }

    private void setupColours()
    {
        // Player
        playerPaint = new Paint();
        playerPaint.setColorFilter(new PorterDuffColorFilter(0xFFB4FFAF, PorterDuff.Mode.MULTIPLY));
        playerPaint.setAntiAlias(true);

        // Timer
        timerPaint = new Paint();
        timerPaint.setColor(0xFFFFFFFF);
        timerPaint.setTextSize(64f);
        timerPaint.setTextAlign(Paint.Align.CENTER);
        timerPaint.setAntiAlias(true);

        // Instructions
        instructionPaint = new Paint();
        instructionPaint.setColor(0xFFCCCCCC);
        instructionPaint.setTextSize(50f);
        instructionPaint.setTextAlign(Paint.Align.CENTER);
        instructionPaint.setAntiAlias(true);

        // Points Earned
        pointsPaint = new Paint();
        pointsPaint.setColor(0xFFFFFFFF);
        pointsPaint.setTextSize(50f);
        pointsPaint.setTextAlign(Paint.Align.CENTER);
        pointsPaint.setAntiAlias(true);

        // Background
        bgPaint = new Paint();
        bgPaint.setColor(0xFF1e192d);

        // Particle Emitters
        particlePaint = new Paint();
        particlePaint.setColor(0xFFFFFFFF);
        particlePaint.setAlpha(150);
        particlePaint.setStrokeWidth(2f);
        particlePaint.setAntiAlias(true);
    }

    private void createInitialParticles()
    {
        particles.clear();
        for (int i = 0; i < 40; i++)
        {
            float x = random.nextFloat() * screenWidth;
            float y = random.nextFloat() * screenHeight;
            float speed = 1 + random.nextFloat() * 2;
            particles.add(new Particle(x, y, speed));
        }
    }

    private void spawnObjects(long currentTime)
    {
        if (currentTime - lastObstacleSpawn >= OBSTACLE_SPAWN_INTERVAL)
        {
            if (random.nextInt(100) < 10)
            {
                float spawnX = 60 + random.nextFloat() * (screenWidth - 120);
                obstacles.add(new Obstacle(spawnX, -50, 8 + random.nextFloat() * 4));
            }
            lastObstacleSpawn = currentTime;
        }

        if (currentTime - lastCollectibleSpawn >= COLLECTIBLE_SPAWN_INTERVAL)
        {
            if (random.nextInt(100) < 25)
            {
                int count = 1 + random.nextInt(3);
                float spawnX = 60 + random.nextFloat() * (screenWidth - 120);

                for (int i = 0; i < count; i++)
                {
                    collectibles.add(new Collectible(spawnX, -60 - (i * 120f), 8f));
                }
            }
            lastCollectibleSpawn = currentTime;
        }
    }

    private void updateObstacles()
    {
        float playerY = screenHeight - 300f;

        for (int i = obstacles.size() - 1; i >= 0; i--)
        {
            Obstacle obs = obstacles.get(i);
            obs.y += obs.speed;

            if (obs.y > screenHeight + 100)
            {
                obstacles.remove(i);
            }
            else
            {
                float deltaX = obs.x - playerX;
                float deltaY = obs.y - playerY;
                float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance < 90)
                {
                    playSound(SoundManager.SFX_EXPLOSION);
                    endMinigameFailed();
                    return;
                }
            }
        }
    }

    private void updateCollectibles()
    {
        float playerY = screenHeight - 300f;

        for (int i = collectibles.size() - 1; i >= 0; i--)
        {
            Collectible col = collectibles.get(i);
            col.y += col.speed;

            if (col.y > screenHeight + 100)
            {
                collectibles.remove(i);
            }
            else
            {
                float deltaX = col.x - playerX;
                float deltaY = col.y - playerY;
                float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance < 80)
                {
                    playSound(SoundManager.SFX_STAR_PICKUP);
                    bonusPoints += 125;
                    collectibles.remove(i);
                }
            }
        }
    }

    private void drawSpeedLineParticles(Canvas canvas)
    {
        for (Particle p : particles)
        {
            canvas.drawLine(p.x, p.y, p.x, p.y - (p.speed * 35), particlePaint);
            p.y += p.speed * 4;

            if (p.y > screenHeight + 100)
            {
                p.y = -100;
                p.x = random.nextFloat() * screenWidth;
                p.speed = 3 + random.nextFloat() * 4;
            }
        }
    }

    private void drawRotatedBitmap(Canvas canvas, Bitmap bitmap, float x, float y, float angle, Paint paint)
    {
        rotationMatrix.reset();
        rotationMatrix.postTranslate(-bitmap.getWidth() / 2f, -bitmap.getHeight() / 2f);
        rotationMatrix.postRotate(angle);
        rotationMatrix.postTranslate(x, y);

        canvas.drawBitmap(bitmap, rotationMatrix, paint);
    }

    private void drawObstacles(Canvas canvas)
    {
        for (Obstacle obs : obstacles)
        {
            rotationMatrix.reset();
            rotationMatrix.postTranslate(-bmpObstacle.getWidth() / 2f, -bmpObstacle.getHeight() / 2f);
            rotationMatrix.postRotate(obstacleRotation);
            rotationMatrix.postTranslate(obs.x, obs.y);
            canvas.drawBitmap(bmpObstacle, rotationMatrix, null);
        }
    }

    private void drawCollectibles(Canvas canvas)
    {
        for (Collectible col : collectibles)
        {
            canvas.drawBitmap(bmpCollectible, col.x - 60, col.y - 60, null);
        }
    }

    private void drawPlayer(Canvas canvas)
    {
        float playerY = screenHeight - 300f;
        drawRotatedBitmap(canvas, bmpPlayer, playerX, playerY, currentRotation, playerPaint);
    }

    private void drawUI(Canvas canvas, long currentTime)
    {
        long remaining = (MINIGAME_DURATION - (currentTime - startTime)) / 1000;
        if (remaining < 0) remaining = 0;

        canvas.drawText(String.valueOf(remaining), screenWidth / 2f, 260f, timerPaint);
        canvas.drawText("Avoid obstacles and collect stars", screenWidth / 2f, 320f, instructionPaint);
        canvas.drawText("Survive to keep your score!", screenWidth / 2f, 380f, instructionPaint);
        canvas.drawText("Points Earned: " + bonusPoints, screenWidth / 2f, 460f, pointsPaint);
    }

    private void endMinigameSuccess()
    {
        stopMinigame();
        if (getContext() instanceof GameScene)
        {
            ((GameScene) getContext()).exitWormhole(bonusPoints);
        }
    }

    private void endMinigameFailed()
    {
        stopMinigame();
        if (getContext() instanceof GameScene)
        {
            ((GameScene) getContext()).exitWormhole(0);
        }
    }

    private void setupSensor(Context context)
    {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null)
        {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void setupAudio(Context context)
    {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load the sounds
        starPickupId = soundPool.load(context, R.raw.mg_starpickup, 1);
        explosionId = soundPool.load(context, R.raw.mg_explosion, 1);

        // Get volume settings
        SettingsManager settingsManager = new SettingsManager(context);
        sfxVolume = settingsManager.getSFXVolume() / 100f;
    }

    private void playSound(int soundId)
    {
        SoundManager.getInstance(getContext()).playSFX(soundId);
    }

    // Start the minigame
    public void startMinigame()
    {
        SettingsManager settings = new SettingsManager(getContext());
        tiltSensitivity = settings.getTiltSensitivityValue();
        isActive = true;
        startTime = System.currentTimeMillis();
        lastObstacleSpawn = startTime;
        lastCollectibleSpawn = startTime;
        playerX = screenWidth / 2f;
        bonusPoints = 375;
        obstacles.clear();
        collectibles.clear();

        if (sensorManager != null && accelerometer != null)
        {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        invalidate();
    }

    // Stop the minigame
    public void stopMinigame()
    {
        isActive = false;
        if (sensorManager != null)
        {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        stopMinigame();
        if (soundPool != null)
        {
            soundPool.release();
            soundPool = null;
        }
    }
}