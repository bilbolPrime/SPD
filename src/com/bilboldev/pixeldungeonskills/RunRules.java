package com.bilboldev.pixeldungeonskills;

import com.bilboldev.utils.Bundle;
import java.io.IOException;

public final class RunRules {
    private static final String MODE = "gameplay_mode";
    private static final String VERSION = "rules_version";
    public static final RunRules CLASSIC = new RunRules(GameplayMode.CLASSIC, 0);
    private static final RunRules REBORN_V1 = new RunRules(GameplayMode.REBORN, 1);
    private static final RunRules REBORN_V2 = new RunRules(GameplayMode.REBORN, 2);
    private static final RunRules REBORN_V3 = new RunRules(GameplayMode.REBORN, 3);

    public final GameplayMode mode;
    public final int version;

    private RunRules(GameplayMode mode, int version) {
        this.mode = mode;
        this.version = version;
    }

    public static RunRules newRun(GameplayMode mode) {
        if (mode == null) throw new IllegalArgumentException("Gameplay mode is required");
        return mode == GameplayMode.REBORN ? REBORN_V3 : CLASSIC;
    }

    public boolean allowsRatKing() {
        return mode == GameplayMode.CLASSIC && !GauntletRun.active();
    }

    public boolean allowsOnDemandHiring() {
        return mode == GameplayMode.CLASSIC && !GauntletRun.active();
    }

    public boolean hasRebornEncounters() {
        return mode == GameplayMode.REBORN && version >= 2;
    }

    public boolean hasThemedDungeons() {
        return mode == GameplayMode.REBORN && version >= 3;
    }

    public boolean hasRebornBosses() {
        return mode == GameplayMode.REBORN && version >= 3;
    }

    public int difficulty(int selected) {
        return mode == GameplayMode.REBORN ? Difficulties.NORMAL.ordinal() : selected;
    }

    public int challenges(int selected) {
        return mode == GameplayMode.REBORN ? 0 : selected;
    }

    public void store(Bundle bundle) {
        bundle.put(MODE, mode.id);
        bundle.put(VERSION, version);
    }

    public static RunRules restore(Bundle bundle) throws IOException {
        if (bundle == null || bundle.isNull()) throw new IOException("Missing save data");
        if (!bundle.contains(MODE) && !bundle.contains(VERSION)) return CLASSIC;
        if (!bundle.contains(MODE) || !bundle.contains(VERSION)) throw new IOException("Incomplete gameplay rules");
        GameplayMode mode;
        try {
            mode = GameplayMode.fromId(bundle.getString(MODE));
        }
        catch (IllegalArgumentException e) {
            throw new IOException("Unsupported gameplay mode", e);
        }
        int version = bundle.getInt(VERSION);

        if (mode == GameplayMode.CLASSIC && version == 0) return CLASSIC;
        if (mode == GameplayMode.REBORN && version == 1) return REBORN_V1;
        if (mode == GameplayMode.REBORN && version == 2) return REBORN_V2;
        if (mode == GameplayMode.REBORN && version == 3) return REBORN_V3;
        throw new IOException("Unsupported gameplay rules version");
    }
}
