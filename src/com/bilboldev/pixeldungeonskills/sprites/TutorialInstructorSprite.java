package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.HeroSkins;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;

public class TutorialInstructorSprite extends MobSprite {
    public TutorialInstructorSprite() {
        texture(HeroSkins.sheet(HeroClass.MERCENARY, 1));
        TextureFilm armor = new TextureFilm(texture, texture.width, 15);
        TextureFilm frames = new TextureFilm(armor, 6, 12, 15);

        idle = new Animation(1, true);
        idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);
        run = new Animation(20, true);
        run.frames(frames, 2, 3, 4, 5, 6, 7);
        attack = new Animation(15, false);
        attack.frames(frames, 13, 14, 15, 0);
        zap = attack.clone();
        operate = new Animation(8, false);
        operate.frames(frames, 16, 17, 16, 17);
        die = new Animation(20, false);
        die.frames(frames, 8, 9, 10, 11, 12, 11);
        play(idle);
    }
}
