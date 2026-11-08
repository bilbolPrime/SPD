package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;

public class WndDailyResult extends WndScrollable {
    private final RedButton leave;
    private float readingTime;

    public WndDailyResult(String result) {
        int width = boundedWidth(180);
        Component title = new IconTitle(WndDaily.image(DailyRun.day()), DailyRun.title(DailyRun.day()));
        title.setRect(0, 0, width, 0);
        HighlightedText text = new HighlightedText(8);
        text.text(result, textWidth(width), false);
        leave = new ActionButton(Messages.get("daily.return")) {
            @Override protected void onClick() {
                if (readingTime >= .8f) Game.switchScene(TitleScene.class);
            }
        };
        leave.setSize(width, 24);
        leave.enable(false);
        layoutContent(width, title, text, 6, leave);
    }

    @Override public void update() {
        super.update();
        readingTime += Game.elapsed;
        leave.enable(readingTime >= .8f);
    }

    @Override public void onBackPressed() { }
    @Override public void onMenuPressed() { }
}
