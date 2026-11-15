package com.bilboldev.pixeldungeonskills.actors.skills.skill4;

import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.ActiveSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import java.util.ArrayList;

public final class TalentSkill extends ActiveSkill {
    public final Talent talent;
    public final RebornSkillTree tree;

    TalentSkill(RebornSkillTree tree, Talent talent) {
        this.tree = tree;
        this.talent = talent;
        name = talent.title();
        image = talent.icon;
        mana = talent.mana;
        useDelay = talent.cooldown;
        quickCast = !talent.passive() && !talent.toggle();
    }

    public int rank() { return tree.rank(talent); }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = new ArrayList<>();
        if (hero != tree.hero) return actions;
        if (rank() > 0) {
            if (talent.toggle()) actions.add(active ? AC_DEACTIVATE : AC_ACTIVATE);
            else if (!talent.passive() && !coolDown() && hero.canUseSkill(getManaCost())) actions.add(AC_CAST);
        }
        if (tree.canLevel(this)) actions.add(AC_ADVANCE);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (hero != tree.hero || !hero.isAlive()) return;
        if (AC_ADVANCE.equals(action)) tree.advance(this);
        else if (talent.toggle() && rank() > 0 && (AC_ACTIVATE.equals(action) || AC_DEACTIVATE.equals(action))) {
            if (AC_ACTIVATE.equals(action)) QuickSlot.remember(hero, this);
            active = AC_ACTIVATE.equals(action);
            if (active) TutorialRun.skillActivated(hero, this);
            QuickSlot.refresh();
        } else if (AC_CAST.equals(action)) {
            TutorialRun.skillActivated(hero, this);
            tree.use(this);
        }
    }

    @Override
    public String info() {
        return info(Skill.availableSkill, false);
    }

    public String info(int freePoints, boolean preview) {
        int rank = rank();
        HeroSubClass subclass = tree.hero.subClass;
        StringBuilder text = new StringBuilder(Messages.get(talent.key() + ".intro"));
        if (rank > 0) {
            text.append("\n\n_").append(Messages.get("skills4.current", rank)).append("_\n")
                    .append(talent.rankDescription(rank, subclass));
            if (rank > level) text.append('\n').append(Messages.get("skills4.ranks", level, rank - level));
        } else if (EquipmentAttribute.bonus(tree.hero, EquipmentAttribute.Type.SKILL, talent.ordinal()) > 0) {
            text.append("\n\n").append(Messages.get("skills4.equipment_locked"));
        }
        if (level < talent.maxRank()) {
            int next = tree.nextRank(talent);
            text.append("\n\n_").append(Messages.get("skills4.next", next)).append("_\n")
                    .append(talent.rankDescription(next, subclass));
        }
        text.append("\n\n").append(Messages.get("skills4.max_rank", talent.maxRank()));
        if (talent == Talent.SPARK) {
            if (subclass == HeroSubClass.WARLOCK) text.append("\n\n").append(Messages.get("skills4.spark.warlock"));
            if (subclass == HeroSubClass.BATTLEMAGE) text.append("\n\n").append(Messages.get("skills4.spark.battlemage"));
        }
        switch (talent.effect) {
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_GHOST: case RAISE_SKELETON: case RAISE_ARCHER:
                if (subclass == HeroSubClass.WARLOCK) text.append("\n\n").append(Messages.get("skills4.summon.warlock"));
                if (subclass == HeroSubClass.LICH) text.append("\n\n").append(Messages.get("skills4.summon.lich"));
                if (subclass == HeroSubClass.SPIRIT_BINDER && talent.effect == Talent.Effect.RAISE_SKELETON)
                    text.append("\n\n").append(Messages.get("skills4.raise.spirit_binder"));
                break;
            default: break;
        }
        if (!talent.passive() && talent.cooldown > 0) {
            text.append("\n\n").append(Messages.get("skills4.cost", getManaCost(), tree.cooldown(talent)));
            if (!preview && coolDown()) text.append('\n').append(Messages.get("skills4.wait", (int)Math.ceil(availableAfter - skillTrack)));
        }
        if (level < talent.maxRank()) {
            String requirements = tree.requirement(talent);
            if (!requirements.isEmpty()) text.append("\n\n").append(requirements);
            if (freePoints == 0)
                text.append("\n\n").append(Messages.get("skills4.no_points"));
        }
        return text.toString();
    }
}
