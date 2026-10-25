package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Ooze;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.PurpleParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.*;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class RebornYog extends RebornBoss {
    private int inherited, cadence, firstInherited;
    private boolean spawned;
    {
        name = "Yog-Dzewa";
        spriteClass = YogSprite.class;
        HP = HT = 960;
        defenseSkill = 20;
        EXP = 50;
        state = PASSIVE;
    }

    @Override
    protected Class<? extends Mob> original() {
        return Yog.class;
    }

    @Override
    public int dr() {
        return opening() ? 0 : 8;
    }

    @Override
    protected boolean getCloser(int target) {
        return false;
    }

    @Override
    protected boolean getFurther(int target) {
        return false;
    }

    @Override
    protected boolean canAttack(Char target) {
        return false;
    }
    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
    static {
        IMMUNITIES.add(Burning.class);
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }

    public int inherited() {
        return inherited;
    }

    public ArrayList<Fist> fists() {
        ArrayList<Fist> result = new ArrayList<>();
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof Fist && ((Fist)mob).ownerId == id() && mob.isAlive()) result.add((Fist)mob);
        return result;
    }

    public void spawnFists() {
        if (spawned) return;
        spawned = true;
        for (int type = 0; type < 2; type++) {
            Fist fist = type == 0 ? new Rotting() : new BurningHand();
            int best = -1;
            for (int cell = 0; cell < Level.LENGTH; cell++) if (Level.passable[cell] && !Level.avoid[cell]
                    && Actor.findChar(cell) == null && Level.distance(pos, cell) >= 2 && Level.distance(pos, cell) <= 4
                    && Level.distance(Dungeon.hero.pos, cell) >= 3 && lineClear(pos, cell)) {
                if (best < 0 || (type == 0 ? cell % Level.WIDTH < best % Level.WIDTH : cell % Level.WIDTH > best % Level.WIDTH)) best = cell;
            }
            if (best >= 0) {
                fist.pos = best;
                fist.ownerId = id();
                GameScene.add(fist);
            }
            else inherited |= type == 0 ? 1 : 2;
        }
    }

    @Override
    protected boolean prepareSkill() {
        int kind = RIFT;
        int first = firstInherited == 2 ? EMBER : ROT, second = firstInherited == 2 ? ROT : EMBER;
        int choice = cadence % 3 == 1 ? first : cadence % 3 == 2 ? second : RIFT;
        if (choice == ROT && (inherited & 1) != 0 || choice == EMBER && (inherited & 2) != 0) kind = choice;
        int[] cells;
        if (kind == RIFT) cells = ray(pos, Dungeon.hero.pos, Level.WIDTH);
        else {
            ArrayList<Integer> marks = new ArrayList<>();
            int aim = Dungeon.hero.pos;
            int offset = kind == EMBER ? 1 : Level.WIDTH;
            for (int i = -1; i <= 1; i++) {
                int cell = aim + i * offset;
                if (valid(cell) && Level.distance(aim, cell) <= 1 && !Level.solid[cell] && lineClear(pos, cell)) marks.add(cell);
            }
            cells = array(marks);
        }
        return warn(kind, pos, cells);
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        strike(cells, kind == RIFT ? 22 : 15, kind == RIFT ? 32 : 23, kind == RIFT);
        if (!isAlive()) return;
        if (kind == ROT) splash(cells, 0xFF718C46);
        else if (kind == RIFT) burst(cells, PurpleParticle.BURST);
        else burst(cells, Speck.LIGHT);
        cadence++;
        if (cadence % 2 == 0) hatch();
        recover(fists().isEmpty() ? 1 : 2);
        cooldown = fists().isEmpty() ? 1 : 3;
    }

    private void hatch() {
        int count = 0;
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof Brood && ((Brood)mob).ownerId == id() && mob.isAlive()) count++;
        if (count >= 3) return;
        for (int offset : Level.NEIGHBOURS8) {
            int cell = pos + offset;
            if (valid(cell) && Level.passable[cell] && !Level.avoid[cell] && Actor.findChar(cell) == null
                    && Level.distance(cell, Dungeon.hero.pos) >= 2 && !dangerous(cell)) {
                Brood larva = new Brood();
                larva.ownerId = id();
                larva.pos = cell;
                GameScene.add(larva);
                return;
            }
        }
    }

    public void inherit(int kind) {
        if (defeated) return;
        if (firstInherited == 0) firstInherited = kind == ROT ? 1 : 2;
        inherited |= kind == ROT ? 1 : 2;
        warning = new int[0];
        recover(2);
        yell(Message.of(this, kind == ROT ? "inherit_rot" : "inherit_fire"));
    }

    @Override
    public void damage(int damage, Object source) {
        int livingFists = fists().size();
        if (damage > 0 && livingFists > 0) {
            damage = Math.max(1, (int)((long)damage * (livingFists >= 2 ? 1 : 3) / 5));
        }
        super.damage(damage, source);
    }

    @Override
    protected void clearSummons() {
        for (Fist fist : fists()) fist.dismiss();
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) if (mob instanceof Brood && ((Brood)mob).ownerId == id()) mob.die(null);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("inherited", inherited);
        b.put("cadence", cadence);
        b.put("spawned", spawned);
        b.put("first_inherited", firstInherited);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        inherited = b.getInt("inherited");
        cadence = b.getInt("cadence");
        spawned = b.getBoolean("spawned");
        firstInherited = b.getInt("first_inherited");
    }

    public abstract static class Fist extends RebornBoss {
        public int ownerId;
        protected int kind;
        {
            defenseSkill = 24;
            EXP = 0;
            state = HUNTING;
            cooldown = 4;
        }

        public RebornYog eye() {
            for (Mob mob : Dungeon.level.mobs) if (mob instanceof RebornYog && mob.id() == ownerId && mob.isAlive()) return (RebornYog)mob;
            return null;
        }

        @Override
        public int attackSkill(Char target) {
            return 34;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(18, 26);
        }

        @Override
        public int dr() {
            return opening() ? 0 : 10;
        }

        @Override
        protected boolean act() {
            if (defeated || !isAlive()) return true;
            if (eye() == null) {
                dismiss();
                return true;
            }
            environmentalEffect();
            return super.act();
        }

        protected void environmentalEffect() { }

        @Override
        protected boolean prepareSkill() {
            if (Level.distance(pos, Dungeon.hero.pos) > (kind == ROT ? 2 : 6)) return false;
            return warn(kind, pos, kind == ROT ? cone(pos, Dungeon.hero.pos, 2) : ray(pos, Dungeon.hero.pos, 6));
        }

        @Override
        protected void resolveSkill(int kind, int[] cells) {
            strike(cells, 22, 30, kind == EMBER);
            if (!isAlive()) return;
            leaveResidue(cells, kind, 3, 5);
            burst(cells, kind == ROT ? Speck.BUBBLE : Speck.LIGHT);
            recover(2);
        }

        @Override
        public void die(Object source) {
            if (defeated) return;
            RebornYog eye = eye();
            dismiss();
            if (eye != null) eye.inherit(kind);
        }

        @Override
        public void storeInBundle(Bundle b) {
            super.storeInBundle(b);
            b.put("eye", ownerId);
        }

        @Override
        public void restoreFromBundle(Bundle b) {
            super.restoreFromBundle(b);
            ownerId = b.getInt("eye");
        }
    }
    public static class Rotting extends Fist {
        private int healedAt = -1;
        {
            name = "rotting fist";
            spriteClass = RottingFistSprite.class;
            HP = HT = 440;
            kind = ROT;
        }

        @Override
        protected Class<? extends Mob> original() {
            return Yog.RottingFist.class;
        }

        @Override
        protected void environmentalEffect() {
            if (healedAt == RebornBoss.heroActions()) return;
            healedAt = RebornBoss.heroActions();
            if (Level.water[pos] && HP < HT) {
                HP = Math.min(HT, HP + 4);
                if (sprite != null) sprite.emitter().burst(ShadowParticle.UP, 2);
            }
        }

        @Override
        public int attackProc(Char enemy, int damage) {
            if (Random.Int(3) == 0) {
                Buff.affect(enemy, Ooze.class);
                if (enemy.sprite != null) enemy.sprite.burst(0xFF000000, 5);
            }
            return super.attackProc(enemy, damage);
        }

        @Override
        public void storeInBundle(Bundle b) {
            super.storeInBundle(b);
            b.put("healed_at", healedAt);
        }

        @Override
        public void restoreFromBundle(Bundle b) {
            super.restoreFromBundle(b);
            healedAt = b.getInt("healed_at");
        }
    }
    public static class BurningHand extends Fist {
        {
            name = "burning fist";
            spriteClass = BurningFistSprite.class;
            HP = HT = 360;
            kind = EMBER;
        }

        @Override
        protected Class<? extends Mob> original() {
            return Yog.BurningFist.class;
        }

        @Override
        protected void environmentalEffect() {
            for (int offset : Level.NEIGHBOURS9) {
                int cell = pos + offset;
                if (valid(cell) && Level.distance(pos, cell) <= 1 && !Level.solid[cell] && !Level.pit[cell])
                    GameScene.add(Blob.seed(cell, 2, Fire.class));
            }
        }
        private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
        static {
            IMMUNITIES.add(Burning.class);
            IMMUNITIES.add(ToxicGas.class);
        }

        @Override
        public HashSet<Class<?>> immunities() {
            return IMMUNITIES;
        }
    }
    public static class Brood extends Yog.Larva {
        public int ownerId;
        private boolean removed;
        {
            state = HUNTING;
        }

        @Override
        public void die(Object source) {
            if (removed) return;
            removed = true;
            HP = 0;
            Actor.remove(this);
            Actor.freeCell(pos);
            Dungeon.level.mobs.remove(this);
            if (sprite != null) sprite.die();
        }

        @Override
        public void storeInBundle(Bundle b) {
            super.storeInBundle(b);
            b.put("eye", ownerId);
        }

        @Override
        public void restoreFromBundle(Bundle b) {
            super.restoreFromBundle(b);
            ownerId = b.getInt("eye");
        }
    }
}
