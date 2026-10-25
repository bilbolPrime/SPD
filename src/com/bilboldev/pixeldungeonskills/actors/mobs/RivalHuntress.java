package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Slow;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.AimedShot;
import com.bilboldev.pixeldungeonskills.actors.skills.Bombvoyage;
import com.bilboldev.pixeldungeonskills.actors.skills.DoubleShot;
import com.bilboldev.pixeldungeonskills.actors.skills.KneeShot;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import java.util.ArrayList;

public class RivalHuntress extends RivalBoss {
    @Override
    public HeroClass heroClass() {
        return HeroClass.HUNTRESS;
    }
    @Override
    protected int[] health() {
        return new int[]{100, 180, 350, 525};
    }
    @Override
    protected int preferredRange() {
        return 6;
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new DoubleShot();
            case 2: return new KneeShot();
            case 3: return new Bombvoyage();
            default: return new AimedShot();
        }
    }

    @Override
    protected boolean prepareSkill() {
        int skill = nextAbility();
        if (skill != 3) return aim(skill, aimedLine(8));
        ArrayList<Integer> cells = new ArrayList<>();
        cells.add(Dungeon.hero.pos);
        for (int offset : Level.NEIGHBOURS4) {
            int cell = Dungeon.hero.pos + offset;
            if (valid(cell) && Level.adjacent(cell, Dungeon.hero.pos) && !Level.solid[cell]) cells.add(cell);
        }
        return aim(skill, array(cells));
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (ability == 1) {
            special(cells, .5f, true);
            if (isAlive()) special(cells, .5f, true);
        } else {
            special(cells, 1, true);
        }
        if (!isAlive()) return;
        if (ability == 2) {
            for (int cell : cells) {
                Char victim = Actor.findChar(cell);
                if (victim != null && victim != this && !ally(victim) && victim.isAlive()) {
                    Buff.prolong(victim, Slow.class, 2);
                }
            }
        }
        if (ability == 3) burst(cells, Speck.STAR);
        else if (sprite != null && sprite.parent != null && cells.length > 0 && visibleEffect(cells))
            ((MissileSprite)sprite.parent.recycle(MissileSprite.class))
                    .reset(warningOrigin, cells[cells.length - 1], ItemSpriteSheet.Arrow, null, null);
        recover(ability == 3 ? 3 : 2);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(ability == 3 ? Assets.SND_BLAST : Assets.SND_MISS, .7f, 1, cells);
    }
}
