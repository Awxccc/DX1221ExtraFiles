package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class GameLogic extends View
{
    // Variables
    private float playerX = 0, playerY = 0, cameraX = 0, cameraY = 0, cameraSpeed = 0.05f;
    private int gameAreaWidth, gameAreaHeight, score = 0,lastMilestone = 0;
    private static final int WIN_SCORE = 2500;
    private boolean gameStarted = false, gameWon = false, gameOver = false;
    private final Random random = new Random();
    private BackgroundEntity background;

    // Node Spawning Variables
    private final float nodeSize = 60f;
    private float howFarAheadToSpawn, whenToSpawnMore, howWideToSpawn, spaceBetweenNodes, lastNodeSpawnY = 0;
    private final ArrayList<Node> nodes = new ArrayList<>();
    private final ArrayList<EnemyEntity> enemies = new ArrayList<>();

    // Power-Ups Variables
    private static final int POWERUP_NONE = 0;
    private static final int POWERUP_TUNNELLER = 1;
    private static final int POWERUP_RANGE_ENHANCER = 2;
    private static final int POWERUP_CONNECTION_STABILIZER = 3;
    private static final int POWERUP_HIJACKED = 4;
    private static final int POWERUP_SIGNAL_BYPASS = 5;
    private final int COLOUR_PLAYER = 0xFFB4FFAF;
    private final int COLOUR_TUNNELLER = 0xFF78EBF5;
    private final int COLOUR_RANGE = 0xFFA54BE1;
    private final int COLOUR_STABILIZER = 0xFFE17341;
    private final int COLOUR_BYPASS = 0xFFE66E91;

    // Power-Ups Timers
    private boolean isTunnellerActive = false;
    private long tunnellerEndTime = 0, tunnellerActionTimer = 0;
    private final long TUNNELLER_DURATION = 2500;
    private boolean isRangeEnhanced = false;
    private long rangeEnhancerEndTime = 0, rangeEnhancerPausedTime = 0;
    private final long RANGE_ENHANCER_DURATION = 10000;
    private boolean isConnectionStabilizerActive = false;
    private long connectionStabilizerEndTime = 0;
    private final long CONNECTION_STABILIZER_DURATION = 5000;
    private boolean isSignalBypassActive = false;
    private long signalBypassEndTime = 0;
    private final long SIGNAL_BYPASS_DURATION = 5000;

    // Trail Variables
    private final ArrayList<float[]> trailSegments = new ArrayList<>();
    private static final float TRAIL_TYPE_NORMAL = 0f;
    private static final float TRAIL_TYPE_TUNNELLER = 1f;
    private static final float TRAIL_TYPE_RANGE = 2f;
    private static final float TRAIL_TYPE_STABILIZER = 3f;
    private static final float TRAIL_TYPE_BYPASS = 5f;

    // Click Range System Variables
    private boolean isShrinking = false;
    private float clickRangeTop = 0, clickRangeBottom = 0;
    private long lastShrinkTime = 0;
    private static final long SHRINK_INTERVAL = 50;
    private static final float BASE_SHRINK_SPEED = 5f, MAX_SHRINK_SPEED = 20f;
    private float normalRangeSize, enhancedRangeSize;

    // Graphics & Animation
    private Bitmap bmpPlayer, bmpNode, bmpPowerup, bmpEnemy, bmpDeath;
    private final Matrix rotationMatrix = new Matrix();
    private float currentRotation = 0f;

    // Death Animation Variables
    private boolean isDying = false;
    private long deathStartTime = 0;
    private float deathYOffset = 0;
    private static final long DEATH_DURATION = 2000;
    private final Rect deathSrcRect = new Rect();
    private final Rect deathDstRect = new Rect();

    //Smoke anim
    private Bitmap bmpSmoke;
    private final ArrayList<SmokeParticle> smokeParticles = new ArrayList<>();
    private static final int SMOKE_FRAME_COUNT = 10;
    private static final int SMOKE_FRAME_DURATION = 50;

    // Paint Colours
    private Paint playerColour, nodeColour, oldNodeColour, tunnellerNodeColour, rangeEnhancerNodeColour;
    private Paint connectionStabilizerNodeColour, hijackedNodeColour, trailPaint, clickRangePaint;
    private Paint signalBypassNodeColour, deathPaint;

    //Audio
    private SoundPool soundPool;
    private int nodeMoveId, gameStartId, powerupCollectedId;
    private float sfxVolume = 1.0f;

    private class Node
    {
        boolean active = true; // Is the player allowed to click on it
        float x, y;
        int powerUpType = POWERUP_NONE;

        Node(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        // Check if this node is ahead of player
        boolean isAheadOfPlayer()
        {
            return y <= playerY;
        }

        // Check if the player has clicked on this node
        boolean wasClicked(float clickX, float clickY)
        {
            if (!isAheadOfPlayer())
                return false;

            if (powerUpType == POWERUP_HIJACKED) // Hijacked nodes cannot be clicked at all
            {
                if (!isSignalBypassActive)
                {
                    return false;
                }
            }

            float screenX = x - cameraX + (gameAreaWidth / 2f);
            float screenY = y - cameraY + (gameAreaHeight / 2f);
            float deltaX = clickX - screenX;
            float deltaY = clickY - screenY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            return distance <= 65;
        }

        // Check if the node is within click range
        boolean isInClickRange()
        {
            // Special Player States
            if (!gameStarted || isTunnellerActive || isConnectionStabilizerActive)
                return true;

            float screenY = y - cameraY + (gameAreaHeight / 2f);
            return screenY >= clickRangeTop && screenY <= clickRangeBottom;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        background.draw(canvas);
        updateCamera();
        updateClickRangeColor();

        currentRotation = (currentRotation + 2) % 360;

        if (shouldShowRange())
        {
            canvas.drawRect(0, clickRangeTop, gameAreaWidth, clickRangeBottom, clickRangePaint);
        }

        drawTrails(canvas);
        float playerScreenX = playerX - cameraX + (gameAreaWidth / 2f);
        float playerScreenY = playerY - cameraY + (gameAreaHeight / 2f);
        drawSmokeParticles(canvas);
        if (isDying)
        {
            drawDeathAnimation(canvas, playerScreenX, playerScreenY);
        }
        else
        {
            drawRotatedBitmap(canvas, bmpPlayer, playerScreenX, playerScreenY, currentRotation, getCurrentPlayerPaint());
        }
        drawNodes(canvas);

        for (EnemyEntity enemy : enemies)
        {
            enemy.draw(canvas, cameraX, cameraY, gameAreaWidth, gameAreaHeight);
        }

        updateGameLogic();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check each node for player clicks
            for (Node node : nodes)
            {
                if (node.active && node.wasClicked(touchX, touchY))
                {
                    // Check if node is within click range
                    if (!node.isInClickRange())
                    {
                        return true;
                    }

                    // Start the game on the first node click
                    if (!gameStarted)
                    {
                        gameStarted = true;
                        isShrinking = true;
                        lastShrinkTime = System.currentTimeMillis();
                        playSound(gameStartId);
                    }
                    else
                    {
                        playSound(nodeMoveId);
                    }

                    if (node.powerUpType == POWERUP_HIJACKED && isSignalBypassActive)
                    {
                        isSignalBypassActive = false;
                    }

                    float oldX = playerX;
                    float oldY = playerY;
                    playerX = node.x;
                    playerY = node.y;
                    spawnSmoke(playerX, playerY + nodeSize);
                    node.active = false;
                    activatePowerUp(node.powerUpType);

                    // Reset click range (if not already tunnelling)
                    if (!isTunnellerActive)
                    {
                        resetClickRange();
                    }

                    addTrail(oldX, oldY, playerX, playerY, getCurrentTrailType());
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        gameAreaWidth = width;
        gameAreaHeight = height - 100;

        // Load and Scale Sprites
        int diameter = (int) (nodeSize * 2);
        int diameterHexHeight = (int) ((nodeSize * 2)*0.8453);
        int diameterTriangleHeight= (int) ((nodeSize * 2)*0.866);

        Bitmap rawSmoke = BitmapFactory.decodeResource(getResources(), R.drawable.hexagonsmoke);
        bmpSmoke = rawSmoke;

        Bitmap rawHexagon = BitmapFactory.decodeResource(getResources(), R.drawable.hexagon);
        Bitmap rawTriangle = BitmapFactory.decodeResource(getResources(), R.drawable.triangle);
        Bitmap rawSquare = BitmapFactory.decodeResource(getResources(), R.drawable.square);

        bmpPlayer = Bitmap.createScaledBitmap(rawHexagon, diameter, diameterHexHeight, true);
        bmpEnemy = Bitmap.createScaledBitmap(rawHexagon, diameter, diameterHexHeight, true);
        bmpNode = Bitmap.createScaledBitmap(rawTriangle, diameter, diameterTriangleHeight, true);
        bmpPowerup = Bitmap.createScaledBitmap(rawSquare, diameter, diameter, true);

        bmpDeath = BitmapFactory.decodeResource(getResources(), R.drawable.hexagondeath);

        // Calculate spawning distances based on screen size
        howFarAheadToSpawn = gameAreaHeight * 1.5f;
        whenToSpawnMore = gameAreaHeight * 0.5f;
        howWideToSpawn = gameAreaWidth * 0.5f;
        spaceBetweenNodes = (nodeSize * 4) + 20;

        // Set the "camera" position at the player's position
        cameraX = playerX;
        cameraY = playerY;

        // Set up click range sizes for normal and enhanced ranges
        enhancedRangeSize = gameAreaHeight;
        normalRangeSize = gameAreaHeight * 0.6f;

        // Initialize click range at a fixed position
        float playerScreenY = (gameAreaHeight / 2f) + 500f;
        float halfRange = normalRangeSize / 2f;
        clickRangeTop = playerScreenY - halfRange;
        clickRangeBottom = playerScreenY + halfRange;

        // BG here - also create initial nodes
        background = new BackgroundEntity(getContext(), width, height);
        createInitialNodes();
    }

    public GameLogic(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupColours();
        setupAudio(context);
    }

    private void updateGameLogic()
    {
        // Just in case, this is added to prevent any messy business
        if (gameOver || isDying)
            return;

        updateClickRange();
        updatePowerUps();
        updateEnemies();

        // Check for win condition at 2500 points
        if (score >= WIN_SCORE && !gameWon)
        {
            gameWon = true;
            triggerGameWin();
            return;
        }

        // Check if we need more nodes
        float distancePlayerMoved = lastNodeSpawnY - playerY;
        if (distancePlayerMoved >= whenToSpawnMore)
        {
            createOffScreenNodes();
            lastNodeSpawnY = playerY;
        }

        // Update score and clean up old nodes
        score = (int) Math.abs(playerY / 100);

        if (score >= lastMilestone + 250)
        {
            lastMilestone = (score / 250) * 250;

            int metersLeft = WIN_SCORE - lastMilestone;
            if (metersLeft > 0)
            {
                triggerMilestoneAlert(metersLeft);
            }
        }

        removeOldNodes();
    }

    private void setupColours()
    {
        // Player colour
        playerColour = new Paint();
        playerColour.setColorFilter(new PorterDuffColorFilter(0xFFB4FFAF, PorterDuff.Mode.MULTIPLY));
        playerColour.setAntiAlias(true);

        // Normal node colour
        nodeColour = new Paint();
        nodeColour.setColorFilter(new PorterDuffColorFilter(0xFFCDEBFF, PorterDuff.Mode.MULTIPLY));
        nodeColour.setAntiAlias(true);

        // Inactive node colour
        oldNodeColour = new Paint();
        oldNodeColour.setColorFilter(new PorterDuffColorFilter(0xFF464B55, PorterDuff.Mode.MULTIPLY));
        oldNodeColour.setAntiAlias(true);

        // Tunneller power-up colour
        tunnellerNodeColour = new Paint();
        tunnellerNodeColour.setColorFilter(new PorterDuffColorFilter(0xFF78EBF5, PorterDuff.Mode.MULTIPLY));
        tunnellerNodeColour.setAntiAlias(true);

        // Range enhancer power-up colour
        rangeEnhancerNodeColour = new Paint();
        rangeEnhancerNodeColour.setColorFilter(new PorterDuffColorFilter(0xFFA54BE1, PorterDuff.Mode.MULTIPLY));
        rangeEnhancerNodeColour.setAntiAlias(true);

        // Connection stabilizer power-up colour
        connectionStabilizerNodeColour = new Paint();
        connectionStabilizerNodeColour.setColorFilter(new PorterDuffColorFilter(0xFFE17341, PorterDuff.Mode.MULTIPLY));
        connectionStabilizerNodeColour.setAntiAlias(true);

        // Signal bypass power-up colour
        signalBypassNodeColour = new Paint();
        signalBypassNodeColour.setColorFilter(new PorterDuffColorFilter(0xFFE66E91, PorterDuff.Mode.MULTIPLY));
        signalBypassNodeColour.setAntiAlias(true);

        // Hijacked node colour
        hijackedNodeColour = new Paint();
        hijackedNodeColour.setColorFilter(new PorterDuffColorFilter(0xFFA52828, PorterDuff.Mode.MULTIPLY));
        hijackedNodeColour.setAntiAlias(true);

        // Trail lines
        trailPaint = new Paint();
        trailPaint.setStrokeWidth(30f);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        trailPaint.setAntiAlias(true);

        // Click Range
        clickRangePaint = new Paint();
        clickRangePaint.setColor(0x40B4FFAF);
        clickRangePaint.setAntiAlias(true);
    }

    private void createInitialNodes()
    {
        // Add the first node directly ahead of the player
        nodes.add(new Node(0, playerY - 200));

        // Create nodes from the player position to spawn distance
        float startY = playerY - 300;
        float endY = playerY - howFarAheadToSpawn;

        for (float y = startY; y >= endY; y -= 200)
        {
            createNewNodes(y);
        }

        lastNodeSpawnY = playerY;
    }

    private void createOffScreenNodes()
    {
        float furthestY = playerY;
        for (Node node : nodes)
        {
            if (node.active && node.y < furthestY)
            {
                furthestY = node.y;
            }
        }

        // Create nodes from furthest point to new spawn limit
        float newSpawnLimit = playerY - howFarAheadToSpawn;
        for (float y = furthestY - 200; y >= newSpawnLimit; y -= 200)
        {
            createNewNodes(y);
            createNewEnemy(y);
        }
    }

    private void createNewNodes(float y)
    {
        // Get all possible node positions
        ArrayList<Float> possibleX = getValidNodePositions(playerX);
        int nodeCount = Math.min(random.nextInt(3) + 1, possibleX.size());

        // Pick random positions to spawn at
        ArrayList<Float> chosenX = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++)
        {
            if (!possibleX.isEmpty())
            {
                int randomIndex = random.nextInt(possibleX.size());
                chosenX.add(possibleX.remove(randomIndex));
            }
        }

        // Create nodes at the chosen positions
        for (float x : chosenX)
        {
            Node newNode = new Node(x, y);

            // First check if node is allowed to become hijacked
            if (score >= 250)
            {
                // Calculate hijacked chance
                // Base odds of 2% at 250m, increment by 2% for every 250m
                int hijackedChance = 2 + ((score - 250) / 250) * 2;
                hijackedChance = Math.min(hijackedChance, 20);

                if (random.nextInt(100) < hijackedChance)
                {
                    newNode.powerUpType = POWERUP_HIJACKED;
                    nodes.add(newNode);
                    continue; // Skip power-up check if hijacked
                }
            }

            // If not hijacked, check for power-up at 2% spawn rate, at a 33%/33%/33% split
            if (random.nextInt(50) == 0)
            {
                int[] types = {POWERUP_TUNNELLER, POWERUP_RANGE_ENHANCER, POWERUP_CONNECTION_STABILIZER, POWERUP_SIGNAL_BYPASS};
                newNode.powerUpType = types[random.nextInt(types.length)];
            }

            nodes.add(newNode);
        }
    }

    private void removeOldNodes()
    {
        // Remove nodes that is behind the player
        for (int i = nodes.size() - 1; i >= 0; i--)
        {
            Node node = nodes.get(i);
            if (node.y > playerY + (gameAreaHeight * 2))
            {
                nodes.remove(i);
            }
        }

        // Remove all trails that is no longer visible
        for (int i = trailSegments.size() - 1; i >= 0; i--)
        {
            float[] segment = trailSegments.get(i);
            float y1 = segment[1];
            float y2 = segment[3];

            if (y1 > playerY + (gameAreaHeight * 2) && y2 > playerY + (gameAreaHeight * 2))
            {
                trailSegments.remove(i);
            }
        }
    }

    private void activatePowerUp(int powerUpType)
    {
        if (powerUpType != POWERUP_NONE && powerUpType != POWERUP_HIJACKED)
        {
            playSound(powerupCollectedId);
        }
        long currentTime = System.currentTimeMillis();

        if (powerUpType == POWERUP_TUNNELLER)
        {
            isTunnellerActive = true;
            tunnellerEndTime = currentTime + TUNNELLER_DURATION;
            tunnellerActionTimer = currentTime;
        }
        else if (powerUpType == POWERUP_RANGE_ENHANCER)
        {
            isRangeEnhanced = true;
            rangeEnhancerEndTime = currentTime + RANGE_ENHANCER_DURATION;
        }
        else if (powerUpType == POWERUP_CONNECTION_STABILIZER)
        {
            isConnectionStabilizerActive = true;
            connectionStabilizerEndTime = currentTime + CONNECTION_STABILIZER_DURATION;

            // Pause range enhancer if active
            if (isRangeEnhanced && rangeEnhancerPausedTime == 0)
            {
                rangeEnhancerPausedTime = currentTime;
            }
        }
        else if (powerUpType == POWERUP_SIGNAL_BYPASS)
        {
            isSignalBypassActive = true;
            signalBypassEndTime = currentTime + SIGNAL_BYPASS_DURATION;
        }
    }

    private void updatePowerUps()
    {
        long currentTime = System.currentTimeMillis();

        if (isSignalBypassActive)
        {
            if (currentTime > signalBypassEndTime)
            {
                isSignalBypassActive = false;
            }
        }

        if (isConnectionStabilizerActive)
        {
            if (currentTime > connectionStabilizerEndTime)
            {
                isConnectionStabilizerActive = false;

                if (isRangeEnhanced && rangeEnhancerPausedTime > 0)
                {
                    long pausedDuration = currentTime - rangeEnhancerPausedTime;
                    rangeEnhancerEndTime += pausedDuration;
                    rangeEnhancerPausedTime = 0;
                }

                if (gameStarted && !isTunnellerActive)
                {
                    resetClickRange();
                }
            }
        }

        if (isTunnellerActive)
        {
            if (currentTime > tunnellerEndTime)
            {
                isTunnellerActive = false;

                if (gameStarted && !isConnectionStabilizerActive)
                {
                    resetClickRange();
                }
            }
            else
            {
                isShrinking = false;

                if (currentTime - tunnellerActionTimer > 150)
                {
                    hopToNearestNode();
                    tunnellerActionTimer = currentTime;
                }
            }
        }

        if (isRangeEnhanced && !isConnectionStabilizerActive)
        {
            if (currentTime > rangeEnhancerEndTime)
            {
                isRangeEnhanced = false;

                if (gameStarted && !isTunnellerActive)
                {
                    resetClickRange();
                }
            }
        }
    }

    private void hopToNearestNode()
    {
        Node bestNode = null;
        float minDistance = Float.MAX_VALUE;

        for (Node node : nodes)
        {
            if (node.active && node.y < playerY)
            {
                float deltaX = node.x - playerX;
                float deltaY = node.y - playerY;
                float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance < minDistance)
                {
                    minDistance = distance;
                    bestNode = node;
                }
            }
        }

        if (bestNode != null)
        {
            addTrail(playerX, playerY, bestNode.x, bestNode.y, TRAIL_TYPE_TUNNELLER);
            playerX = bestNode.x;
            playerY = bestNode.y;
            bestNode.active = false;
        }
        else
        {
            isTunnellerActive = false;
        }
    }

    // Update the click range during shrinking
    private void updateClickRange()
    {
        // Don't shrink if the connection stabilizer power-up is active or if the game hasn't started yet
        if (!gameStarted || !isShrinking || isConnectionStabilizerActive)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShrinkTime >= SHRINK_INTERVAL)
        {
            // Calculate dynamic shrink speed based on distance
            float currentShrinkSpeed;
            if (score >= 2000) // If distance is above 2000m, force max shrink speed
            {
                currentShrinkSpeed = MAX_SHRINK_SPEED;
            }
            else // Otherwise, slowly scale up the speed over distance
            {
                currentShrinkSpeed = BASE_SHRINK_SPEED + ((float) score / 2000) * 15f;
            }

            clickRangeTop += currentShrinkSpeed;
            clickRangeBottom -= currentShrinkSpeed;

            // Stop shrinking when the click range reaches zero
            if (clickRangeTop >= clickRangeBottom)
            {
                float centerY = (clickRangeTop + clickRangeBottom) / 2f;
                clickRangeTop = centerY;
                clickRangeBottom = centerY;
                isShrinking = false;
                startDeathSequence();
            }

            lastShrinkTime = currentTime;
        }
    }

    private void resetClickRange()
    {
        float playerScreenY = (gameAreaHeight / 2f) + 500f;
        float currentRangeSize;

        // Choose range size based on active power-ups
        if (isConnectionStabilizerActive)
        {
            currentRangeSize = enhancedRangeSize;
        }
        else if (isRangeEnhanced)
        {
            currentRangeSize = enhancedRangeSize;
        }
        else
        {
            currentRangeSize = normalRangeSize;
        }

        // Set click range around the player
        float halfRange = currentRangeSize / 2f;
        clickRangeTop = playerScreenY - halfRange;
        clickRangeBottom = playerScreenY + halfRange;

        // Enable or disable shrinking
        if (isConnectionStabilizerActive)
        {
            isShrinking = false;
        }
        else
        {
            isShrinking = true;
            lastShrinkTime = System.currentTimeMillis();
        }
    }

    private void updateClickRangeColor()
    {
        if (isConnectionStabilizerActive)
        {
            clickRangePaint.setColor(0x40FF8000); // Orange
        }
        else if (isRangeEnhanced)
        {
            clickRangePaint.setColor(0x40A54BC8); // Purple
        }
        else
        {
            clickRangePaint.setColor(0x4046CD7D); // Green
        }
    }

    private void addTrail(float startX, float startY, float endX, float endY, float type)
    {
        trailSegments.add(new float[]{startX, startY, endX, startY, type});
        trailSegments.add(new float[]{endX, startY, endX, endY, type});
    }

    // Gets the Trail ID matching current state
    private float getCurrentTrailType() {
        if (isTunnellerActive) return TRAIL_TYPE_TUNNELLER;
        if (isConnectionStabilizerActive) return TRAIL_TYPE_STABILIZER;
        if (isRangeEnhanced) return TRAIL_TYPE_RANGE;
        if (isSignalBypassActive) return TRAIL_TYPE_BYPASS;
        return TRAIL_TYPE_NORMAL;
    }

    private void drawTrails(Canvas canvas)
    {
        for (float[] segment : trailSegments)
        {
            float startX = segment[0] - cameraX + (gameAreaWidth / 2f);
            float startY = segment[1] - cameraY + (gameAreaHeight / 2f);
            float endX = segment[2] - cameraX + (gameAreaWidth / 2f);
            float endY = segment[3] - cameraY + (gameAreaHeight / 2f);
            float type = segment[4];

            // Set trail color based on type
            if (type == TRAIL_TYPE_TUNNELLER) trailPaint.setColor(COLOUR_TUNNELLER);
            else if (type == TRAIL_TYPE_STABILIZER) trailPaint.setColor(COLOUR_STABILIZER);
            else if (type == TRAIL_TYPE_RANGE) trailPaint.setColor(COLOUR_RANGE);
            else if (type == TRAIL_TYPE_BYPASS) trailPaint.setColor(COLOUR_BYPASS);
            else trailPaint.setColor(COLOUR_PLAYER);

            // Only draw trail if visible on screen
            if (Math.max(startY, endY) > -100 && Math.min(startY, endY) < gameAreaHeight + 100)
            {
                canvas.drawLine(startX, startY, endX, endY, trailPaint);
            }
        }
    }

    private class SmokeParticle
    {
        float x, y;
        int currentFrame = 0;
        long lastUpdate;
        boolean active = true;

        SmokeParticle(float x, float y)
        {
            this.x = x;
            this.y = y;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    private void spawnSmoke(float x, float y)
    {
        smokeParticles.add(new SmokeParticle(x, y));
    }

    private void drawSmokeParticles(Canvas canvas)
    {
        if (bmpSmoke == null) return;

        long currentTime = System.currentTimeMillis();
        int frameWidth = bmpSmoke.getWidth() / SMOKE_FRAME_COUNT;
        int frameHeight = bmpSmoke.getHeight();
        Rect src = new Rect();
        Rect dst = new Rect();

        for (int i = smokeParticles.size() - 1; i >= 0; i--)
        {
            SmokeParticle p = smokeParticles.get(i);
            if (currentTime - p.lastUpdate > SMOKE_FRAME_DURATION)
            {
                p.currentFrame++;
                p.lastUpdate = currentTime;
                if (p.currentFrame >= SMOKE_FRAME_COUNT)
                {
                    p.active = false;
                }
            }

            if (p.active)
            {
                float screenX = p.x - cameraX + (gameAreaWidth / 2f);
                float screenY = p.y - cameraY + (gameAreaHeight / 2f);

                if (screenY > -200 && screenY < gameAreaHeight + 200)
                {
                    src.set(p.currentFrame * frameWidth, 0, (p.currentFrame + 1) * frameWidth, frameHeight);
                    float size = nodeSize * 2.5f;
                    dst.set((int)(screenX - size/2), (int)(screenY - size/2), (int)(screenX + size/2), (int)(screenY + size/2));

                    canvas.drawBitmap(bmpSmoke, src, dst, null);
                }
            }
            else
            {
                smokeParticles.remove(i);
            }
        }
    }

    private void drawRotatedBitmap(Canvas canvas, Bitmap bitmap, float x, float y, float angle, Paint paint)
    {
        rotationMatrix.reset();
        // Rotate
        rotationMatrix.postTranslate(-bitmap.getWidth() / 2f, -bitmap.getHeight() / 2f);
        rotationMatrix.postRotate(angle);
        rotationMatrix.postTranslate(x, y);

        canvas.drawBitmap(bitmap, rotationMatrix, paint);
    }

    private void drawDeathAnimation(Canvas canvas, float x, float y)
    {
        long elapsed = System.currentTimeMillis() - deathStartTime;

        if (elapsed > DEATH_DURATION)
        {
            triggerGameOver();
            return;
        }

        deathYOffset += 2f;
        float drawY = y - deathYOffset;

        int frameCount = 10;
        if (bmpDeath != null) {
            int frameWidth = bmpDeath.getWidth() / frameCount;
            int frameHeight = bmpDeath.getHeight();

            int frameIndex = (int) ((elapsed / (float) DEATH_DURATION) * frameCount);
            if (frameIndex >= frameCount) frameIndex = frameCount - 1;

            deathSrcRect.set(frameIndex * frameWidth, 0, (frameIndex + 1) * frameWidth, frameHeight);

            float size = nodeSize * 2;
            deathDstRect.set((int)(x - size/2), (int)(drawY - size/2), (int)(x + size/2), (int)(drawY + size/2));

            canvas.drawBitmap(bmpDeath, deathSrcRect, deathDstRect, deathPaint);
        }
    }

    private void drawNodes(Canvas canvas)
    {
        for (Node node : nodes)
        {
            if (node.active)
            {
                float nodeScreenX = node.x - cameraX + (gameAreaWidth / 2f);
                float nodeScreenY = node.y - cameraY + (gameAreaHeight / 2f);

                // Only draw nodes if visible on screen
                if (nodeScreenY > -100 && nodeScreenY < gameAreaHeight + 100)
                {
                    Paint nodePaint = getNodeColor(node);
                    Bitmap spriteToUse;

                    // Select sprite based on type
                    if (node.powerUpType == POWERUP_NONE || node.powerUpType == POWERUP_HIJACKED)
                    {
                        spriteToUse = bmpNode;
                    }
                    else
                    {
                        spriteToUse = bmpPowerup;
                    }

                    drawRotatedBitmap(canvas, spriteToUse, nodeScreenX, nodeScreenY, currentRotation, nodePaint);
                }
            }
        }
    }

    private void startDeathSequence()
    {
        if (isDying || gameOver) return;

        isDying = true;
        deathStartTime = System.currentTimeMillis();
        deathYOffset = 0;
        Paint currentPaint = getCurrentPlayerPaint();
        deathPaint = new Paint();
        deathPaint.setColorFilter(currentPaint.getColorFilter());
        deathPaint.setAntiAlias(true);
    }

    private Paint getCurrentPlayerPaint() {
        if (isTunnellerActive) return tunnellerNodeColour;
        if (isConnectionStabilizerActive) return connectionStabilizerNodeColour;
        if (isRangeEnhanced) return rangeEnhancerNodeColour;
        if (isSignalBypassActive) return signalBypassNodeColour;
        return playerColour;
    }

    private Paint getNodeColor(Node node)
    {
        if (!node.isAheadOfPlayer())
        {
            return oldNodeColour; // Gray
        }
        else if (node.powerUpType == POWERUP_TUNNELLER)
        {
            return tunnellerNodeColour; // Cyan
        }
        else if (node.powerUpType == POWERUP_RANGE_ENHANCER)
        {
            return rangeEnhancerNodeColour; // Purple
        }
        else if (node.powerUpType == POWERUP_CONNECTION_STABILIZER)
        {
            return connectionStabilizerNodeColour; // Orange
        }
        else if (node.powerUpType == POWERUP_SIGNAL_BYPASS)
        {
            return signalBypassNodeColour; // Yellow
        }
        else if (node.powerUpType == POWERUP_HIJACKED)
        {
            return hijackedNodeColour; // Red
        }
        else
        {
            return nodeColour; // White
        }
    }

    // Update the camera to follow the player
    private void updateCamera()
    {
        float oldCameraY = cameraY;
        cameraX = smoothMovement(cameraX, playerX, cameraSpeed);
        cameraY = smoothMovement(cameraY, playerY - 500f, cameraSpeed);
        float deltaY = oldCameraY - cameraY;

        // Move background based on camera movement
        background.update(deltaY * 0.5f);
    }

    // Spawn enemies in the game scene after a certain distance
    private void createNewEnemy(float y)
    {
        if (score >= 300) // Start spawning enemies at 300m
        {
            // Calculate enemy spawn chance: starts at 2% at 300m, increases by 1% every 300m
            int enemyChance = 2 + ((score - 300) / 300);
            enemyChance = Math.min(enemyChance, 10);

            if (random.nextInt(100) < enemyChance)
            {
                ArrayList<Float> possibleX = getValidNodePositions(playerX);
                if (!possibleX.isEmpty())
                {
                    float spawnX = possibleX.get(random.nextInt(possibleX.size()));
                    enemies.add(new EnemyEntity(spawnX, y, bmpEnemy));
                }
            }
        }
    }

    // Update all enemies and check for collisions
    private void updateEnemies()
    {
        for (EnemyEntity enemy : enemies)
        {
            enemy.update(playerX, playerY, cameraY, gameAreaHeight);

            // Check if the enemy touched the player
            if (enemy.checkEnemyCollision(playerX, playerY))
            {
                startDeathSequence();
                return;
            }
        }

        // Remove any enemies that went off screen below
        for (int i = enemies.size() - 1; i >= 0; i--)
        {
            if (enemies.get(i).shouldRemoveEnemy(playerY, gameAreaHeight))
            {
                enemies.remove(i);
            }
        }
    }

    private ArrayList<Float> getValidNodePositions(float centerX)
    {
        ArrayList<Float> positions = new ArrayList<>();
        float leftEdge = centerX - howWideToSpawn;
        float rightEdge = centerX + howWideToSpawn;
        positions.add(centerX);

        // Add positions to the right
        for (float x = centerX + spaceBetweenNodes; x <= rightEdge; x += spaceBetweenNodes)
        {
            positions.add(x);
        }

        // Add positions to the left
        for (float x = centerX - spaceBetweenNodes; x >= leftEdge; x -= spaceBetweenNodes)
        {
            positions.add(x);
        }

        return positions;
    }

    private void triggerGameWin()
    {
        if (getContext() instanceof GameScene)
        {
            ((GameScene) getContext()).onGameWin(score);
        }
    }

    private void triggerGameOver()
    {
        // We make sure this doesn't get called twice
        if (gameOver)
            return;

        gameOver = true; // Stop for real

        if (getContext() instanceof GameScene)
        {
            ((GameScene) getContext()).onGameOver(score);
        }
    }

    private void triggerMilestoneAlert(int metersLeft)
    {
        if (getContext() instanceof GameScene)
        {
            ((GameScene) getContext()).onMilestoneReached(metersLeft);
        }
    }

    public int getScore()
    {
        return score;
    }

    private float smoothMovement(float current, float target, float speed)
    {
        return current + (target - current) * speed;
    }

    private boolean shouldShowRange()
    {
        // Show range when game started (except during tunneller)
        return gameStarted && !isTunnellerActive;
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
        nodeMoveId = soundPool.load(context, R.raw.node_move, 1);
        gameStartId = soundPool.load(context, R.raw.game_start, 1);
        powerupCollectedId = soundPool.load(context, R.raw.powerup_collected, 1);

        // Get volume settings
        SettingsManager settingsManager = new SettingsManager(context);
        sfxVolume = settingsManager.getSFXVolume() / 100f;
    }
    private void playSound(int soundId)
    {
        if (soundPool != null) {
            soundPool.play(soundId, sfxVolume, sfxVolume, 1, 0, 1f);
        }
    }
}