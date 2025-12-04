package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class GameLogic extends View
{
    // Variables
    private float playerX = 0, playerY = 0, cameraX = 0, cameraY = 0, cameraSpeed = 0.05f, lastNodeSpawnY = 0;
    private int gameAreaWidth, gameAreaHeight;
    private ArrayList<Node> nodes = new ArrayList<>();
    private Random random = new Random();

    // Node Spawning Variables
    private float howFarAheadToSpawn, whenToSpawnMore, howWideToSpawn, spaceBetweenNodes;
    private float nodeSize = 60f;

    // Colour Variables
    private Paint playerColor, nodeColor, oldNodeColor, scoreColor, tunnellerNodeColor, trailPaint;

    // Score Variables
    private int score = 0;

    // BG variable
    private BackgroundEntity background;

    //Power up Variables
    private static final int POWERUP_NONE = 0;
    private static final int POWERUP_TUNNELLER = 1;
    private final long TUNNELLER_DURATION = 3000;
    private int activePowerUpType = POWERUP_NONE;
    private long powerUpEndTime = 0;
    private long powerUpActionTimer = 0;

    //Trail line renderer
    private ArrayList<float[]> trailSegments = new ArrayList<>();
    private static final float TRAIL_TYPE_NORMAL = 0f;
    private static final float TRAIL_TYPE_TUNNELLER = 1f;

    // Node Class
    private class Node
    {
        float x, y;
        boolean active = true; // Is the player allowed to click on it
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

        // Check if player clicked on this node
        boolean wasClicked(float clickX, float clickY)
        {
            if (!isAheadOfPlayer())
                return false;

            float screenX = x - cameraX + (gameAreaWidth / 2f);
            float screenY = y - cameraY + (gameAreaHeight / 2f);
            float deltaX = clickX - screenX;
            float deltaY = clickY - screenY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            return distance <= 65;
        }
    }

    public GameLogic(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setupColors();
    }

    private void setupColors()
    {
        playerColor = new Paint();
        playerColor.setColor(0xFFFFFFFF); // White
        playerColor.setAntiAlias(true);

        nodeColor = new Paint();
        nodeColor.setColor(0xFFFF0000); // Bright red
        nodeColor.setAntiAlias(true);

        oldNodeColor = new Paint();
        oldNodeColor.setColor(0xFF800000); // Dark red
        oldNodeColor.setAntiAlias(true);

        scoreColor = new Paint();
        scoreColor.setColor(0xFFFFFFFF); // White
        scoreColor.setTextSize(60);
        scoreColor.setAntiAlias(true);
        scoreColor.setFakeBoldText(true);
        scoreColor.setTextAlign(Paint.Align.RIGHT);

        tunnellerNodeColor = new Paint();
        tunnellerNodeColor.setColor(0xFF00FFFF); // Cyan
        tunnellerNodeColor.setAntiAlias(true);

        trailPaint = new Paint();
        trailPaint.setStrokeWidth(30f);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        trailPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        gameAreaWidth = width;
        gameAreaHeight = height - 100;

        // Calculate spawning distances based on screen size
        howFarAheadToSpawn = gameAreaHeight * 1.5f;
        whenToSpawnMore = gameAreaHeight * 0.5f;
        howWideToSpawn = gameAreaWidth * 0.5f;
        spaceBetweenNodes = (nodeSize * 4) + 20;

        // Start the "camera" at the player position
        cameraX = playerX;
        cameraY = playerY;

        // BG here
        background = new BackgroundEntity(getContext(), width, height);
        createInitialNodes();
    }

    // Smooth camera movement
    private float smoothMove(float current, float target, float speed)
    {
        return current + (target - current) * speed;
    }

    // Update camera to follow the player
    private void updateCamera()
    {
        cameraX = smoothMove(cameraX, playerX, cameraSpeed);
        cameraY = smoothMove(cameraY, playerY, cameraSpeed);
    }

    // Get all valid X positions where we can place nodes
    private ArrayList<Float> getValidNodePositions(float centerX)
    {
        ArrayList<Float> positions = new ArrayList<>();
        float leftEdge = centerX - howWideToSpawn;
        float rightEdge = centerX + howWideToSpawn;

        positions.add(centerX);

        // Add positions going right
        for (float x = centerX + spaceBetweenNodes; x <= rightEdge; x += spaceBetweenNodes)
        {
            positions.add(x);
        }

        // Add positions going left
        for (float x = centerX - spaceBetweenNodes; x >= leftEdge; x -= spaceBetweenNodes)
        {
            positions.add(x);
        }

        return positions;
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
            ArrayList<Float> chosenPositions = new ArrayList<>();
            ArrayList<Float> possiblePositions = getValidNodePositions(playerX);
            int nodeCount = Math.min(random.nextInt(3) + 1, possiblePositions.size());

            for (int i = 0; i < nodeCount; i++)
            {
                if (!possiblePositions.isEmpty())
                {
                    int randomIndex = random.nextInt(possiblePositions.size());
                    float chosenX = possiblePositions.remove(randomIndex);
                    chosenPositions.add(chosenX);
                }
            }

            for (float nodeX : chosenPositions)
            {
                nodes.add(new Node(nodeX, y));
            }
        }

        lastNodeSpawnY = playerY;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        background.update(); // BG first so its behind everything
        background.draw(canvas);
        updateCamera();

        // Render the trail paths
        for (float[] segment : trailSegments)
        {
            // Apply camera offset to coordinates
            float startX = segment[0] - cameraX + (gameAreaWidth / 2f);
            float startY = segment[1] - cameraY + (gameAreaHeight / 2f);
            float endX = segment[2] - cameraX + (gameAreaWidth / 2f);
            float endY = segment[3] - cameraY + (gameAreaHeight / 2f);
            float type = segment[4];

            if (type == TRAIL_TYPE_TUNNELLER)
            {
                trailPaint.setColor(tunnellerNodeColor.getColor());
            }
            else
            {
                trailPaint.setColor(playerColor.getColor());
            }
            // Only draw if visible
            if (Math.max(startY, endY) > -100 && Math.min(startY, endY) < gameAreaHeight + 100)
            {
                canvas.drawLine(startX, startY, endX, endY, trailPaint);
            }
        }

        float playerScreenX = playerX - cameraX + (gameAreaWidth / 2f);
        float playerScreenY = playerY - cameraY + (gameAreaHeight / 2f);
        canvas.drawCircle(playerScreenX, playerScreenY, nodeSize, playerColor);

        // Render all active nodes
        for (Node node : nodes)
        {
            if (node.active)
            {
                float nodeScreenX = node.x - cameraX + (gameAreaWidth / 2f);
                float nodeScreenY = node.y - cameraY + (gameAreaHeight / 2f);

                // Only draw if the node is visible on screen
                if (nodeScreenY > -100 && nodeScreenY < gameAreaHeight + 100)
                {
                    // Choose node colour
                    Paint color;
                    if (!node.isAheadOfPlayer())
                    {
                        color = oldNodeColor;
                    }
                    else if (node.powerUpType == POWERUP_TUNNELLER)
                    {
                        color = tunnellerNodeColor;
                    }
                    else
                    {
                        color = nodeColor;
                    }
                    canvas.drawCircle(nodeScreenX, nodeScreenY, nodeSize, color);
                }
            }
        }
        canvas.drawText("Distance: " + score, gameAreaWidth - 50, 150, scoreColor);

        updateGameLogic();
        invalidate();
    }

    // Handle all game logic updates
    private void updateGameLogic()
    {
        if (activePowerUpType != POWERUP_NONE)
        {
            long currentTime = System.currentTimeMillis();

            if (currentTime > powerUpEndTime)
            {
                activePowerUpType = POWERUP_NONE;
            }
            else
            {
                switch (activePowerUpType)
                {
                    case POWERUP_TUNNELLER:
                        // Hop every 150ms
                        if (currentTime - powerUpActionTimer > 150)
                        {
                            hopToNearestNode();
                            powerUpActionTimer = currentTime;
                        }
                        break;
                }
            }
        }
        // Check if player moved far enough to need more nodes
        float distancePlayerMoved = lastNodeSpawnY - playerY;

        if (distancePlayerMoved >= whenToSpawnMore)
        {
            createMoreNodes();
            lastNodeSpawnY = playerY;
        }
        score = (int) Math.abs(playerY / 100);

        removeOldNodes();
    }

    // Create more nodes ahead when player advances
    private void createMoreNodes()
    {
        float furthestNodeY = playerY;
        for (Node node : nodes)
        {
            if (node.active && node.y < furthestNodeY)
            {
                furthestNodeY = node.y;
            }
        }

        // Create nodes from furthest point to new spawn distance
        float newSpawnLimit = playerY - howFarAheadToSpawn;

        for (float y = furthestNodeY - 200; y >= newSpawnLimit; y -= 200)
        {
            ArrayList<Float> possiblePositions = getValidNodePositions(playerX);
            int nodeCount = Math.min(random.nextInt(3) + 1, possiblePositions.size());
            ArrayList<Float> chosenPositions = new ArrayList<>();

            for (int i = 0; i < nodeCount; i++)
            {
                if (!possiblePositions.isEmpty())
                {
                    int randomIndex = random.nextInt(possiblePositions.size());
                    float chosenX = possiblePositions.remove(randomIndex);
                    chosenPositions.add(chosenX);
                }
            }

            for (float nodeX : chosenPositions)
            {
                Node newNode = new Node(nodeX, y);

                // 10% chance to be a power-up
                if (random.nextInt(10) == 0)
                {
                    newNode.powerUpType = POWERUP_TUNNELLER;
                }
                nodes.add(newNode);
            }
        }
    }

    // Remove nodes that are too far behind player
    private void removeOldNodes()
    {
        for (int i = nodes.size() - 1; i >= 0; i--)
        {
            Node node = nodes.get(i);

            if (node.y > playerY + (gameAreaHeight * 2))
            {
                nodes.remove(i);
            }
        }
        for (int i = trailSegments.size() - 1; i >= 0; i--)
        {//If both ends are far below the player, remove it
            float y1 = trailSegments.get(i)[1];
            float y2 = trailSegments.get(i)[3];

            if (y1 > playerY + (gameAreaHeight * 2) && y2 > playerY + (gameAreaHeight * 2))
            {
                trailSegments.remove(i);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check if any node was clicked
            for (Node node : nodes)
            {
                if (node.active && node.wasClicked(touchX, touchY))
                {
                    float oldX = playerX;
                    float oldY = playerY;

                    // Move player to clicked node
                    playerX = node.x;
                    playerY = node.y;
                    node.active = false;

                    //Create trail
                    addTrail(oldX, oldY, playerX, playerY, TRAIL_TYPE_NORMAL);

                    if (node.powerUpType == POWERUP_TUNNELLER)
                    {
                        activatePowerUp(POWERUP_TUNNELLER, TUNNELLER_DURATION);
                    }

                    return true;
                }
            }
        }
        return true;
    }
    private void activatePowerUp(int type, long duration)
    {
        activePowerUpType = type;
        powerUpEndTime = System.currentTimeMillis() + duration;
    }
    public int getScore()
    {
        return score;
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
                float dist = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (dist < minDistance)
                {
                    minDistance = dist;
                    bestNode = node;
                }
            }
        }
        // Perform the hop if a node was found
        if (bestNode != null)
        {
            addTrail(playerX, playerY, bestNode.x, bestNode.y, activePowerUpType);

            playerX = bestNode.x;
            playerY = bestNode.y;
            bestNode.active = false;

        }
        else {
            activePowerUpType = POWERUP_NONE;
        }
    }
    private void addTrail(float startX, float startY, float endX, float endY, float type)
    {
        trailSegments.add(new float[]{startX, startY, endX, startY, type});
        trailSegments.add(new float[]{endX, startY, endX, endY, type});
    }
}