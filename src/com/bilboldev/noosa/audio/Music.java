/*
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bilboldev.noosa.audio;

import java.io.IOException;

import com.bilboldev.noosa.Game;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

public enum Music implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

	INSTANCE;
	
	private MediaPlayer player;
	private LayeredMusic layered;
	
	private String lastPlayed;
	private boolean lastLooping;
	
	private boolean enabled = true;
	private boolean prepared;
	private boolean paused;
	private float volume = 1f;
	private float masterVolume = 1f;
	private float fadeVolume = 1f;
	
	public synchronized void play( String assetName, boolean looping ) {
		
		if (player != null && assetName != null && assetName.equals(lastPlayed)) {
			return;
		}
		
		stop();
		
		lastPlayed = assetName;
		lastLooping = looping;
		
		if (!enabled || assetName == null) {
			return;
		}
		
		try (AssetFileDescriptor afd = Game.instance.getAssets().openFd(assetName)) {
			
			player = new MediaPlayer();
			player.setAudioStreamType( AudioManager.STREAM_MUSIC );
			player.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength() );
			player.setOnPreparedListener( this );
			player.setOnErrorListener( this );
			player.setLooping( looping );
			player.setVolume(volume * masterVolume * fadeVolume, volume * masterVolume * fadeVolume);
			player.prepareAsync();
			
		} catch (IOException | RuntimeException e) {
			stop();
		}
	}
	
	public synchronized void mute() {
		lastPlayed = null;
		stop();
	}

    public synchronized void playLayered(LayeredMusic.Track track, boolean boss) {
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

    public synchronized void boss(boolean active) {
        if (layered != null) layered.boss(active);
    }

	@Override
	public synchronized void onPrepared( MediaPlayer ready ) {
		if (ready != player) return;
		prepared = true;
		ready.setVolume(volume * masterVolume * fadeVolume, volume * masterVolume * fadeVolume);
		if (enabled && !paused) ready.start();
	}
	
	@Override
	public synchronized boolean onError( MediaPlayer mp, int what, int extra ) {
		if (mp == player) stop();
		return true;
	}
	
	public synchronized void pause() {
		paused = true;
		if (layered != null) layered.paused(true);
		if (player != null && prepared) {
			player.pause();
		}
	}
	
	public synchronized void resume() {
		paused = false;
		if (layered != null) layered.paused(!enabled);
		if (player != null && prepared && enabled) {
			player.start();
		}
	}
	
	public synchronized void stop() {
		prepared = false;
		if (layered != null) {
			layered.close();
			layered = null;
		}
		if (player != null) {
			player.release();
			player = null;
		}
	}
	
	public synchronized void volume( float value ) {
		volume = Math.max(0, Math.min(1, value));
		if (layered != null) layered.volume(volume * masterVolume * fadeVolume);
		if (player != null && prepared) {
			player.setVolume(volume * masterVolume * fadeVolume, volume * masterVolume * fadeVolume);
		}
	}

	public synchronized void masterVolume(float value) {
		masterVolume = Math.max(0, Math.min(1, value));
		volume(volume);
	}

	public synchronized void fadeVolume(float value) {
		fadeVolume = Math.max(0, Math.min(1, value));
		volume(volume);
	}
	
	public synchronized boolean isPlaying() {
		return layered != null ? layered.isPlaying() : player != null && prepared && player.isPlaying();
	}

	public synchronized boolean isPlaying(String assetName) {
		return isCurrent(assetName) && isPlaying();
	}

    public synchronized boolean isCurrent(String assetName) {
        return (player != null || layered != null) && assetName != null && assetName.equals(lastPlayed);
    }
	
	public synchronized void enable( boolean value ) {
		enabled = value;
		if (layered != null) {
			layered.paused(paused || !enabled);
			return;
		}
		if (!value) {
			if (player != null && prepared) player.pause();
		} else if (player != null) {
            if (prepared && !paused) player.start();
		} else {
			play( lastPlayed, lastLooping );
		}
	}
	
	public synchronized boolean isEnabled() {
		return enabled;
	}
}
