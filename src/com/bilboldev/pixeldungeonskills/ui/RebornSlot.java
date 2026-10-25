package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;

public final class RebornSlot extends ColorBlock {
    private final RebornChrome rim = new RebornChrome(Chrome.Type.BUTTON)
            .material(RebornChrome.Material.IRON).outlineOnly();

    private RebornSlot(float width, float height, int color) {
        super(width, height, color);
    }

    public static ColorBlock create(float width, float height, int color) {
        return Presentation.reborn() ? new RebornSlot(width, height, color) : new ColorBlock(width, height, color);
    }

    @Override
    public void draw() {
        float r = rm, g = gm, b = bm;
        float ar = ra, ag = ga, ab = ba;
        rm *= .43f;
        gm *= .43f;
        bm *= .52f;
        ra *= .43f;
        ga *= .43f;
        ba *= .52f;
        super.draw();
        rm = r;
        gm = g;
        bm = b;
        ra = ar;
        ga = ag;
        ba = ab;
        rim.x = x;
        rim.y = y;
        rim.size(width(), height());
        rim.camera = camera();
        rim.rm = Math.max(.8f, r);
        rim.gm = Math.max(.8f, g);
        rim.bm = Math.max(.8f, b);
        rim.am = am;
        rim.aa = aa;
        rim.draw();
    }

    @Override
    public void destroy() {
        rim.destroy();
        super.destroy();
    }
}
