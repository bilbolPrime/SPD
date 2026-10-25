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
package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.Collection;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Journal;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.items.EquipableItem;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.quest.DarkGold;
import com.bilboldev.pixeldungeonskills.items.quest.Pickaxe;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Room.Type;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.BlacksmithSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBlacksmith;
import com.bilboldev.pixeldungeonskills.windows.WndQuest;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Blacksmith extends NPC {

	private static final String TXT_GOLD_1 =
		"Hey human! Wanna be useful, eh? Take dis pickaxe and mine me some _dark gold ore_, _15 pieces_ should be enough. " +
		"What do you mean, how am I gonna pay? You greedy...\n" +
		"Ok, ok, I don't have money to pay, but I can do some smithin' for you. Consider yourself lucky, " +
		"I'm the only blacksmith around.";
	private static final String TXT_BLOOD_1 =
		"Hey human! Wanna be useful, eh? Take dis pickaxe and _kill a bat_ wit' it, I need its blood on the head. " +
		"What do you mean, how am I gonna pay? You greedy...\n" +
		"Ok, ok, I don't have money to pay, but I can do some smithin' for you. Consider yourself lucky, " +
		"I'm the only blacksmith around.";
	private static final String TXT2 =
		"Are you kiddin' me? Where is my pickaxe?!";
	private static final String TXT3 =
		"Dark gold ore. 15 pieces. Seriously, is it dat hard?";
	private static final String TXT4 =
		"I said I need bat blood on the pickaxe. Chop chop!";
	private static final String TXT_COMPLETED =
		"Oh, you have returned... Better late dan never.";
	private static final String TXT_GET_LOST =
		"I'm busy. Get lost!";

	private static final String TXT_LOOKS_BETTER	= "custom.generated.entry_your_s_certainly_looks_better_now_820141699f";

	{
		name = "troll blacksmith";
		spriteClass = BlacksmithSprite.class;
	}

	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}

	@Override
	public void interact() {

		sprite.turnTo( pos, Dungeon.hero.pos );

		if (!Quest.given) {

			GameScene.show( new WndQuest(this, Quest.alternative ? Message.key("custom.generated.entry_hey_human_wanna_be_useful_eh_take_dis_pickaxe_an_8917d0831b") : Message.key("custom.generated.entry_hey_human_wanna_be_useful_eh_take_dis_pickaxe_an_597aa8f33b")) {

				@Override
				public void onBackPressed() {
					super.onBackPressed();

					Quest.given = true;
					Quest.completed = false;

					Pickaxe pick = new Pickaxe();
					if (pick.doPickUp( Dungeon.hero )) {
						GLog.i("custom.generated.entry_you_now_have_s_f58f5ff690", Message.itemName(pick));
					} else {
						Dungeon.level.drop( pick, Dungeon.hero.pos ).sprite.drop();
					}
				};
			} );

			Journal.add( Journal.Feature.TROLL );

		} else if (!Quest.completed) {
			if (Quest.alternative) {

				Pickaxe pick = Dungeon.hero.belongings.getItem( Pickaxe.class );
				if (pick == null) {
					tell(Message.key("actors.mobs.npcs.blacksmith.lost_pick"));
				} else if (!pick.bloodStained) {
					tell(Message.key("custom.generated.entry_i_said_i_need_bat_blood_on_the_pickaxe_chop_chop_35b7166235"));
				} else {
					if (pick.isEquipped( Dungeon.hero )) {
						pick.doUnequip( Dungeon.hero, false );
					}
					pick.detach( Dungeon.hero.belongings.backpack );
					tell(Message.key("custom.generated.entry_oh_you_have_returned_better_late_dan_never_12886947b9"));

					Quest.completed = true;
					Quest.reforged = false;
				}

			} else {

				Pickaxe pick = Dungeon.hero.belongings.getItem( Pickaxe.class );
				DarkGold gold = Dungeon.hero.belongings.getItem( DarkGold.class );
				if (pick == null) {
					tell(Message.key("actors.mobs.npcs.blacksmith.lost_pick"));
				} else if (gold == null || gold.quantity() < 15) {
					tell(Message.key("custom.generated.entry_dark_gold_ore_15_pieces_seriously_is_it_dat_hard_c63523ff3e"));
				} else {
					if (pick.isEquipped( Dungeon.hero )) {
						pick.doUnequip( Dungeon.hero, false );
					}
					pick.detach( Dungeon.hero.belongings.backpack );
					gold.detachAll( Dungeon.hero.belongings.backpack );
					tell(Message.key("custom.generated.entry_oh_you_have_returned_better_late_dan_never_12886947b9"));

					Quest.completed = true;
					Quest.reforged = false;
				}

			}
		} else if (!Quest.reforged) {

			GameScene.show( new WndBlacksmith( this, Dungeon.hero ) );

		} else {

			tell(Message.key("actors.mobs.npcs.blacksmith.get_lost"));

		}
	}

	private void tell( Message text ) {
		GameScene.show( new WndQuest(this, text) );
	}

	public static Message verify( Item item1, Item item2 ) {

		if (item1 == item2) {
			return Message.key("custom.generated.entry_select_2_different_items_not_the_same_item_twice_8d69937618");
		}

		if (item1.getClass() != item2.getClass()) {
			return Message.key("custom.generated.entry_select_2_items_of_the_same_type_f9099de249");
		}

		if (!item1.isIdentified() || !item2.isIdentified()) {
			return Message.key("custom.generated.entry_i_need_to_know_what_i_m_working_with_identify_th_273cd0e619");
		}

		if (item1.cursed || item2.cursed) {
			return Message.key("custom.generated.entry_i_don_t_work_with_cursed_items_6c5c655749");
		}

		if (item1.level() < 0 || item2.level() < 0) {
			return Message.key("custom.generated.entry_it_s_a_junk_the_quality_is_too_poor_9011d9c01a");
		}

		if (!item1.isUpgradable() || !item2.isUpgradable()) {
			return Message.key("custom.generated.entry_i_can_t_reforge_these_items_ab02a9e2e0");
		}

		return null;
	}

	public static void upgrade( Item item1, Item item2 ) {

		Item first, second;
		if (item2.level() > item1.level()) {
			first = item2;
			second = item1;
		} else {
			first = item1;
			second = item2;
		}

		Sample.INSTANCE.play( Assets.SND_EVOKE );
		ScrollOfUpgrade.upgrade( Dungeon.hero );
		Item.evoke( Dungeon.hero );

		if (first.isEquipped( Dungeon.hero )) {
			((EquipableItem)first).doUnequip( Dungeon.hero, true );
		}
		first.upgrade();
		GLog.p(TXT_LOOKS_BETTER, Message.itemName(first));
		Dungeon.hero.spendAndNext( 2f );
		Badges.validateItemLevelAquired( first );

		if (second.isEquipped( Dungeon.hero )) {
			((EquipableItem)second).doUnequip( Dungeon.hero, false );
		}
		second.detachAll( Dungeon.hero.belongings.backpack );

		Quest.reforged = true;

		Journal.remove( Journal.Feature.TROLL );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public String description() {
		return
			"This troll blacksmith looks like all trolls look: he is tall and lean, and his skin resembles stone " +
			"in both color and texture. The troll blacksmith is tinkering with unproportionally small tools.";
	}

	public static class Quest {

		private static boolean spawned;

		private static boolean alternative;
		private static boolean given;
		private static boolean completed;
		private static boolean reforged;

		public static void reset() {
			spawned		= false;
			given		= false;
			completed	= false;
			reforged	= false;
		}

		private static final String NODE	= "blacksmith";

		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REFORGED	= "reforged";

		public static void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REFORGED, reforged );
			}

			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	=  node.getBoolean( ALTERNATIVE );
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reforged = node.getBoolean( REFORGED );
			} else {
				reset();
			}
		}

		public static void spawn( Collection<Room> rooms ) {
			if (!spawned && Dungeon.depth > 11 && Random.Int( 15 - Dungeon.depth ) == 0) {

				Room blacksmith = null;
				for (Room r : rooms) {
					if (r.type == Type.STANDARD && r.width() > 4 && r.height() > 4) {
						blacksmith = r;
						blacksmith.type = Type.BLACKSMITH;

						spawned = true;
						alternative = Random.Int( 2 ) == 0;

						given = false;

						break;
					}
				}
			}
		}
	}
}
