/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;

public class WndWelcome extends WndScrollable {
    public WndWelcome(String message) {
        int width = boundedWidth(176);
        IconTitle title = new IconTitle(Icons.SKILLS.get(), Messages.get(WndWelcome.class, "title"));
        title.setRect(0, 0, width, 0);
        RenderedTextMultiline body = PixelScene.renderMultiline(message + "\n\n"
                + Messages.get(WndWelcome.class, "classic"), 8);
        body.maxWidth(textWidth(width));

        layoutContent(width, title, body, 6);
    }

    @Override
    public void hide() {
        VersionNewsInfo.closeWelcome();
        super.hide();
    }
}
