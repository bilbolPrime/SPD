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

import com.bilboldev.pixeldungeonskills.effects.CosmeticPet;
import com.bilboldev.pixeldungeonskills.effects.CosmeticAccessory;
import com.bilboldev.pixeldungeonskills.CosmeticAccessories;
import com.bilboldev.pixeldungeonskills.HeroSkins;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.actors.buffs.Healing;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRecovery;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.effects.SupporterAura;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.RectF;

import com.bilboldev.pixeldungeonskills.network.PartySession;

public class HeroSprite extends CharSprite {
	private final int floor = Dungeon.levelKey();

	public boolean onCurrentLevel() {
		return floor == Dungeon.levelKey();
	}

	protected static final int FRAME_WIDTH	= 12;
    protected static final int FRAME_HEIGHT	= 15;

    protected static final int RUN_FRAMERATE	= 20;

    protected static TextureFilm tiers;

    protected Animation fly;
    protected Animation read;
    private boolean equippedWeaponAnimation;
    private SupporterAura supporterAura;
    private CosmeticPet cosmeticPet;
    private CosmeticAccessory cosmeticAccessory;
    public CosmeticPet petImage() { return cosmeticPet == null ? null : cosmeticPet.image(); }
    public CosmeticAccessory accessoryImage() {
        Hero hero = (Hero)ch;
        int style = hero.tutorial ? 0 : CosmeticAccessories.selection(hero);
        if (cosmeticAccessory == null && style > 0) cosmeticAccessory = new CosmeticAccessory(this, hero.heroClass);
        if (cosmeticAccessory == null) return null;
        cosmeticAccessory.heroClass(hero.heroClass);
        cosmeticAccessory.style(style);
        cosmeticAccessory.pose();
        return cosmeticAccessory.visible ? cosmeticAccessory : null;
    }
    private Emitter healing, manaRecovery;
    private static final Emitter.Factory MANA_PARTICLES = new Emitter.Factory() {
        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            Speck speck = (Speck)emitter.recycle(Speck.class);
            speck.reset(index, x, y, Speck.HEALING);
            speck.color(0x245AA8);
        }
    };

	public HeroSprite() {
		this(Dungeon.hero);
	}

	public HeroSprite(Hero hero) {
		super();

		link( hero );

		texture( hero.spritesheet() );
		updateArmor();

		if (hero.isAlive()) {
			idle();
		} else {
			play(die);
			curFrame = die.frames.length - 1;
			frame(die.frames[curFrame]);
			finished = true;
		}
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
		if (!LocalCoop.active() || ch == GameScene.cameraHero()) Camera.main.target = this;
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		Hero partner = LocalCoop.partner((Hero)ch);
		if (!LocalCoop.active() || (ch == GameScene.cameraHero() && (PartySession.network() || GameScene.isPerspectiveView()
				|| LocalCoop.following(partner) || partner != null && !partner.isAlive()))) {
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

        accessoryImage();
        if (cosmeticAccessory != null) cosmeticAccessory.update();

        if (Game.scene() instanceof GameScene && parent != null) {
            healing = recoveryEffect(healing, ch.isAlive() && ch.buff(Healing.class) != null, Speck.factory(Speck.HEALING));
            manaRecovery = recoveryEffect(manaRecovery, ch.isAlive() && ch.buff(ManaRecovery.class) != null, MANA_PARTICLES);
            if (cosmeticPet == null && PartySession.pet((Hero)ch) > 0) {
                cosmeticPet = new CosmeticPet(this);
                parent.add(cosmeticPet);
            }
            int style = PartySession.aura((Hero)ch);
            if (style > 0 && supporterAura == null) {
                supporterAura = new SupporterAura(this);
                GameScene.effect(supporterAura);
            }
            if (supporterAura != null) supporterAura.style(style);
        }
	}

    private Emitter recoveryEffect(Emitter effect, boolean recovering, Emitter.Factory particles) {
        if (!recovering) {
            if (effect != null) effect.killAndErase();
            return null;
        }
        if (effect == null) {
            effect = new Emitter();
            effect.autoKill = false;
            effect.pos(this);
            effect.pour(particles, 0.4f);
            GameScene.effect(effect);
        }
        effect.visible = effect.on = visible;
        return effect;
    }

    @Override
    public void kill() {
        healing = recoveryEffect(healing, false, null);
        manaRecovery = recoveryEffect(manaRecovery, false, null);
        super.kill();
    }

    @Override
    public void destroy() {
        healing = recoveryEffect(healing, false, null);
        manaRecovery = recoveryEffect(manaRecovery, false, null);
        if (cosmeticAccessory != null) {
            cosmeticAccessory.destroy();
            cosmeticAccessory = null;
        }
        super.destroy();
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
        float red = rm, green = gm;
        if (partyTint()) {
            rm *= 0.72f;
            gm *= 0.86f;
        }
        try {
            super.draw();
            CosmeticAccessory accessory = accessoryImage();
            if (accessory != null) accessory.draw();
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

	public static Image avatar(Hero hero, int armorTier) {
        return avatar(hero.heroClass, armorTier, hero.skin);
    }

    public static Image avatar(HeroClass cl, int armorTier) {
        return avatar(cl, armorTier, 0);
    }

    public static Image avatar(HeroClass cl, int armorTier, int skin) {

		RectF patch = tiers().get( armorTier );
		Image avatar = new Image(HeroSkins.sheet(cl, skin));
		RectF frame = avatar.texture.uvRect( 1, 0, FRAME_WIDTH, FRAME_HEIGHT );
		frame.shift( patch.left, patch.top );
		avatar.frame( frame );

		return avatar;
	}
}
