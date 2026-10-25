package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornKing;

public class RebornUndeadSprite extends UndeadSprite {
    public RebornUndeadSprite() {
        zap = attack.clone();
    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        roleColor();
    }

    @Override
    public void resetColor() {

        float opacity = am;
        super.resetColor();
        roleColor();
        if (ch != null) am = opacity;
    }

    private void roleColor() {
        if (!(ch instanceof RebornKing.Retainer)) return;
        switch (((RebornKing.Retainer)ch).role) {
            case 1: hardlight(.65f, .8f, 1f);
            break;
            case 2: hardlight(1f, .75f, .45f);
            break;
            case 3: hardlight(.65f, 1f, .65f);
            break;
            default: hardlight(1f, 1f, 1f);
        }
    }

    @Override
    public void onComplete(Animation animation) {
        if (animation == zap) idle();
        super.onComplete(animation);
    }
}
