package com.bilboldev.noosa.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum Music {

    INSTANCE;

    private boolean enabled = true;
    private boolean paused;
    private String lastPlayed;
    private boolean lastLooping;
    private float volume = 1f;
    private float masterVolume = 1f;
    private float fadeVolume = 1f;
    private com.badlogic.gdx.audio.Music player;
    private LayeredMusic layered;

    public void play(String assetName, boolean looping) {
        if (isCurrent(assetName)) {
            return;
        }

        stop();

        lastPlayed = assetName;
        lastLooping = looping;

        if (!enabled || assetName == null) {
            return;
        }

        FileHandle asset = resolveAsset(assetName);
        if (asset == null || !asset.exists()) {
            return;
        }

        try {
            player = Gdx.audio.newMusic(asset);
            player.setLooping(looping);
            player.setVolume(volume * masterVolume * fadeVolume);
            if (!paused) player.play();
        } catch (Exception ignored) {
            stop();
        }
    }

    public void mute() {
        lastPlayed = null;
        stop();
    }

    public void playLayered(LayeredMusic.Track track, boolean boss) {
        if (layered == null || layered.failure() != null) {
            stop();
            layered = new LayeredMusic(MusicOutput::asset, MusicOutput::create);
        }
        lastPlayed = track.id;
        lastLooping = true;
        layered.volume(volume * masterVolume * fadeVolume);
        layered.paused(paused || !enabled);
        layered.play(track, boss);
    }

    public void boss(boolean active) {
        if (layered != null) layered.boss(active);
    }

    public void pause() {
        paused = true;
        if (layered != null) layered.paused(true);
        if (player != null) {
            player.pause();
        }
    }

    public void resume() {
        paused = false;
        if (layered != null) layered.paused(!enabled);
        if (enabled && player != null) {
            player.play();
        }
    }

    public void stop() {
        if (layered != null) {
            layered.close();
            layered = null;
        }
        if (player != null) {
            try {
                player.stop();
            } finally {
                player.dispose();
                player = null;
            }
        }
    }

    public void volume(float value) {
        volume = Math.max(0, Math.min(1, value));
        if (layered != null) layered.volume(volume * masterVolume * fadeVolume);
        if (player != null) {
            player.setVolume(volume * masterVolume * fadeVolume);
        }
    }

    public void masterVolume(float value) {
        masterVolume = Math.max(0, Math.min(1, value));
        volume(volume);
    }

    public void fadeVolume(float value) {
        fadeVolume = Math.max(0, Math.min(1, value));
        volume(volume);
    }

    public boolean isPlaying() {
        return layered != null ? layered.isPlaying() : player != null && player.isPlaying();
    }

    public boolean isPlaying(String assetName) {
        return assetName != null && assetName.equals(lastPlayed) && isPlaying();
    }

    public boolean isCurrent(String assetName) {
        return (player != null || layered != null) && assetName != null && assetName.equals(lastPlayed);
    }

    public void enable(boolean value) {
        enabled = value;
        if (layered != null) {
            layered.paused(paused || !enabled);
            return;
        }
        if (!value) {
            if (player != null) player.pause();
        } else if (player != null) {
            if (!paused) player.play();
        } else if (lastPlayed != null) {
            play(lastPlayed, lastLooping);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static FileHandle resolveAsset(String assetName) {
        return assetName == null ? null : Gdx.files.internal(assetName);
    }
}
