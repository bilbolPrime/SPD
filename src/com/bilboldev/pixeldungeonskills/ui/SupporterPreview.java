package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.SupporterAura;

public final class SupporterPreview extends Component {
    private final Image hero;
    private final SupporterAura aura;

    public SupporterPreview(int style) {
        this(style, null);
    }

    public SupporterPreview(int style, Hero current) {
        hero = LibraryCatalog.heroImage(current == null ? HeroClass.WARRIOR.spritesheet() : current.heroClass.spritesheet(),
                current == null ? 6 : current.tier());
        hero.scale.set(3);
        aura = new SupporterAura(hero);
        aura.style(style);
        add(hero);
        add(aura);
    }

    public void style(int style) { aura.style(style); }

    @Override protected void layout() {
        hero.x = x + (width - hero.width()) / 2;
        hero.y = y + (height - hero.height()) / 2;
        aura.update();
    }
}
