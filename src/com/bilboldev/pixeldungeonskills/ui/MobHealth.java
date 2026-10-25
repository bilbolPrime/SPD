package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornDM300;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornGoo;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornTengu;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornYog;
import com.bilboldev.pixeldungeonskills.actors.mobs.Yog;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

public final class MobHealth {
    public static final float SHATTER_DURATION = 0.65f;
    private static final float HIT_DURATION = 0.28f;

    private static final HashMap<Long, Memory> seen = new HashMap<>();
    private static WeakReference<Object> run = new WeakReference<>(null);

    private final ArrayList<State> states = new ArrayList<>();
    private final List<State> bars = Collections.unmodifiableList(states);
    private final IdentityHashMap<Char, State> tracked = new IdentityHashMap<>();
    private final ArrayList<Char> occupants = new ArrayList<>();
    private Level level;
    private Char hero;

    public List<State> bars() {
        return bars;
    }

    public void clear() {
        states.clear();
        tracked.clear();
        occupants.clear();
        hero = null;
        level = null;
    }

    public void rebind() {
        hero = Dungeon.hero;
        level = Dungeon.level;
        tracked.clear();
        for (int i = states.size() - 1; i >= 0; i--) {
            State state = states.get(i);
            if (state.mob == null) continue;
            Actor actor = Actor.findById(state.mob.id());
            if (!(actor instanceof Mob) || actor.getClass() != state.mob.getClass()
                    || state.rows != rows((Char)actor)) {
                states.remove(i);
                continue;
            }
            state.mob = (Char)actor;
            state.memory.mob = new WeakReference<>(state.mob);
            state.memory.level = new WeakReference<>(level);
            tracked.put(state.mob, state);
        }
    }

    public void update(Group sprites, Char target, float elapsed) {
        Object identity = LocalCoop.active() ? PartySession.runIdentity() : Dungeon.hero;
        if (run.get() != identity) {
            seen.clear();
            run = new WeakReference<>(identity);
        }
        if (hero != Dungeon.hero || level != Dungeon.level) {
            states.clear();
            tracked.clear();
            hero = Dungeon.hero;
            level = Dungeon.level;
        }
        if (sprites == null || hero == null || level == null) return;

        occupants.clear();
        for (int i = 0; i < sprites.length; i++) {
            Gizmo member = sprites.memberAt(i);
            if (member instanceof com.bilboldev.pixeldungeonskills.scenes.PartyPresentation.Death) {
                State bar = ((com.bilboldev.pixeldungeonskills.scenes.PartyPresentation.Death)member).health;
                if (bar != null && bar.deathAge < SHATTER_DURATION && !states.contains(bar)) states.add(bar);
            }
            if (!(member instanceof CharSprite)) continue;
            Char ch = ((CharSprite) member).ch;
            if (ch instanceof Mob) occupants.add(ch);
        }

        for (int i = 0; i < occupants.size(); i++) {
            Char ch = occupants.get(i);
            if (ch.HT <= 0 || ch instanceof NPC && ch.HT <= 1 && target != ch
                    || !ch.isAlive() || !visible(ch) || tracked.containsKey(ch)) continue;
            long key = key(ch);
            Memory memory = seen.get(key);
            if (memory == null || memory.type != ch.getClass()
                    || memory.level.get() == level && memory.mob.get() != ch) {
                memory = new Memory(ch, level);
                seen.put(key, memory);
            } else {
                memory.mob = new WeakReference<>(ch);
                memory.level = new WeakReference<>(level);
            }
            State state = new State(ch, memory, key);
            state.shields = memory.shields < 0 ? shields(ch) : memory.shields;
            states.add(state);
            tracked.put(ch, state);
        }

        float time = Math.max(0, Math.min(elapsed, 0.1f));
        for (int i = states.size() - 1; i >= 0; i--) {
            State state = states.get(i);
            update(state, target, time);
            if (state.deathAge >= SHATTER_DURATION || state.deathAge >= 0 && !state.visible) {
                states.remove(i);
                tracked.remove(state.mob);
            }
        }
    }

    private void update(State state, Char target, float elapsed) {
        Char ch = state.mob;
        state.damageAge += elapsed;
        for (int i = state.breaks.size() - 1; i >= 0; i--) {
            Break broken = state.breaks.get(i);
            broken.age += elapsed;
            if (broken.age >= SHATTER_DURATION) state.breaks.remove(i);
        }
        if (state.deathAge >= 0) {
            state.deathAge += elapsed;
            state.visible &= inSight(state.cell);
            return;
        }

        boolean alive = ch.isAlive();
        boolean onScreen = visible(ch);
        if (!alive) {
            state.visible = state.visible && inSight(ch.pos) && ch.invisible <= 0;
            state.deathAge = 0;
            if (state.visible) {
                for (int row = 0; row < state.rows; row++) {
                    if ((long) state.hp * state.rows > (long) row * state.maxHp) {
                        state.breaks.add(new Break(row, 0));
                    }
                }
                breakShields(state, state.shields | state.fist);
            }
            state.hp = 0;
            state.shields = 0;
            seen.remove(state.key);
            return;
        }

        state.visible = onScreen && (!(ch instanceof NPC) || ch.HT > 1 || target == ch);
        state.selected = target == ch;
        if (onScreen) {
            state.cell = ch.pos;
            state.worldX = ch.sprite.x + ch.sprite.width * 0.5f;
            state.worldY = ch.sprite.y - 1;
            state.spriteHeight = ch.sprite.height;
            state.revealAge = Math.min(2, state.revealAge + elapsed);
            state.memory.revealAge = state.revealAge;
        }
        if (ch.HP < state.hp && onScreen) state.damageAge = 0;

        int phase = Math.max(state.phase, phase(ch, state.rows));
        if (onScreen) {
            for (int previous = state.phase; previous < phase; previous++) {
                state.breaks.add(new Break(state.rows - previous - 1, 0));
            }
        }
        state.phase = phase;
        state.memory.phase = phase;

        int shields = shields(ch);
        if (onScreen) breakShields(state, state.shields & ~shields);
        state.shields = shields;
        state.hp = Math.max(0, ch.HP);
        state.maxHp = Math.max(1, ch.HT);
        state.memory.hp = state.hp;
        state.memory.shields = state.shields;
    }

    private static void breakShields(State state, int mask) {
        if ((mask & 1) != 0) state.breaks.add(new Break(-1, 1));
        if ((mask & 2) != 0) state.breaks.add(new Break(-1, 2));
    }

    private int shields(Char eye) {
        if (!(eye instanceof RebornYog) && !(eye instanceof Yog)) return 0;
        int mask = 0;
        for (int i = 0; i < occupants.size(); i++) {
            Char fist = occupants.get(i);
            if (!fist.isAlive()) continue;
            if (eye instanceof RebornYog) {
                if (!(fist instanceof RebornYog.Fist)
                        || ((RebornYog.Fist) fist).ownerId != eye.id()) continue;
                mask |= fist(fist);
            } else if (fist instanceof Yog.RottingFist || fist instanceof Yog.BurningFist) {
                mask |= fist(fist);
            }
        }
        return mask;
    }

    private static int fist(Char ch) {
        if (ch instanceof RebornYog.Rotting || ch instanceof Yog.RottingFist) return 1;
        if (ch instanceof RebornYog.BurningHand || ch instanceof Yog.BurningFist) return 2;
        return 0;
    }

    private static boolean visible(Char ch) {
        return ch.sprite != null && ch.sprite.exists && ch.sprite.visible
                && ch.invisible <= 0 && inSight(ch.pos);
    }

    private static boolean inSight(int cell) {
        return Dungeon.visible != null && cell >= 0 && cell < Dungeon.visible.length
                && Dungeon.visible[cell];
    }

    private static long key(Char ch) {
        return ((long) Dungeon.depth << 32) | (ch.id() & 0xffffffffL);
    }

    private static int rows(Char ch) {
        if (ch instanceof RebornTengu) return 4;
        if (ch instanceof RebornKing || ch instanceof RivalBoss) return 3;
        if (ch instanceof RebornGoo || ch instanceof RebornDM300) return 2;
        RebornChampion champion = ch.buff(RebornChampion.class);
        if (champion != null) return champion.isPair() ? 2 : 3;
        return 1;
    }

    private static int phase(Char ch, int rows) {
        if (rows == 1) return 0;
        int threshold = Math.min(rows - 1,
                Math.max(0, (int) (((long) ch.HT - ch.HP) * rows / Math.max(1, ch.HT))));
        if (ch instanceof RebornTengu) {
            return Math.max(threshold, Integer.bitCount(((RebornTengu) ch).unlockedSkills() & 7));
        }
        return ch instanceof RebornBoss ? Math.max(threshold, ((RebornBoss) ch).phase()) : threshold;
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private static final class Memory {
        final Class<?> type;
        WeakReference<Char> mob;
        WeakReference<Level> level;
        float revealAge;
        int phase;
        int hp;
        int shields = -1;

        Memory(Char ch, Level level) {
            type = ch.getClass();
            mob = new WeakReference<>(ch);
            this.level = new WeakReference<>(level);
            phase = phase(ch, rows(ch));
            hp = Math.max(0, ch.HP);
        }
    }

    public static final class State {
        public Char mob;
        public final boolean boss, champion, yog;
        public final int width, rows, fist;
        public int hp, maxHp, phase, shields;
        public int cell;
        public float worldX, worldY, spriteHeight;
        public float revealAge;
        public float damageAge = HIT_DURATION;
        public float deathAge = -1;
        public boolean visible, selected;

        private final long key;
        private final Memory memory;
        private final ArrayList<Break> breaks = new ArrayList<>();
        private final List<Break> fragments = Collections.unmodifiableList(breaks);

        private State(Char ch, Memory memory, long key) {
            this(ch, memory, key, false);
        }

        private State(Char ch, Memory memory, long key, boolean cosmetic) {
            mob = cosmetic ? null : ch;
            this.memory = memory;
            this.key = key;
            boss = Bestiary.isBoss(ch);
            champion = ch.champ >= 0;
            yog = ch instanceof Yog || ch instanceof RebornYog;
            fist = fist(ch);
            width = Math.max(boss ? 24 : 8, (int)Math.ceil(ch.sprite.width)) + (champion ? 4 : 0);
            rows = rows(ch);
            hp = memory.hp;
            maxHp = Math.max(1, ch.HT);
            phase = memory.phase;
            revealAge = memory.revealAge;
        }

        public List<Break> breaks() {
            return fragments;
        }

        public boolean isVisible() {
            return visible && (deathAge >= 0 ? inSight(cell) : mob.isAlive() && MobHealth.visible(mob));
        }

        public float fill(int row) {
            return clamp((float) hp * rows / maxHp - row);
        }

        public float reveal(int row) {
            float delay = boss ? row * 0.14f : 0;
            float duration = boss ? 0.6f : 0.28f;
            float progress = clamp((revealAge - delay) / duration);
            return progress * progress * (3 - 2 * progress);
        }

        public float shakeX() {
            float strength = 1 - clamp(damageAge / HIT_DURATION);
            return (float) Math.sin(damageAge * 105) * strength * (boss ? 1.15f : 0.75f);
        }

        public float shakeY() {
            float strength = 1 - clamp(damageAge / HIT_DURATION);
            return (float) Math.cos(damageAge * 80) * strength * 0.45f;
        }
    }

    public static State death(Char ch, int cell) {
        if (!(ch instanceof Mob) || ch instanceof NPC && ch.HT <= 1) return null;
        long key = key(ch);
        Memory memory = seen.remove(key);
        if (memory == null) memory = new Memory(ch, Dungeon.level);
        State state = new State(ch, memory, key, true);
        state.cell = cell;
        state.worldX = ch.sprite.x + ch.sprite.width / 2;
        state.worldY = ch.sprite.y - 1;
        state.spriteHeight = ch.sprite.height;
        state.visible = visible(ch);
        state.deathAge = 0;
        for (int row = 0; row < state.rows; row++) {
            if ((long)state.hp * state.rows > (long)row * state.maxHp) state.breaks.add(new Break(row, 0));
        }
        breakShields(state, Math.max(0, memory.shields) | state.fist);
        state.hp = 0;
        return state;
    }

    public static final class Break {
        public final int row;
        public final int shield;
        public float age;

        private Break(int row, int shield) {
            this.row = row;
            this.shield = shield;
        }
    }
}
