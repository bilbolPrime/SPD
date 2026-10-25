package com.bilboldev.pixeldungeonskills.actors.Online;

import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Rat;
import com.bilboldev.pixeldungeonskills.levels.MovieLevel;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.OnlineFoeSprite;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;

public class OnlineFoe extends Mob{
    public String Identifier;
    public String Tag;

    public boolean isAI = false;
    {
        spriteClass = OnlineSprite.class;
        state = PASSIVE;
        EXP = 0;
    }

    @Override
    public boolean act() {
        spend( 0.01f );
        return true;
    }

    public void ApprovedMove(int step)
    {
        int oldPos = pos;
        move( step );
        sprite.move( oldPos, pos );
    }

    @Override
    public void die( Object cause ) {
        destroy();
        sprite.die();
    }

    public void DieWithReason (String Reason)
    {
        this.sprite.showStatus(CharSprite.NEUTRAL, Reason);
        die(null);
    }
}
