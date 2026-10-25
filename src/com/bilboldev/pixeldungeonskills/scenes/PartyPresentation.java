package com.bilboldev.pixeldungeonskills.scenes;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Levitation;
import com.bilboldev.pixeldungeonskills.actors.buffs.Light;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.AttackIndicator;
import com.bilboldev.pixeldungeonskills.ui.HealthIndicator;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.Bundle;

public final class PartyPresentation {
    private static final HashMap<Integer, Pose> previous = new HashMap<>();
    private static final LinkedHashMap<Integer, Death> deaths = new LinkedHashMap<>();
    private static final java.util.LinkedHashSet<Integer> falls = new java.util.LinkedHashSet<>();
    private static Object run;
    private static int depth;
    private static double[] direction;
    private static PointF scroll;
    private static List<CharSprite> remains = new ArrayList<>();

    private static class Pose {
        final Class<?> type;
        final PointF position;
        final int cell, hp;
        final boolean flipped;
        final CharSprite.VisualAnimation animation;
        final CharSprite.VisualJump jump;

        Pose(Char ch) {
            type = ch.getClass();
            position = new PointF(ch.sprite.x, ch.sprite.y);
            cell = ch.pos;
            hp = ch.HP;
            flipped = ch.sprite.flipHorizontal;
            animation = ch.sprite.captureVisualAnimation();
            jump = ch.sprite.captureVisualJump();
        }
    }

    private PartyPresentation() { }

    public static final class Update {
        private final GameScene scene = (GameScene)Game.scene();
        private final Level level = Dungeon.level;
        private final HashMap<Integer, Char> actors = new HashMap<>();
        private final Char healthTarget = HealthIndicator.instance.target();
        private final boolean hadBoss = com.bilboldev.pixeldungeonskills.DungeonMusic.hasBoss();
        private final int sequence;

        public Update(Bundle incoming, int sequence) {
            this.sequence = sequence;
            if (run != PartySession.runIdentity() || depth != Dungeon.depth) clear();
            run = PartySession.runIdentity();
            depth = Dungeon.depth;
            for (Hero hero : LocalCoop.heroes()) actors.put(hero.id(), hero);
            for (Mob mob : level.mobs) actors.put(mob.id(), mob);
            captureDeaths(incoming);
        }

        public void apply() {
            for (Hero hero : LocalCoop.heroes()) update(hero);
            for (Mob mob : Dungeon.level.mobs) update(mob);
            for (Char removed : actors.values()) discard(removed.sprite);

            for (Heap heap : Dungeon.level.heaps.values()) {
                Heap old = level.heaps.get(heap.pos);
                if (old == null || old.sprite == null || !old.sprite.exists) scene.addHeapSprite(heap);
                else {
                    ItemSprite sprite = heap.sprite = old.sprite;
                    sprite.heap = heap;
                    sprite.view(heap.image(), heap.glowing());
                }
            }
            for (Heap heap : level.heaps.values()) {
                if (Dungeon.level.heaps.get(heap.pos) == null && heap.sprite != null) heap.sprite.kill();
            }
            for (Plant plant : Dungeon.level.plants.values()) {
                Plant old = level.plants.get(plant.pos);
                if (old != null && old.getClass() == plant.getClass() && old.sprite != null && old.sprite.exists)
                    plant.sprite = old.sprite;
                else scene.addPlantSprite(plant);
            }
            for (Plant plant : level.plants.values()) {
                Plant next = Dungeon.level.plants.get(plant.pos);
                if ((next == null || next.sprite != plant.sprite) && plant.sprite != null) plant.sprite.kill();
            }
            for (Blob blob : Dungeon.level.blobs.values()) {
                Blob old = level.blobs.get(blob.getClass());
                if (old != null && old.emitter != null && old.emitter.exists) old.emitter.rebind(blob);
                else scene.addBlobSprite(blob);
            }
            for (Blob blob : level.blobs.values()) {
                if (!Dungeon.level.blobs.containsKey(blob.getClass()) && blob.emitter != null) blob.emitter.on = false;
            }

            boolean changed = !java.util.Arrays.equals(level.map, Dungeon.level.map);
            System.arraycopy(Dungeon.level.map, 0, level.map, 0, level.map.length);
            Dungeon.level.map = level.map;
            FirstPersonView view = GameScene.firstPersonView();
            if (view != null) view.rebind(Dungeon.level);
            if (changed) GameScene.updateMap();
            GameScene.afterObserve();
            GameScene.updateFog();
            boolean hasBoss = com.bilboldev.pixeldungeonskills.DungeonMusic.hasBoss();
            if (hadBoss != hasBoss) com.bilboldev.noosa.audio.Music.INSTANCE.boss(hasBoss);
            attachDeaths(scene);
            for (int id : falls) {
                Actor actor = Actor.findById(id);
                if (actor instanceof Char && ((Char)actor).sprite != null) ((Char)actor).sprite.visible = false;
            }
            HealthIndicator.instance.target(replacement(healthTarget));
            HealthIndicator.instance.mobHealth().rebind();
            AttackIndicator.rebind();
            QuickSlot.refresh();
            for (int i = 0; i < scene.length; i++) {
                Gizmo member = scene.memberAt(i);
                if (member instanceof StatusPane) ((StatusPane)member).rebind(Dungeon.hero);
            }
            scene.hero = (HeroSprite)Dungeon.hero.sprite;
        }

        private void update(Char ch) {
            Char old = actors.remove(ch.id());
            if (old == null || old.getClass() != ch.getClass() || old.sprite == null || !old.sprite.exists
                    || ch instanceof Mob && old.sprite.getClass() != ((Mob)ch).spriteClass) {
                if (old != null) discard(old.sprite);
                if (ch instanceof Hero) scene.mobs.add(new HeroSprite((Hero)ch));
                else scene.addMobSprite((Mob)ch);
                if (!ch.isAlive()) ch.sprite.dieVisual();
                return;
            }
            CharSprite sprite = old.sprite;
            sprite.ch = ch;
            ch.sprite = sprite;
            if (ch instanceof Hero && (((Hero)old).tier() != ((Hero)ch).tier()
                    || ((Hero)old).heroClass != ((Hero)ch).heroClass)) {
                sprite.texture(((Hero)ch).heroClass.spritesheet());
                ((HeroSprite)sprite).updateArmor();
            }
            state(sprite, CharSprite.State.BURNING, old.buff(Burning.class) != null, ch.buff(Burning.class) != null);
            state(sprite, CharSprite.State.LEVITATING, old.buff(Levitation.class) != null, ch.buff(Levitation.class) != null);
            state(sprite, CharSprite.State.INVISIBLE, old.invisible > 0, ch.invisible > 0);
            state(sprite, CharSprite.State.ILLUMINATED, old.buff(Light.class) != null, ch.buff(Light.class) != null);
            state(sprite, CharSprite.State.FROZEN, old.buff(Frost.class) != null, ch.buff(Frost.class) != null);
            sprite.paused = ch.isAlive() && (ch.paralysed || ch.buff(Frost.class) != null);
            if (ch.buff(RebornChampion.class) != null) ch.buff(RebornChampion.class).updateSpriteState();
            else if (old.buff(RebornChampion.class) != null) sprite.NotAChamp();

            sprite.visible = ch instanceof Hero || Dungeon.visible[ch.pos];
            if (old.isAlive() && !ch.isAlive()) sprite.dieVisual();
            else if (!old.isAlive() && ch.isAlive()) {
                sprite.alpha(1);
                sprite.idle();
            }
            if (ch.isAlive()) {
                sprite.updateVisualPosition(old.pos, sequence);
                if (ch.HP < old.HP) sprite.flash();
            }
        }

        private void discard(CharSprite sprite) {
            if (sprite == null) return;
            stopEmitters(scene.emitters, sprite);
            stopEmitters(scene.mobs, sprite);
            sprite.discardVisual();
        }
    }

    private static Char replacement(Char old) {
        if (old == null) return null;
        Actor actor = Actor.findById(old.id());
        return actor instanceof Char && actor.getClass() == old.getClass() ? (Char)actor : null;
    }

    public static void move(int actorId, int from, int to, int sequence, int step) {
        Actor actor = Actor.findById(actorId);
        if (actor instanceof Char && ((Char)actor).sprite != null)
            ((Char)actor).sprite.queueVisualMove(from, to, sequence, step);
    }

    private static void state(CharSprite sprite, CharSprite.State state, boolean previous, boolean current) {
        if (previous == current) return;
        if (current) sprite.add(state);
        else sprite.remove(state);
    }

    private static void stopEmitters(Group group, CharSprite sprite) {
        for (int i = 0; i < group.length; i++) {
            Gizmo member = group.memberAt(i);
            if (member instanceof Emitter && ((Emitter)member).renderAnchor() == sprite) ((Emitter)member).on = false;
        }
    }

    public static void capture() {
        capture(null);
    }

    public static void capture(Bundle incoming) {
        if (run != PartySession.runIdentity() || depth != Dungeon.depth) clear();
        previous.clear();
        direction = null;
        scroll = null;
        run = PartySession.runIdentity();
        if (!PartySession.client() || !(Game.scene() instanceof GameScene) || Dungeon.level == null) return;
        remains = GameScene.takeBossRemains();
        depth = Dungeon.depth;
        for (Hero hero : LocalCoop.heroes()) {
            remember(hero);
        }
        for (Mob mob : Dungeon.level.mobs) {
            remember(mob);
        }
        captureDeaths(incoming);
        FirstPersonView view = GameScene.firstPersonView();
        if (view != null) {
            direction = view.direction();
        } else if (Camera.main != null && Camera.main.target == null) {
            scroll = new PointF(Camera.main.scroll.x, Camera.main.scroll.y);
        }
    }

    private static void captureDeaths(Bundle incoming) {
        if (incoming != null && incoming.getInt("depth") == depth && incoming.contains("dead_mobs")) {
            int[] ids = incoming.getIntArray("dead_mobs"), cells = incoming.getIntArray("dead_cells");
            for (int i = 0; i < ids.length; i++) {
                Actor actor = Actor.findById(ids[i]);
                if (!(actor instanceof Mob) || deaths.containsKey(ids[i]) || falls.contains(ids[i])) continue;
                CharSprite sprite = ((Mob)actor).sprite;
                if (sprite == null || !sprite.exists || !sprite.visible) continue;
                if (deaths.size() == PartySession.MAX_DEATHS) discard(deaths.values().iterator().next());
                deaths.put(ids[i], new Death(ids[i], cells[i], sprite));
            }
        }
    }

    public static void restore() {
        if (!PartySession.client() || run != PartySession.runIdentity() || depth != Dungeon.depth) {
            clear();
            return;
        }
        GameScene.restoreBossRemains(remains);
        remains.clear();
        for (Hero hero : LocalCoop.heroes()) {
            restore(hero);
        }
        for (Mob mob : Dungeon.level.mobs) {
            restore(mob);
        }
        GameScene scene = (GameScene)Game.scene();
        PartyVisuals.restore(scene);
        attachDeaths(scene);
        for (int id : falls) {
            Actor actor = Actor.findById(id);
            if (actor instanceof Mob && ((Mob)actor).sprite != null) ((Mob)actor).sprite.visible = false;
        }
        FirstPersonView view = GameScene.firstPersonView();
        if (view != null && direction != null) {
            view.direction(direction);
        } else if (view == null && scroll != null && Camera.main != null) {
            Camera.main.target = null;
            Camera.main.scroll.set(scroll);
        }
        previous.clear();
        direction = null;
        scroll = null;
    }

    private static void attachDeaths(GameScene scene) {
        for (Death death : deaths.values()) {
            death.visible = Dungeon.visible[death.cell];
            if (death.parent == null) scene.mobs.add(death);
        }
    }

    public static void animate(int actorId, String kind, int cell) {
        if (!PartySession.client() || PartySession.applyingSnapshot()
                || !(Game.scene() instanceof GameScene) || cell < 0 || cell >= Level.LENGTH) return;
        Actor actor = Actor.findById(actorId);
        if (!(actor instanceof Char)) return;
        Char ch = (Char)actor;
        if (ch.sprite != null && ch.sprite.visible && ch.sprite.exists && ch.isAlive())
            ch.sprite.playVisualAnimation(kind, cell);
    }

    public static void release() {
        for (Hero hero : LocalCoop.heroes()) {
            if (hero.sprite != null) hero.sprite.finishVisualPlayback();
        }
        for (Mob mob : Dungeon.level.mobs) {
            if (mob.sprite != null) {
                mob.sprite.finishVisualPlayback();
                if (falls.contains(mob.id())) {
                    mob.sprite.visible = Dungeon.visible[mob.pos];
                    if (deaths.containsKey(mob.id())) discard(deaths.get(mob.id()));
                }
            }
        }
        falls.clear();
        previous.clear();
        PartyVisuals.clear();
    }

    public static void fall(int actorId, int cell) {
        if (falls.contains(actorId)) return;
        Death existing = deaths.get(actorId);
        if (existing != null) {
            existing.startFall();
            if (falls.size() == PartySession.MAX_DEATHS) falls.remove(falls.iterator().next());
            falls.add(actorId);
            return;
        }
        Actor actor = Actor.findById(actorId);
        if (!(actor instanceof Mob)) return;
        CharSprite sprite = ((Mob)actor).sprite;
        if (sprite == null || !sprite.visible) return;
        if (falls.size() == PartySession.MAX_DEATHS) falls.remove(falls.iterator().next());
        if (deaths.size() == PartySession.MAX_DEATHS) discard(deaths.values().iterator().next());
        falls.add(actorId);
        Death fall = new Death(actorId, cell, sprite, true);
        deaths.put(actorId, fall);
        ((GameScene)Game.scene()).mobs.add(fall);
        sprite.visible = false;
    }

    public static void clear() {
        for (CharSprite sprite : remains) sprite.destroy();
        remains.clear();
        previous.clear();
        for (Death death : new ArrayList<>(deaths.values())) discard(death);
        falls.clear();
        direction = null;
        scroll = null;
        run = null;
    }

    static void detachDeaths(GameScene scene) {
        if (!PartySession.client() || !PartySession.applyingSnapshot()) {
            clear();
            return;
        }
        for (Death death : deaths.values()) {
            if (death.parent == scene.mobs) scene.mobs.erase(death);
        }
    }

    private static void discard(Death death) {
        death.killAndErase();
        death.destroy();
    }

    public static final class Death extends MovieClip {
        final int id, cell;
        public final com.bilboldev.pixeldungeonskills.ui.MobHealth.State health;
        private final float opacity, groundOffset;
        private float duration;
        private boolean falling;
        private float age;

        Death(int id, int cell, CharSprite sprite) {
            this(id, cell, sprite, false);
        }

        Death(int id, int cell, CharSprite sprite, boolean falling) {
            this.falling = falling;
            health = com.bilboldev.pixeldungeonskills.ui.MobHealth.death(sprite.ch, cell);
            this.id = id;
            this.cell = cell;
            copy(sprite);
            flipHorizontal = sprite.flipHorizontal;
            flipVertical = sprite.flipVertical;
            scale.set(sprite.scale);
            origin.set(sprite.origin);
            point(sprite.worldToCamera(cell));
            groundOffset = sprite.groundY() - sprite.y;
            rm = sprite.rm;
            gm = sprite.gm;
            bm = sprite.bm;
            opacity = sprite.alpha();
            Animation animation = sprite.deathAnimation();
            duration = falling ? 1 : animation == null ? 0 : animation.delay * animation.frames.length;
            if (falling) startFall();
            else if (animation != null) play(animation);
        }

        private void startFall() {
            falling = true;
            duration = 1;
            age = 0;
            origin.set(width / 2, height - 8);
            angularSpeed = 720;
        }

        public float groundY() {
            return y + groundOffset;
        }

        @Override
        public void update() {
            super.update();
            age += Game.elapsed;
            if (falling) {
                scale.set(Math.max(0, 1 - age));
                alpha(opacity * Math.max(0, 1 - age));
                if (age >= duration) discard(this);
                return;
            }
            alpha(opacity * Math.min(1, Math.max(0, 1 - (age - duration) / 3f)));
            if (age >= duration + 3f) discard(this);
        }

        @Override
        public void destroy() {
            deaths.remove(id);
            super.destroy();
        }
    }

    private static void remember(Char ch) {
        if (ch.sprite != null && ch.sprite.visible && ch.sprite.exists && ch.isAlive())
            previous.put(ch.id(), new Pose(ch));
    }

    private static void restore(Char ch) {
        Pose old = previous.get(ch.id());
        CharSprite sprite = ch.sprite;
        if (old == null || old.type != ch.getClass() || sprite == null || !sprite.visible || !ch.isAlive()) return;
        sprite.flipHorizontal = old.flipped;
        sprite.frame(sprite.frame());
        if (sprite.restoreVisualJump(old.jump)) return;
        if (old.cell != ch.pos && Level.distance(old.cell, ch.pos) <= 2)
            sprite.moveVisual(old.cell, old.position);
        else if (old.cell == ch.pos) sprite.restoreVisualAnimation(old.animation);
        if (ch.HP < old.hp) {
            sprite.flash();
        }
    }
}
