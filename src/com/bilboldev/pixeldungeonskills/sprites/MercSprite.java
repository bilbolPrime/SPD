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
package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.effects.ArcherMaidenHalo;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Dart;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;

public class MercSprite extends MobSprite {

	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;

    private HiredMerc.MERC_TYPES type = HiredMerc.MERC_TYPES.Brute;

    public boolean hasHalo = false;

    public ArcherMaidenHalo halo = null;

	public MercSprite() {
		super();
	}
	
	public void updateArmor( HiredMerc.MERC_TYPES type) {

        this.type = type;



            switch (type) {
                case Brute:
                     texture(Assets.WARRIOR);
                    break;
                case Wizard:
                    texture(Assets.MAGE);
                    break;
                case Thief:
                    texture(Assets.ROGUE);
                    break;
                case Archer:  texture(Assets.HUNTRESS);
                    break;
                case ArcherMaiden:
                    texture(Assets.ARCHER_MAIDEN);
                    break;
            }


		TextureFilm film = new TextureFilm( HeroSprite.tiers(), type != HiredMerc.MERC_TYPES.ArcherMaiden ? 6 : 0, FRAME_WIDTH, FRAME_HEIGHT );
		
		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( 20, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		
		attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );

        zap = attack.clone();

        die = new Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );


        operate = new Animation( 8, false );
        operate.frames( film, 16, 17, 16, 17 );

        idle();
	}

    public void updateArmor( ) {

        if(false && hasHalo == false)
        {
            hasHalo = true;
            add(State.ARCHERMAIDEN);
            GameScene.effect(halo = new ArcherMaidenHalo(this));
        }


        switch (type) {
            case Brute:
                texture(Assets.WARRIOR);
                break;
            case Wizard:
                texture(Assets.MAGE);
                break;
            case Thief:
                texture(Assets.ROGUE);
                break;
        }


        TextureFilm film = new TextureFilm( HeroSprite.tiers(), type != HiredMerc.MERC_TYPES.ArcherMaiden ? ((HiredMerc)ch).getArmorTier() : 0, FRAME_WIDTH, FRAME_HEIGHT );

        idle = new Animation( 1, true );
        idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new Animation( 20, true );
        run.frames( film, 2, 3, 4, 5, 6, 7 );

        die = new Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );

        zap = attack.clone();

        attack = new Animation( 15, false );
        attack.frames( film, 13, 14, 15, 0 );

        operate = new Animation( 8, false );
        operate.frames( film, 16, 17, 16, 17 );

        idle();
    }

    private int cellToAttack;

    @Override
    public void attack( int cell ) {
        if(type != HiredMerc.MERC_TYPES.Archer && type != HiredMerc.MERC_TYPES.ArcherMaiden)
        {
            super.attack(cell);
            return;
        }
        if (!Level.adjacent(cell, ch.pos)) {

            cellToAttack = cell;
            turnTo( ch.pos , cell );
            play( zap );

        } else {

            super.attack( cell );

        }
    }

    @Override
    public void onComplete( Animation anim ) {
        if(type != HiredMerc.MERC_TYPES.Archer && type != HiredMerc.MERC_TYPES.ArcherMaiden)
        {
            super.onComplete(anim);
            return;
        }

        if (anim == zap) {
            idle();

            ((MissileSprite)parent.recycle( MissileSprite.class )).
                    reset( ch.pos, cellToAttack, new Arrow(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    } );
        } else {
            super.onComplete( anim );
        }
    }
}
