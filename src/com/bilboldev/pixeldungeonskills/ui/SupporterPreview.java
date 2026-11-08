package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.CosmeticPets;
import com.bilboldev.pixeldungeonskills.HeroSkins;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.SupporterAura;

public final class SupporterPreview extends Component {
    private static final HeroClass[] HEROES = {HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE,
            HeroClass.HUNTRESS, HeroClass.NECROMANCER, HeroClass.MERCENARY};
    private final MovieClip hero;
    private final SupporterAura aura;
    private Image pet;
    private boolean showcase;
    private float elapsed;
    private int appearance;

    public SupporterPreview(int style) {
        this(style, null);
    }

    public SupporterPreview(int style, Hero current) {
        this(style, current, current == null ? 0 : current.skin, 0);
    }

    public SupporterPreview(int style, Hero current, int skin, int companion) {
        hero = LibraryCatalog.heroImage(current == null ? HeroSkins.sheet(HeroClass.WARRIOR, skin) : current.spritesheet(),
                current == null ? 6 : current.tier());
        hero.scale.set(3);
        aura = new SupporterAura(hero);
        aura.style(style);
        add(hero);
        add(aura);
        pet(companion);
    }

    public void style(int style) { aura.style(style); }

    public void pet(int companion) {
        if (pet != null) {
            remove(pet);
            pet.destroy();
        }
        pet = companion == 0 ? null : CosmeticPets.image(companion);
        if (pet != null) {
            pet.scale.set(pet.scale.x * 3);
            add(pet);
        }
        layout();
    }

    public void cycle() {
        showcase = true;
        showAppearance();
    }

    private void showAppearance() {
        hero.texture(HeroSkins.sheet(HEROES[appearance % HEROES.length], 1));
        TextureFilm tiers = new TextureFilm(hero.texture, hero.texture.width, 15);
        TextureFilm frames = new TextureFilm(tiers, 6, 12, 15);
        hero.play(new MovieClip.Animation(1, true).frames(frames, 0, 0, 0, 1, 0, 0, 1, 1));
        style(appearance % 5 + 1);
        pet(appearance % CosmeticPets.COUNT + 1);
    }

    @Override public void update() {
        if (showcase) {
            elapsed += Game.elapsed;
            if (elapsed >= 4) {
                elapsed %= 4;
                appearance++;
                showAppearance();
            }
            float opacity = Math.min(1, Math.min(elapsed / .25f, (4 - elapsed) / .25f));
            hero.alpha(opacity);
            if (pet != null) pet.alpha(opacity);
        }
        super.update();
    }

    @Override protected void layout() {
        float groupWidth = hero.width() + (pet == null ? 0 : pet.width() + 8);
        hero.x = x + (width - groupWidth) / 2;
        hero.y = y + (height - hero.height()) / 2;
        aura.update();
        if (pet != null) {
            pet.x = hero.x + hero.width() + 8;
            pet.y = hero.y + hero.height() - pet.height();
        }
    }
}
