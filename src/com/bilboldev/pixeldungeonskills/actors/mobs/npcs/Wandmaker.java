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
import java.util.ArrayList;
import java.util.Locale;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.Journal;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.quest.CorpseDust;
import com.bilboldev.pixeldungeonskills.items.quest.PhantomFish;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfAmok;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfAvalanche;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfDisintegration;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfLightning;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfPoison;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfRegrowth;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfSlowness;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfReach;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.PrisonLevel;
import com.bilboldev.pixeldungeonskills.levels.RebornDungeonBuilder;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.plants.Rotberry;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.WandmakerSprite;
import com.bilboldev.pixeldungeonskills.windows.WndQuest;
import com.bilboldev.pixeldungeonskills.windows.WndWandmaker;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Wandmaker extends NPC {

	{
		name = "old wandmaker";
		spriteClass = WandmakerSprite.class;
	}

	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}

	@Override
	public Message defenseText() {
        return Message.key("actors.mobs.npcs.wandmaker.absorbed");
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
	public void interact() {
		sprite.turnTo( pos, Dungeon.hero.pos );
		Quest.type.handler.interact( this );
	}

	private void tell(String key, Object... args) {
		GameScene.show( new WndQuest(this, Message.key(key, args)) );
	}

	@Override
	public String description() {
		return
			"This old but hale gentleman wears a slightly confused " +
			"expression. He is protected by a magic shield.";
	}

	public static class Quest {

		enum Type {
			ILLEGAL( null ), BERRY( berryQuest ), DUST( dustQuest ), FISH( fishQuest );

			public QuestHandler handler;
			private Type( QuestHandler handler ) {
				this.handler = handler;
			}
		}

		private static Type type;

		private static boolean spawned;
		private static boolean given;
        private static int seedCell = -1;

		public static Wand wand1;
		public static Wand wand2;

		public static void reset() {
			spawned = false;
            given = false;
            seedCell = -1;

			wand1 = null;
			wand2 = null;
		}

		private static final String NODE		= "wandmaker";

		private static final String SPAWNED		= "spawned";
		private static final String TYPE		= "type";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		private static final String WAND1		= "wand1";
		private static final String WAND2		= "wand2";

		public static void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {

				node.put( TYPE, type.toString() );

				node.put( GIVEN, given );
                if (seedCell >= 0) node.put("seed_cell", seedCell);

				node.put( WAND1, wand1 );
				node.put( WAND2, wand2 );
			}

			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
            seedCell = node.contains("seed_cell") ? node.getInt("seed_cell") : -1;

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {

				type = node.getEnum( TYPE, Type.class );
				if (type == Type.ILLEGAL) {
					type = node.getBoolean( ALTERNATIVE ) ? Type.DUST : Type.BERRY;
				}

				given = node.getBoolean( GIVEN );

				wand1 = (Wand)node.get( WAND1 );
				wand2 = (Wand)node.get( WAND2 );
			} else {
				reset();
			}
		}

		public static void spawn( PrisonLevel level, Room room ) {
			if (!spawned && Dungeon.depth > 6 && Random.Int( 10 - Dungeon.depth ) == 0) {

				Wandmaker npc = new Wandmaker();
				do {
					npc.pos = room.random();
				} while (level.map[npc.pos] == Terrain.ENTRANCE || level.map[npc.pos] == Terrain.SIGN);
				level.mobs.add( npc );
				Actor.occupyCell( npc );

				spawned = true;
				switch (Random.Int( 3 )) {
				case 0:
					type = Type.BERRY;
					break;
				case 1:
					type = Type.DUST;
					break;
				case 2:
					type = Type.FISH;
					int water = 0;
					for (int i=0; i < Level.LENGTH; i++) {
						if (Level.water[i]) {
							if (++water > Level.LENGTH / 16) {
								type = Random.Int( 2 ) == 0 ? Type.BERRY : Type.DUST;
								break;
							}
						}
					}
					break;
				}

				given = false;

				switch (Random.Int( 5 )) {
				case 0:
					wand1 = new WandOfAvalanche();
					break;
				case 1:
					wand1 = new WandOfDisintegration();
					break;
				case 2:
					wand1 = new WandOfFirebolt();
					break;
				case 3:
					wand1 = new WandOfLightning();
					break;
				case 4:
					wand1 = new WandOfPoison();
					break;
				}
				wand1.random().upgrade();

				switch (Random.Int( 5 )) {
				case 0:
					wand2 = new WandOfAmok();
					break;
				case 1:
					wand2 = new WandOfBlink();
					break;
				case 2:
					wand2 = new WandOfRegrowth();
					break;
				case 3:
					wand2 = new WandOfSlowness();
					break;
				case 4:
					wand2 = new WandOfReach();
					break;
				}
				wand2.random().upgrade();
			}
		}

        public static void prepareItem(Level level) {
            if (!RunSeed.active() || seedCell >= 0) return;
            boolean here = false;
            for (Mob mob : level.mobs) {
                if (mob instanceof Wandmaker) here = true;
            }
            if (!here) return;
            int[] reachable = RebornDungeonBuilder.distances(level, level.entrance);
            ArrayList<Integer> preferred = new ArrayList<>();
            ArrayList<Integer> free = new ArrayList<>();
            for (int cell = 0; cell < Level.LENGTH; cell++) {
                if (reachable[cell] < 0 || cell == level.entrance || cell == level.exit) continue;
                Heap heap = level.heaps.get(cell);
                if (type == Type.DUST && heap != null && heap.type == Heap.Type.SKELETON) preferred.add(cell);
                if (heap != null || level.plants.get(cell) != null) continue;
                boolean occupied = false;
                for (Mob mob : level.mobs) {
                    if (mob.pos == cell) occupied = true;
                }
                if (occupied) continue;
                free.add(cell);
                if (type == Type.FISH && Level.water[cell]) preferred.add(cell);
            }
            try (Random.Scope random = RunSeed.scope(RunSeed.QUEST, Dungeon.depth)) {
                seedCell = Random.element(preferred.isEmpty() ? free : preferred);
            }
        }

		public static void complete() {
			wand1 = null;
			wand2 = null;
			updateLore();

			Journal.remove( Journal.Feature.WANDMAKER );
		}

		public static void updateLore() {
			if (spawned && given) {
				Lore.quest("wandmaker_" + type.name().toLowerCase(Locale.ROOT), wand1 == null && wand2 == null);
			}
		}
	}

	abstract public static class QuestHandler {

		protected String txtQuest1;
		protected String txtQuest2;

		public void interact( Wandmaker wandmaker ) {
			if (Quest.given) {

				Item item = checkItem();
				if (item != null) {
					GameScene.show( new WndWandmaker( wandmaker, item ) );
				} else {
					wandmaker.tell( txtQuest2, Message.hero(Dungeon.hero) );
				}

			} else {
				wandmaker.tell( txtQuest1 );
				Quest.given = true;
				Quest.updateLore();

				placeItem();

				Journal.add( Journal.Feature.WANDMAKER );
			}
		}

		abstract protected Item checkItem();
		abstract protected void placeItem();
	}

	private static final QuestHandler berryQuest = new QuestHandler() {
		{
			txtQuest1 = "network.partytext.wandmaker_berry_intro";
			txtQuest2 = "custom.generated.entry_any_luck_with_a_rotberry_seed_s_no_don_t_worry_i_03363cc863";
		}

		@Override
		protected Item checkItem() {
			return Dungeon.hero.belongings.getItem( Rotberry.Seed.class );
		}

		@Override
		protected void placeItem() {
			int shrubPos = Quest.seedCell >= 0 ? Quest.seedCell
                    : questCell(RebornDungeonBuilder.distances(Dungeon.level, Dungeon.level.entrance));
			Dungeon.level.plant( new Rotberry.Seed(), shrubPos );
		}
	};

	private static final QuestHandler dustQuest = new QuestHandler() {
		{
			txtQuest1 = "network.partytext.wandmaker_dust_intro";
			txtQuest2 = "custom.generated.entry_any_luck_with_corpse_dust_s_bone_piles_are_the_m_efd412a68e";
		}

		@Override
		protected Item checkItem() {
			return Dungeon.hero.belongings.getItem( CorpseDust.class );
		}

		@Override
		protected void placeItem() {
			if (Quest.seedCell >= 0) {
                Heap heap = Dungeon.level.drop(new CorpseDust(), Quest.seedCell);
                heap.type = Heap.Type.SKELETON;
                heap.sprite.link();
                return;
            }
            int[] reachable = RebornDungeonBuilder.distances(Dungeon.level, Dungeon.level.entrance);
			ArrayList<Heap> candidates = new ArrayList<Heap>();
			for (Heap heap : Dungeon.level.heaps.values()) {
				if (heap.type == Heap.Type.SKELETON && !Dungeon.visible[heap.pos] && reachable[heap.pos] >= 0) {
					candidates.add( heap );
				}
			}

			if (candidates.size() > 0) {
				Random.element( candidates ).drop( new CorpseDust() );
			} else {
				int pos = questCell(reachable);

				Heap heap = Dungeon.level.drop( new CorpseDust(), pos );
				heap.type = Heap.Type.SKELETON;
				heap.sprite.link();
			}
		}
	};

	private static final QuestHandler fishQuest = new QuestHandler() {
		{
			txtQuest1 = "network.partytext.wandmaker_fish_intro";
			txtQuest2 = "custom.generated.entry_any_luck_with_a_phantom_fish_s_you_may_want_to_t_cf21ee679e";
		}

		@Override
		protected Item checkItem() {
			return Dungeon.hero.belongings.getItem( PhantomFish.class );
		}

		@Override
		protected void placeItem() {
			if (Quest.seedCell >= 0) {
                Heap heap = Dungeon.level.drop(new PhantomFish(), Quest.seedCell);
                if (Level.water[Quest.seedCell]) heap.type = Heap.Type.HIDDEN;
                heap.sprite.link();
                return;
            }
            int[] reachable = RebornDungeonBuilder.distances(Dungeon.level, Dungeon.level.entrance);
			ArrayList<Integer> water = new ArrayList<>();
			for (int cell = 0; cell < Level.LENGTH; cell++) {
				if (reachable[cell] >= 0 && Level.water[cell] && Dungeon.level.heaps.get(cell) == null)
					water.add(cell);
			}
			if (water.isEmpty()) {
				Dungeon.level.drop(new PhantomFish(), questCell(reachable));
			} else {
				Heap heap = Dungeon.level.drop(new PhantomFish(), Random.element(water));
				heap.type = Heap.Type.HIDDEN;
				heap.sprite.link();
			}
		}
	};

	private static int questCell(int[] reachable) {
		ArrayList<Integer> hidden = new ArrayList<>();
		ArrayList<Integer> visible = new ArrayList<>();
		for (int cell = 0; cell < Level.LENGTH; cell++) {
			if (reachable[cell] < 0 || Dungeon.level.heaps.get(cell) != null
					|| Dungeon.level.plants.get(cell) != null || Actor.findChar(cell) != null) continue;
			(Dungeon.visible[cell] ? visible : hidden).add(cell);
		}
		if (!hidden.isEmpty()) return Random.element(hidden);
		if (!visible.isEmpty()) return Random.element(visible);
		return Dungeon.hero.pos;
	}
}
