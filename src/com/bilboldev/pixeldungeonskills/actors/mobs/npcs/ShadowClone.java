package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.actors.Char;

/**
 * Created by Moussa on 22-Jan-17.
 */
public class ShadowClone extends MirrorImage {
    {
        name = "Shadow Clone";
    }

    int lifeSpan = 2;

    @Override
    public int attackProc( Char enemy, int damage ) {
        int dmg = super.attackProc( enemy, damage );
        dmg = 1 + dmg / 4;

        lifeSpan--;
        if(lifeSpan < 1) {
            destroy();
            sprite.die();
        }
        return dmg;
    }

    @Override
    public String description() {
        return
                "Shadow clones share your strength and cannot be attacked. \nThey will however disappear after dealing some punishment.";
    }

}
