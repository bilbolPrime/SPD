package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;

public final class SupporterIcon extends Image {
    private final int style;
    private float phase;

    public SupporterIcon() {
        this(-1);
    }

    public SupporterIcon(int style) {
        super(Icons.SUPPORTED.get());
        this.style = style;
        update();
    }

    @Override public void update() {
        super.update();
        phase += Game.elapsed;
        int current = style < 0 ? SupporterEntitlements.selectedAura() : style;
        visible = current > 0;
        hardlight(color(current, phase));
    }

    public static int color(int style, float phase) {
        switch (style) {
            case 1: return 0xE6A169;
            case 2: return 0x65D2C2;
            case 3: return 0xBA93F2;
            case 4: return 0xF6D365;
            case SupporterEntitlements.SUBSCRIBER_ICON:
                int shimmer = (int)(28 * (1 + Math.sin(phase * 1.5f)));
                return ((199 + shimmer) << 16) | (227 << 8) | (255 - shimmer / 2);
            default: return 0xFFFFFF;
        }
    }
}
