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
package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.pixeldungeonskills.messages.Message;

import java.util.ArrayList;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.levels.DailyHoardLevel;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Gold extends Item {

	int recovered;

	private static final String TXT_COLLECT	= "Collect gold coins to spend them later in a shop.";
	private static final String TXT_INFO	= "A pile of %d gold coins. " + TXT_COLLECT;
	private static final String TXT_INFO_1	= "One gold coin. " + TXT_COLLECT;

	{
		name = "gold";
		image = ItemSpriteSheet.GOLD;
		stackable = true;
	}

	public Gold() {
		this( 1 );
	}

	public Gold( int value ) {
		this.quantity = value;
	}

	public static Gold recovered(int value) {
		Gold gold = new Gold(value);
		gold.recovered = value;
		return gold;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		return new ArrayList<String>();
	}

	@Override
	public boolean doPickUp( Hero hero ) {

		int found = quantity - recovered;
		int bonus = hero.skillTree.getLootBonus(found);
		Dungeon.gold += quantity + bonus;
		if (found > 0) {
			Statistics.goldCollected += found + bonus;
			Badges.validateGoldCollected();
		}

		GameScene.pickUp( this );
		if (Dungeon.level instanceof DailyHoardLevel) ((DailyHoardLevel)Dungeon.level).collectGold(this, hero);
		if (bonus == 0) {
			hero.sprite.showStatus( CharSprite.NEUTRAL, Message.key("custom.generated.entry_d_dbf0d0f640", quantity));
		} else {
			hero.sprite.showStatus( CharSprite.NEUTRAL, Message.key("custom.generated.entry_d_d_loot_d4156b722a", quantity, bonus));
		}

		hero.spendAndNext( TIME_TO_PICK_UP );

		Sample.INSTANCE.play( Assets.SND_GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );

		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String info() {
		return desc();
	}

	@Override
	public Item random() {
		quantity = Random.Int( 20 + Dungeon.depth * 10, 40 + Dungeon.depth * 20 );
		return this;
	}

	private static final String VALUE	= "value";
	private static final String RECOVERED = "recovered";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VALUE, quantity );
		if (recovered > 0) bundle.put( RECOVERED, recovered );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		quantity = bundle.getInt( VALUE );
		recovered = bundle.getInt( RECOVERED );
	}
}
