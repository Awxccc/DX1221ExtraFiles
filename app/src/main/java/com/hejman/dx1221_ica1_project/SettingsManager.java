package com.hejman.dx1221_ica1_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.ImageButton;
import android.view.View;
public class SettingsManager
{
    private static final String PREF_NAME = "GameSettings";
    private static final String KEY_MUSIC = "music_vol";
    private static final String KEY_SFX = "sfx_vol";
    private ImageButton settingsButton;
    private final SharedPreferences prefs;

    public SettingsManager(Context context)
    {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Set up sliders from the main menu class
    public void bindSliders(SeekBar musicSlider, SeekBar sfxSlider)
    {
        musicSlider.setProgress(getMusicVolume());
        sfxSlider.setProgress(getSFXVolume());

        musicSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveMusicVolume(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sfxSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveSFXVolume(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void saveMusicVolume(int volume)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_MUSIC, volume);
        editor.apply();
    }

    private void saveSFXVolume(int volume)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SFX, volume);
        editor.apply();
    }
    public void setSettingsButton(ImageButton button)
    {
        this.settingsButton = button;
    }

    public void setButtonVisibility(boolean isVisible)
    {
        if (settingsButton != null)
        {
            if (isVisible)
            {
                settingsButton.setVisibility(View.VISIBLE);
            } else
            {
                settingsButton.setVisibility(View.GONE);
            }
        }
    }

    public int getMusicVolume()
    {
        return prefs.getInt(KEY_MUSIC, 100);
    }

    public int getSFXVolume()
    {
        return prefs.getInt(KEY_SFX, 100);
    }
}