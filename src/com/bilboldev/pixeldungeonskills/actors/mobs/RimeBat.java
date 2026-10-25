package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class RimeBat extends IceMob {
    {
        name = "Rime bat";
        spriteClass = IceSprite.Bat.class;
        HP = HT = 40;
        defenseSkill = 20;
        EXP = 7;
        maxLvl = 18;
        baseSpeed = 1.75f;
        flying = true;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(7, 13); }
    @Override public int attackSkill(Char enemy) { return 20; }
    @Override public int dr() { return 3; }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (cooldown == 0 && enemy.buff(Frostbite.class) == null) {
            Buff.affect(enemy, Frostbite.class, 2);
            cooldown = 4;
        }
        return super.attackProc(enemy, damage);
    }
}
