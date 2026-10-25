package com.bilboldev.noosa.audio;

import com.bilboldev.pixeldungeonskills.network.PartyEffects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public enum Sample {

    INSTANCE;

    public static final int MAX_STREAMS = 8;

    private final Map<Object, Sound> ids = new HashMap<Object, Sound>();
    private boolean enabled = true;
    private float volume = 1f;

    public void reset() {
        for (Sound sound : ids.values()) {
            sound.dispose();
        }
        ids.clear();
    }

    public void pause() {
        for (Sound sound : ids.values()) {
            sound.pause();
        }
    }

    public void resume() {
        for (Sound sound : ids.values()) {
            sound.resume();
        }
    }

    public void load(String... assets) {
        for (String asset : assets) {
            if (asset == null || ids.containsKey(asset)) {
                continue;
            }

            FileHandle handle = Gdx.files.internal(asset);
            if (!handle.exists()) {
                continue;
            }

            ids.put(asset, Gdx.audio.newSound(handle));
        }
    }

    public void unload(Object src) {
        Sound sound = ids.remove(src);
        if (sound != null) {
            sound.dispose();
        }
    }

    public int play(Object id) {
        return play(id, 1, 1, 1);
    }

    public int play(Object id, float volume) {
        return play(id, volume, volume, 1);
    }

    public int play(Object id, float leftVolume, float rightVolume, float rate) {
        if (!PartyEffects.sound(id, leftVolume, rightVolume, rate)) return -1;
        Sound sound = ids.get(id);
        if (!enabled || volume == 0 || sound == null) {
            return -1;
        }

        float level = Math.max(leftVolume, rightVolume);
        float pan = 0f;
        if (level > 0f) {
            pan = clamp((rightVolume - leftVolume) / level);
        }

        long streamId = sound.play(level * volume, rate, pan);
        return (int) streamId;
    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void volume(float value) {
        volume = Math.max(0, Math.min(1, value));
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static float clamp(float value) {
        return Math.max(-1f, Math.min(1f, value));
    }
}
