/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.items.wands;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Blindness;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.skills.DrainLifeSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.CorpseExplosionSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.CurseSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonArcherSkill;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.BlastParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.items.Bomb;
import com.bilboldev.pixeldungeonskills.items.Corpse;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.NinjaBomb;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.WraithSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Iterator;

public class WandOfMagicCasting extends Wand {

	{
		name = "Wand of Hax";
	}

    public void castSpell(CAST_TYPES casting)
    {
        this.casting = casting;
        MissionScene.selectCell(zapper);
    }

    public void castSpellCost()
    {
        switch (casting)
        {
            case DARK_BOLT:
                Dungeon.hero.MP -= Dungeon.hero.heroSkills.passiveB2.getManaCost();
                Dungeon.hero.heroSkills.passiveB2.castTextYell();
                break;
            case DOMINANCE:
                Dungeon.hero.MP -= Dungeon.hero.heroSkills.passiveB3.getManaCost();
                Dungeon.hero.heroSkills.passiveB3.castTextYell();
                break;
            case SOUL_SPARK:
                Dungeon.hero.MP -= Dungeon.hero.heroSkills.active2.getManaCost();
                Dungeon.hero.heroSkills.active2.castTextYell();
                break;
            case SPARK:
                Skill lastUsed = Dungeon.hero.heroSkills.lastUsed;
                if (lastUsed instanceof RaiseSkeletonSkill
                        || lastUsed instanceof CurseSkill
                        || lastUsed instanceof DrainLifeSkill
                        || lastUsed instanceof CorpseExplosionSkill
                        || lastUsed instanceof RaiseSkeletonArcherSkill) {
                    Dungeon.hero.MP -= lastUsed.getManaCost();
                    lastUsed.castTextYell();
                } else {
                    Dungeon.hero.skillTree.castSpark();
                }
                break;
            case NINJA_BOMB:
                Dungeon.hero.MP -= Dungeon.hero.heroSkills.active2.getManaCost();
                Dungeon.hero.heroSkills.active2.castTextYell();
                break;
        }
    }

    public enum CAST_TYPES  {DARK_BOLT, DOMINANCE, SOUL_SPARK, SPARK, NINJA_BOMB};
    public CAST_TYPES casting = CAST_TYPES.DARK_BOLT;

    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {
                if (LocalCoop.contains(Actor.findChar(target))) return;

                curUser = Dungeon.hero;
                final Wand curWand = Legend.haxWand;
                final int stopCell = Ballistica.cast(curUser.pos, target, true, true);
                final Skill lastUsed = Dungeon.hero.heroSkills.lastUsed;
                final int cell = shouldTargetFirstCorpse(((WandOfMagicCasting) curWand).casting, lastUsed)
                        ? firstCorpseOnCurrentTrace(stopCell)
                        : stopCell;
                curUser.sprite.zap(cell);

                ((WandOfMagicCasting)curWand).castSpellCost();
                curUser.busy();

                curWand.fx(cell, new Callback() {
                    @Override
                    public void call() {
                        curWand.onZap(cell);
                        curWand.wandUsed();
                    }
                });

                Invisibility.dispel();

            }
        }

        @Override
        public Message prompt() {
            return Message.key(Wand.class, "prompt");
        }
    };

	@Override
	protected void onZap( int cell ) {
        Skill lastUsed = Dungeon.hero.heroSkills.lastUsed;
        if (casting == CAST_TYPES.SPARK && (lastUsed instanceof RaiseSkeletonSkill || lastUsed instanceof RaiseSkeletonArcherSkill || lastUsed instanceof CorpseExplosionSkill)) {
            if (!consumeCorpseAt(cell)) {
                GLog.i("custom.generated.entry_nothing_happened_7fbc05693e");
                return;
            }

            if (lastUsed instanceof RaiseSkeletonSkill) {
                raiseSkeleton(cell, ((RaiseSkeletonSkill)lastUsed).level);
                return;
            }

            if (lastUsed instanceof RaiseSkeletonArcherSkill) {
                raiseSkeletonArcher(cell, ((RaiseSkeletonArcherSkill)lastUsed).level);
                return;
            }

            corpseExplode(cell, explosionDamage((CorpseExplosionSkill)lastUsed));
            return;
        }

		Char ch = Actor.findChar( cell );
		if (ch != null) {
            if(ch instanceof NPC && casting != CAST_TYPES.SOUL_SPARK)
                return;

            if(!Bestiary.isBoss(ch) && casting == CAST_TYPES.DARK_BOLT) {
                ch.sprite.emitter().burst(ShadowParticle.CURSE, 6);
                Sample.INSTANCE.play(Assets.SND_CURSED);
                SummonedPet minion = new SummonedPet(WraithSprite.class);
                minion.nameText(Message.key("custom.generated.entry_consumed_soul_756c7537e1"));
                minion.screams = false;
                minion.HT = ch.HT;
                minion.HP = minion.HT;
                minion.defenseSkill = 5;
                minion.pos = cell;
                GameScene.add(minion);
                minion.sprite.alpha(0);
                minion.sprite.parent.add(new AlphaTweener(minion.sprite, 1, 0.15f));

                ch.die(null);
            }
            else  if(casting == CAST_TYPES.DOMINANCE)
            {

                if(!ch.name.equals("Temari") && !ch.name.equals("Necromancer")){
                    ch.sprite.emitter().burst(ShadowParticle.CURSE, 6);
                    Sample.INSTANCE.play(Assets.SND_CURSED);
                    SummonedPet minion = new SummonedPet(ch.sprite.getClass());
                    minion.nameText(Message.key("items.wands.wandofmagiccasting.enslaved", Message.actor(ch)));
                    minion.screams = false;
                    minion.HT = ch.HT;
                    minion.HP = minion.HT;
                    minion.defenseSkill = ch.defenseSkill(Dungeon.hero);
                    minion.pos = cell;
                    GameScene.add(minion);
                    minion.sprite.alpha(0);
                    minion.sprite.parent.add(new AlphaTweener(minion.sprite, 1, 0.15f));
                    ch.sprite.visible = false;
                    ch.die(null);
                }
            }
            else  if(casting == CAST_TYPES.SOUL_SPARK)
            {
                ch.HP = ch.HT;
                ch.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4);
            }
            else  if(casting == CAST_TYPES.SPARK)
            {
                int damage = (int)Dungeon.hero.skillTree.getSparkDamage();

                if (lastUsed instanceof CurseSkill) {
                    int duration = 10 * Math.max(1, ((CurseSkill)lastUsed).level);
                    Buff.prolong( ch, NecroWeakened.class, duration ).setLevel(((CurseSkill)lastUsed).level);
                    ch.sprite.showStatus(CharSprite.NEGATIVE, Message.key("custom.generated.entry_cursed_ec3cb0fdc4"));
                    return;
                }

                if (lastUsed instanceof DrainLifeSkill || lastUsed instanceof CorpseExplosionSkill) {
                    damage *= 2;
                }

                int hpBefore = ch.HP;
                ch.damage(damage, Dungeon.hero);

                int dealt = Math.max(0, hpBefore - Math.max(0, ch.HP));

                if (lastUsed instanceof DrainLifeSkill && dealt > 0 && Dungeon.hero.isAlive()) {
                    Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + dealt);
                    Dungeon.hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.2f, 2);
                }

                if(Dungeon.hero.skillTree.getSparkBlind())
                {
                    Buff.prolong( ch, Blindness.class, Random.Int( 3, 6 ) );
                    ch.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
                    ch.sprite.showStatus(CharSprite.WARNING, Message.key("custom.generated.entry_blinded_1501b64828"));
                }
            }

		} else {

			GLog.i("custom.generated.entry_nothing_happened_7fbc05693e");

		}
	}

	protected void fx( int cell, Callback callback ) {
        if(casting == CAST_TYPES.DARK_BOLT)
		    MagicMissile.shadow( curUser.sprite.parent, curUser.pos, cell, callback, 1 );
        else if(casting == CAST_TYPES.DOMINANCE)
            MagicMissile.shadow(curUser.sprite.parent, curUser.pos, cell, callback, 3);
        else if(casting == CAST_TYPES.SOUL_SPARK)
            MagicMissile.whiteLight(curUser.sprite.parent, curUser.pos, cell, callback);
        else if(casting == CAST_TYPES.SPARK)
            MagicMissile.blueLight(curUser.sprite.parent, curUser.pos, cell, callback);

        if(casting != CAST_TYPES.NINJA_BOMB){
            Sample.INSTANCE.play( Assets.SND_ZAP );
        }

        if(casting == CAST_TYPES.NINJA_BOMB){
            NinjaBomb nb = new NinjaBomb();
            nb.strength = Dungeon.hero.heroSkills.active2.level;
            nb.cast(curUser, cell);
        }
	}

	@Override
	public String desc() {
		return
			"The vile blast of this twisted bit of wood will imbue its target " +
			"with a deadly venom. A creature that is poisoned will suffer periodic " +
			"damage until the effect ends. The duration of the effect increases " +
			"with the level of the staff.";
	}

    private void raiseSkeleton(int cell, int level) {
        trimRaisedSkeletonsToLimit();
        int spawnPos = cell;
        if (Actor.findChar(spawnPos) != null) {
            for (int n : Level.NEIGHBOURS4) {
                int c = cell + n;
                if (c >= 0 && c < Level.LENGTH && Level.passable[c] && Actor.findChar(c) == null) {
                    spawnPos = c;
                    break;
                }
            }
            if (Actor.findChar(spawnPos) != null) {
                return;
            }
        }

        SummonedPet skeleton = new SummonedPet(SummonedPet.PET_TYPES.SKELETON);
        skeleton.spawn(summonPower(level, false));
        if (Dungeon.hero.subClass == HeroSubClass.SPIRIT_BINDER) {
            skeleton.setLifetimeMultiplier(2);
        }
        skeleton.name = "Raised Skeleton";
        skeleton.pos = spawnPos;
        GameScene.add(skeleton);
        Actor.addDelayed(new Pushing(skeleton, Dungeon.hero.pos, spawnPos), -1);
        skeleton.sprite.alpha(0);
        skeleton.sprite.parent.add(new AlphaTweener(skeleton.sprite, 1, 0.15f));
    }

    private void raiseSkeletonArcher(int cell, int level) {
        trimRaisedSkeletonsToLimit();
        int spawnPos = cell;
        if (Actor.findChar(spawnPos) != null) {
            for (int n : Level.NEIGHBOURS4) {
                int c = cell + n;
                if (c >= 0 && c < Level.LENGTH && Level.passable[c] && Actor.findChar(c) == null) {
                    spawnPos = c;
                    break;
                }
            }
            if (Actor.findChar(spawnPos) != null) {
                return;
            }
        }

        SummonedPet skeleton = new SummonedPet(SummonedPet.PET_TYPES.SKELETON_ARCHER);
        skeleton.spawn(summonPower(level, true));
        skeleton.name = "Raised Skeleton Archer";
        skeleton.pos = spawnPos;
        GameScene.add(skeleton);
        Actor.addDelayed(new Pushing(skeleton, Dungeon.hero.pos, spawnPos), -1);
        skeleton.sprite.alpha(0);
        skeleton.sprite.parent.add(new AlphaTweener(skeleton.sprite, 1, 0.15f));
    }

    private void trimRaisedSkeletonsToLimit() {
        int limit = Math.max(1, 1 + Dungeon.hero.skillTree.summoningLimitBonus());
        while (countRaisedSkeletons() >= limit) {
            Mob oldest = findRaisedSkeleton();
            if (oldest == null) {
                break;
            }
            oldest.die(this);
        }
    }

    private Mob findRaisedSkeleton() {
        for (Mob mob : new ArrayList<Mob>(Dungeon.level.mobs)) {
            if (mob instanceof SummonedPet
                    && (!LocalCoop.active() || ((SummonedPet)mob).owner() == Dungeon.hero)
                    && ("Raised Skeleton".equals(mob.name) || "Raised Skeleton Archer".equals(mob.name))) {
                return mob;
            }
        }
        return null;
    }

    private int countRaisedSkeletons() {
        int count = 0;
        for (Mob mob : Dungeon.level.mobs) {
            if (mob instanceof SummonedPet
                    && (!LocalCoop.active() || ((SummonedPet)mob).owner() == Dungeon.hero)
                    && ("Raised Skeleton".equals(mob.name) || "Raised Skeleton Archer".equals(mob.name))) {
                count++;
            }
        }
        return count;
    }

    private void corpseExplode(int cell, int damage) {
        Sample.INSTANCE.play( Assets.SND_BLAST, 2 );
        if (Dungeon.visible[cell]) {
            CellEmitter.center( cell ).burst( BlastParticle.FACTORY, 30 );
        }
        for (int n : Level.NEIGHBOURS9) {
            int c = cell + n;
            if (c < 0 || c >= Level.LENGTH) {
                continue;
            }
            if (Dungeon.visible[c]) {
                CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
            }
            Char nearby = Actor.findChar(c);
            if (nearby != null) {
                nearby.damage(Math.max(1, damage - Random.Int(nearby.dr() + 1)), Bomb.class);
            }
        }
    }

    private int summonPower(int level, boolean archer) {
        int power = Math.max(1, level);
        if (Dungeon.hero.skillTree.summoningLimitBonus() > 0) {
            power *= 2;
        }
        return power;
    }

    private int explosionDamage(CorpseExplosionSkill skill) {
        return 12 + Math.max(1, skill.level) * 6;
    }

    private static boolean shouldTargetFirstCorpse(CAST_TYPES casting, Skill lastUsed) {
        return casting == CAST_TYPES.SPARK
                && (lastUsed instanceof RaiseSkeletonSkill
                || lastUsed instanceof RaiseSkeletonArcherSkill
                || lastUsed instanceof CorpseExplosionSkill);
    }

    private static int firstCorpseOnCurrentTrace(int fallbackCell) {
        for (int i = 1; i < Ballistica.distance; i++) {
            int cell = Ballistica.trace[i];
            if (hasCorpseAt(cell)) {
                return cell;
            }
        }
        return fallbackCell;
    }

    private static boolean hasCorpseAt(int cell) {
        Heap heap = Dungeon.level.heaps.get(cell);
        if (heap == null || heap.items == null) {
            return false;
        }
        for (com.bilboldev.pixeldungeonskills.items.Item item : heap.items) {
            if (item instanceof Corpse) {
                return true;
            }
        }
        return false;
    }

    private boolean consumeCorpseAt(int cell) {
        Heap heap = Dungeon.level.heaps.get(cell);
        if (heap == null || heap.items == null) {
            return false;
        }

        for (Iterator<com.bilboldev.pixeldungeonskills.items.Item> it = heap.items.iterator(); it.hasNext();) {
            com.bilboldev.pixeldungeonskills.items.Item item = it.next();
            if (item instanceof Corpse) {
                if (item.quantity() > 1) {
                    item.quantity(item.quantity() - 1);
                } else {
                    it.remove();
                }
                if (heap.items.isEmpty()) {
                    heap.destroy();
                } else if (heap.sprite != null) {
                    heap.sprite.view( heap.image(), heap.glowing() );
                }
                return true;
            }
        }

        return false;
    }
}
