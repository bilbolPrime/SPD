package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;
import com.bilboldev.pixeldungeonskills.windows.WndDaily;
import com.bilboldev.utils.PointF;
import java.util.ArrayList;

final class PlayPanel extends TitleHubPanel {
    private final ArrayList<TitleMenuButton> modes = new ArrayList<>();
    private final TitleMenuButton daily;
    private long date = DailyRun.today();

    PlayPanel(TitleScene host) {
        super(host, msg("play"));
        mode(msg("dungeon"), null, TitleScene.Page.HEROES);
        mode(Messages.get("tutorial.menu"), new Image(Assets.TUTORIAL), TitleScene.Page.TUTORIAL);
        daily = mode(Messages.get("daily.menu"), WndDaily.image(date), TitleScene.Page.DAILY);
        mode(Messages.get("journey.title"), new Image(Assets.JOURNEY), TitleScene.Page.JOURNEY);
        mode(Messages.get("gauntlet.title"), new RatKingSprite(), TitleScene.Page.GAUNTLET);

        cardW = portrait ? screenW - 24 : Math.min(270, screenW * .43f);
        cardY = portrait ? Math.max(110, screenH * .44f) : Math.max(86, 48 + target);
        float gap = 6;
        float height = Math.min(Math.max(34, target), (screenH - cardY - 30 - 4 * gap) / 5);
        for (int i = 0; i < modes.size(); i++)
            modes.get(i).setRect(cardX, PixelScene.align(cardY + i * (height + gap)), cardW, height);
    }

    private TitleMenuButton mode(String label, Image icon, TitleScene.Page page) {
        TitleMenuButton button = new TitleMenuButton(label, 0, page == TitleScene.Page.HEROES) {
            @Override protected void onClick() { host.navigate(page); }
        };
        if (icon != null) button.icon(icon);
        add(button);
        modes.add(button);
        return button;
    }

    @Override void pointerMoved(PointF point) {
        super.pointerMoved(point);
        for (TitleMenuButton button : modes)
            button.hover(point != null && button.isActive() && button.isVisible() && button.inside(point.x, point.y));
    }

    @Override public void update() {
        super.update();
        if (date != DailyRun.today()) {
            date = DailyRun.today();
            daily.icon(WndDaily.image(date));
            daily.setRect(daily.left(), daily.top(), daily.width(), daily.height());
        }
    }
}
