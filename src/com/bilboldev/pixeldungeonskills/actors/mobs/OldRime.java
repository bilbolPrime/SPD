package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.IceCaves;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.particles.IceParticle;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.levels.IceBossLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class OldRime extends RebornBoss {
    {
        name = "Old Rime";
        spriteClass = IceSprite.Rime.class;
        HP = HT = 450;
        defenseSkill = 18;
        EXP = 30;
        maxLvl = 30;
    }

    @Override protected Class<? extends Mob> original() { return OldRime.class; }
    @Override public int damageRoll() { return Random.NormalIntRange(18, 26); }
    @Override public int attackSkill(Char enemy) { return 30; }
    @Override public int dr() { return 9 - phase * 3; }
    @Override public HashSet<Class<?>> immunities() { return IceMob.IMMUNITIES; }
    @Override public boolean meleeWarning() { return warning.length > 0 && warningKind == SLIME; }

    @Override
    protected void warningSound(int kind, int[] cells) {
        sound(Assets.SND_PUFF, 0.6f, 0.7f, cells);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(kind == DRILL ? Assets.SND_PUFF : Assets.SND_ROCKS, 0.8f, 1, cells);
        if (kind != DRILL) sound(Assets.SND_SHATTER, 0.8f, 0.9f, cells);
    }

    @Override
    public void damage(int damage, Object source) {
        int previousPhase = phase;
        super.damage(damage, source);
        phase = Math.max(phase, Math.min(2, (HT - HP) * 3 / HT));
        if (phase > previousPhase && isAlive() && sprite != null && sprite.parent != null && sprite.visible) {
            sprite.centerEmitter().burst(IceParticle.SHARDS, 12);
            Sample.INSTANCE.play(Assets.SND_SHATTER);
        }
    }

    @Override
    protected boolean prepareSkill() {
        int stage = abilityPhase();
        if (stage >= 2 && Random.Int(3) == 0)
            return warn(DRILL, pos, cone(pos, Dungeon.hero.pos, 3));
        if (stage >= 1 && (!Level.adjacent(pos, Dungeon.hero.pos) || Random.Int(2) == 0)) {
            ArrayList<Integer> cells = new ArrayList<>();
            cells.add(Dungeon.hero.pos);
            for (int offset : Level.NEIGHBOURS8) {
                int cell = Dungeon.hero.pos + offset;
                if (valid(cell) && Level.adjacent(cell, Dungeon.hero.pos) && Level.passable[cell]
                        && lineClear(pos, cell)) cells.add(cell);
            }
            int second = cells.size() > 1 ? cells.get(Random.Int(1, cells.size())) : Dungeon.hero.pos;
            return warn(ORDER, pos, second == Dungeon.hero.pos ? new int[]{second}
                    : new int[]{Dungeon.hero.pos, second});
        }
        return Level.adjacent(pos, Dungeon.hero.pos) && warn(SLIME, pos, new int[]{Dungeon.hero.pos});
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (kind == SLIME) {
            if (Level.adjacent(pos, cells[0])) strike(cells, 28, 38);
            recover(1);
        } else if (kind == ORDER) {
            strike(cells, 18, 27, true);
            recover(1);
        } else {
            strike(cells, 12, 20, true);
            for (int cell : cells) {
                if (Level.solid[cell] || !lineClear(warningOrigin, cell)) continue;
                Char victim = Actor.findChar(cell);
                if (victim != null && victim.isAlive() && victim != this && !ally(victim) && victim.buff(Frostbite.class) == null)
                    Buff.affect(victim, Frostbite.class, 2);
            }
            recover(2);
        }
        if (sprite != null && sprite.parent != null && isAlive() && sprite.visible) sprite.zap(cells[0]);
        for (int cell : cells) {
            if (!valid(cell) || !Dungeon.visible[cell] || Level.solid[cell] || !lineClear(warningOrigin, cell)) continue;
            if (kind == ORDER) {
                CellEmitter.center(cell).burst(IceParticle.SPIKES, 1);
                CellEmitter.center(cell).burst(IceParticle.SHARDS, 6);
            } else if (kind == DRILL && sprite != null && sprite.parent != null) {
                MagicMissile breath = (MagicMissile)sprite.parent.recycle(MagicMissile.class);
                breath.reset(warningOrigin, cell, 100, null);
                breath.size(6);
                breath.pour(IceParticle.MIST, .04f);
            } else if (kind == SLIME && Level.adjacent(pos, cell)) {
                CellEmitter.center(cell).burst(IceParticle.SHARDS, 14);
                CellEmitter.get(cell).burst(IceParticle.MIST, 5);
            }
        }
    }

    @Override
    protected void rewards() {
        if (IceCaves.bossSlain()) return;
        IceCaves.defeatBoss();
        ((IceBossLevel)Dungeon.level).openExit();
        GameScene.bossSlain();
        Hero[] heroes = LocalCoop.active() ? LocalCoop.heroes() : new Hero[]{Dungeon.hero};
        for (Hero hero : heroes) {
            Item reward = IceWeapon.reward(hero.heroClass);
            Heap heap = Dungeon.level.drop(reward, pos, this);
            if (heap.sprite != null) heap.sprite.drop();
        }
    }
}
