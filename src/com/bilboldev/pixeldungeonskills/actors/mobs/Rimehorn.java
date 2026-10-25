package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class Rimehorn extends IceMob {
    {
        name = "Rimehorn";
        spriteClass = IceSprite.Horn.class;
        HP = HT = 90;
        defenseSkill = 10;
        EXP = 10;
        maxLvl = 20;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(14, 25); }
    @Override public int attackSkill(Char enemy) { return 25; }
    @Override public int dr() { return 6; }

    @Override
    protected boolean prepare() {
        return !rooted && warn(RebornBoss.ray(pos, Dungeon.hero.pos, 3), 5);
    }

    @Override
    protected void resolve(int[] cells) {
        if (rooted || pos != attackOrigin) return;
        int destination = pos;
        for (int cell : cells) {
            if (!Level.passable[cell] || !RebornBoss.lineClear(pos, cell)) break;
            if (Actor.findChar(cell) != null) {
                strike(new int[]{cell}, 22, 32);
                break;
            }
            destination = cell;
        }
        if (isAlive() && destination != pos) {
            int from = pos;
            move(destination);
            Actor.freeCell(from);
            Actor.occupyCell(this);
            if (sprite != null) sprite.move(from, destination);
        }
        recover();
    }
}
