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

import com.bilboldev.pixeldungeonskills.network.PartyEffects;

import android.graphics.Color;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.tweeners.PosTweener;
import com.bilboldev.noosa.tweeners.Tweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.utils.PointF;

public class Stab extends ItemSprite implements Tweener.Listener {
	private static final int SIZE	= 16;

	private enum Phase {
		FADE_IN, STATIC, FADE_OUT
	}

	private static final float FADE_IN_TIME		= 0.2f;
	private static final float STATIC_TIME		= 0.4f;
	private static final float FADE_OUT_TIME	= 0.2f;

	private static final float ALPHA	= 0.6f;

	private int color;

    private float fromX, fromY, toX, toY;
    private float motionStartX, motionStartY, motionDX, motionDY;

	private Phase phase;
	private float duration;
	private float passed;

	public Stab(Item item) {
		this(item.image());
	}

	public Stab(int image) {
		super( image, null );
		originToCenter();
        color = Color.WHITE;

		phase = Phase.FADE_IN;
		duration = FADE_IN_TIME;
		passed = 0;
	}

	@Override
	public void update() {
		super.update();

		switch (phase) {
		case FADE_IN:

			alpha( passed / duration * ALPHA );
			scale.set( 0.2f );
			break;
		case STATIC:
			tint( color, passed / duration * 0.8f );
            scale.set( 0.8f );
			break;
		case FADE_OUT:
			alpha( (1 - passed / duration) * ALPHA );
			scale.set(  0.2f );
			break;
		}

		if ((passed += Game.elapsed) > duration) {
			switch (phase) {
			case FADE_IN:
				phase = Phase.STATIC;
				duration = STATIC_TIME;
				break;
			case STATIC:
				phase = Phase.FADE_OUT;
				duration = FADE_OUT_TIME;
				break;
			case FADE_OUT:
				kill();
				break;
			}

			passed = 0;
		}
	}

    private void rotateEffect(Char source, Char target)
    {
        PointF src = DungeonTilemap.tileToWorld( source.pos );
        PointF dest = DungeonTilemap.tileToWorld( target.pos );
        PointF d = PointF.diff( dest, src );
        angle = 135 - (float)(Math.atan2( d.x, d.y ) / 3.1415926 * 180);

        x = source.sprite.x;
        y = source.sprite.y;

        int width = Dungeon.level.width();
        fromX = source.pos % width + 0.5f + source.sprite.movementOffsetX();
        fromY = source.pos / width + 0.5f + source.sprite.movementOffsetY();
        toX = target.pos % width + 0.5f + (target.sprite == null ? 0 : target.sprite.movementOffsetX());
        toY = target.pos / width + 0.5f + (target.sprite == null ? 0 : target.sprite.movementOffsetY());
        motionStartX = x;
        motionStartY = y;
        motionDX = dest.x - x;
        motionDY = dest.y - y;

        PosTweener tweener = new PosTweener( this, dest, 0.6f );
        tweener.listener = this;
        source.sprite.parent.add( tweener );
    }

    private double travelProgress() {
        double lengthSquared = motionDX * motionDX + motionDY * motionDY;
        return lengthSquared == 0 ? 1 : Math.max(0, Math.min(1,
                ((x - motionStartX) * motionDX + (y - motionStartY) * motionDY) / lengthSquared));
    }

    public double worldX() {
        return fromX + (toX - fromX) * travelProgress();
    }

    public double worldY() {
        return fromY + (toY - fromY) * travelProgress();
    }

    public double directionX() {
        return toX - fromX;
    }

    public double directionY() {
        return toY - fromY;
    }

	public static void show( Char ch, Char ch2, Item item ) {

		if (!ch.sprite.visible) {
			return;
		}
		PartyEffects.stab(ch.pos, ch2.pos, item.image());

		Stab sprite = new Stab( item );
        sprite.rotateEffect(ch, ch2);
		ch.sprite.parent.add( sprite );
	}

    public void visualPath(int from, int to) {
        PointF source = DungeonTilemap.tileToWorld(from);
        PointF destination = DungeonTilemap.tileToWorld(to);
        point(source);
        PointF delta = PointF.diff(destination, source);
        angle = 135 - (float)Math.toDegrees(Math.atan2(delta.x, delta.y));
        int width = Dungeon.level.width();
        fromX = from % width + .5f;
        fromY = from / width + .5f;
        toX = to % width + .5f;
        toY = to / width + .5f;
        motionStartX = x;
        motionStartY = y;
        motionDX = delta.x;
        motionDY = delta.y;
        PosTweener tweener = new PosTweener(this, destination, .6f);
        tweener.listener = this;
        parent.add(tweener);
    }

    @Override
    public void onComplete( Tweener tweener ) {
        kill();
    }
}
