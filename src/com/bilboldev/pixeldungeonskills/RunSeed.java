package com.bilboldev.pixeldungeonskills;

import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.io.IOException;

public final class RunSeed {
    public static final int SYMBOLS = 6;
    public static final int INITIAL = 1, FLOOR = 2, GRASS = 3, PLANT = 4, WELL = 5, ALCHEMY = 6, QUEST = 7, CHALLENGES = 8;
    public final long code;
    public final boolean custom;

    public RunSeed(long code, boolean custom) {
        if (!valid(code)) throw new IllegalArgumentException("Invalid seed symbols");
        this.code = code;
        this.custom = custom;
    }

    public static boolean valid(long code) {
        if (code < 0 || code >= 1_000_000_000_000_000_000L) return false;
        for (int i = 0; i < SYMBOLS; i++, code /= 1000) {
            if (code % 1000 >= SeedIcons.COUNT) return false;
        }
        return true;
    }

    public static RunSeed fromSymbols(int[] symbols, boolean custom) {
        if (symbols == null || symbols.length != SYMBOLS) throw new IllegalArgumentException("Invalid seed length");
        long code = 0;
        for (int symbol : symbols) {
            if (symbol < 0 || symbol >= SeedIcons.COUNT || symbol >= 1000)
                throw new IllegalArgumentException("Invalid seed symbol");
            code = code * 1000 + symbol;
        }
        return new RunSeed(code, custom);
    }

    public static RunSeed random() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int[] symbols = new int[SYMBOLS];
        for (int i = 0; i < symbols.length; i++) symbols[i] = random.nextInt(SeedIcons.COUNT);
        return fromSymbols(symbols, false);
    }

    public int symbol(int slot) {
        if (slot < 0 || slot >= SYMBOLS) throw new IllegalArgumentException("Invalid seed slot");
        long value = code;
        for (int i = slot + 1; i < SYMBOLS; i++) value /= 1000;
        return (int)(value % 1000);
    }

    public String text() {
        StringBuilder text = new StringBuilder("001");
        for (int i = 0; i < SYMBOLS; i++) {
            int symbol = symbol(i);
            text.append('-');
            if (symbol < 100) text.append('0');
            if (symbol < 10) text.append('0');
            text.append(symbol);
        }
        return text.toString();
    }

    public static RunSeed parse(String text) {
        if (text == null || text.length() > 64) return null;
        text = text.trim();
        if (text.length() != 3 + SYMBOLS * 4 || !text.startsWith("001-")) return null;
        int[] symbols = new int[SYMBOLS];
        for (int i = 0; i < SYMBOLS; i++) {
            int start = 3 + i * 4;
            if (text.charAt(start) != '-') return null;
            int symbol = 0;
            for (int j = 1; j <= 3; j++) {
                char digit = text.charAt(start + j);
                if (digit < '0' || digit > '9') return null;
                symbol = symbol * 10 + digit - '0';
            }
            if (symbol >= SeedIcons.COUNT) return null;
            symbols[i] = symbol;
        }
        return fromSymbols(symbols, true);
    }

    public long value(int domain, int position) {
        long value = code ^ 0x51ED270B4C683D29L ^ ((long)domain << 32) ^ position;
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public static boolean active() {
        return Dungeon.rules().hasSeed() && Dungeon.seed != null;
    }

    public static boolean customRun() {
        return active() && Dungeon.seed.custom;
    }

    public static Random.Scope scope(int domain, int position) {
        return active() ? Random.use(Dungeon.seed.value(domain, position)) : null;
    }

    public static Random.Scope terrain(int domain, int cell) {
        if (!active()) return null;
        int use = Dungeon.level.seedUses[cell]++;
        long seed = Dungeon.seed.value(domain, Dungeon.levelKey());
        return Random.use(seed ^ ((long)cell << 32) ^ use);
    }

    public void store(Bundle bundle) {
        int[] symbols = new int[SYMBOLS];
        for (int i = 0; i < symbols.length; i++) symbols[i] = symbol(i);
        bundle.put("run_seed", symbols);
        bundle.put("custom_seed", custom);
    }

    public static RunSeed restore(Bundle bundle, RunRules rules) throws IOException {
        if (!rules.hasSeed()) return null;
        if (!bundle.contains("run_seed") || !bundle.contains("custom_seed"))
            throw new IOException("Missing or invalid run seed");
        try {
            return fromSymbols(bundle.getIntArray("run_seed"), bundle.getBoolean("custom_seed"));
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid run seed", invalid);
        }
    }
}
