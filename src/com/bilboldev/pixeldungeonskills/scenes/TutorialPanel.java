package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

final class TutorialPanel extends TitleHubPanel {
    TutorialPanel(TitleScene host) {
        super(host, Messages.get("tutorial.menu"));
        cardW = portrait ? screenW - 24 : Math.min(270, screenW * .43f);
        HighlightedText explanation = new HighlightedText(9);
        explanation.setColor(0xDDD4C5, 0xECD095);
        explanation.text(Messages.get("tutorial.preview"), (int)cardW - 28, false);
        cardY = portrait ? Math.max(150, screenH * .52f) : Math.max(86, 48 + target);
        cardH = Math.min(screenH - cardY - 24, explanation.height() + target + 40);
        if (!portrait) cardY += (screenH - cardY - cardH - 24) / 2;
        card();

        ScrollPane scroll = new ScrollPane(explanation);
        add(scroll);
        scroll.setRect(cardX + 12, cardY + 14, cardW - 24, cardH - target - 36);

        button(this, Messages.get("tutorial.start"), true, () -> {
            TitleScene.leavingForRun();
            TutorialRun.start();
        }).setRect(cardX + 12, cardY + cardH - target - 12, cardW - 24, target);
    }
}
