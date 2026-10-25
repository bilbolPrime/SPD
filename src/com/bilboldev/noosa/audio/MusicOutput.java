package com.bilboldev.noosa.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import com.bilboldev.noosa.Game;
import java.io.IOException;
import java.io.InputStream;

final class MusicOutput {
    static InputStream asset(String path) throws IOException {
        return Game.instance.getAssets().open(path);
    }
    static LayeredMusic.Output create() throws IOException {
        int minimum = AudioTrack.getMinBufferSize(LayeredMusic.RATE,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack track = new AudioTrack(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                new AudioFormat.Builder().setSampleRate(LayeredMusic.RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build(),
                Math.max(minimum, LayeredMusic.RATE / 5 * 4), AudioTrack.MODE_STREAM,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE);
        if (track.getState() != AudioTrack.STATE_INITIALIZED) {
            track.release();
            throw new IOException("AudioTrack unavailable");
        }
        return new LayeredMusic.Output() {
            private boolean closed;
            @Override
            public int write(short[] pcm, int offset, int length) {
                return track.write(pcm, offset, length);
            }

            @Override
            public synchronized void paused(boolean paused) {
                if (!closed) {
                    if (paused) track.pause();
                    else track.play();
                }
            }

            @Override
            public synchronized void volume(float volume) {
                if (!closed) track.setVolume(volume);
            }

            @Override
            public synchronized void close() {
                if (!closed) {
                    closed = true;
                    track.pause();
                    track.flush();
                    track.release();
                }
            }
        };
    }
}
