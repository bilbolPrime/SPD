package com.bilboldev.noosa.audio;

import com.jcraft.jorbis.PcmVorbisFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class LayeredMusic implements Closeable {
    public static final int RATE = 48000;
    private static final int BLOCK = 1024, CROSSFADE = RATE;

    public static final class Track {
        public final String id, base, layer;
        public final int frames, beats;
        public Track(String id, String base, String layer, int frames, int beats) {
            this.id = id;
            this.base = base;
            this.layer = layer;
            this.frames = frames;
            this.beats = beats;
        }
    }
    public interface Assets {
        InputStream open(String path) throws IOException;
    }
    public interface Output extends Closeable {
        int write(short[] pcm, int offset, int length) throws Exception;
        void paused(boolean paused);
        void volume(float volume);
    }
    public interface Outputs {
        Output open() throws Exception;
    }

    private final Assets assets;
    private final Outputs outputs;
    private final Object wake = new Object();
    private volatile Track requested;
    private volatile boolean boss, paused, closed, running;
    private volatile float volume = 1;
    private volatile Output output;
    private volatile Throwable failure;
    private volatile long renderedFrames;
    private Thread worker;

    public LayeredMusic(Assets assets, Outputs outputs) {
        this.assets = assets;
        this.outputs = outputs;
    }

    public synchronized void play(Track track, boolean boss) {
        this.boss = boss;
        requested = track;
        if (worker == null) {
            worker = new Thread(this::run, "Dungeon music");
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void boss(boolean boss) {
        this.boss = boss;
    }

    public void paused(boolean value) {
        synchronized (wake) {
            paused = value;
            if (output != null) output.paused(value);
            wake.notifyAll();
        }
    }

    public void volume(float value) {
        synchronized (wake) {
            volume = Math.max(0, Math.min(1, value));
            if (output != null) output.volume(volume);
        }
    }

    public boolean isPlaying() {
        return running && !paused && !closed;
    }

    public Throwable failure() {
        return failure;
    }

    public long renderedFrames() {
        return renderedFrames;
    }

    private void awaitResume() throws InterruptedException {
        synchronized (wake) {
            while (paused && !closed) wake.wait();
        }
    }

    private void run() {
        Mix current = null, previous = null;
        float[] a = new float[BLOCK * 2], b = new float[BLOCK * 2];
        short[] pcm = new short[BLOCK * 2];
        int fade = CROSSFADE;
        try {
            awaitResume();
            if (closed) return;
            Output sink = outputs.open();
            synchronized (wake) {
                if (closed) {
                    sink.close();
                    return;
                }
                output = sink;
                sink.volume(volume);
                sink.paused(paused);
            }
            running = true;
            while (!closed) {
                awaitResume();
                if (closed) break;
                Track next = requested;
                if (current == null || !current.track.id.equals(next.id)) {
                    Mix incoming = new Mix(next, assets);
                    if (previous != null) previous.close();
                    previous = current;
                    current = incoming;
                    fade = 0;
                }
                current.boss(boss);
                current.render(a, BLOCK);
                if (previous != null) previous.render(b, BLOCK);
                for (int frame = 0; frame < BLOCK; frame++) {
                    float gain = Math.min(1, (fade + frame) / (float)CROSSFADE);
                    gain = gain * gain * (3 - 2 * gain);
                    for (int channel = 0; channel < 2; channel++) {
                        int i = frame * 2 + channel;
                        float sample = a[i] * gain + (previous == null ? 0 : b[i] * (1 - gain));
                        pcm[i] = (short)Math.max(-32768, Math.min(32767, Math.round(sample)));
                    }
                }
                fade = Math.min(CROSSFADE, fade + BLOCK);
                if (fade == CROSSFADE && previous != null) {
                    previous.close();
                    previous = null;
                }
                int offset = 0;
                while (offset < pcm.length && !closed) {
                    awaitResume();
                    if (closed) break;
                    int written = sink.write(pcm, offset, pcm.length - offset);
                    if (written < 0) throw new IOException("Audio output failed: " + written);
                    if (written == 0) {
                        Thread.sleep(2);
                        continue;
                    }
                    offset += written;
                }
                renderedFrames += offset / 2;
            }
        } catch (Exception error) {
            if (!closed) {
                failure = error;
                System.err.println("Dungeon music: " + error);
            }
        } finally {
            running = false;
            if (current != null) current.close();
            if (previous != null) previous.close();
            synchronized (wake) {
                if (output != null) try {
                    output.close();
                } catch (IOException ignored) {}
                output = null;
            }
        }
    }

    @Override
    public void close() {
        synchronized (wake) {
            closed = true;
            if (output != null) try {
                output.close();
            } catch (IOException ignored) {}
            output = null;
            wake.notifyAll();
        }
        if (worker != null) worker.interrupt();
    }

    public static final class Mix implements Closeable {
        public final Track track;
        private final byte[] baseOgg, layerOgg;
        private Decoder base, layer, nextBase, nextLayer;
        private final short[] basePcm = new short[BLOCK * 2], layerPcm = new short[BLOCK * 2];
        private long position;
        private boolean desired;
        private long start, duration;
        private float from, to, gain;

        public Mix(Track track, Assets assets) throws IOException {
            this.track = track;
            baseOgg = readAsset(assets, track.base);
            layerOgg = readAsset(assets, track.layer);
            base = new Decoder(baseOgg);
            try {
                layer = new Decoder(layerOgg);
            } catch (IOException error) {
                base.close();
                throw error;
            }
        }

        public long position() {
            return position;
        }

        public float bossGain() {
            return gain;
        }

        public void boss(boolean value) {
            if (desired == value) return;
            desired = value;

            double beat = track.frames / (double)track.beats;
            start = value ? Math.round(Math.ceil(position / beat) * beat) : position;
            duration = Math.round(beat * (value ? 4 : 8));
            from = gain;
            to = value ? 1 : 0;
        }

        public void render(float[] target, int frames) throws IOException {
            if (frames > BLOCK || target.length < frames * 2) throw new IllegalArgumentException("Audio block too large");
            int done = 0;
            while (done < frames) {
                int cursor = (int)(position % track.frames);
                if (nextBase == null && track.frames - cursor <= RATE) {
                    nextBase = new Decoder(baseOgg);
                    nextLayer = new Decoder(layerOgg);
                }
                int count = Math.min(frames - done, track.frames - cursor);
                base.read(basePcm, count * 2);
                layer.read(layerPcm, count * 2);
                for (int f = 0; f < count; f++, position++) {
                    if (duration > 0 && position >= start) {
                        float t = Math.min(1, (position - start) / (float)duration);
                        float smooth = .5f - .5f * (float)Math.cos(Math.PI * t);
                        gain = from + (to - from) * smooth;
                    }
                    int i = f * 2, out = (done + f) * 2;
                    target[out] = basePcm[i] + gain * layerPcm[i];
                    target[out + 1] = basePcm[i + 1] + gain * layerPcm[i + 1];
                }
                done += count;
                if (position % track.frames == 0) {
                    base.close();
                    layer.close();
                    base = nextBase;
                    layer = nextLayer;
                    nextBase = nextLayer = null;
                }
            }
        }

        @Override
        public void close() {
            if (base != null) base.close();
            if (layer != null) layer.close();
            if (nextBase != null) nextBase.close();
            if (nextLayer != null) nextLayer.close();
        }
    }

    private static byte[] readAsset(Assets assets, String path) throws IOException {
        try (InputStream input = assets.open(path); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
            return bytes.toByteArray();
        }
    }

    private static final class Decoder implements Closeable {
        private final PcmVorbisFile file;
        private final byte[] bytes = new byte[8192];
        private int offset, limit;
        Decoder(byte[] ogg) throws IOException {
            try {
                file = new PcmVorbisFile(new ByteArrayInputStream(ogg));
            }
            catch (Exception error) {
                throw new IOException("Cannot decode soundtrack", error);
            }
            if (file.getInfo(-1).channels != 2 || file.getInfo(-1).rate != RATE) {
                close();
                throw new IOException("Soundtrack must be stereo 48 kHz");
            }
        }
        void read(short[] target, int samples) throws IOException {
            for (int i = 0; i < samples; i++) {
                if (offset == limit) {
                    limit = file.readPcm(bytes, bytes.length);
                    offset = 0;
                    if (limit <= 0 || (limit & 3) != 0) throw new IOException("Incomplete soundtrack PCM: " + limit);
                }
                target[i] = (short)((bytes[offset] & 255) | (bytes[offset + 1] << 8));
                offset += 2;
            }
        }

        @Override
        public void close() {
            try {
                file.close();
            } catch (IOException ignored) {}
        }
    }
}
