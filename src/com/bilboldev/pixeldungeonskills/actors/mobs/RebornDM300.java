package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.noosa.Camera;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfThorns;
import com.bilboldev.pixeldungeonskills.levels.CavesBossLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.DM300Sprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class RebornDM300 extends RebornBoss {
    private int heat, heatedAt = -1, impact = -1;
    private boolean ventPending;
    {
        name = "DM-300";
        spriteClass = DM300Sprite.class;
        HP = HT = 352;
        defenseSkill = 18;
        EXP = 30;
        loot = new RingOfThorns().random();
        lootChance = 0.333f;
        state = HUNTING;
    }

    @Override
    protected Class<? extends Mob> original() {
        return DM300.class;
    }

    @Override
    public int attackSkill(Char target) {
        return 28;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(17, 23);
    }

    @Override
    public int dr() {
        return opening() ? 0 : 10;
    }

    public int heat() {
        return heat;
    }

    @Override
    protected boolean controlled() {
        return rooted || super.controlled();
    }

    @Override
    protected boolean act() {
        phase = HP * 2 <= HT ? 1 : phase;
        if (heatedAt != RebornBoss.heroActions() && !controlled() && !opening()
                && !ventPending && warning.length == 0 && seesHero()) {
            heatedAt = RebornBoss.heroActions();
            heat = Math.min(5, heat + 1);
        }
        return super.act();
    }

    @Override
    public int attackProc(Char target, int damage) {
        heat = Math.min(5, heat + 1);
        return super.attackProc(target, damage);
    }

    @Override
    public void move(int step) {
        super.move(step);
        if (isAlive() && Dungeon.level.map[pos] == Terrain.INACTIVE_TRAP && HP < HT) {
            HP += Random.Int(1, HT - HP);
            if (sprite != null) sprite.emitter().burst(ElmoParticle.FACTORY, 5);
            if (Dungeon.visible[pos] && Dungeon.hero.isAlive()) GLog.n(this.getClass(), "repair");
        }
    }

    @Override
    protected boolean prepareSkill() {
        if (ventPending) {
            ArrayList<Integer> cells = new ArrayList<>();

            int[] jet = ray(pos, Dungeon.hero.pos, 6);
            if (!contains(jet, Dungeon.hero.pos)) return false;
            for (int cell : jet) cells.add(cell);

            if (phase > 0) for (int cell : cone(pos, Dungeon.hero.pos, 3))
                if (!cells.contains(cell)) cells.add(cell);
            return warn(STEAM, pos, array(cells));
        }
        if (heat < (phase == 0 ? 3 : 2)) return false;
        int[] cells = ray(pos, Dungeon.hero.pos, 8);
        if (!contains(cells, Dungeon.hero.pos)) return false;
        int end = cells[cells.length - 1];
        int previous = cells.length > 1 ? cells[cells.length - 2] : pos;
        int next = end + (end - previous);
        impact = Dungeon.level instanceof CavesBossLevel
                && ((CavesBossLevel)Dungeon.level).isRebornButtress(next) ? next : -1;

        if (impact < 0) for (int i = 0; i < cells.length; i++) if (cells[i] == Dungeon.hero.pos) {
            cells = Arrays.copyOf(cells, i + 1);
            break;
        }
        return warn(DRILL, pos, cells);
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (kind == DRILL) {
            strike(cells, 23, 32);
            if (!isAlive()) return;
            int destination = pos;
            ArrayList<Integer> trail = new ArrayList<>();
            trail.add(pos);
            for (int cell : cells) {
                if (!valid(cell) || Level.solid[cell] || Actor.findChar(cell) != null) break;
                destination = cell;
                trail.add(cell);
            }
            if (destination != pos && !rooted) {
                int old = pos;
                move(destination);
                Actor.freeCell(old);
                Actor.occupyCell(this);
                if (sprite != null) sprite.move(old, pos);
                Dungeon.observe();
            }
            boolean crashed = impact >= 0 && Level.adjacent(pos, impact)
                    && Dungeon.level instanceof CavesBossLevel
                    && ((CavesBossLevel)Dungeon.level).breakRebornButtress(impact);
            heat = 0;
            ventPending = !crashed;
            burst(cells, Speck.ROCK);
            if (visibleEffect(cells)) {
                CellEmitter.get(pos).burst(Speck.factory(Speck.ROCK), crashed ? 12 : 7);
                Camera.main.shake(crashed ? 3 : 2, crashed ? .7f : .35f);
            }

            for (int cell : trail) if (cell == pos || !rooted) gas(cell, 10);
            gas(pos, 30);
            if (crashed) recover(4);
            else {

                openingUntil = RebornBoss.heroActions();
                cooldown = 0;
            }
        } else {
            strike(cells, 14, 22, true);
            if (!isAlive()) return;
            burst(cells, Speck.STEAM);
            for (int cell : cells) if (valid(cell) && !Level.solid[cell] && lineClear(warningOrigin, cell)) gas(cell, 10);
            gas(pos, 30);
            ventPending = false;
            heat = 0;
            recover(1);
            cooldown = phase == 0 ? 3 : 2;
        }
    }

    private void gas(int cell, int amount) {
        GameScene.add(Blob.seed(cell, amount, ToxicGas.class));
    }

    @Override
    public void interruptSkill() {
        super.interruptSkill();
        heat = 0;
        ventPending = false;
    }
    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
    static {
        IMMUNITIES.add(ToxicGas.class);
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("heat", heat);
        b.put("heated_at", heatedAt);
        b.put("impact", impact);
        b.put("vent_pending", ventPending);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        heat = b.getInt("heat");
        heatedAt = b.getInt("heated_at");
        impact = b.getInt("impact");
        ventPending = b.getBoolean("vent_pending");
    }
}
