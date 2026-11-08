package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.HeroSkins;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalHuntress;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalServant;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.levels.Level;

public class RivalSprite extends MobSprite {
    private boolean meleeWarning;

    @Override
    public void link(Char ch) {
        HeroClass hero = ch instanceof RivalBoss ? ((RivalBoss)ch).heroClass() : HeroClass.ROGUE;
        int tier = ch instanceof RivalBoss ? ((RivalBoss)ch).tier() : ((RivalServant)ch).tier();
        appearance(hero, tier);
        super.link(ch);
        resetColor();
    }

    public void appearance(HeroClass hero, int tier) {
        texture(HeroSkins.sheet(hero, 1));
        TextureFilm tiers = new TextureFilm(texture, texture.width, HeroSprite.FRAME_HEIGHT);
        TextureFilm frames = new TextureFilm(tiers, tier + 1, HeroSprite.FRAME_WIDTH, HeroSprite.FRAME_HEIGHT);
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
        resetColor();
    }

    @Override
    public void attack(int cell) {
        if (!(ch instanceof RivalHuntress) || Level.adjacent(ch.pos, cell)) {
            super.attack(cell);
            return;
        }
        Char archer = ch;
        Level floor = Dungeon.level;
        zap(cell, () -> {
            idle();
            if (Dungeon.level != floor || !archer.isAlive() || parent == null) return;
            Sample.INSTANCE.play(Assets.SND_MISS);
            ((MissileSprite)parent.recycle(MissileSprite.class)).reset(archer.pos, cell, ItemSpriteSheet.Arrow, null, () -> {
                if (Dungeon.level == floor && archer.isAlive()) archer.onAttackComplete();
            });
        });
    }

    @Override
    public void update() {
        super.update();
        boolean warning = ch instanceof RivalBoss && ch.isAlive() && ((RivalBoss)ch).meleeWarning();
        if (warning) showAlert();
        else if (meleeWarning) hideAlert();
        meleeWarning = warning;
        if (warning && emo != null) {

            emo.heightOffset = 16;
            emo.visible = visible;
        }
    }

    @Override
    public void resetColor() {
        float opacity = am;
        super.resetColor();
        tint(0xB52B32, 0.38f);
        if (ch != null) am = opacity;
    }
}
