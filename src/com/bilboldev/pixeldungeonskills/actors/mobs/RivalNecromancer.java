package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.CorpseExplosionSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.DarkBolt;
import com.bilboldev.pixeldungeonskills.actors.skills.DrainLifeSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonArcherSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundle;
import java.util.ArrayList;

public class RivalNecromancer extends RivalBoss {
    private int summoned, healed, fallenCell = -1;

    @Override
    public HeroClass heroClass() {
        return HeroClass.NECROMANCER;
    }
    @Override
    protected int[] health() {
        return new int[]{90, 160, 310, 465};
    }
    @Override
    protected int preferredRange() {
        return 4;
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new DrainLifeSkill();
            case 2: return new RaiseSkeletonArcherSkill();
            case 3: return new CorpseExplosionSkill();
            case 4: return new DarkBolt();
            default: return new RaiseSkeletonSkill();
        }
    }

    @Override
    protected boolean prepareSkill() {
        int skill = nextAbility();
        if (skill == 3 && fallenCell < 0) skill = 1;
        if (skill == 0 || skill == 2) {
            int count = 0;
            for (Mob mob : Dungeon.level.mobs)
                if (mob instanceof RivalServant && ((RivalServant)mob).ownerId == id() && mob.isAlive()) count++;
            int cap = tier() == 1 ? 1 : tier() == 4 ? 3 : 2;
            if (count >= cap || summoned >= tier() * 3) {
                if (tier() < 2 || phase == 0) return aim(4, aimedLine(5));
                skill = 1;
            } else return aim(skill, new int[]{pos});
        }
        if (skill == 3) {
            ArrayList<Integer> cells = new ArrayList<>();
            cells.add(fallenCell);
            for (int offset : Level.NEIGHBOURS4) {
                int cell = fallenCell + offset;
                if (valid(cell) && Level.adjacent(cell, fallenCell) && !Level.solid[cell]) cells.add(cell);
            }
            return aim(skill, array(cells));
        }
        return aim(skill, aimedLine(5));
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if ((ability == 0 || ability == 2) && cells.length == 1 && cells[0] == pos) {
            if (summon(ability == 2 ? 1 : 0) != null) summoned++;
        } else {
            int damage = spell(cells);
            if (!isAlive()) return;
            if (ability == 1) {
                int healing = Math.min(Math.min(damage / 2, HT - HP), HT / 10 - healed);
                HP += healing;
                healed += healing;
                if (healing > 0) burst(new int[]{pos}, Speck.HEALING);
            }
            if (ability == 3) {
                fallenCell = -1;
                burst(cells, Speck.BONE);
            } else if (sprite != null && sprite.parent != null && cells.length > 0 && visibleEffect(cells))
                MagicMissile.shadow(sprite.parent, warningOrigin, cells[cells.length - 1], () -> {});
        }
        recover(2);
    }

    void servantFell(int cell) {
        fallenCell = cell;
    }

    @Override
    public void damage(int damage, Object source) {
        if (damage > 0 && ability == 1 && warning.length > 0) interruptSkill();
        super.damage(damage, source);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(ability == 3 ? Assets.SND_BLAST : Assets.SND_BONES, .7f, 1, cells);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_summoned", summoned);
        b.put("rival_healed", healed);
        b.put("rival_fallen_cell", fallenCell);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        summoned = b.getInt("rival_summoned");
        healed = b.getInt("rival_healed");
        fallenCell = b.getInt("rival_fallen_cell");
    }
}
