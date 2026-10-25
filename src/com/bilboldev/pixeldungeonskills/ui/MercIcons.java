package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;

public final class MercIcons {
    private MercIcons() { }

    public static Image portrait(HiredMerc.MERC_TYPES type) {
        int column;
        switch (type) {
            case Brute: column = 0; break;
            case Wizard: column = 1; break;
            case Thief: column = 2; break;
            case Archer: column = 3; break;
            case ArcherMaiden: column = 4; break;
            default: throw new IllegalArgumentException("Unknown mercenary");
        }
        return new Image(Assets.MERC_ICONS, column * 16, 0, 16, 16);
    }

    public static Image company() {
        return new Image(Assets.MERC_ICONS, 80, 0, 16, 16);
    }
}
