package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.actors.buffs.FlavourBuff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

public class IAmTheLawSkill extends ActiveSkill2 {

    {
        name = "I AM THE LAW";
        castText = Message.key("custom.generated.necro_merc.i_am_the_law");
        tier = 2;
        image = 167;
        mana = 6;
        useDelay = 6f;
        level = 0;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (level > 0 && hero.MP >= getManaCost() && !coolDown()) {
            actions.add(AC_CAST);
        }

        if (hero.skillTree.canLevel(this)) {
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == AC_CAST && hero.MP >= getManaCost() && !coolDown()) {
            hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();
            castTextYell();

            LawBuff old = hero.buff(LawBuff.class);
            if (old != null) {
                old.detach();
            }

            int bonusHp = Math.max(1, Math.round(hero.HT * (0.1f * level)));
            hero.HT += bonusHp;
            hero.HP += bonusHp;

            LawBuff buff = Buff.affect(hero, LawBuff.class, level * 25f);
            buff.bonusHp = bonusHp;
            hero.heroSkills.lastUsed = this;
            return;
        }

        if (action == AC_ADVANCE) {
            super.execute(hero, action);
        }
    }

    @Override
    protected boolean upgrade() {
        return true;
    }

    @Override
    public String info() {
        return "Sometimes people need to be reminded.\n\n"
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++) {
            String levelDescription = "Level " + i + ": +" + (i * 10) + "% health for " + (i * 25) + " turns";
            if (i == level) {
                sb.append(highlight(levelDescription));
            } else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String requiresInfo() {
        if (level == 0) {
            return "\nRequires: Marshal";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements() {
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(MarshalSkill.class);
        return toReturn;
    }

    public static class LawBuff extends FlavourBuff {
        public int bonusHp;

        @Override
        public void detach() {
            if (target != null) {
                target.HT -= bonusHp;
                target.HP = Math.max(1, target.HP - bonusHp);
                if (target.HP > target.HT) {
                    target.HP = target.HT;
                }
            }
            super.detach();
        }
    }
}
