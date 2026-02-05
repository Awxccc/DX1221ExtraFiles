package com.hejman.dx1221_ica2_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.ImageButton;
import android.view.View;
//Done by Jonathan
public class SettingsManager
{
    private static final String PREF_NAME = "GameSettings";
    private static final String KEY_MUSIC = "music_vol";
    private static final String KEY_SFX = "sfx_vol";
    private static final String KEY_TILT = "tilt_sensitivity";
    private ImageButton settingsButton;
    private final SharedPreferences prefs;

    public interface OnVolumeChangedListener {
        void onVolumeChanged(int volume);
    }
    public SettingsManager(Context context)
    {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Set up sliders from the main menu class
    public void bindSliders(SeekBar musicSlider, SeekBar sfxSlider, SeekBar tiltSlider, OnVolumeChangedListener musicListener)
    {
        musicSlider.setProgress(getMusicVolume());
        sfxSlider.setProgress(getSFXVolume());
        tiltSlider.setProgress(getTiltSensitivity());

        musicSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveMusicVolume(progress);
                if (fromUser && musicListener != null)
                {
                    musicListener.onVolumeChanged(progress);
                }
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

        tiltSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                saveTiltSensitivity(progress);
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
    private void saveTiltSensitivity(int sensitivity)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TILT, sensitivity);
        editor.apply();
    }
    public int getTiltSensitivity()
    {
        return prefs.getInt(KEY_TILT, 50);
    }

    public float getTiltSensitivityValue()
    {
        int progress = getTiltSensitivity();
        return 1.0f + (progress / 10.0f);
    }
}