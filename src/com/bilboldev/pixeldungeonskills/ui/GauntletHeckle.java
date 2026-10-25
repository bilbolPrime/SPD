package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatKing;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class GauntletHeckle extends Component {
    private NinePatch background;
    private Image portrait;
    private RenderedTextMultiline speaker, message;

    @Override
    protected void createChildren() {
        background = Chrome.get(Chrome.Type.TOAST_TR);
        add(background);
        portrait = Icons.RAT_KING.get();
        add(portrait);
        speaker = PixelScene.renderMultiline("", 6);
        speaker.hardlight(Window.titleColor());
        add(speaker);
        message = PixelScene.renderMultiline("", 7);
        add(message);
        visible = false;
    }

    public void say(String text) {
        speaker.text(Messages.get(RatKing.class, "name"), false);
        message.text(text, false);
        layout();
        visible = true;
    }

    @Override
    protected void layout() {
        float textX = x + 5 + portrait.width() + 4;
        int textWidth = Math.max(1, (int)(width - (textX - x) - 5));
        speaker.maxWidth(textWidth);
        message.maxWidth(textWidth);
        speaker.setPos(textX, y + 5);
        message.setPos(textX, speaker.bottom() + 2);
        height = Math.max(portrait.height(), speaker.height() + 2 + message.height()) + 10;
        portrait.x = x + 5;
        portrait.y = y + (height - portrait.height()) / 2f;
        background.x = x;
        background.y = y;
        background.size(width, height);
    }
}
