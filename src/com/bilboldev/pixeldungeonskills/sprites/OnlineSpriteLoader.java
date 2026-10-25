package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class OnlineSpriteLoader extends MobSprite {

    protected static final int FRAME_WIDTH	= 12;
    protected static final int FRAME_HEIGHT	= 15;

    protected static final int RUN_FRAMERATE	= 20;
    public String text = "A.I. Player";
    public int color = DEFAULT;
    public BitmapText message = new BitmapText();

    public OnlineSpriteLoader() {
        super();

        texture( Assets.PLANTS );

        texture( Assets.RAT );
        texture( Assets.CRAB );
        texture( Assets.GNOLL );
        texture( Assets.GOO );
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

    @Override
    public void die() {
        super.die();

    }
}
