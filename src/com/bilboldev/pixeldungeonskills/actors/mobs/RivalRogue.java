package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.DoubleStab;
import com.bilboldev.pixeldungeonskills.actors.skills.ShadowCloneSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.SmokeBomb;
import com.bilboldev.pixeldungeonskills.actors.skills.Venom;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundle;

public class RivalRogue extends RivalBoss {
    private boolean cloneUsed;
    private int smokeTurns;

    @Override
    public HeroClass heroClass() {
        return HeroClass.ROGUE;
    }
    @Override
    protected int[] health() {
        return new int[]{100, 180, 350, 525};
    }

    @Override
    public boolean meleeWarning() {
        return warning.length > 0 && (ability == 0 || ability == 2);
    }

    @Override
    protected boolean act() {
        if (smokeTurns > 0) smokeTurns--;
        return super.act();
    }

    @Override
    public int defenseSkill(Char enemy) {
        return super.defenseSkill(enemy) * (smokeTurns > 0 ? 8 : 1);
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new SmokeBomb();
            case 2: return new Venom();
            case 3: return new ShadowCloneSkill();
            default: return new DoubleStab();
        }
    }

    @Override
    protected boolean prepareSkill() {
        int skill = nextAbility();
        if (skill == 3 && cloneUsed) skill = 0;
        if (skill == 1 || skill == 3) return aim(skill, new int[]{pos});
        return Level.adjacent(pos, Dungeon.hero.pos) && aim(skill, new int[]{Dungeon.hero.pos});
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (ability == 1) {
            burst(new int[]{pos}, Speck.STEAM);
            smokeTurns = 2;
            reposition();
        } else if (ability == 3) {
            cloneUsed = summon(2) != null;
        } else {

            special(cells, ability == 2 ? .4f : .5f, false);
            if (isAlive()) special(cells, ability == 2 ? .4f : .5f, false);
            if (ability == 2 && isAlive()) {
                for (int cell : cells) {
                    Char victim = Actor.findChar(cell);
                    if (victim != null && victim != this && !ally(victim) && victim.isAlive()) {
                        Buff.affect(victim, Poison.class).setChampionDuration(2);
                    }
                }
            }
            burst(cells, Speck.STAR);
        }
        recover(2);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_clone_used", cloneUsed);
        b.put("rival_smoke_turns", smokeTurns);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        cloneUsed = b.getBoolean("rival_clone_used");
        smokeTurns = b.getInt("rival_smoke_turns");
    }
}
