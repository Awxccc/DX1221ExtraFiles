package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class BackgroundEntity
{
    private Bitmap backgroundBitmap;
    private Bitmap backgroundBitmap1;
    private float screenWidth, screenHeight;
    private float backgroundPosition;
    private float scrollSpeed = 10f;

    public BackgroundEntity(Context context, int width, int height)
    {
        this.screenWidth = width;
        this.screenHeight = height;
        int backgroundResId = R.drawable.my_game_bg;
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), backgroundResId);

        backgroundBitmap = Bitmap.createScaledBitmap(bmp, width, height, true);
        backgroundBitmap1 = Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    public void update()
    {
        // + make bg go down - go up
        backgroundPosition = (backgroundPosition + scrollSpeed) % screenHeight;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(backgroundBitmap, 0, backgroundPosition, null);
        canvas.drawBitmap(backgroundBitmap1, 0, backgroundPosition - screenHeight, null);
    }
}