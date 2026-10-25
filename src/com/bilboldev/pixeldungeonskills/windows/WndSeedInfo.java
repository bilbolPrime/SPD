package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.SeedDisplay;

public class WndSeedInfo extends WndScrollable {
    public WndSeedInfo(RunSeed seed) {
        int width = boundedWidth(180);
        Component heading = new Component();
        HighlightedText title = new HighlightedText(9);
        title.text(Messages.get(RunSeed.class, seed != null && seed.custom ? "custom" : "title"), width, false);
        title.setColor(titleColor(), titleColor());
        heading.add(title);
        SeedDisplay icons = new SeedDisplay(seed, RunSeed.SYMBOLS, false);
        icons.useScrollPaneInput();
        icons.setRect(0, title.bottom() + 6, width, 28);
        heading.add(icons);
        heading.setSize(width, icons.bottom());

        HighlightedText text = new HighlightedText(8);
        String description = seed == null ? Messages.get(RunSeed.class, "unknown") : seed.text();
        if (seed != null && seed.custom) description += "\n\n" + Messages.get(RunSeed.class, "custom_notice");
        text.text(description, textWidth(width), false);
        Component content = new Component();
        content.add(text);
        content.setSize(textWidth(width), text.height());
        if (seed == null) {
            layoutContent(width, heading, content, 6);
        } else {
            RedButton copy = new RedButton(Messages.get(RunSeed.class, "copy")) {
                @Override protected void onClick() {
                    try {
                        PartyPrompts.copy(seed.text());
                        text(Messages.get(RunSeed.class, "copied"));
                    } catch (RuntimeException unavailable) {
                        Game.scene().add(new WndMessage(Messages.get(RunSeed.class, "clipboard_unavailable")));
                    }
                }
            };
            copy.setSize(width, 20);
            layoutContent(width, heading, content, 6, copy);
        }
    }
}
