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
package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Visual;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.features.Door;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.PointF;

public class Pushing extends Actor {

    public static int knockbackCell(Char source, Char target) {
        if (!Level.adjacent(source.pos, target.pos)) return -1;
        return knockbackCell(target, Integer.signum(target.pos % Level.WIDTH - source.pos % Level.WIDTH),
                Integer.signum(target.pos / Level.WIDTH - source.pos / Level.WIDTH));
    }

    private static int knockbackCell(Char target, int dx, int dy) {
        if (target.rooted || !target.isAlive()) return -1;
        int x = target.pos % Level.WIDTH + dx;
        int y = target.pos / Level.WIDTH + dy;
        if (x < 0 || x >= Level.WIDTH || y < 0 || y >= Level.HEIGHT) return -1;
        int cell = x + y * Level.WIDTH;
        if ((!Level.passable[cell] && !Level.avoid[cell]) || Level.pit[cell] || Actor.findChar(cell) != null) return -1;
        if (target instanceof Mob && !((Mob)target).canBeKnockedBackInto(cell)) return -1;
        return cell;
    }

    public static void knockBack(Char source, Char target) {
        knockBack(source.pos, target, 1);
    }

    public static void knockBack(int sourceCell, Char target, int distance) {
        if (!Level.adjacent(sourceCell, target.pos) || !target.isAlive()) return;
        int origin = target.pos;
        int dx = Integer.signum(origin % Level.WIDTH - sourceCell % Level.WIDTH);
        int dy = Integer.signum(origin / Level.WIDTH - sourceCell / Level.WIDTH);
        int destination = origin;
        for (int step = 0; step < distance; step++) {
            int next = knockbackCell(target, dx, dy);
            if (next == -1) break;
            int previous = target.pos;
            Actor.freeCell(previous);
            target.pos = destination = next;
            Actor.occupyCell(target);
            if (Dungeon.level.map[previous] == Terrain.OPEN_DOOR) Door.leave(previous);
            if (Dungeon.level.map[destination] == Terrain.DOOR) Door.enter(destination);
            if (target == Dungeon.hero) Dungeon.hero.interrupt();
            if (!target.flying) {
                if (target instanceof Mob) Dungeon.level.mobPress((Mob)target);
                else Dungeon.level.press(destination, target);
            }
            if (!target.isAlive() || target.pos != destination) {
                if (Actor.findChar(destination) == target) Actor.freeCell(destination);
                if (target.isAlive()) Actor.occupyCell(target);
                break;
            }
        }
        if (destination == origin) return;
        if (target != Dungeon.hero && target.sprite != null) {
            target.sprite.visible = Dungeon.visible[target.pos];
        }

        if (target.isAlive() && target.pos == destination && target.sprite != null) {
            if (target.sprite.parent != null && target.sprite.visible) {
                Actor.addDelayed(new Pushing(target, origin, destination), -1f);
            } else {
                target.sprite.place(destination);
            }
        }
        if (target == Dungeon.hero) Dungeon.observe();
    }

	private CharSprite sprite;
	private int from;
	private int to;

	private Effect effect;

	public Pushing( Char ch, int from, int to ) {
		sprite = ch.sprite;
		this.from = from;
		this.to = to;
	}

	@Override
	protected boolean act() {
		if (sprite != null) {

			if (effect == null) {
				new Effect();
			}
			return false;

		} else {

			Actor.remove( Pushing.this );
			return true;
		}
	}

	public class Effect extends Visual {

		private static final float DELAY = 0.15f;

		private PointF end;

		private float delay;

		public Effect() {
			super( 0, 0, 0, 0 );

			point( sprite.worldToCamera( from ) );
			end = sprite.worldToCamera( to );

			speed.set( 2 * (end.x - x) / DELAY, 2 * (end.y - y) / DELAY );
			acc.set( -speed.x / DELAY, -speed.y / DELAY );

			delay = 0;

			sprite.parent.add( this );
		}

		@Override
		public void update() {
			super.update();

			if ((delay += Game.elapsed) < DELAY) {

				sprite.x = x;
				sprite.y = y;

			} else {

				sprite.point( end );

				killAndErase();
				Actor.remove( Pushing.this );

				next();
			}
		}
	}

}
