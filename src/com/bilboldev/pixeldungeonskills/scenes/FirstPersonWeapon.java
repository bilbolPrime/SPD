package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.quest.Pickaxe;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.DualSwords;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Knuckles;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite.Glowing;

final class FirstPersonWeapon {
    private final Camera ui;
    private final Image sprite = new Image(Assets.ITEMS);
    private int image = -1;
    private Glowing glowing;
    private float glowPhase;
    private boolean glowUp = true;

    FirstPersonWeapon(Camera ui) {
        this.ui = ui;
        sprite.camera = ui;
        sprite.flipHorizontal = true;
        sprite.origin.set(13, 13);
    }

    void draw() {
        updateGlow(0);
        sprite.visible = false;
        if (Dungeon.hero == null || !Dungeon.hero.isAlive() || Dungeon.hero.invisible > 0) return;
        HeroSprite hero = Dungeon.hero.sprite instanceof HeroSprite ? (HeroSprite)Dungeon.hero.sprite : null;
        if (hero != null && hero.hideHeldWeapon()) return;
        KindOfWeapon weapon = Dungeon.hero.belongings.weapon;

        if (!(weapon instanceof MeleeWeapon || weapon instanceof Wand || weapon instanceof Pickaxe)
                || weapon instanceof Knuckles || weapon instanceof DualSwords) return;
        int next = weapon.image(), columns = sprite.texture.width / 16;
        if (next < 0 || next >= columns * (sprite.texture.height / 16)) return;
        if (image != next) {
            sprite.frame((next % columns) * 16, (next / columns) * 16, 16, 16);
            image = next;
        }
        float size = Math.min(ui.width * 0.36f, ui.height * 0.52f);
        float progress = hero == null ? -1 : hero.attackAnimationProgress();
        float swing = progress < 0 ? 0 : (float)Math.sin(progress * Math.PI);
        sprite.scale.set(size / 16);
        sprite.angle = 12 - 45 * swing;

        float portraitLift = ui.height > ui.width ? 24 : 0;
        sprite.x = ui.width * 0.91f - sprite.origin.x - size * 0.1f * swing;
        sprite.y = ui.height + 8 - sprite.origin.y - portraitLift - size * 0.15f * swing;
        sprite.visible = true;
        sprite.draw();
    }

    void update() {
        updateGlow(Game.elapsed);
    }

    private void updateGlow(float elapsed) {
        KindOfWeapon weapon = Dungeon.hero == null ? null : Dungeon.hero.belongings.weapon;
        Glowing next = weapon == null ? null : weapon.glowing();
        if (next != glowing) {
            glowing = next;
            glowPhase = 0;
            glowUp = true;
        }
        if (glowing == null) {
            sprite.resetColor();
            return;
        }

        float period = Math.max(0.001f, glowing.period);
        if (glowUp && (glowPhase += elapsed) > period) {
            glowUp = false;
            glowPhase = period;
        } else if (!glowUp && (glowPhase -= elapsed) < 0) {
            glowUp = true;
            glowPhase = 0;
        }
        float value = glowPhase / period * 0.6f;
        sprite.rm = sprite.gm = sprite.bm = 1 - value;
        sprite.ra = glowing.red * value;
        sprite.ga = glowing.green * value;
        sprite.ba = glowing.blue * value;
    }

    void destroy() {
        sprite.destroy();
    }
}
