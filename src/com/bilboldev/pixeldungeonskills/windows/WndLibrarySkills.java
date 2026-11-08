package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Shelf;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import java.util.ArrayList;

public final class WndLibrarySkills extends WndLibrary {
    private final ArrayList<SkillIcon> skills = new ArrayList<>();

    public WndLibrarySkills(HeroClass hero) {
        int width = boundedWidth(landscapeLayout() ? 240 : 180);
        Entry entry = LibraryCatalog.find(Shelf.CLASSES, "hero." + hero.name());
        Component heading = InspectionTitle.create(entry.image(), entry.name());
        heading.setRect(0, 0, width, 0);
        Component content = new Component();
        boolean landscape = landscapeLayout();
        float column = landscape ? textWidth(width) / 3f : textWidth(width);
        ArrayList<HeroSubClass> branches = new ArrayList<>();
        for (Talent talent : Talent.values()) {
            if (talent.heroClass == hero && talent.available() && !branches.contains(talent.subclass))
                branches.add(talent.subclass);
        }
        for (int branch = 0; branch < branches.size(); branch++) {
            HeroSubClass subclass = branches.get(branch);
            String label = (subclass == HeroSubClass.NONE ? Message.heroClass(hero) : Message.subclass(subclass, false)).toString();
            BitmapText title = PixelScene.createText(Messages.capitalize(label), 7);
            title.measure();
            title.scale.set(title.scale.x * Math.min(1, (column - 4) / title.width()));
            title.x = (landscape ? branch * column : 0) + (column - title.width()) / 2;
            title.y = (landscape ? 0 : branch * 138) + 4;
            title.hardlight(0xECD095);
            content.add(title);
            for (Talent talent : Talent.values()) {
                if (talent.heroClass != hero || talent.subclass != subclass || !talent.available()) continue;
                Entry skill = LibraryCatalog.find(Shelf.ABILITIES, talent.name());
                float left = landscape ? branch * column : 0;
                float top = (landscape ? 0 : branch * 138) + 24;
                float center = left + column * (.18f + .32f * talent.column);
                SkillIcon icon = new SkillIcon(skill, 1.25f);
                icon.setRect(center - 10, top + talent.row * 28, 20, 20);
                if (talent.prerequisite >= 0) {
                    for (Talent parent : Talent.values()) {
                        if (parent.heroClass != hero || parent.subclass != talent.subclass || parent.slot != talent.prerequisite) continue;
                        float x = left + column * (.18f + .32f * parent.column);
                        float y = top + parent.row * 28 + 20;
                        float bend = (y + icon.top()) / 2;
                        connection(content, x, y, x, bend);
                        connection(content, x, bend, center, bend);
                        connection(content, center, bend, center, icon.top());
                        break;
                    }
                }
                content.add(icon);
                skills.add(icon);
            }
        }
        content.setSize(textWidth(width), landscape ? 132 : 408);
        layoutLibrary(width, heading, content, 6);
    }

    private void connection(Component content, float x1, float y1, float x2, float y2) {
        ColorBlock line = new ColorBlock(Math.max(1, Math.abs(x2 - x1)), Math.max(1, Math.abs(y2 - y1)), 0xFF554E61);
        line.x = Math.min(x1, x2);
        line.y = Math.min(y1, y2);
        content.addToBack(line);
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override public void onClick(float x, float y) {
                for (SkillIcon skill : skills) {
                    if (skill.inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        WndLibrarySkills.this.parent.add(new Description(skill.entry));
                        return;
                    }
                }
            }
        };
    }

    private static final class SkillIcon extends Component {
        final Entry entry;
        private final Image image;

        SkillIcon(Entry entry, float scale) {
            this.entry = entry;
            image = entry.image();
            image.scale.set(.75f * scale);
            add(image);
        }

        @Override protected void layout() {
            image.x = x + (width - image.width()) / 2;
            image.y = y + (height - image.height()) / 2;
        }
    }

    private static final class Description extends WndLibrary {
        Description(Entry entry) {
            int width = boundedWidth(landscapeLayout() ? 260 : 144);
            Component heading = InspectionTitle.create(entry.image(), entry.name());
            heading.setRect(0, 0, width, 0);
            HighlightedText info = new HighlightedText(8);
            info.setColor(0xDDD4C5, 0xECD095);
            info.text(entry.info(), textWidth(width) - 8, false);
            info.setPos(4, 4);
            Component content = new Component();
            content.add(info);
            content.setSize(textWidth(width), info.bottom() + 4);
            layoutLibrary(width, heading, content, 6);
        }
    }
}
