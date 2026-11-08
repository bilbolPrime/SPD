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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;

public class DailyCitySprite extends UndeadSprite {
    private final int kind;

    public DailyCitySprite() {
        this(0);
    }

    public DailyCitySprite(int kind) {
        this.kind = kind;
        if (kind == 1 || kind == 3) {
            texture(kind == 1 ? Assets.MONK : Assets.WARLOCK);
            TextureFilm frames = new TextureFilm(texture, kind == 1 ? 15 : 12, kind == 1 ? 14 : 15);
            idle = new Animation(kind == 1 ? 6 : 2, true);
            run = new Animation(15, true);
            attack = new Animation(12, false);
            die = new Animation(15, false);
            if (kind == 1) {
                idle.frames(frames, 1, 0, 1, 2);
                run.frames(frames, 11, 12, 13, 14, 15, 16);
                attack.frames(frames, 3, 4, 3, 4);
                die.frames(frames, 1, 7, 8, 8, 9, 10);
            } else {
                idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);
                run.frames(frames, 0, 2, 3, 4);
                attack.frames(frames, 0, 5, 6);
                die.frames(frames, 0, 7, 8, 8, 9, 10);
            }
            zap = attack.clone();
            play(idle);
        }
        guardColor();
    }

    @Override public void resetColor() {
        float opacity = am;
        super.resetColor();
        guardColor();
        if (ch != null) am = opacity;
    }

    private void guardColor() {
        switch (kind) {
            case 0: hardlight(1f, .8f, .45f); break;
            case 2: hardlight(.7f, .85f, 1f); break;
            default: hardlight(1f, 1f, 1f);
        }
    }
}
