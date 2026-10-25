package com.bilboldev.noosa.audio;

import com.badlogic.gdx.Gdx;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

final class MusicOutput {
    static InputStream asset(String path) {
        return Gdx.files.internal(path).read();
    }
    static LayeredMusic.Output create() throws Exception {
        AudioFormat format = new AudioFormat(LayeredMusic.RATE, 16, 2, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format, LayeredMusic.RATE / 5 * 4);
        return new LayeredMusic.Output() {
            private final byte[] bytes = new byte[4096];
            private volatile float volume = 1;
            @Override
            public int write(short[] pcm, int offset, int length) {
                int count = Math.min(length, bytes.length / 2);
                for (int i = 0; i < count; i++) {
                    int sample = Math.round(pcm[offset + i] * volume);
                    bytes[i * 2] = (byte)sample;
                    bytes[i * 2 + 1] = (byte)(sample >> 8);
                }
                return line.write(bytes, 0, count * 2) / 2;
            }

            @Override
            public void paused(boolean paused) {
                if (paused) line.stop();
                else line.start();
            }

            @Override
            public void volume(float value) {
                volume = value;
            }

            @Override
            public void close() {
                line.stop();
                line.flush();
                line.close();
            }
        };
    }
}
