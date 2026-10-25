package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Slow;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.SlowSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Spark;
import com.bilboldev.pixeldungeonskills.actors.skills.SpiritArmor;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundle;
import java.util.ArrayList;

public class RivalMage extends RivalBoss {
    private int shield, shieldSpent;

    @Override
    public HeroClass heroClass() {
        return HeroClass.MAGE;
    }
    @Override
    protected int[] health() {
        return new int[]{95, 175, 330, 490};
    }
    @Override
    protected int preferredRange() {
        return 4;
    }

    @Override
    protected Skill abilityName() {
        switch (ability) {
            case 1: return new SpiritArmor();
            case 2: return new SlowSkill();
            default: return new Spark();
        }
    }

    @Override
    protected boolean prepareSkill() {
        int skill = nextAbility();
        if (skill == 1 && shieldSpent >= HT / 10) skill = 0;
        if (skill == 1) return aim(skill, new int[]{pos});
        int[] cells = aimedLine(6);
        if (skill == 3 && cells.length > 0) {
            ArrayList<Integer> volley = new ArrayList<>();
            int target = Dungeon.hero.pos;
            int side = Math.abs(target % Level.WIDTH - pos % Level.WIDTH)
                    > Math.abs(target / Level.WIDTH - pos / Level.WIDTH) ? Level.WIDTH : 1;
            for (int aim : new int[]{target, target - side, target + side}) {
                if (!valid(aim) || Level.distance(aim, target) > 1) continue;
                for (int cell : ray(pos, aim, 6)) {
                    if (!volley.contains(cell)) volley.add(cell);
                }
            }
            cells = array(volley);
        }
        return aim(skill, cells);
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (ability == 1) {
            shield = Math.min(HT / 20, HT / 10 - shieldSpent);
            shieldSpent += shield;
            burst(cells, Speck.LIGHT);
        } else {
            spell(cells);
            for (int cell : cells) {
                Char victim = Actor.findChar(cell);
                if (ability == 2 && victim != null && victim != this && !ally(victim) && victim.isAlive())
                    Buff.prolong(victim, Slow.class, 2);
            }
            if (sprite != null && sprite.parent != null && cells.length > 0 && visibleEffect(cells))
                MagicMissile.blueLight(sprite.parent, warningOrigin, cells[cells.length - 1], () -> {});
            burst(cells, Speck.LIGHT);
        }
        recover(ability == 3 ? 3 : 2);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(Assets.SND_ZAP, .7f, 1, cells);
    }

    @Override
    public void damage(int damage, Object source) {
        int absorbed = Math.min(Math.max(0, damage), shield);
        shield -= absorbed;
        super.damage(damage - absorbed, source);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_shield", shield);
        b.put("rival_shield_spent", shieldSpent);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        shield = b.getInt("rival_shield");
        shieldSpent = b.getInt("rival_shield_spent");
    }
}
