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

import com.bilboldev.pixeldungeonskills.network.PartyEffects;

import com.bilboldev.glwrap.Matrix;
import com.bilboldev.glwrap.Vertexbuffer;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.noosa.Visual;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.tweeners.PosTweener;
import com.bilboldev.noosa.tweeners.Tweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.ArcherMaidenHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampBlackHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampRedHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampWhiteHalo;
import com.bilboldev.pixeldungeonskills.effects.ChampYellowHalo;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.effects.FloatingText;
import com.bilboldev.pixeldungeonskills.effects.IceBlock;
import com.bilboldev.pixeldungeonskills.effects.RebornAura;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.Splash;
import com.bilboldev.pixeldungeonskills.effects.TorchHalo;
import com.bilboldev.pixeldungeonskills.effects.particles.FlameParticle;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfInvisibility;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.RectF;
import com.bilboldev.utils.Random;

import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
public class CharSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	public static final int DEFAULT		= 0xFFFFFF;
	public static final int POSITIVE	= 0x00FF00;
	public static final int NEGATIVE	= 0xFF0000;
	public static final int WARNING		= 0xFF8800;
	public static final int NEUTRAL		= 0xFFFF00;

	private static final float MOVE_INTERVAL	= 0.1f;
	private static final float FLASH_INTERVAL	= 0.05f;

	public enum State {
		BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED, CHAMPRED, CHAMPBLACK, CHAMPWHITE, CHAMPYELLOW, ARCHERMAIDEN, CHAMPREBORN
	}

	protected Animation idle;
	protected Animation run;
	protected Animation attack;
	protected Animation operate;
	protected Animation zap;
	protected Animation die;

	public RectF[] idleFrames() {
		return idle == null ? null : idle.frames;
	}

	public Animation deathAnimation() {
		return die;
	}

	public static final class VisualAnimation {
		private final String kind;
		private final int cell, frame;
		private final float timer;

		private VisualAnimation(String kind, int cell, int frame, float timer) {
			this.kind = kind;
			this.cell = cell;
			this.frame = frame;
			this.timer = timer;
		}
	}

	public VisualAnimation captureVisualAnimation() {
		String kind = animationKind(curAnim);
		return kind == null || finished ? null : new VisualAnimation(kind, facingCell, curFrame, frameTimer);
	}

	public void restoreVisualAnimation(VisualAnimation state) {
		if (state == null) return;
		playVisualAnimation(state.kind, state.cell);
		curFrame = Math.min(state.frame, curAnim.frames.length - 1);
		frameTimer = state.timer;
		frame(curAnim.frames[curFrame]);
	}

	public static final class VisualJump {
		final int from, to;
		float elapsed;
		VisualJump(int from, int to, float elapsed) {
			this.from = from;
			this.to = to;
			this.elapsed = elapsed;
		}
		float duration() { return Math.max(.1f, Level.distance(from, to) * .1f); }
		float progress() { return Math.min(1, elapsed / duration()); }
	}

	private VisualJump visualJump;
	private String pendingVisualAnimation;
	private int pendingVisualCell;

	public VisualJump captureVisualJump() {
		return visualJump == null ? null : new VisualJump(visualJump.from, visualJump.to, visualJump.elapsed);
	}

	public void jumpVisual(int from, int to) {
		if (!PartySession.client() || ch == null) return;
		stopVisualMotion();
		visualJump = new VisualJump(from, to, 0);
		listener = completed -> { };
		play(jumpAnimation());
		turnTo(from, to);
		updateVisualJump(0);
	}

	public boolean restoreVisualJump(VisualJump jump) {
		if (jump == null || ch.pos != jump.from && ch.pos != jump.to) return false;
		jumpVisual(jump.from, jump.to);
		visualJump.elapsed = jump.elapsed;
		updateVisualJump(0);
		return true;
	}

	protected Animation jumpAnimation() { return run; }

	public float jumpHeight() {
		if (visualJump == null) return 0;
		float progress = visualJump.progress();
		return Level.distance(visualJump.from, visualJump.to) * progress * (1 - progress);
	}

	private void updateVisualJump(float elapsed) {
		if (visualJump == null) return;
		visualJump.elapsed += elapsed;
		float progress = visualJump.progress();
		point(PointF.inter(worldToCamera(visualJump.from), worldToCamera(visualJump.to), progress)
				.offset(0, -jumpHeight() * DungeonTilemap.SIZE));
		if (progress == 1) {
			if (ch.pos == visualJump.to) visualJump = null;
			idle();
		}
	}

	protected Callback animCallback;

	protected Tweener motion;
	private final java.util.ArrayDeque<int[]> visualMoves = new java.util.ArrayDeque<>();
	private int visualCell = -1;
	private int visualSequence = -1;
	private int visualStep;
	private float movementPause;

	protected Animation movementAnimation() {
		return run;
	}

	protected boolean playingMovement() {
		return curAnim == run;
	}

	public float movementOffsetX() {
		if (PartySession.client() && visualCell >= 0 && ch != null)
			return (x - worldToCamera(ch.pos).x) / DungeonTilemap.SIZE;
		if (visualJump != null && ch != null) return (x - worldToCamera(ch.pos).x) / DungeonTilemap.SIZE;
		return isMoving && motion instanceof PosTweener && motion.exists
				? (x - ((PosTweener)motion).end.x) / DungeonTilemap.SIZE : 0;
	}

	public float movementOffsetY() {
		if (PartySession.client() && visualCell >= 0 && ch != null)
			return (y - worldToCamera(ch.pos).y) / DungeonTilemap.SIZE + jumpHeight();
		if (visualJump != null && ch != null)
			return (y - worldToCamera(ch.pos).y) / DungeonTilemap.SIZE + jumpHeight();
		return isMoving && motion instanceof PosTweener && motion.exists
				? (y - ((PosTweener)motion).end.y) / DungeonTilemap.SIZE : 0;
	}

	protected Emitter burning;
	protected Emitter levitation;

	protected IceBlock iceBlock;
	protected TorchHalo halo;

    public ChampRedHalo champRedHalo;
    public ChampYellowHalo champYellowHalo;
    public ChampBlackHalo champBlackHalo;
    public ChampWhiteHalo champWhiteHalo;
    public RebornAura rebornAura;
    public ArcherMaidenHalo archerMaidenHalo;

	protected EmoIcon emo;

	private Tweener jumpTweener;
	private Callback jumpCallback;

	private float flashTime = 0;
	private int facingCell;

	protected boolean sleeping = false;

	public Char ch;

	public boolean isMoving = false;

	protected float perspectiveRaise    = 6 / 16f;

	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1.2f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.25f;

	public CharSprite() {
		super();
		listener = this;
	}

	public void link( Char ch ) {
		this.ch = ch;
		ch.sprite = this;

		place( ch.pos );
		turnTo( ch.pos, Random.Int( Level.LENGTH ) );

		renderShadow = Difficulties.is3d;
		ch.updateSpriteState();
	}

	public PointF worldToCamera( int cell ) {

		final int csize = DungeonTilemap.SIZE;

		if(!Difficulties.is3d)
			return new PointF(
				((cell % Level.WIDTH) + 0.5f) * csize - width * 0.5f,
				((cell / Level.WIDTH) + 1.0f) * csize - height
			);

		return new PointF(
				PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width * 0.5f),
				PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height - csize * perspectiveRaise)
		);
	}

	public float groundY() {
		float top = y;

		if (jumpTweener != null && jumpTweener.exists) {
			JumpTweener jump = (JumpTweener)jumpTweener;
			float progress = jump.interval > 0 ? Math.min(1, jump.elapsed / jump.interval) : 1;
			top = jump.start.y + (jump.end.y - jump.start.y) * progress;
		}
		return top + height + (Difficulties.is3d ? DungeonTilemap.SIZE * perspectiveRaise : 0);
	}

    public void NotAChamp()
    {
        if (rebornAura != null)
            rebornAura.killAndErase();
        if(champRedHalo != null)
            champRedHalo.putOut();
        if(champYellowHalo != null)
            champYellowHalo.putOut();
        if(champBlackHalo != null)
            champBlackHalo.putOut();
        if(champWhiteHalo != null)
            champWhiteHalo.putOut();
    }

	public void place( int cell ) {
		point( worldToCamera( cell ) );
	}

	public void showStatus( int color, String text, Object... args ) {
		if (visible) {
			text = Messages.maybeTranslate( text, args );
			PartyEffects.number(this, color, text);
			showStatusText(color, text);
		}
	}

	public void showStatus(int color, Message message) {
		if (visible) {
			PartyEffects.status(this, color, message);
			showStatusText(color, message.toString());
		}
	}

	private void showStatusText(int color, String text) {
		if (ch != null) {
			FloatingText.show( x + width * 0.5f, y, this, text, color );
		} else {
			FloatingText.show( x + width * 0.5f, y, text, color );
		}
	}

	public void idle() {
		play( idle );
	}

	@Override
	public void play(Animation anim, boolean force) {
		boolean started = force || curAnim != anim || finished;
		super.play(anim, force);
		if (started && ch != null && PartySession.network() && !PartySession.client()) {
			String kind = animationKind(anim);
			if (anim != null && kind != null) PartySession.animation(ch.id(), kind, facingCell);
		}
	}

	public void playVisualAnimation(String kind, int cell) {
		if (!PartySession.client() || ch == null) return;
		Animation animation = visualAnimation(kind);
		if (animation == null) return;
		if (isMoving) {
			pendingVisualAnimation = kind;
			pendingVisualCell = cell;
			return;
		}
		turnTo(ch.pos, cell);
		listener = completed -> {
			if (completed != idle) {
				idle();
				listener = this;
			}
		};
		play(animation, true);
	}

	protected Animation visualAnimation(String kind) {
		return "attack".equals(kind) ? attack : "zap".equals(kind) ? zap : "operate".equals(kind) ? operate : null;
	}

	protected String animationKind(Animation animation) {
		if (animation == null) return null;
		return animation == attack ? "attack" : animation == zap ? "zap" : animation == operate ? "operate" : null;
	}

	public void move( int from, int to ) {
		play( movementAnimation() );
		PartySession.move(ch.id(), from, to);

		motion = new PosTweener( this, worldToCamera( to ), MOVE_INTERVAL );
		motion.listener = this;
		parent.add( motion );

		isMoving = true;

		turnTo( from , to );

		if (visible && Level.water[from] && !ch.flying) {
			GameScene.ripple( from );
		}

		ch.onMotionComplete();
	}

	public void moveVisual(int from, PointF previousPosition) {
		if (parent == null || ch == null) return;
		stopVisualMotion();
		point(previousPosition);
		visualCell = ch.pos;
		startVisualMove(from, ch.pos);
	}

	public void queueVisualMove(int from, int to, int sequence, int step) {
		if (parent == null || ch == null || !ch.isAlive() || step <= visualStep) return;
		visualJump = null;
		visualSequence = sequence;
		visualStep = step;
		visualCell = to;
		if (!visible) {
			stopVisualMotion();
			visualCell = to;
			point(worldToCamera(to));
		} else if (isMoving) {

			if (visualMoves.size() == 8) visualMoves.clear();
			visualMoves.addLast(new int[]{from, to});
		} else {
			startVisualMove(from, to);
		}
	}

	private void startVisualMove(int from, int to) {
		turnTo(from, to);
		listener = null;
		play(movementAnimation());
		isMoving = true;
		movementPause = 0;
		motion = new PosTweener(this, worldToCamera(to), MOVE_INTERVAL);
		motion.listener = completed -> {
			isMoving = false;
			motion = null;
			completed.killAndErase();
			if (!visualMoves.isEmpty()) {
				int[] next = visualMoves.removeFirst();
				startVisualMove(next[0], next[1]);
			} else if (pendingVisualAnimation != null) {
				String kind = pendingVisualAnimation;
				pendingVisualAnimation = null;
				playVisualAnimation(kind, pendingVisualCell);
			} else {
				movementPause = 0.05f;
			}
		};
		parent.add(motion);
	}

	public void updateVisualPosition(int previousCell) {
		if (ch == null || previousCell == ch.pos) return;
		if (visualJump != null && (ch.pos == visualJump.from || ch.pos == visualJump.to)) return;
		if (visible && Level.distance(previousCell, ch.pos) <= 2) {
			moveVisual(previousCell, new PointF(x, y));
		} else {
			stopVisualMotion();
			point(worldToCamera(ch.pos));
		}
	}

	public void updateVisualPosition(int previousCell, int sequence) {
		if (visualStep > 0 && (visualSequence >= sequence || ch.pos == visualCell)) return;
		if (visualCell >= 0) previousCell = visualCell;
		updateVisualPosition(previousCell);
	}

	public void dieVisual() {
		pendingVisualAnimation = null;
		stopVisualMotion();
		animCallback = null;
		listener = completed -> { };
		paused = false;
		die();
	}

	public void discardVisual() {
		pendingVisualAnimation = null;
		stopVisualMotion();
		animCallback = null;
		listener = null;
		remove(State.BURNING);
		remove(State.LEVITATING);
		remove(State.FROZEN);
		remove(State.ILLUMINATED);
		NotAChamp();
		killAndErase();
		destroy();
	}

	public void finishVisualPlayback() {
		pendingVisualAnimation = null;
		listener = this;
		stopVisualMotion();
		if (ch != null) place(ch.pos);
		idle();
	}

	private void stopVisualMotion() {
		visualJump = null;
		visualMoves.clear();
		visualCell = -1;
		movementPause = 0;
		if (motion != null) {
			motion.killAndErase();
			motion = null;
		}
		isMoving = false;
	}

	public void interruptMotion() {
		if (motion != null) {
			onComplete( motion );
		}
	}

	public void attack( int cell ) {
		turnTo( ch.pos, cell );
		play( attack );
	}

	public void attack( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		play( attack );
	}

    public void attack(int cell, Callback callback, Item item) {
        attack(cell, callback);
    }

	public void operate( int cell ) {
		turnTo( ch.pos, cell );
		play( operate );
	}

	public void zap( int cell ) {
		turnTo( ch.pos, cell );
		play( zap );
	}

    public void zap(int cell, Item item) {
        zap(cell);
    }

	public void turnTo( int from, int to ) {
		facingCell = to;
		int fx = from % Level.WIDTH;
		int tx = to % Level.WIDTH;
		if (tx > fx) {
			flipHorizontal = false;
		} else if (tx < fx) {
			flipHorizontal = true;
		}
	}

	public void jump( int from, int to, Callback callback ) {
		PartyEffects.jump(this, from, to);
		jumpCallback = callback;

		int distance = Level.distance( from, to );
		jumpTweener = new JumpTweener( this, worldToCamera( to ), distance * 4, distance * 0.1f );
		jumpTweener.listener = this;
		parent.add( jumpTweener );

		turnTo( from, to );
	}

	public void die() {
		sleeping = false;
		play( die );

		if (emo != null) {
			emo.killAndErase();
		}
	}

	public Emitter emitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( this );
		return emitter;
	}

	public Emitter centerEmitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( center() );
		emitter.renderAnchor( this );
		return emitter;
	}

	public Emitter bottomEmitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( x, y + height, width, 0 );
		emitter.renderAnchor( this );
		return emitter;
	}

	public void burst( final int color, int n ) {
		if (visible) {
			Splash.at( center(), color, n );
		}
	}

	public void bloodBurstA( PointF from, int damage ) {
		if (visible) {
			PointF c = center();
			int n = (int)Math.min( 9 * Math.sqrt( (double)damage / ch.HT ), 9 );
			Splash.at( c, PointF.angle( from, c ), 3.1415926f / 2, blood(), n );
		}
	}

	public int blood() {
		return 0xFFBB0000;
	}

	public void flash() {
		PartyEffects.actor(
				PartyEffects.FLASH, this);
		ra = ba = ga = 1f;
		flashTime = FLASH_INTERVAL;
	}

	public void add( State state ) {
		switch (state) {
		case BURNING:
			burning = emitter();
			burning.pour( FlameParticle.FACTORY, 0.06f );
			if (visible) {
				Sample.INSTANCE.play( Assets.SND_BURNING );
			}
			break;
		case LEVITATING:
			levitation = emitter();
			levitation.pour( Speck.factory( Speck.JET ), 0.02f );
			break;
		case INVISIBLE:
			PotionOfInvisibility.melt( ch );
			break;
		case PARALYSED:
			paused = true;
			break;
		case FROZEN:
			iceBlock = IceBlock.freeze( this );
			paused = true;
			break;
		case ILLUMINATED:
			GameScene.effect( halo = new TorchHalo( this ) );
			break;
        case CHAMPRED:
            GameScene.effect( champRedHalo = new ChampRedHalo( this ) );
            break;
        case CHAMPWHITE:
            GameScene.effect( champWhiteHalo = new ChampWhiteHalo( this ) );
            break;
        case CHAMPBLACK:
            GameScene.effect( champBlackHalo = new ChampBlackHalo( this ) );
            break;
        case CHAMPYELLOW:
            GameScene.effect( champYellowHalo = new ChampYellowHalo( this ) );
            break;
        case CHAMPREBORN:
            GameScene.effect( rebornAura = new RebornAura( this ) );
            break;
        case ARCHERMAIDEN:

                break;
		}
	}

	public void remove( State state ) {
		switch (state) {
		case BURNING:
			if (burning != null) {
				burning.on = false;
				burning = null;
			}
			break;
		case LEVITATING:
			if (levitation != null) {
				levitation.on = false;
				levitation = null;
			}
			break;
		case INVISIBLE:
			alpha( 1f );
			break;
		case PARALYSED:
			paused = false;
			break;
		case FROZEN:
			if (iceBlock != null) {
				iceBlock.melt();
				iceBlock = null;
			}
			paused = false;
			break;
		case ILLUMINATED:
			if (halo != null) {
				halo.putOut();
			}
			break;
		}
	}

	@Override
	public void update() {

		if (ch != null && !isMoving && visualJump == null && (jumpTweener == null || !jumpTweener.exists)
				&& playingMovement() && (movementPause -= Game.elapsed) <= 0) idle();

		super.update();

		if (paused && listener != null) {
			listener.onComplete( curAnim );
		}
		updateVisualJump(Game.elapsed);

		if (flashTime > 0 && (flashTime -= Game.elapsed) <= 0) {
			resetColor();
		}

		if (burning != null) {
			burning.visible = visible;
		}
		if (levitation != null) {
			levitation.visible = visible;
		}
		if (iceBlock != null) {
			iceBlock.visible = visible;
		}
		if (sleeping) {
			showSleep();
		} else {
			hideSleep();
		}
		if (emo != null) {
			emo.visible = visible;
		}
	}

	public void showSleep() {
		if (emo instanceof EmoIcon.Sleep) {

		} else {
			if (emo != null) {
				emo.killAndErase();
			}
			emo = new EmoIcon.Sleep( this );
		}
	}

	public void hideSleep() {
		if (emo instanceof EmoIcon.Sleep) {
			emo.killAndErase();
			emo = null;
		}
	}

	public void showAlert() {
		if (emo instanceof EmoIcon.Alert) {

		} else {
			if (emo != null) {
				emo.killAndErase();
			}
			emo = new EmoIcon.Alert( this );
		}
	}

	public void hideAlert() {
		if (emo instanceof EmoIcon.Alert) {
			emo.killAndErase();
			emo = null;
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (rebornAura != null) rebornAura.killAndErase();

		if (emo != null) {
			emo.killAndErase();
			emo = null;
		}
	}

	protected float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width() * (1f - shadowWidth)) / 2f,
				(height() * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				verticesBuffer.position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
		}

		super.draw();

	}

	@Override
	public void onComplete( Tweener tweener ) {
		if (ch instanceof Hero
				&& LocalCoop.contains(ch) && Dungeon.hero != ch) {
			LocalCoop.withHero((Hero)ch, () -> onComplete(tweener));
			return;
		}
		if (tweener == jumpTweener) {

			if (visible && Level.water[ch.pos] && !ch.flying) {
				GameScene.ripple( ch.pos );
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}

		} else if (tweener == motion) {

			isMoving = false;

			motion.killAndErase();
			motion = null;
		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (ch instanceof Hero
				&& LocalCoop.contains(ch) && Dungeon.hero != ch) {
			LocalCoop.withHero((Hero)ch, () -> onComplete(anim));
			return;
		}

		if (animCallback != null) {
			animCallback.call();
			animCallback = null;
		} else {

			if (anim == attack) {

				idle();
				ch.onAttackComplete();

			} else if (anim == operate) {

				idle();
				ch.onOperateComplete();

			}

		}
	}

	private static class JumpTweener extends Tweener {

		public Visual visual;

		public PointF start;
		public PointF end;

		public float height;

		public JumpTweener( Visual visual, PointF pos, float height, float time ) {
			super( visual, time );

			this.visual = visual;
			start = visual.point();
			end = pos;

			this.height = height;
		}

		@Override
		protected void updateValues( float progress ) {
			visual.point( PointF.inter( start, end, progress ).offset( 0, -height * 4 * progress * (1 - progress) ) );
		}
	}
}
