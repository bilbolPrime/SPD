package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.HeadShotSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.IAmTheLawSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.QuickDrawSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.SteadyAimSkill;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.utils.Bundle;

public class RivalMercenary extends RivalBoss {
    private int bullets = 3;

    @Override
    public HeroClass heroClass() {
        return HeroClass.MERCENARY;
    }
    @Override
    protected int[] health() {
        return new int[]{110, 200, 385, 580};
    }
    @Override
    protected int preferredRange() {
        return 5;
    }

    @Override
    protected int responsesRequired() {
        return ability == 2 ? 2 : 1;
    }

    private boolean reload() {
        if (bullets > 0) return false;
        bullets = 3;
        yell(Message.of(this, "reload"));
        recover(2);
        return true;
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (reload()) {
            spend(TICK);
            return true;
        }
        return super.doAttack(enemy);
    }

    @Override
    public boolean attack(Char enemy) {
        if (bullets <= 0) return false;
        bullets--;
        return super.attack(enemy);
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new SteadyAimSkill();
            case 2: return new HeadShotSkill();
            case 3: return new IAmTheLawSkill();
            default: return new QuickDrawSkill();
        }
    }

    @Override
    protected boolean prepareSkill() {
        if (reload()) return true;
        int skill = nextAbility();
        return aim(skill, skill == 3 ? cone(pos, Dungeon.hero.pos, 5) : aimedLine(7));
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        bullets = ability == 3 ? 0 : Math.max(0, bullets - 1);
        special(cells, ability == 0 ? .8f : 1, true);
        burst(cells, Speck.STAR);
        recover(ability >= 2 ? 3 : 2);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(Assets.SND_BLAST, .6f, 1.2f, cells);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_bullets", bullets);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        bullets = b.getInt("rival_bullets");
    }
}
