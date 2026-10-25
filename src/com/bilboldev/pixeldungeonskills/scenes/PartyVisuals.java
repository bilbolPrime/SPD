package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.*;
import com.bilboldev.pixeldungeonskills.network.PartyEffects;
import com.bilboldev.pixeldungeonskills.network.PartyParticles;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.network.PartyText;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.PointF;
import java.util.ArrayList;

public final class PartyVisuals {
    private static final int MAX_ACTIVE = 128;
    private static final ArrayList<Effect> active = new ArrayList<>();
    private static Object run;
    private static int depth;

    private PartyVisuals() { }

    public static void play(Bundle event) {
        if (!PartySession.client() || !(Game.scene() instanceof GameScene)) return;
        if (run != PartySession.runIdentity() || depth != Dungeon.depth) clear();
        run = PartySession.runIdentity();
        depth = Dungeon.depth;
        int type = event.getInt("type");
        CharSprite sprite = sprite(event.getInt("actor"));
        if (type == PartyEffects.SOUND) {
            PartyEffects.playSound(event);
            return;
        }
        if (type == PartyEffects.JUMP) {
            if (sprite != null) sprite.jumpVisual(event.getInt("from"), event.getInt("to"));
            return;
        }
        if (type == PartyEffects.FALL) {
            PartyPresentation.fall(event.getInt("actor"), event.getInt("cell"));
            return;
        }
        if (type == PartyEffects.FLASH) {
            if (sprite != null) sprite.flash();
            return;
        }
        if (active.size() == MAX_ACTIVE) remove(0);
        Group group = new Group();
        Gizmo display = group;
        switch (type) {
            case PartyEffects.MISSILE:
                MissileSprite missile = new MissileSprite();
                group.add(missile);
                int glow = event.getInt("glow");
                missile.reset(event.getInt("from"), event.getInt("to"), event.getInt("image"),
                        glow < 0 ? null : new ItemSprite.Glowing(glow, event.getFloat("period")), null);
                break;
            case PartyEffects.MAGIC:
                MagicMissile magic = new MagicMissile();
                group.add(magic);
                magic.reset(event.getInt("from"), event.getInt("to"), event.getFloat("speed"), null);
                magic.size(event.getFloat("size"));
                magic.pour(PartyParticles.factory(event.getInt("factory"), false), event.getFloat("interval"));
                break;
            case PartyEffects.LIGHTNING:
                int[] cells = event.getIntArray("cells");
                group.add(new Lightning(cells, cells.length, null));
                break;
            case PartyEffects.PARTICLES:
                ParticleEffect emitter = new ParticleEffect(event.getInt("actor"));
                emitter.pos(event.getFloat("x"), event.getFloat("y"), event.getFloat("width"), event.getFloat("height"));
                emitter.bind();
                emitter.start(PartyParticles.factory(event.getInt("factory"), event.getBoolean("light")),
                        event.getFloat("interval"), event.getInt("quantity"));
                group.add(emitter);
                break;
            case PartyEffects.SPLASH:
                Splash.at(group, new PointF(event.getFloat("x"), event.getFloat("y")),
                        event.getFloat("direction"), event.getFloat("cone"), event.getInt("color"), event.getInt("quantity"));
                break;
            case PartyEffects.STATUS:
                FloatingText text = new FloatingText();
                String value = event.contains("number")
                        ? (event.getBoolean("plus") ? "+" : "") + event.getInt("number")
                        : PartyText.render(event.getBundle("text"));
                text.resetAt(event.getFloat("x"), event.getFloat("y"), event.getInt("cell"),
                        event.getInt("actor"), event.getFloat("height"), value, event.getInt("color"));
                group.destroy();
                display = text;
                break;
            case PartyEffects.RAY:
                group.add(new DeathRay(new PointF(event.getFloat("x"), event.getFloat("y")),
                        new PointF(event.getFloat("to_x"), event.getFloat("to_y"))));
                break;
            case PartyEffects.WOUND:
                Wound wound = new Wound();
                wound.reset(event.getInt("cell"));
                wound.angle = event.getFloat("angle");
                group.add(wound);
                break;
            case PartyEffects.SPELL:
                group.add(SpellSprite.visual(event.getInt("actor"), event.getInt("cell"), event.getInt("icon")));
                break;
            case PartyEffects.RIPPLE:
                Ripple ripple = new Ripple();
                ripple.reset(event.getInt("cell"));
                group.add(ripple);
                break;
            case PartyEffects.FLARE:
                new Flare(event.getInt("rays"), event.getFloat("radius"))
                        .color(event.getInt("color"), event.getBoolean("light"))
                        .show(group, new PointF(event.getFloat("x"), event.getFloat("y")), event.getFloat("duration"));
                break;
            case PartyEffects.STAB:
                Stab stab = new Stab(event.getInt("image"));
                group.add(stab);
                stab.visualPath(event.getInt("from"), event.getInt("to"));
                break;
            default:
                group.destroy();
                throw new IllegalArgumentException("Unknown cosmetic effect");
        }
        Effect effect = new Effect(display, type == PartyEffects.STATUS);
        active.add(effect);
        effect.attach((GameScene)Game.scene());
    }

    public static void update() {
        if (run != null && (!PartySession.client() || run != PartySession.runIdentity() || depth != Dungeon.depth)) {
            clear();
            return;
        }
        for (int i = active.size() - 1; i >= 0; i--) {
            Effect effect = active.get(i);
            effect.age += Game.elapsed;
            if (effect.age > 8 || !effect.display.exists
                    || effect.display instanceof Group && ((Group)effect.display).countLiving() == 0) remove(i);
        }
    }

    static void detach(GameScene scene) {
        if (!PartySession.client() || !PartySession.applyingSnapshot()) {
            clear();
            return;
        }
        for (Effect effect : active) {
            if (effect.display.parent != null) effect.display.parent.erase(effect.display);
            for (Gizmo owned : effect.owned) if (owned instanceof ParticleEffect)
                ((ParticleEffect)owned).renderAnchor(null);
        }
    }

    static void restore(GameScene scene) {
        if (run != PartySession.runIdentity() || depth != Dungeon.depth) {
            clear();
            return;
        }
        for (Effect effect : active) {
            effect.attach(scene);
            for (Gizmo owned : effect.owned) if (owned instanceof ParticleEffect) ((ParticleEffect)owned).bind();
        }
    }

    public static void clear() {
        for (int i = active.size() - 1; i >= 0; i--) remove(i);
        run = null;
    }

    private static void remove(int index) {
        Effect effect = active.remove(index);
        effect.display.killAndErase();
        for (Gizmo owned : effect.owned) {

            if (owned.parent != effect.display) owned.destroy();
        }
        effect.display.destroy();
    }

    private static CharSprite sprite(int id) {
        Actor actor = Actor.findById(id);
        return actor instanceof Char ? ((Char)actor).sprite : null;
    }

    private static final class Effect {
        final Gizmo display;
        final boolean status;
        final ArrayList<Gizmo> owned = new ArrayList<>();
        float age;
        Effect(Gizmo display, boolean status) {
            this.display = display;
            this.status = status;
            if (display instanceof Group) {
                Group group = (Group)display;
                for (int i = 0; i < group.length; i++) if (group.memberAt(i) != null) owned.add(group.memberAt(i));
            }
        }
        void attach(GameScene scene) {
            (status ? scene.statuses : scene.effects).add(display);
        }
    }

    private static final class ParticleEffect extends Emitter {
        final int actor;
        ParticleEffect(int actor) { this.actor = actor; }
        void bind() { renderAnchor(sprite(actor)); }
    }
}
