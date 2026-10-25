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
package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.input.GameAction;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.windows.WndPartner;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.skills.CurrentSkills;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.gauntlet.GauntletArena;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.windows.WndCatalogus;
import com.bilboldev.pixeldungeonskills.windows.WndHero;
import com.bilboldev.pixeldungeonskills.windows.WndInfoCell;
import com.bilboldev.pixeldungeonskills.windows.WndInfoItem;
import com.bilboldev.pixeldungeonskills.windows.WndInfoMob;
import com.bilboldev.pixeldungeonskills.windows.WndInfoPlant;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndMerc;
import com.bilboldev.pixeldungeonskills.windows.WndMercs;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryRecruitment;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndRatKing;
import com.bilboldev.pixeldungeonskills.windows.WndSkill;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.pixeldungeonskills.windows.WndTradeItem;

public class Toolbar extends Component {

	private Tool btnWait;
    private Tool btnSkill;
    private Tool btnMerc;
    private Tool btnLastUsed;

    private Tool btnKing;
	private Tool btnInfoSearch;
	private Tool btnInventory;
	private Tool[] quickSlots;

	private PickedUpItem pickedUp;

	private boolean lastEnabled = true;

    public static boolean tapAgainToSearch = false;

	private static Toolbar instance;

	public Toolbar() {
		super();

		instance = this;

		height = btnInventory.height();
	}

	@Override
	protected void createChildren() {

		add( btnWait = new Tool( 0, 7, 20, 25 ) {
			@Override
			protected void onClick() {
				Dungeon.hero.rest( false );
			};
			protected boolean onLongClick() {
				Dungeon.hero.rest( true );
				return true;
			};
		} );

        add( btnSkill = new Tool( 20, 7, 20, 25 ) {
			private SkillsIndicator skillsIndicator;
            @Override
            protected void onClick() {
				if (PartySession.openSkills()) return;
				GameScene.show(new WndSkillsNew(null, null));
            };
            protected boolean onLongClick() {
				if (PartySession.openSkills()) return true;
				GameScene.show(new WndSkillsNew(null, null));
                return true;
            };

			@Override
			protected void createChildren() {
				super.createChildren();
				skillsIndicator = new SkillsIndicator();
				add( skillsIndicator );
			};
			@Override
			protected void layout() {
				super.layout();
				skillsIndicator.fill( this );
			};
        });

        add( btnKing = new Tool( 135, 7, 21, 25 ) {
            @Override
            protected void onClick() {
                WndRatKing.show(WndRatKing.Mode.NORMAL);
            };
            protected boolean onLongClick() {
                WndRatKing.show(WndRatKing.Mode.NORMAL);
                return true;
            };
        });

            add(btnLastUsed = new Tool(40, 7, 20, 25) {
				private SkillsIndicator skillsIndicator;
                @Override
                protected void onClick() {
                    final Hero user = Dungeon.hero;
                    if (PartySession.input(user, "last_skill", 0)) return;
                    if (!LocalCoop.defer(user, () -> user.heroSkills.showLastUsed())) user.heroSkills.showLastUsed();
                }

                ;

                protected boolean onLongClick() {
                    final Hero user = Dungeon.hero;
                    if (PartySession.input(user, "last_skill", 0)) return true;
                    if (!LocalCoop.defer(user, () -> user.heroSkills.showLastUsed())) user.heroSkills.showLastUsed();
                    return true;
                }

				@Override
				protected void createChildren() {
					super.createChildren();
					skillsIndicator = new SkillsIndicator();
					skillsIndicator.skillCooldown = true;
					add( skillsIndicator );
				};
				@Override
				protected void layout() {
					super.layout();
					skillsIndicator.fill( this );
				};
            });

        add(btnMerc = LocalCoop.active() ? new PartnerTool() : new Tool(252, 7, 20, 25) {
            @Override
            protected void onClick() {
                if(Dungeon.hero.hiredMerc == null)
                {
                    if (!Dungeon.rules().allowsOnDemandHiring()) {
                        GameScene.show(new WndMessage(MercenaryRecruitment.noMercMessage()));
                        return;
                    }

                    GameScene.show(new WndMercs(WndMercs.Mode.ALL));
                }
                else
                {
                   GameScene.show(new WndMerc(null, null));
                }

            };
            protected boolean onLongClick() {
                if (Dungeon.hero.hiredMerc == null && !Dungeon.rules().allowsOnDemandHiring()) {
                    onClick();
                    return true;
                }
                if(Dungeon.hero.hiredMerc == null)
					GameScene.show(new WndSkill(null, CurrentSkills.mercMenu));
                else
                    GameScene.show(new WndMerc(null, null));
                return true;
            };});

		add( btnInfoSearch = new Tool( 107, 7, 20, 25 ) {
			@Override
			protected void onClick() {
                if(tapAgainToSearch == false) {
                    GameScene.selectCell(informer);
                }
                else
                {
                    Dungeon.hero.search(true);
                }
                tapAgainToSearch = !tapAgainToSearch;
			}
            @Override
            protected boolean onLongClick() {
                Dungeon.hero.search(true); return true;
            }
		} );

		add( btnInventory = new Tool( 60, 7, 23, 25 ) {
			private GoldIndicator gold;
			@Override
			protected void onClick() {
				if (PartySession.openInventory()) return;
				GameScene.show( new WndBag( Dungeon.hero.belongings.backpack, null, WndBag.Mode.ALL, null ) );
			}
			protected boolean onLongClick() {
				GameScene.show( new WndCatalogus() );
				return true;
			};
			@Override
			protected void createChildren() {
				super.createChildren();
				gold = new GoldIndicator();
				add( gold );
			};
			@Override
			protected void layout() {
				super.layout();
				gold.fill( this );
			};
		} );

		quickSlots = new Tool[QuickSlot.MAX_SLOTS];
		for (int i = 0; i < quickSlots.length; i++) {
			quickSlots[i] = new QuickslotTool(83, 7, 22, 25, i);
			add(quickSlots[i]);
		}

		add( pickedUp = new PickedUpItem() );

		btnWait.bindAction(GameAction.WAIT);
		btnWait.bindAlternateAction(GameAction.REST);
		btnSkill.bindAction(GameAction.SKILLS);
		btnLastUsed.bindAction(GameAction.LAST_SKILL);
		btnKing.bindAction(GameAction.RAT_KING);
		btnMerc.bindAction(GameAction.MERCENARIES);
		btnInfoSearch.bindAction(GameAction.EXAMINE);
		btnInventory.bindAction(GameAction.INVENTORY);
		updateModeTools();
	}

	@Override
	protected void layout() {
		btnWait.setPos( x, y );

		btnInfoSearch.setPos( 0, 70 );
        btnMerc.setPos( 0, 40 );
        btnSkill.setPos(btnWait.right(), y);
        btnKing.setPos(0, 100);
        btnLastUsed.setPos(btnSkill.right(), y);
		int count = Math.min(Presentation.quickSlots(), maxQuickSlots(width, camera().height));
		float right = x + width;
		for (int i = 0; i < quickSlots.length; i++) {
			Tool slot = quickSlots[i];
			slot.visible = i < count;
			slot.enable(slot.visible && lastEnabled);
			if (slot.visible) {
				right -= slot.width();
				slot.setPos(right, y);
			}
		}
		btnInventory.setPos(right - btnInventory.width(), y);
	}

	public void layoutViewButton(Button button) {
		if (camera().width >= camera().height) {
			button.setRect(btnLastUsed.right(), btnLastUsed.top(), btnLastUsed.width(), btnLastUsed.height());
		} else {
			float gap = btnKing.top() - btnInfoSearch.bottom();
			button.setRect(btnKing.left(), btnKing.bottom() + gap, btnKing.width(), btnKing.height());
		}
	}

	@Override
	public void update() {
		super.update();

		boolean enabled = LocalCoop.active() ? LocalCoop.acceptsInput() && Dungeon.hero.isAlive() : Dungeon.hero.ready;
		if (LocalCoop.active()) btnLastUsed.visible = Dungeon.hero.heroSkills.lastUsed != null;
		if (lastEnabled != enabled) {
			lastEnabled = enabled;

			for (Gizmo tool : members) {
				if (tool instanceof Tool) {
                    ((Tool)tool).enable(lastEnabled && (!(tool instanceof QuickslotTool) || tool.visible));

                    if(tool == btnLastUsed)
                        tool.visible = Dungeon.hero.heroSkills.lastUsed != null;
                    if(tool == btnMerc && Dungeon.depth == ColdGirl.FROST_DEPTH)
                        ((Tool)tool).enable(false);
				}
			}
		}

		if (!Dungeon.hero.isAlive()) {
			btnInventory.enable( true );
		}
		updateModeTools();
	}

	private void updateModeTools() {
		boolean enabled = LocalCoop.active() ? LocalCoop.acceptsInput() && Dungeon.hero.isAlive() : Dungeon.hero.ready;
		btnKing.visible = Dungeon.rules().allowsRatKing();
		btnKing.enable(btnKing.visible && enabled);
		if (LocalCoop.active()) {
			Hero partner = LocalCoop.partner(Dungeon.hero);
			btnMerc.visible = true;
			btnMerc.enable(enabled && partner != null
					&& (partner.isAlive() || PartySession.network() || PartySession.splitControls())
					&& GameScene.canAcceptMovement() && Window.topWindow() == null);
		} else {
			btnMerc.visible = !GauntletRun.active();
			btnMerc.enable(btnMerc.visible && enabled && Dungeon.depth != ColdGirl.FROST_DEPTH);
		}
		if (btnMerc instanceof PartnerTool) ((PartnerTool)btnMerc).updateWaiting();
	}

	public void pickup( Item item ) {
		pickedUp.reset( item,
			btnInventory.centerX(),
			btnInventory.centerY() );
	}

	public static int maxQuickSlots() {
		return maxQuickSlots(PixelScene.uiCamera.width, PixelScene.uiCamera.height);
	}

	private static int maxQuickSlots(float width, float height) {
		int left = width >= height ? 80 : 60;
		return Math.max(1, Math.min(QuickSlot.MAX_SLOTS, (int)(width - left - 23) / 22));
	}

	public static void updateQuickSlots() {
		if (instance != null) {
			QuickSlot.cancel();
			instance.layout();
		}
	}

	@Override
	public void destroy() {
		if (instance == this) instance = null;
		super.destroy();
	}

	private static CellSelector.Listener informer = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {

            tapAgainToSearch = false;

			if (cell == null) {
				return;
			}

			if (cell < 0 || cell > Level.LENGTH || (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
				GameScene.show( new WndMessage(Message.key("scenes.gamescene.dont_know")) ) ;
				return;
			}

			if (!Dungeon.visible[cell]) {

				GauntletArena arena = GauntletRun.arena();
				if (arena != null && arena.rewardCells().contains(cell)) {
					Heap reward = arena.heaps.get(cell);
					if (reward != null && !reward.isEmpty() && reward.type != Heap.Type.HIDDEN) {
						GameScene.show( new WndInfoItem( reward ) );
						return;
					}
				}
				GameScene.show( new WndInfoCell( cell ) );
				return;
			}

			if (cell == Dungeon.hero.pos) {
				GameScene.show( new WndHero() );
				return;
			}

			Char occupant = Actor.findChar(cell);
			if (LocalCoop.contains(occupant)) {
				GameScene.ready();
				LocalCoop.select((Hero)occupant);
				GameScene.show(new WndHero());
				return;
			}
			Mob mob = (Mob)occupant;
			if (mob != null) {
				GameScene.show( new WndInfoMob( mob ) );
				return;
			}

			Heap heap = Dungeon.level.heaps.get( cell );
			if (heap != null && heap.type != Heap.Type.HIDDEN) {
				if (heap.type == Heap.Type.FOR_SALE && heap.size() == 1 && heap.peek().price() > 0) {
					GameScene.show( new WndTradeItem( heap, false ) );
				} else {
					GameScene.show( new WndInfoItem( heap ) );
				}
				return;
			}

			Plant plant = Dungeon.level.plants.get( cell );
			if (plant != null) {
				GameScene.show( new WndInfoPlant( plant ) );
				return;
			}

			GameScene.show( new WndInfoCell( cell ) );
		}
		@Override
		public Message prompt() {
			return Message.key("custom.toolbar.examine_prompt");
		}
	};

	private static class Tool extends Button {

		private static final int BGCOLOR = 0x7B8073;
		private final int disabledColor = Presentation.reborn() ? 0x6B527C : BGCOLOR;

		protected Image base;

		public Tool( int x, int y, int width, int height ) {
			super();

			base.frame( x, y, width, height );

			this.width = width;
			this.height = height;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			base = new Image( RebornHud.texture( Assets.TOOLBAR ) );
			add( base );
		}

		@Override
		protected void layout() {
			super.layout();

			base.x = x;
			base.y = y;
		}

		@Override
		protected void onTouchDown() {
			base.brightness( 1.4f );
		}

		@Override
		protected void onTouchUp() {
			if (active) {
				base.resetColor();
			} else {
				base.tint( disabledColor, 0.7f );
			}
		}

		public void enable( boolean value ) {
			if (value != active) {
				if (value) {
					base.resetColor();
				} else {
					base.tint( disabledColor, 0.7f );
				}
				active = value;
			}
		}
	}

	private static class PartnerTool extends Tool {
		private Image portrait;
		private ColorBlock waitingBack;
		private ColorBlock[] waitingDots;
		private float waitingTime;
		private Hero shownPartner;
		private int shownTier = -1;

		PartnerTool() {
			super(83, 7, 22, 25);
			updatePortrait();
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			add(portrait = new Image());
			add(waitingBack = new ColorBlock(14, 6, 0xEE211C2A));
			waitingBack.visible = false;
			waitingDots = new ColorBlock[3];
			for (int i = 0; i < waitingDots.length; i++) {
				add(waitingDots[i] = new ColorBlock(2, 2, 0xFFE6DAF2));
				waitingDots[i].visible = false;
			}
		}

		@Override
		protected void layout() {
			super.layout();
			portrait.x = PixelScene.align(PixelScene.uiCamera, x + (width - portrait.width()) / 2);
			portrait.y = PixelScene.align(PixelScene.uiCamera, y + (height - portrait.height()) / 2);
			waitingBack.x = PixelScene.align(PixelScene.uiCamera, x + (width - waitingBack.width()) / 2);
			waitingBack.y = PixelScene.align(PixelScene.uiCamera, y - waitingBack.height() + 1);
			for (int i = 0; i < waitingDots.length; i++) {
				waitingDots[i].x = waitingBack.x + 2 + i * 4;
				waitingDots[i].y = waitingBack.y + 2;
			}
		}

		@Override
		public void update() {
			super.update();
			updatePortrait();
		}

		private void updatePortrait() {
			Hero partner = LocalCoop.partner(Dungeon.hero);
			portrait.visible = partner != null;
			if (partner != null && (shownPartner != partner || shownTier != partner.tier())) {
				shownPartner = partner;
				shownTier = partner.tier();
				portrait.copy(HeroSprite.avatar(partner.heroClass, shownTier));
				layout();
			}
		}

		void updateWaiting() {
			waitingTime = PartySession.waitingForPartner() ? waitingTime + Game.elapsed : 0;
			boolean waiting = waitingTime >= .35f;
			waitingBack.visible = waiting;
			for (int i = 0; i < waitingDots.length; i++) {
				waitingDots[i].visible = waiting;
				float pulse = (float)Math.cos((waitingTime - .35f) * Math.PI * 2 / 1.2f - i * .8f);
				waitingDots[i].alpha(.35f + .65f * Math.max(0, pulse));
			}
		}

		@Override
		public void enable(boolean value) {
			super.enable(value);
			portrait.alpha(value ? 1 : .35f);
		}

		@Override
		protected void onClick() {
			if (!active || !LocalCoop.acceptsInput() || Window.topWindow() != null) return;
			if (PartySession.network() || PartySession.splitControls()) {
				GameScene.show(new WndPartner(LocalCoop.partner(Dungeon.hero)));
				return;
			}
			LocalCoop.select(LocalCoop.partner(Dungeon.hero));
			updatePortrait();
		}

		@Override
		protected boolean onLongClick() {
			if (active && LocalCoop.acceptsInput() && Window.topWindow() == null)
				GameScene.show(new WndPartner(LocalCoop.partner(Dungeon.hero)));
			return true;
		}
	}

	private static class QuickslotTool extends Tool {

		private QuickSlot slot;

		public QuickslotTool( int x, int y, int width, int height, int index ) {
			super( x, y, width, height );
			slot.index(index);
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			slot = new QuickSlot();
			add( slot );
		}

		@Override
		protected void layout() {
			super.layout();
			slot.setRect( x + 1, y + 2, width - 2, height - 2 );
		}

		@Override
		public void enable( boolean value ) {
			slot.enable( value );
			super.enable( value );
		}
	}

	private static class PickedUpItem extends ItemSprite {

		private static final float DISTANCE = DungeonTilemap.SIZE;
		private static final float DURATION = 0.2f;

		private float dstX;
		private float dstY;
		private float left;

		public PickedUpItem() {
			super();

			originToCenter();

			active =
			visible =
				false;
		}

		public void reset( Item item, float dstX, float dstY ) {
			view( item.image(), item.glowing() );

			active =
			visible =
				true;

			this.dstX = dstX - ItemSprite.SIZE / 2;
			this.dstY = dstY - ItemSprite.SIZE / 2;
			left = DURATION;

			x = this.dstX - DISTANCE;
			y = this.dstY - DISTANCE;
			alpha( 1 );
		}

		@Override
		public void update() {
			super.update();

			if ((left -= Game.elapsed) <= 0) {

				visible =
				active =
					false;

			} else {
				float p = left / DURATION;
				scale.set( (float)Math.sqrt( p ) );
				float offset = DISTANCE * p;
				x = dstX - offset;
				y = dstY - offset;
			}
		}
	}
}
