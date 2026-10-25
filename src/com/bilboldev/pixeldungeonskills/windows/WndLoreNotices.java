package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;

public final class WndLoreNotices extends WndLibrary {
    public WndLoreNotices() {
        int width = boundedWidth(PixelDungeon.landscape() ? 260 : 160);
        IconTitle heading = new IconTitle(Icons.LORE.get(),
                LibraryCatalog.text("new_discoveries"));
        heading.setRect(0, 0, width, 0);
        Component content = new Component();
        float y = 0;
        for (Lore.Notice notice : Lore.takeNotices()) {
            Image image = notice.image();
            image.scale.set(Math.min(2, 24 / Math.max(image.width, image.height)));
            image.x = (28 - image.width()) / 2;
            image.y = y;
            content.add(image);
            HighlightedText title = new HighlightedText(8);
            title.text(notice.title, width - 36, false);
            title.setPos(32, y);
            content.add(title);
            HighlightedText detail = new HighlightedText(7);
            detail.setColor(0xBCA98B, 0xECD095);
            detail.text(notice.detail, width - 36, false);
            detail.setPos(32, title.bottom() + 2);
            content.add(detail);
            y = Math.max(y + 28, detail.bottom()) + 8;
        }
        content.setSize(width, Math.max(1, y - 8));
        layoutLibrary(width, heading, content, 8);
    }
}
