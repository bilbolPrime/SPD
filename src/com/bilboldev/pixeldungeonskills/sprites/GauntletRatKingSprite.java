package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;

public class GauntletRatKingSprite extends RatKingSprite {
    @Override
    public void die() {
        texture(Assets.RAT);
        TextureFilm frames = new TextureFilm(texture, 16, 15);
        die = new Animation(10, false);
        die.frames(frames, 11, 12, 13, 14);
        super.die();
    }
}
