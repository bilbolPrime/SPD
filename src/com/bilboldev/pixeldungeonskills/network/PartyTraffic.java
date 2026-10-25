package com.bilboldev.pixeldungeonskills.network;

import java.io.IOException;

final class PartyTraffic {
    private final int rate, burst;
    private double messages;
    private double bytes = 16 * 1024 * 1024;
    private long updated = System.nanoTime();

    PartyTraffic() {
        this(60, 128);
    }

    PartyTraffic(int rate, int burst) {
        this.rate = rate;
        this.burst = burst;
        messages = burst;
    }

    void receive(int size) throws IOException {
        if (!allow(size)) throw new IOException("Party traffic limit exceeded");
    }

    boolean allow(int size) {
        long now = System.nanoTime();
        double seconds = Math.max(0, (now - updated) / 1000000000.0);
        updated = now;
        messages = Math.min(burst, messages + seconds * rate);
        bytes = Math.min(16 * 1024 * 1024, bytes + seconds * 2 * 1024 * 1024);
        if (size < 0 || messages < 1 || bytes < size) return false;
        messages--;
        bytes -= size;
        return true;
    }
}
