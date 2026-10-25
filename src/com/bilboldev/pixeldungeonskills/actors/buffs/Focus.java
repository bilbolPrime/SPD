package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.ActiveSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillContainer;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Focus extends FlavourBuff {
    public static final float DURATION = 100f;

    public static float cooldown(float turns) {
        return Math.max(0f, turns - Math.max(1f, turns * .25f));
    }

    public static void apply(Hero hero) {
        if (hero.buff(Focus.class) == null) {
            Set<Skill> skills = new HashSet<>();
            Collections.addAll(skills, hero.heroSkills.active1, hero.heroSkills.active2, hero.heroSkills.active3);
            for (SkillContainer slot : hero.skillTree.getSkills()) skills.add(slot.skill);
            for (Skill skill : skills) {
                if (skill instanceof ActiveSkill) {
                    float remaining = skill.availableAfter - Skill.skillTrack;
                    if (remaining > 1f) skill.availableAfter = Skill.skillTrack + cooldown(remaining);
                }
            }
        }
        Buff.prolong(hero, Focus.class, DURATION);
    }

    @Override
    public int icon() {
        return BuffIndicator.FOCUS;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String displayName() {
        return Messages.get(this, "remaining", (int)Math.ceil(Math.max(0, cooldown())));
    }
}
