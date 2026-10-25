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

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.RectF;

import com.bilboldev.pixeldungeonskills.network.PartySession;

public class HeroSprite extends CharSprite {

	protected static final int FRAME_WIDTH	= 12;
    protected static final int FRAME_HEIGHT	= 15;

    protected static final int RUN_FRAMERATE	= 20;

    protected static TextureFilm tiers;

    protected Animation fly;
    protected Animation read;
    private boolean equippedWeaponAnimation;

	public HeroSprite() {
		this(Dungeon.hero);
	}

	public HeroSprite(Hero hero) {
		super();

		link( hero );

		texture( hero.heroClass.spritesheet() );
		updateArmor();

		idle();
	}

	public void updateArmor() {

		TextureFilm film = new TextureFilm( tiers(), ((Hero)ch).tier(), FRAME_WIDTH, FRAME_HEIGHT );

		if (idle == null) idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

		if (run == null) run = new Animation( RUN_FRAMERATE, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		if (die == null) die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12, 11 );

		if (attack == null) attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );

		if (zap == null) zap = attack.clone();
		else zap.frames(film, 13, 14, 15, 0);

		if (operate == null) operate = new Animation( 8, false );
		operate.frames( film, 16, 17, 16, 17 );

		if (fly == null) fly = new Animation( 1, true );
		fly.frames( film, 18 );

		if (read == null) read = new Animation( 20, false );
		read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );
		if (curAnim != null) frame(curAnim.frames[curFrame]);
	}

	@Override
	public void place( int p ) {
		super.place( p );
		if (!LocalCoop.active() || ch == Dungeon.hero) Camera.main.target = this;
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		Hero partner = LocalCoop.partner((Hero)ch);
		if (!LocalCoop.active() || (ch == Dungeon.hero && (PartySession.network() || GameScene.isPerspectiveView()
				|| partner != null && !partner.isAlive()))) {
			Camera.main.target = this;
		}
	}

	@Override
	protected Animation movementAnimation() {
		return ch != null && ch.flying ? fly : run;
	}

	@Override
	protected boolean playingMovement() {
		return curAnim == fly || super.playingMovement();
	}

	@Override
	public void jump( int from, int to, Callback callback ) {
		super.jump( from, to, callback );
		play( fly );
	}

	public void read() {
		animCallback = new Callback() {
			@Override
			public void call() {
				idle();
				ch.onOperateComplete();
			}
		};
		play( read );
	}

	@Override
	protected String animationKind(Animation animation) {
		return animation != null && animation == read ? "read" : super.animationKind(animation);
	}

	@Override
	protected Animation visualAnimation(String kind) {
		return "read".equals(kind) ? read : super.visualAnimation(kind);
	}

	@Override
	protected Animation jumpAnimation() { return fly; }

	@Override
	public void update() {
		try {
			sleeping = ((Hero) ch).restoreHealth;
		}
		catch (Exception e){

		}

		super.update();
	}

	public boolean sprint( boolean on ) {
		run.delay = on ? 0.625f / RUN_FRAMERATE : 1f / RUN_FRAMERATE;
		return on;
	}

    public boolean partyTint() {
        return PartySession.network()
                && ch != PartySession.localHero();
    }

    @Override
    public void draw() {
        if (!partyTint()) {
            super.draw();
            return;
        }
        float red = rm, green = gm;
        rm *= 0.72f;
        gm *= 0.86f;
        try {
            super.draw();
        } finally {
            rm = red;
            gm = green;
        }
    }

    @Override
    public void play(Animation anim, boolean force) {
        super.play(anim, force);

        equippedWeaponAnimation = anim != null && anim == attack;
    }

    @Override
    public void zap(int cell, Item item) {
        zap(cell);
        equippedWeaponAnimation = item != null && item == ((Hero)ch).belongings.weapon;
    }

    @Override
    public void attack(int cell, Callback callback, Item item) {
        attack(cell, callback);
        equippedWeaponAnimation = item != null && item == ((Hero)ch).belongings.weapon;
    }

    public boolean hideHeldWeapon() {
        return curAnim != null && (curAnim == read || curAnim == operate
                || (curAnim == zap || curAnim == attack) && !equippedWeaponAnimation);
    }

    public float attackAnimationProgress() {
        if (!equippedWeaponAnimation || (curAnim != attack && curAnim != zap) || finished
                || curAnim == null || curAnim.delay <= 0 || curAnim.frames.length == 0) return -1;
        return Math.min(1, (curFrame + frameTimer / curAnim.delay) / curAnim.frames.length);
    }

	public static TextureFilm tiers() {
		if (tiers == null) {
			SmartTexture texture = TextureCache.get( Assets.ROGUE );
			tiers = new TextureFilm( texture, texture.width, FRAME_HEIGHT );
		}

		return tiers;
	}

	public static Image avatar( HeroClass cl, int armorTier ) {

		RectF patch = tiers().get( armorTier );
		Image avatar = new Image( cl.spritesheet() );
		RectF frame = avatar.texture.uvRect( 1, 0, FRAME_WIDTH, FRAME_HEIGHT );
		frame.shift( patch.left, patch.top );
		avatar.frame( frame );

		return avatar;
	}
}
