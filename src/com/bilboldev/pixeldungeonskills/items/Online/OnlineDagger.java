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
package com.bilboldev.pixeldungeonskills.items.Online;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Dagger;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class OnlineDagger extends Dagger {

    @Override
    public void doDrop( Hero hero ) {
        if (!isEquipped( hero ) || doUnequip( hero, false, false )) {
            super.doDrop( hero );
        }
    }

    @Override
    public void doThrow( Hero hero ) {

    }

    @Override
    public boolean collect() {
        return false;
    }

    @Override
    public boolean doPickUp( Hero hero ) {
            return false;
    }
}
