package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.SeedIcons;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.ui.ChallengeIcons;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndRunSetup extends WndScrollable {
    private final ControlRow choices = new ControlRow();

    public WndRunSetup(HeroClass hero, RunSeed seed, int challenges) {
        int width = boundedWidth(landscapeLayout() ? 320 : 180);
        int bodyWidth = textWidth(width);
        boolean wide = landscapeLayout() && bodyWidth >= 260;
        float cardWidth = wide ? (bodyWidth - 8) / 2f : bodyWidth;
        IconTitle heading = new IconTitle(HeroSprite.avatar(hero, 0), GameplayMode.REBORN.title());
        heading.setRect(0, 0, width, 0);

        HighlightedText seedTitle = new HighlightedText(9);
        seedTitle.setColor(titleColor(), titleColor());
        seedTitle.text(Messages.get(RunSeed.class, seed == null ? "title" : "custom"), (int)cardWidth - 16, false);
        seedTitle.setPos(8, 8);
        choices.add(seedTitle);
        float cell = (cardWidth - 16 - (RunSeed.SYMBOLS - 1) * 3) / RunSeed.SYMBOLS;
        float slotHeight = Math.min(30, cell);
        for (int i = 0; i < RunSeed.SYMBOLS; i++) {
            RedButton slot = action(seed == null ? "?" : "", 1);
            slot.setRect(8 + i * (cell + 3), seedTitle.bottom() + 6, cell, slotHeight);
            if (seed != null) {
                Image icon = SeedIcons.entry(seed.symbol(i)).image();
                float size = slotHeight - 6;
                icon.scale.set(Math.min(size / icon.width, size / icon.height));
                icon.x = PixelScene.align(slot.left() + (cell - icon.width()) / 2);
                icon.y = PixelScene.align(slot.top() + (slotHeight - icon.height()) / 2);
                slot.add(icon);
            }
        }
        RedButton chooseSeed = action(Messages.get(RunSeed.class, "choose"), 1);
        chooseSeed.setRect(8, seedTitle.bottom() + slotHeight + 18, cardWidth - 16, 24);
        chooseSeed.setSize(chooseSeed.width(), Math.max(24, chooseSeed.reqHeight()));
        float seedBottom = chooseSeed.bottom();
        if (seed != null) {
            RedButton random = action(Messages.get(RunSeed.class, "random"), 3);
            random.setRect(8, seedBottom + 4, cardWidth - 16, 24);
            random.setSize(random.width(), Math.max(24, random.reqHeight()));
            seedBottom = random.bottom();
        }
        float challengeX = wide ? cardWidth + 8 : 0;
        float top = wide ? 0 : seedBottom + 16;
        ChallengeIcons icons = new ChallengeIcons(challenges, Math.min(22, (cardWidth - 16) / 5), 5, true) {
            { choices.addControl(hotArea); }

            @Override public void open() {
                select(2);
            }
        };
        icons.setPos(challengeX + (cardWidth - icons.width()) / 2, top + 8);
        choices.add(icons);
        RedButton chooseChallenges = action(RebornChallenges.title() + ": " + Integer.bitCount(challenges), 2);
        chooseChallenges.setRect(challengeX + 8, icons.bottom() + 6, cardWidth - 16, 24);
        chooseChallenges.setSize(chooseChallenges.width(), Math.max(24, chooseChallenges.reqHeight()));
        float bottom = Math.max(seedBottom, chooseChallenges.bottom()) + 8;
        panel(0, 0, cardWidth, wide ? bottom : seedBottom + 8, RebornChrome.Material.ARCANE);
        panel(challengeX, top, cardWidth, bottom - top, RebornChrome.Material.IRON);

        HighlightedText notice = new HighlightedText(7);
        notice.text(Messages.get(RunSeed.class, seed == null ? "automatic" : "custom_notice"), bodyWidth - 8, false);
        notice.setPos(4, bottom + 10);
        choices.add(notice);
        choices.setSize(bodyWidth, notice.bottom() + 4);

        RedButton start = new ActionButton(Messages.get(RunSeed.class, "start")) {
            @Override protected void onClick() {
                select(0);
            }
        };
        start.setSize(width, 26);
        layoutContent(width, heading, choices, 6, start);
    }

    private RedButton action(String label, int index) {
        RedButton button = new ActionButton(label) {
            { choices.addControl(hotArea); }

            @Override protected void onClick() {
                select(index);
            }
        };
        choices.add(button);
        return button;
    }

    private void panel(float x, float y, float width, float height, RebornChrome.Material material) {
        RebornChrome frame = new RebornChrome(Chrome.Type.BUTTON).material(material);
        frame.x = x;
        frame.y = y;
        frame.size(width, height);
        choices.addToBack(frame);
    }

    private void select(int index) {
        hide();
        onSelect(index);
    }

    protected void onSelect(int index) { }

    @Override protected ScrollPane createScrollPane(Component content) {
        return choices.scrollPane(content);
    }
}
