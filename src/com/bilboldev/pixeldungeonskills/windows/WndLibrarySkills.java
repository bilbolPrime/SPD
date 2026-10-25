package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Shelf;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillContainer;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillTree;
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
        SkillTree tree = LibraryCatalog.skillTree(hero);
        float available = Game.height / camera.zoom - frameHeight() - heading.height() - 20;
        float scale = Math.min(2, Math.min((textWidth(width) - 8) / 105f, available / 77f));
        float left = (textWidth(width) - 105 * scale) / 2;
        for (ColorBlock block : tree.getBackground()) {
            block.x = left + (block.x - 7) * scale;
            block.y = (block.y - 14) * scale;
            block.size(block.width() * scale, block.height() * scale);
            content.add(block);
        }
        for (SkillContainer node : tree.getSkills()) {
            Entry skill = LibraryCatalog.find(Shelf.ABILITIES, "skill." + hero.name() + "." + node.skill.image);
            SkillIcon icon = new SkillIcon(skill, scale);
            float x = node.x == 4 ? 98 : 7 + 28 * (node.x - 1);
            float y = 21 * (node.y - 1);
            icon.setRect(left + (x - 9) * scale, (y - 2) * scale, 18 * scale, 18 * scale);
            content.add(icon);
            skills.add(icon);
        }
        content.setSize(textWidth(width), 77 * scale);
        layoutLibrary(width, heading, content, 6);
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
