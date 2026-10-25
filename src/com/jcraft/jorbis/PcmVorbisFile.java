package com.jcraft.jorbis;

import java.io.InputStream;

public final class PcmVorbisFile extends VorbisFile {
    private final float[][][] samples = new float[1][][];
    private final int[] offsets = new int[2];
    public PcmVorbisFile(InputStream input) throws JOrbisException {
        super(input, null, 0);
    }

    public int readPcm(byte[] target, int length) {
        for (;;) {
            if (decode_ready) {
                int frames = Math.min(vd.synthesis_pcmout(samples, offsets), length / 4);
                if (frames > 0) {
                    int output = 0;
                    for (int frame = 0; frame < frames; frame++) for (int channel = 0; channel < 2; channel++) {
                        int value = Math.max(-32768, Math.min(32767,
                                Math.round(samples[0][channel][offsets[channel] + frame] * 32768)));
                        target[output++] = (byte)value;
                        target[output++] = (byte)(value >> 8);
                    }
                    vd.synthesis_read(frames);
                    pcm_offset += frames;
                    return frames * 4;
                }
            }
            int packet = process_packet(1);
            if (packet <= 0) return packet;
        }
    }

    @Override
    public void close() throws java.io.IOException {
        super.close();
        clear();
    }
}
