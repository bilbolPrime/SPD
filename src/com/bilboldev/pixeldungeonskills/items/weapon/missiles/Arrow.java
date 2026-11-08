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
package com.bilboldev.pixeldungeonskills.items.weapon.missiles;

import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.actors.buffs.SnipersMark;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Paralysis;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.BlastParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.WeaponEffect;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class Arrow extends MissileWeapon {

	{
		name = "arrow";
		image = ItemSpriteSheet.Arrow;

        stackable = true;
	}

	public Arrow() {
		this( 1 );
	}

	public Arrow(int number) {
		super();
		quantity = number;
	}

    @Override
    public void cast( final Hero user, int dst ) {
        if (!canShoot(user)) return;
        curUser = user;
        curItem = this;
        if (user.talents() != null) {
            if (!RebornChallenges.targetAllowed(user, dst)) return;
            int targets = RebornChallenges.active(RebornChallenges.POINT_BLANK) ? 0 : user.talents().arrowPenetration();
            if (targets > 0 && dst != user.pos && Dungeon.level.insideMap(dst)) castPiercing(user, dst, targets);
            else super.cast(user, dst);
            return;
        }
        if(user.heroSkills.passiveB3.passThroughTargets(false) > 0)
        {
            castSPD(user, dst, user.heroSkills.passiveB3.passThroughTargets(true));
        }
        else
            super.cast(user, dst);
    }

    private void castPiercing(Hero user, int dst, int targets) {
        Level level = Dungeon.level;
        Ballistica.cast(user.pos, dst, true, false);
        ArrayList<Integer> path = new ArrayList<>();
        Char first = null;
        for (int i = 1; i < Ballistica.distance; i++) {
            int cell = Ballistica.trace[i];
            Char target = Actor.findChar(cell);
            if (target != null && (!(target instanceof Mob) || !((Mob)target).hostile)) break;
            path.add(cell);
            if (target != null) {
                if (first == null) first = target;
                if (targets-- == 0) break;
            }
        }
        if (path.isEmpty()) return;
        int end = path.get(path.size() - 1);
        float delay = TIME_TO_THROW * speedFactor(user);
        SnipersMark mark = user.buff(SnipersMark.class);
        if (first != null && mark != null) {
            if (mark.object == first.id()) delay *= 0.5f;
            user.remove(mark);
        }
        final float shotDelay = delay;
        user.sprite.zap(end);
        user.busy();
        QuickSlot.target(this, first);
        Sample.INSTANCE.play(Assets.SND_MISS, 0.6f, 0.6f, 1.5f);
        ((MissileSprite)user.sprite.parent.recycle(MissileSprite.class)).reset(user.pos, end, this, () -> {
            if (Dungeon.level != level || !user.isAlive()) return;
            LocalCoop.withHero(user, () -> {
                if (!canShoot(user)) {
                    user.next();
                    return;
                }
                Arrow arrow = (Arrow)detach(user.belongings.backpack);
                if (arrow == null) {
                    user.next();
                    return;
                }
                curUser = user;
                curItem = arrow;
                arrow.droppedByHero = true;
                boolean hit = false;
                int last = user.pos;
                for (int cell : path) {
                    if (!user.isAlive() || (!Level.passable[cell] && !Level.avoid[cell]) || Level.losBlocking[cell]) break;
                    Char target = Actor.findChar(cell);
                    if (target != null && (!(target instanceof Mob) || !((Mob)target).hostile)) break;
                    last = cell;
                    if (target != null && target.isAlive()) hit |= user.shoot(target, arrow);
                }
                if (!hit) arrow.miss(last);
                user.talents().arrowLanded();
                if (user.talents().doubleShot(user.isAlive() && user.belongings.bow != null
                        && quantity > 0 && user.belongings.backpack.contains(this))) {
                    cast(user, dst);
                } else if (user.isAlive()) {
                    user.spendAndNext(shotDelay);
                }
            });
        });
    }

    @Override
    public void doThrow(Hero hero) {
        if (!canShoot(hero)) return;
        final Bow bow = hero.belongings.bow;
        GameScene.selectCell(new CellSelector.Listener() {
            @Override public void onSelect(Integer cell) {
                if (cell == null || hero.belongings.bow != bow || LocalCoop.contains(Actor.findChar(cell))) return;
                cast(hero, cell);
            }

            @Override public Message prompt() {
                return Message.key(Item.class, "prompt");
            }
        });
    }

    @Override
    public void castSPD(Hero user, int dst, int skip) {
        if (canShoot(user)) super.castSPD(user, dst, skip);
    }

    private boolean canShoot(Hero hero) {
        if (!hero.isAlive() || quantity <= 0 || !hero.belongings.backpack.contains(this)) {
            QuickSlot.cancel();
            return false;
        }
        if (hero.belongings.bow == null) {
            QuickSlot.cancel();
            GLog.w(Bow.class, "equip");
            return false;
        }
        return true;
    }

    @Override
    protected void onThrow( int cell ) {

        if(Dungeon.hero.heroSkills.passiveB3.multiTargetActive == false || Dungeon.hero.heroSkills.active3.active == true) {

            boolean bombArrow = Dungeon.hero.skillTree.arrowToBomb();
            if(bombArrow) {
            if (Level.pit[cell]) {
                super.onThrow(cell);
            } else {
                Sample.INSTANCE.play(Assets.SND_BLAST, 2);

                if (Dungeon.visible[cell]) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                Hero archer = curUser;
                WeaponEffect prefix = archer != null && archer.belongings.bow != null
                        ? archer.belongings.bow.weaponEffect : null;
                boolean terrainAffected = false;
                for (int n : Level.NEIGHBOURS9) {
                    int c = cell + n;
                    if (c >= 0 && c < Level.LENGTH) {
                        if (Dungeon.visible[c]) {
                            CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                        }

                        if (Level.flamable[c]) {
                            Level.set(c, Terrain.EMBERS);
                            GameScene.updateMap(c);
                            terrainAffected = true;
                        }

                        Char ch = Actor.findChar(c);
                        if (ch != null) {
                            int dmg = Dungeon.hero.skillTree.arrowToBombDamage() - Random.Int(ch.dr());
                            if (dmg > 0) {
                                explosionDamage(ch, dmg, archer, c == cell ? prefix : null);
                                if (ch.isAlive()) {
                                    Buff.prolong(ch, Paralysis.class, Dungeon.hero.skillTree.arrowToBombParalysis());
                                }
                            }
                        }
                    }
                }

                if (terrainAffected) {
                    Dungeon.observe();
                }
            }
        }

         else   super.onThrow(cell);
            return;
        }

        Ballistica.distance = Math.min( Ballistica.distance, Level.distance(Dungeon.hero.pos, cell) );

        ArrayList<Char> chars = new ArrayList<Char>();

        for (int i=1; i < Ballistica.distance + 1; i++) {

            int c = Ballistica.trace[i];

            Char ch;
            if ((ch = Actor.findChar(c)) != null) {
                chars.add( ch );
            }
        }

        GLog.i("custom.generated.manual.s_targets", chars.size());
        boolean hitOne = false;
        for (Char ch : chars) {
            if (!curUser.shootThrough(ch, this)) {

            }
            else
                hitOne = true;
        }

        if(hitOne == false)
            miss( cell );

        Dungeon.hero.rangedWeapon = null;
    }

    protected void explosionDamage(Char target, int damage, Hero archer, WeaponEffect prefix) {
        if (prefix != null && !prefix.roll(archer, target)) prefix = null;
        if (prefix != null) damage *= prefix.damageMultiplier();
        int health = target.HP;
        target.damage(damage, this, CombatTextIcon.EXPLOSION);
        if (prefix != null) {
            prefix.onHit(archer, target, Math.max(0, health - Math.max(0, target.HP)));
        }
    }

    public void arrowEffect(Char attacker, Char defender)
    {

    }

	@Override
	public String desc() {
		return
			"Arrows are more powerful and accurate than darts, but they require an equipped bow.";
	}

	@Override
	public Item random() {
        quantity = Random.Int(5, 20);
		return this;
	}

	@Override
	public int price() {
		return quantity * 5;
	}

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (hero.belongings.bow != null) {
            if (!actions.contains( AC_THROW )) {
                actions.add( AC_THROW );
            }
        } else {
            actions.remove( AC_THROW );
        }
        actions.remove(AC_EQUIP);

        return actions;
    }

    @Override
    public int min(){return 3;}

    @Override
    public int max(){return 5;}

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );

        return info.toString();
    }

}
