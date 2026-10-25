package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.RedButton;

public class WndGauntletVictory extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.EMBER;
    }

    private static final int WIDTH = 128;

    public WndGauntletVictory() {
        int width = Presentation.reborn() ? boundedWidth(WIDTH) : WIDTH;
        Image rat = new Image(Assets.RAT);
        rat.frame(new TextureFilm(rat.texture, 16, 15).get(14));
        rat.scale.set(2);
        rat.x = (width - rat.width()) / 2f;

        BitmapTextMultiline message = PixelScene.createMultiline(Messages.get("gauntlet.victory_message"), 6);
        message.maxWidth = width;
        message.measure();
        message.y = rat.height() + 6;

        RedButton close = new RedButton(Messages.get("gauntlet.title_screen")) {
            @Override
            protected void onClick() {
                hide();
                GauntletRun.leave();
            }
        };
        if (Presentation.reborn()) {
            Component heading = new Component();
            heading.add(rat);
            heading.setSize(width, rat.height());
            message.maxWidth = textWidth(width);
            message.measure();
            close.setSize(width, 20);
            layoutContent(width, heading, textContent(message), 8, close);
        } else {
            add(rat);
            add(message);
            close.setRect(0, message.y + message.height() + 8, width, 20);
            add(close);
            resize(width, (int)Math.ceil(close.bottom()));
        }
    }

    @Override
    public void onBackPressed() {

    }
}
