package com.hejman.dx1221_ica1_project;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameScene extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamescene);
        Button backButton = findViewById(R.id.back_to_menu_button);

        // Added a simple back button in the game scene
        backButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}