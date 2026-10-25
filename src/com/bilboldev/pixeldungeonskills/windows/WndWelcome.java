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

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.SteamPageButton;

public class WndWelcome extends WndScrollable {
    public WndWelcome(String message) {
        int width = boundedWidth(176);
        IconTitle title = new IconTitle(Icons.SKILLS.get(), Messages.get(WndWelcome.class, "title"));
        title.setRect(0, 0, width, 0);
        String text = message + "\n\n" + Messages.get(WndWelcome.class, "classic");
        if (Game.isFreeDesktop()) text += "\n\n" + Messages.get(WndWelcome.class, "free_desktop");
        RenderedTextMultiline body = PixelScene.renderMultiline(text, 8);
        body.maxWidth(textWidth(width));

        if (Game.isFreeDesktop()) {
            SteamPageButton steam = new SteamPageButton();
            steam.setSize(width, 20);
            layoutContent(width, title, body, 6, steam);
        } else {
            layoutContent(width, title, body, 6);
        }
    }

    @Override
    public void hide() {
        VersionNewsInfo.closeWelcome();
        super.hide();
    }
}
