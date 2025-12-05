package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class BackgroundEntity
{
    private final Bitmap backgroundBitmap;
    private final Bitmap backgroundBitmap1;
    private final float screenHeight;
    private float backgroundPosition;

    public BackgroundEntity(Context context, int width, int height)
    {
        this.screenHeight = height;
        int backgroundResId = R.drawable.my_game_bg;
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), backgroundResId);

        backgroundBitmap = Bitmap.createScaledBitmap(bmp, width, height, true);
        backgroundBitmap1 = Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    public void update(float scrollAmount)
    {
        // + make bg go down - go up
        backgroundPosition = (backgroundPosition + scrollAmount) % screenHeight;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(backgroundBitmap, 0, backgroundPosition, null);
        canvas.drawBitmap(backgroundBitmap1, 0, backgroundPosition - screenHeight, null);
    }
}