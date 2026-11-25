package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.windows.WndJourneySlots;

final class JourneyPanel extends TitleHubPanel {
    JourneyPanel(TitleScene host) {
        super(host, Messages.get("journey.title"));
        cardW = portrait ? screenW - 24 : Math.min(270, screenW * .43f);
        HighlightedText explanation = new HighlightedText(9);
        explanation.setColor(0xDDD4C5, 0xECD095);
        String description = Messages.get("journey.intro");
        if (!Badges.isUnlocked(Badges.Badge.JOURNEY_COMPLETE)) {
            description += "\n\n" + Messages.get("journey.badge_hint",
                    (Object)Messages.get("badges$badge.journey_complete.title"));
        }
        explanation.text(description, (int)cardW - 28, false);
        cardY = portrait ? Math.max(150, screenH * .48f) : Math.max(86, 48 + target);
        cardH = Math.min(screenH - cardY - 24, explanation.height() + target + 40);
        if (!portrait) cardY += (screenH - cardY - cardH - 24) / 2;
        card();

        ScrollPane scroll = new ScrollPane(explanation);
        add(scroll);
        scroll.setRect(cardX + 12, cardY + 14, cardW - 24, cardH - target - 36);
        button(this, Messages.get("runseed.start"), true, () -> host.add(new WndJourneySlots()))
                .setRect(cardX + 12, cardY + cardH - target - 12, cardW - 24, target);
    }
}
