package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.online.OnlineMessageHepler;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class OnlineSprite extends MobSprite {

    protected static final int FRAME_WIDTH	= 12;
    protected static final int FRAME_HEIGHT	= 15;

    protected static final int RUN_FRAMERATE	= 20;
    public String text = "A.I. Player";
    public int color = DEFAULT;
    public BitmapText message = new BitmapText();

    public OnlineSprite() {
        super();

        texture( Assets.SOLDIER_WARRIOR);

        TextureFilm frames = new TextureFilm( texture, FRAME_WIDTH, FRAME_HEIGHT );

        idle = new Animation( 1, true );
        idle.frames( frames, 0, 0, 0);

        run = new Animation( RUN_FRAMERATE, true );
        run.frames( frames, 2, 3, 4, 5, 6, 7 );

        die = new Animation( 20, false );
        die.frames( frames, 8, 9, 10, 11, 12, 11 );

        attack = new Animation( 15, false );
        attack.frames( frames, 13, 14, 15, 0 );

        zap = attack.clone();

        play( idle );
    }

    public void LoadRat()
    {
        texture( Assets.RAT );

        TextureFilm frames = new TextureFilm( texture, 16, 15 );

        idle = new Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1 );

        run = new Animation( 10, true );
        run.frames( frames, 6, 7, 8, 9, 10 );

        attack = new Animation( 15, false );
        attack.frames( frames, 2, 3, 4, 5, 0 );

        zap = attack.clone();

        die = new Animation( 10, false );
        die.frames( frames, 11, 12, 13, 14 );

        play( idle );

        showRatFlag();
    }

    public void LoadCrab()
    {
        texture( Assets.CRAB );

        TextureFilm frames = new TextureFilm( texture, 16 );

        idle = new Animation( 5, true );
        idle.frames( frames, 0, 1, 0, 2 );

        run = new Animation( 15, true );
        run.frames( frames, 3, 4, 5, 6 );

        attack = new Animation( 12, false );
        attack.frames( frames, 7, 8, 9 );

        die = new Animation( 12, false );
        die.frames( frames, 10, 11, 12, 13 );

        play( idle );

        showRatFlag();
    }

    public void LoadGnoll()
    {
        texture( Assets.GNOLL );

        TextureFilm frames = new TextureFilm( texture, 12, 15 );

        idle = new Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new Animation( 12, true );
        run.frames( frames, 4, 5, 6, 7 );

        attack = new Animation( 12, false );
        attack.frames( frames, 2, 3, 0 );

        die = new Animation( 12, false );
        die.frames( frames, 8, 9, 10 );

        play( idle );
        showRatFlag();
    }

    public void LoadGoo()
    {
        texture( Assets.GOO );

        TextureFilm frames = new TextureFilm( texture, 20, 14 );

        idle = new Animation( 10, true );
        idle.frames( frames, 0, 1 );

        run = new Animation( 10, true );
        run.frames( frames, 0, 1 );

        attack = new Animation( 10, false );
        attack.frames( frames, 5, 0, 6 );

        die = new Animation( 10, false );
        die.frames( frames, 2, 3, 4 );

        play( idle );

        showRatFlag();
    }

    public void resetData()
    {

    }

    public void showRatFlag() {
        if (emo instanceof EmoIcon.RatFlag) {

        } else {
            if (emo != null) {
                emo.killAndErase();
            }
            emo = new EmoIcon.RatFlag( this );
        }
    }

    public void hideRatFlag() {
        if (emo instanceof EmoIcon.RatFlag) {
            emo.killAndErase();
            emo = null;
        }
    }

    public void showOnlineTurn() {
        if (emo instanceof EmoIcon.OnlineTurn) {

        } else {
            if (emo != null) {
                emo.killAndErase();
            }
            emo = new EmoIcon.OnlineTurn( this );
        }
    }

    public void hideOnlineTurn() {
        if (emo instanceof EmoIcon.OnlineTurn) {
            emo.killAndErase();
            emo = null;
        }
    }

    @Override
    public void onComplete( Animation anim ) {

        if (animCallback != null) {
            animCallback.call();
            animCallback = null;
        } else {

            if (anim == attack) {

                idle();

            } else if (anim == operate) {

                idle();

            }

        }

        if (anim == die) {
            parent.add( new AlphaTweener( this, 0, FADE_TIME ) {
                @Override
                protected void onComplete() {
                    OnlineSprite.this.killAndErase();
                    parent.erase( this );
                };
            } );
        }
    }

    @Override
    public void die() {
        super.die();

    }
}
