package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class Frostback extends IceMob {
    {
        name = "Frostback gnoll";
        spriteClass = IceSprite.Gnoll.class;
        HP = HT = 76;
        defenseSkill = 14;
        EXP = 9;
        maxLvl = 19;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(12, 23); }
    @Override public int attackSkill(Char enemy) { return 24; }
    @Override public int dr() { return 7; }
    @Override public boolean meleeWarning() { return warning.length > 0; }

    @Override
    protected boolean prepare() {
        return Level.adjacent(pos, Dungeon.hero.pos) && warn(new int[]{Dungeon.hero.pos}, 4);
    }

    @Override
    protected void resolve(int[] cells) {
        if (Level.adjacent(pos, cells[0])) strike(cells, 26, 34);
        recover();
    }
}
