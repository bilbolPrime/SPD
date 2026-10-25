package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.*;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LibraryRivalSkills extends Component {
    private final HighlightedText title = new HighlightedText(9);
    private final ArrayList<Component> rows = new ArrayList<>();
    private final int columns;

    public LibraryRivalSkills(float width, HeroClass hero, int depth) {
        this.width = width;
        columns = width >= 180 ? 2 : 1;
        float cellWidth = (width - 8 * (columns - 1)) / columns;
        title.setColor(0xECD095, 0xECD095);
        title.text(LibraryCatalog.text("skills"), (int)width, false);
        add(title);
        for (Skill skill : skills(hero, depth)) {
            SkillSprite icon = new SkillSprite(skill.image);
            HighlightedText name = new HighlightedText(8);
            name.setColor(0xDDD4C5, 0xECD095);
            name.text(Messages.capitalize(Messages.maybeTranslate(skill.name)), (int)cellWidth - 24, false);
            Component row = new Component() {
                @Override protected void layout() {
                    icon.x = x;
                    icon.y = y;
                    name.setPos(x + 24, y);
                }
            };
            row.add(icon);
            row.add(name);
            row.setSize(cellWidth, Math.max(icon.height(), name.height()));
            rows.add(row);
        }
        for (Component row : rows) add(row);
        layout();
    }

    @Override
    protected void layout() {
        title.setPos(x, y);
        float top = title.bottom() + 6;
        for (int i = 0; i < rows.size(); i += columns) {
            float bottom = top;
            for (int column = 0; column < columns && i + column < rows.size(); column++) {
                Component row = rows.get(i + column);
                row.setPos(x + column * (row.width() + 8), top);
                bottom = Math.max(bottom, row.bottom());
            }
            top = bottom + 4;
        }
        height = top - y - 4;
    }

    public static List<Skill> skills(HeroClass hero, int depth) {
        if (depth < 5 || depth > 20 || depth % 5 != 0) throw new IllegalArgumentException("Invalid rival depth");
        Skill[] choices;
        switch (hero) {
            case WARRIOR:
                choices = new Skill[]{new Smash(), new KnockBack(), new Smite(), new Rampage()};
                break;
            case MAGE:
                choices = new Skill[]{new Spark(), new SpiritArmor(), new SlowSkill(), new Spark()};
                break;
            case ROGUE:
                choices = new Skill[]{new DoubleStab(), new SmokeBomb(), new Venom(), new ShadowCloneSkill()};
                break;
            case HUNTRESS:
                choices = new Skill[]{new AimedShot(), new DoubleShot(), new KneeShot(), new Bombvoyage()};
                break;
            case NECROMANCER:
                choices = new Skill[]{new RaiseSkeletonSkill(), new DrainLifeSkill(), new RaiseSkeletonArcherSkill(), new CorpseExplosionSkill()};
                break;
            case MERCENARY:
                choices = new Skill[]{new QuickDrawSkill(), new SteadyAimSkill(), new HeadShotSkill(), new IAmTheLawSkill()};
                break;
            default: throw new IllegalArgumentException("Unknown rival hero");
        }
        ArrayList<Skill> skills = new ArrayList<>(Arrays.asList(choices).subList(0, depth / 5));
        if (hero == HeroClass.NECROMANCER) skills.add(new DarkBolt());
        return skills;
    }
}
