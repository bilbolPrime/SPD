package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.skills.*;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import java.util.ArrayList;
import java.util.Locale;

public final class LibraryChampionSkills extends Component {
    private static final int GAP = 8;
    private final ArrayList<SkillRow> rows = new ArrayList<>();
    private final int columns;

    public LibraryChampionSkills(float width, RebornChampion.Skill[] skills) {
        this.width = width;
        columns = width >= 320 ? Math.min(3, skills.length) : 1;
        float rowWidth = (width - GAP * (columns - 1)) / columns;
        for (RebornChampion.Skill skill : skills) {
            SkillRow row = new SkillRow(rowWidth, skill);
            rows.add(row);
            add(row);
        }
        layout();
    }

    @Override
    protected void layout() {
        float top = y;
        for (int i = 0; i < rows.size(); i += columns) {
            float bottom = top;
            for (int column = 0; column < columns && i + column < rows.size(); column++) {
                SkillRow row = rows.get(i + column);
                row.setPos(x + column * (row.width() + GAP), top);
                bottom = Math.max(bottom, row.bottom());
            }
            top = bottom + GAP;
        }
        height = Math.max(0, top - y - GAP);
    }

    static Skill heroSkill(RebornChampion.Skill skill) {
        switch (skill) {
            case SMASH: return new Smash();
            case SMITE: return new Smite();
            case KNOCKBACK: return new KnockBack();
            case VENOM: return new Venom();
            case DOUBLE_STRIKE: return new DoubleStab();
            case DOUBLE_SHOT: return new DoubleShot();
            case KNEE_SHOT: return new KneeShot();
            case CURSE: return new CurseSkill();
            case SLOW: return new Slow();
            default: throw new IllegalArgumentException("Unknown champion skill: " + skill);
        }
    }

    static String description(RebornChampion.Skill skill) {
        return LibraryCatalog.text("champion_" + skill.name().toLowerCase(Locale.ROOT));
    }

    private static final class SkillRow extends Component {
        private final SkillSprite icon;
        private final HighlightedText name, description;

        SkillRow(float width, RebornChampion.Skill skill) {
            Skill ability = heroSkill(skill);
            icon = new SkillSprite(ability.image);
            icon.scale.set(1.5f);
            add(icon);
            name = new HighlightedText(8);
            name.setColor(0xECD095, 0xECD095);
            name.text(Messages.capitalize(Messages.maybeTranslate(ability.name)), (int)width - 32, false);
            add(name);
            description = new HighlightedText(8);
            description.setColor(0xDDD4C5, 0xECD095);
            description.text(LibraryChampionSkills.description(skill), (int)width - 32, false);
            add(description);
            setSize(width, Math.max(icon.height(), name.height() + 3 + description.height()));
        }

        @Override
        protected void layout() {
            icon.x = x;
            icon.y = y;
            name.setPos(x + 32, y);
            description.setPos(name.left(), name.bottom() + 3);
        }
    }
}
