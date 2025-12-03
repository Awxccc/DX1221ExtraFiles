package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class MainMenu_Particle extends View
{
    // Variables
    private ArrayList<Particle> particles;
    private Paint paint;
    private Random random;
    private int screenWidth, screenHeight;

    public MainMenu_Particle(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        random = new Random();
        particles = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight)
    {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        screenWidth = width;
        screenHeight = height;
        createParticles();
    }

    private void createParticles()
    {
        particles.clear();

        for (int i = 0; i < 30; i++)
        {
            float x = random.nextFloat() * screenWidth;
            float y = random.nextFloat() * screenHeight;
            float speed = 1 + random.nextFloat() * 2;

            particles.add(new Particle(x, y, speed));
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // Loop through all the particles
        for (Particle particle : particles)
        {
            // Draw some white circles and move the particles up
            canvas.drawCircle(particle.x, particle.y, 3, paint);
            particle.y = particle.y - particle.speed;

            // Reset the particle if it goes off screen
            if (particle.y < -10) {
                particle.y = screenHeight + 10;
                particle.x = random.nextFloat() * screenWidth;
                particle.speed = 1 + random.nextFloat() * 2;
            }
        }

        invalidate(); // Redraw that shit
    }

    private class Particle
    {
        // Particle Variables
        float x, y, speed;

        Particle(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
}