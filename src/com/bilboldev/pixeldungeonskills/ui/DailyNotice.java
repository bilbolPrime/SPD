package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.windows.WndDaily;

public class DailyNotice extends Component {
    private final NinePatch background;
    private final Image icon;
    private final RenderedTextMultiline message;

    public DailyNotice() {
        add(background = Chrome.get(Chrome.Type.TOAST_TR));
        add(icon = WndDaily.image(DailyRun.day()));
        icon.scale.set(.6f);
        add(message = PixelScene.renderMultiline("", 7));
        visible = false;
    }

    public void text(String text) {
        message.text(text, false);
        layout();
    }

    public void opacity(float value) {
        background.alpha(value);
        icon.alpha(value);
        message.alpha(value);
    }

    @Override protected void layout() {
        message.maxWidth(Math.max(1, (int)(width - icon.width() - 14)));
        height = Math.max(icon.height(), message.height()) + 10;
        message.setPos(x + icon.width() + 9, y + 5);
        icon.x = x + 5;
        icon.y = y + (height - icon.height()) / 2;
        background.x = x;
        background.y = y;
        background.size(width, height);
    }
}
