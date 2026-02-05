package com.hejman.dx1221_ica2_project;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import java.util.HashMap;
//Done by Jonathan
public class SoundManager
{
    private static SoundManager instance;
    private SoundPool soundPool;
    private MediaPlayer bgmPlayer;
    private SettingsManager settingsManager;
    private HashMap<Integer, Integer> soundMap;
    private Context context;
    private int currentBgmResId = -1;

    public static final int SFX_BUTTON_CLICK = R.raw.button_click;
    public static final int SFX_PLAYER_LOST = R.raw.player_lost;
    public static final int SFX_PLAYER_WON = R.raw.player_won;
    public static final int SFX_NODE_MOVE = R.raw.node_move;
    public static final int SFX_GAME_START = R.raw.game_start;
    public static final int SFX_POWERUP = R.raw.powerup_collected;
    public static final int SFX_STAR_PICKUP = R.raw.mg_starpickup;
    public static final int SFX_EXPLOSION = R.raw.mg_explosion;

    private SoundManager(Context context)
    {
        // Using context to prevent memory leaks hate memory leaks
        this.context = context.getApplicationContext();
        this.settingsManager = new SettingsManager(this.context);
        this.soundMap = new HashMap<>();

        initSoundPool();
        preloadSounds();
    }

    public static synchronized SoundManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new SoundManager(context);
        }
        return instance;
    }

    private void initSoundPool()
    {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
    }

    private void preloadSounds()
    {
        load(SFX_BUTTON_CLICK);
        load(SFX_PLAYER_LOST);
        load(SFX_PLAYER_WON);
        load(SFX_NODE_MOVE);
        load(SFX_GAME_START);
        load(SFX_POWERUP);
        load(SFX_STAR_PICKUP);
        load(SFX_EXPLOSION);
    }

    private void load(int resourceId)
    {
        int soundId = soundPool.load(context, resourceId, 1);
        soundMap.put(resourceId, soundId);
    }

    public void playSFX(int resourceId)
    {
        float volume = settingsManager.getSFXVolume() / 100f;
        Integer soundId = soundMap.get(resourceId);

        if (soundId != null)
        {
            soundPool.play(soundId, volume, volume, 1, 0, 1f);
        }
    }

    public void playBGM(int resourceId)
    {
        if (bgmPlayer != null && bgmPlayer.isPlaying() && currentBgmResId == resourceId)
        {
            updateMusicVolume();
            return;
        }
        stopBGM();

        currentBgmResId = resourceId;
        bgmPlayer = MediaPlayer.create(context, resourceId);

        if (bgmPlayer != null)
        {
            bgmPlayer.setLooping(true);
            updateMusicVolume();
            bgmPlayer.start();

            bgmPlayer.setOnErrorListener((mp, what, extra) -> {
                stopBGM();
                playBGM(resourceId);
                return true;
            });
        }
    }

    public void stopBGM()
    {
        if (bgmPlayer != null)
        {
            if (bgmPlayer.isPlaying())
            {
                bgmPlayer.stop();
            }
            bgmPlayer.release();
            bgmPlayer = null;
        }
        currentBgmResId = -1;
    }

    public void pauseBGM()
    {
        if (bgmPlayer != null && bgmPlayer.isPlaying())
        {
            bgmPlayer.pause();
        }
    }
    public void updateMusicVolume()
    {
        if (bgmPlayer != null)
        {
            float volume = settingsManager.getMusicVolume() / 100f;
            bgmPlayer.setVolume(volume, volume);
        }
    }
}