package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Slow;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.KnockBack;
import com.bilboldev.pixeldungeonskills.actors.skills.Rampage;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.Smash;
import com.bilboldev.pixeldungeonskills.actors.skills.Smite;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;

public class RivalWarrior extends RivalBoss {
    @Override
    public HeroClass heroClass() {
        return HeroClass.WARRIOR;
    }
    @Override
    protected int[] health() {
        return new int[]{135, 240, 465, 695};
    }

    @Override
    public boolean meleeWarning() {
        return warning.length > 0;
    }

    @Override
    public int damageRoll() {
        return Math.round(super.damageRoll() * (tier() == 4 && phase == 2 ? 1.15f : 1));
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new KnockBack();
            case 2: return new Smite();
            case 3: return new Rampage();
            default: return new Smash();
        }
    }

    @Override
    protected boolean prepareSkill() {
        int skill = nextAbility();
        if (skill == 3) return aim(skill, aimedLine(5));
        return Level.distance(pos, Dungeon.hero.pos) <= 2 && aim(skill, cone(pos, Dungeon.hero.pos, 2));
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        special(cells, 1, false);
        if (!isAlive()) return;
        burst(cells, Speck.STAR);
        for (int cell : cells) {
            Char victim = Actor.findChar(cell);
            if (victim == null || victim == this || ally(victim) || !victim.isAlive()) continue;
            if (ability == 1 || ability == 3) Pushing.knockBack(warningOrigin, victim, 1);
            if (ability == 2) Buff.prolong(victim, Slow.class, 2);
        }
        if (ability == 3 && !rooted) {
            int destination = pos;
            for (int cell : cells) {
                if (!Level.passable[cell] || Level.avoid[cell] || Actor.findChar(cell) != null) break;
                destination = cell;
            }
            if (destination != pos) {
                int from = pos;
                move(destination);
                if (sprite != null) sprite.move(from, destination);
            }
        }
        recover(ability == 3 ? 3 : 2);
    }
}
