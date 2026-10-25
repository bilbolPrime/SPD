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

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class WndGauntletDie extends Window {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.EMBER;
    }

    private static final int WIDTH = 120;
    public WndGauntletDie() {
        IconTitle titlebar = new IconTitle();
        titlebar.icon(Icons.RAT_KING.get());
        titlebar.label("BAHAHAHAHA!!!");
        titlebar.setRect(0, 0, WIDTH, 0);
        add(titlebar);

        BitmapTextMultiline message = PixelScene.createMultiline(
                Messages.get("custom.ratking.gauntlet_window", GauntletScene.level), 6);
        message.maxWidth = WIDTH;
        message.measure();
        message.y = titlebar.bottom() + 2;
        add(message);
        RedButton close = new RedButton("Just go away...") {
            @Override
            protected void onClick() {
                onBackPressed();
            }
        };
        close.setRect(0, message.y + message.height() + 2, WIDTH, 20);
        add(close);
        resize(WIDTH, (int)close.bottom());
    }

    @Override
    public void onBackPressed() {
        hide();
        GauntletRun.leave();
    }
}
