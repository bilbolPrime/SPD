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
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.input.ControlSettings;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.JourneyRoute;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.windows.WndPartner;
import com.bilboldev.pixeldungeonskills.windows.WndDaily;
import com.bilboldev.pixeldungeonskills.windows.WndDailyChallenge;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.skills.CurrentSkills;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.gauntlet.GauntletArena;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.JourneyScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.windows.WndCatalogus;
import com.bilboldev.pixeldungeonskills.windows.WndHero;
import com.bilboldev.pixeldungeonskills.windows.WndHeroAppearance;
import com.bilboldev.pixeldungeonskills.windows.WndInfoCell;
import com.bilboldev.pixeldungeonskills.windows.WndInfoItem;
import com.bilboldev.pixeldungeonskills.windows.WndInfoMob;
import com.bilboldev.pixeldungeonskills.windows.WndInfoPlant;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndMerc;
import com.bilboldev.pixeldungeonskills.windows.WndMercs;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryRecruitment;
import com.bilboldev.pixeldungeonskills.windows.WndRatKing;
import com.bilboldev.pixeldungeonskills.windows.WndRebornChallenges;
import com.bilboldev.pixeldungeonskills.windows.WndSkill;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.pixeldungeonskills.windows.WndSettings;
import com.bilboldev.pixeldungeonskills.windows.WndTradeItem;

public class Toolbar extends Component {

	private Tool btnWait;
    private Tool btnSleep;
    private Tool btnSkill;
    private Tool btnTree;
    private Tool btnSearch;
    private Tool btnSettings;
    private Tool btnMerc;
    private PartnerTool btnParty;
    private Tool btnSupport;
    private Tool btnKing;
	private Tool btnChallenges;
	private Tool btnInfoSearch;
	private Tool btnInventory;
	private Tool[] quickSlots;
	private Group skillTray, searchTray, waitTray;
	private TouchArea outsideTray;
	private boolean compactControls;
	private boolean skillsOpen, searchOpen, waitOpen;
	private float skillSlide, searchSlide, waitSlide;
	private int trayHero;
	private Tool[] menuTools;

	private PickedUpItem pickedUp;
	private ColorBlock tutorialGlow;
	private RebornChrome tutorialGuide;
	private float guideTime;

	private boolean lastEnabled = true;

	private static Toolbar instance;

	public Toolbar() {
		super();

		instance = this;

		height = btnInventory.height();
	}

	@Override
	protected void createChildren() {
		compactControls = !Window.isDesktopRuntime() && Presentation.compactButtons();
		add(outsideTray = new TouchArea(0, 0, 0, 0) {
			@Override protected void onClick(Touch touch) {
				closeTrays();
			}
		});
		outsideTray.active = false;
		add(skillTray = new Group());
		add(searchTray = new Group());
		add(waitTray = new Group());

		add( btnWait = new Tool( 0, 7, 20, 25 ) {
			@Override
			protected void onClick() {
				if (waitOpen) {
					Dungeon.hero.rest(false);
				} else {
					closeTrays();
					waitOpen = true;
					trayHero = Dungeon.hero.id();
					layoutTrays();
				}
			};
			protected boolean onLongClick() {
				sleep();
				return true;
			};
		} );
		waitTray.add(btnSleep = new IconTool(Icons.SLEEP.get()) {
			@Override protected void onClick() { sleep(); }
			@Override protected void layout() {
				super.layout();
				if (GameActions.enabled && ControlSettings.keyboardHints) icon.y = PixelScene.align(y + 3);
			}
		});

        add( btnSkill = new Tool( 20, 7, 20, 25 ) {
			private SkillsIndicator skillsIndicator;
            @Override
            protected void onClick() {
				if (compactControls) toggleSkills();
				else openSkills();
            };
            protected boolean onLongClick() {
				openSkills();
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
		skillTray.add(btnTree = new Tool(20, 7, 20, 25) {
			@Override protected void onClick() { openSkills(); }
		});
		Image settingsIcon = Icons.PREFS.get();
		if (!Presentation.reborn()) settingsIcon.tint(0xFFE4AE, .35f);
		Group menu = compactControls ? skillTray : this;
		menu.add(btnSettings = new IconTool(settingsIcon) {
			@Override protected void onClick() {
				closeTrays();
				GameScene.show(new WndSettings(true, true));
			}
		});

        menu.add( btnKing = new Tool( 135, 7, 21, 25 ) {
            @Override
            protected void onClick() {
                closeTrays();
                WndRatKing.show(WndRatKing.Mode.NORMAL);
            };
            protected boolean onLongClick() {
                onClick();
                return true;
            };
        });
		add(btnChallenges = new ChallengeTool());

        Group companionControls = DailyRun.active() || GauntletRun.active() || JourneyRun.active() ? this : menu;
        companionControls.add(btnMerc = DailyRun.active() ? new DailyTool() : GauntletRun.active() ? new GauntletTool()
                : JourneyRun.active() ? new JourneyTool() : LocalCoop.active() ? new PartnerTool() : new MercTool());
        if (!Window.isDesktopRuntime() && btnMerc instanceof PartnerTool) add(btnParty = new PartnerTool());
        menu.add(btnSupport = new SupportTool());
		menuTools = new Tool[]{btnTree, btnMerc, btnKing, btnSupport, btnSettings};

		add( btnInfoSearch = new Tool( 107, 7, 20, 25 ) {
			@Override
			protected void onClick() {
				if (searchOpen) {
					closeTrays();
				} else {
					closeTrays();
					searchOpen = true;
					trayHero = Dungeon.hero.id();
					GameScene.selectCell(informer);
					layoutTrays();
				}
			}
            @Override
            protected boolean onLongClick() {
                search();
                return true;
            }

			@Override public boolean triggerAction(boolean alternate) {
				if (!alternate && searchOpen) {
					if (!acceptsAction()) return false;
					Toolbar.this.search();
					return true;
				}
				return super.triggerAction(alternate);
			}
		} );
		searchTray.add(btnSearch = new IconTool(new Image(Assets.TOOLBAR_SEARCH)) {
			@Override protected void onClick() { search(); }
		});

		add( btnInventory = new Tool( 60, 7, 23, 25 ) {
			private GoldIndicator gold;
			@Override
			protected void onClick() {
				closeTrays();
				if (PartySession.openInventory()) return;
				GameScene.show( new WndBag( Dungeon.hero.belongings.backpack, null, WndBag.Mode.ALL, null ) );
			}
			protected boolean onLongClick() {
				closeTrays();
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
		if (TutorialRun.active()) {
			add(tutorialGlow = new ColorBlock(1, 1, 0xFFE6C57E));
			tutorialGlow.visible = false;
			add(tutorialGuide = new RebornChrome(Chrome.Type.BUTTON).outlineOnly());
			tutorialGuide.highlight(true);
			tutorialGuide.brightness(1.6f);
			tutorialGuide.visible = false;
		}

		btnWait.bindAction(GameAction.WAIT);
		btnWait.bindAlternateAction(GameAction.REST);
		btnSleep.bindAction(GameAction.REST);
		bindModeActions();
		btnInfoSearch.bindAction(GameAction.EXAMINE);
		btnInventory.bindAction(GameAction.INVENTORY);
		updateSkillIcon();
		updateModeTools();
		layoutTrays();
	}

	@Override
	protected void layout() {
		btnInfoSearch.setPos(x, y);
		btnWait.setPos(btnInfoSearch.right(), y);
        btnSkill.setPos(btnWait.right(), y);
		btnChallenges.setPos(x, compactControls ? sideButtonTop() : 100);
		if (btnParty != null) btnParty.setPos(x, 40);
		if (btnMerc.parent == this) btnMerc.setPos(x, 40);
		if (!compactControls) {
			btnSettings.setPos(x, 70);
			btnKing.setPos(x, 100);
			btnSupport.setPos(x, 160);
		}
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
		outsideTray.width = camera().width;
		outsideTray.height = camera().height;
		layoutTrays();
	}

	public void layoutViewButton(Button button) {
		if (camera().width >= camera().height) {
			button.setRect(btnSkill.right(), y, 22, btnSkill.height());
		} else {
			float top = compactControls ? sideButtonTop() + (btnChallenges.visible ? 30 : 0) : 130;
			button.setRect(x, top, 21, btnSkill.height());
		}
	}

	private float sideButtonTop() {
		boolean progressOrParty = (btnParty != null && btnParty.visible)
				|| (btnMerc.parent == this && btnMerc.visible);
		return progressOrParty ? 70 : 40;
	}

	private void bindModeActions() {
		GameActions.unbind(btnSkill);
		GameActions.unbind(btnKing);
		GameActions.unbind(btnMerc);
		if (compactControls) {
			btnSkill.bindAlternateAction(GameAction.SKILLS);
		} else {
			btnSkill.bindAction(GameAction.SKILLS);
			btnKing.bindAction(GameAction.RAT_KING);
			btnMerc.bindAction(GameAction.MERCENARIES);
		}
	}

	private void updateSkillIcon() {
		btnSkill.base.texture(RebornHud.texture(compactControls ? Assets.TOOLBAR_MENU : Assets.TOOLBAR));
		btnSkill.base.frame(compactControls ? 0 : 20, compactControls ? 0 : 7, 20, 25);
	}

	public static void refreshButtonLayout() {
		if (instance == null) return;
		boolean compact = !Window.isDesktopRuntime() && Presentation.compactButtons();
		if (instance.compactControls == compact) return;
		closeTrays();
		instance.skillSlide = instance.searchSlide = instance.waitSlide = 0;
		instance.compactControls = compact;
		Group menu = compact ? instance.skillTray : instance;
		menu.add(instance.btnSettings);
		menu.add(instance.btnKing);
		menu.add(instance.btnSupport);
		if (!(instance.btnMerc instanceof DailyTool) && !(instance.btnMerc instanceof GauntletTool)
				&& !(instance.btnMerc instanceof JourneyTool)) menu.add(instance.btnMerc);
		instance.bindModeActions();
		instance.updateSkillIcon();
		instance.updateModeTools();
		instance.layout();
		GameScene.updateButtonLayout();
	}

	@Override
	public void update() {
		super.update();

		boolean enabled = LocalCoop.active() ? LocalCoop.acceptsInput() && Dungeon.hero.isAlive() : Dungeon.hero.ready;
		if (lastEnabled != enabled) {
			lastEnabled = enabled;

			for (Gizmo tool : members) {
				if (tool instanceof Tool) {
					((Tool)tool).enable(lastEnabled && (!(tool instanceof QuickslotTool) || tool.visible));
				}
			}
		}

		if (!Dungeon.hero.isAlive()) {
			btnInventory.enable( true );
		}
		updateModeTools();
		if ((skillsOpen || searchOpen || waitOpen) && (Window.topWindow() != null || !GameScene.canAcceptGameplayInput()
				|| Dungeon.hero.id() != trayHero
				|| !Dungeon.hero.isAlive() || (searchOpen && !canUseHero()))) closeTrays();
		skillSlide = slide(skillSlide, skillsOpen);
		searchSlide = slide(searchSlide, searchOpen);
		waitSlide = slide(waitSlide, waitOpen);
		layoutTrays();
		updateTutorialGuide();
	}

	private boolean canUseHero() {
		return Dungeon.hero.isAlive() && (LocalCoop.active() ? LocalCoop.acceptsInput() : Dungeon.hero.ready);
	}

	private void openSkills() {
		if (!canUseHero()) return;
		closeTrays();
		if (PartySession.openSkills()) return;
		GameScene.show(new WndSkillsNew(null, null));
	}

	private void search() {
		closeTrays();
		Dungeon.hero.search(true);
	}

	private void sleep() {
		if (!canUseHero()) return;
		int owner = Dungeon.hero.id();
		closeTrays();
		GameScene.show(new WndOptions(Icons.SLEEP.get(), Message.key("ui.toolbar.sleep"),
				Message.key("ui.toolbar.sleep_confirm"), Message.key("ui.toolbar.sleep"),
				Message.key("ui.toolbar.item_cancel")) {
			@Override protected void onSelect(int index) {
				if (index == 0 && Dungeon.hero.id() == owner && canUseHero()) Dungeon.hero.rest(true);
			}
		});
	}

	private void toggleSkills() {
		boolean open = !skillsOpen;
		closeTrays();
		skillsOpen = open;
		trayHero = Dungeon.hero.id();
		layoutTrays();
	}

	public static boolean closeTrays() {
		if (instance == null) return false;
		boolean open = instance.skillsOpen || instance.searchOpen || instance.waitOpen;
		boolean inspecting = instance.searchOpen;
		instance.skillsOpen = instance.searchOpen = instance.waitOpen = false;
		instance.btnSkill.selected = instance.btnInfoSearch.selected = instance.btnWait.selected = false;
		instance.outsideTray.active = false;
		instance.skillTray.active = instance.searchTray.active = instance.waitTray.active = false;
		if (inspecting) GameScene.ready();
		return open;
	}

	public static boolean menuOpen() {
		return instance != null && instance.skillsOpen;
	}

	public static void targeting(CellSelector.Listener listener, boolean selecting) {
		if (instance != null && listener != informer && (selecting || instance.searchOpen)) {
			instance.searchOpen = false;
			closeTrays();
		}
	}

	public static boolean shortcut(GameAction action) {
		if (instance == null) return false;
		Tool tool = action == GameAction.RAT_KING ? instance.btnKing
				: action == GameAction.MERCENARIES ? instance.btnMerc : null;
		return tool != null && tool.shortcut();
	}

	public static Button keyHintButton(GameAction action) {
		Button button = GameActions.visibleButton(action);
		if (instance != null) {
			if (button == instance.btnInfoSearch && instance.searchOpen) return instance.btnSearch;
		}
		return button;
	}

	private static float slide(float amount, boolean open) {
		return open ? Math.min(1, amount + Game.elapsed / .18f) : Math.max(0, amount - Game.elapsed / .18f);
	}

	private void layoutTrays() {
		if (menuTools == null || btnSearch == null || btnSleep == null) return;
		skillTray.visible = compactControls && skillSlide > 0;
		skillTray.active = skillTray.visible && skillsOpen && skillSlide == 1;
		searchTray.visible = searchSlide > 0;
		searchTray.active = searchTray.visible && searchOpen && searchSlide == 1;
		waitTray.visible = waitSlide > 0;
		waitTray.active = waitTray.visible && waitOpen && waitSlide == 1;
		outsideTray.active = skillsOpen || waitOpen;
		btnSkill.selected = skillsOpen;
		btnInfoSearch.selected = searchOpen;
		btnWait.selected = waitOpen;
		if (!skillTray.visible && !searchTray.visible && !waitTray.visible) return;
		float progress = skillSlide * skillSlide * (3 - 2 * skillSlide);
		int index = 0;
		for (Tool tool : menuTools) {
			if (!tool.visible || tool.parent != skillTray) continue;
			float left = btnSkill.left() + (index % 2) * 24;
			float top = y - (index / 2 + 1) * 29;
			tool.setPos(btnSkill.left() + (left - btnSkill.left()) * progress,
					y + (top - y) * progress);
			index++;
		}
		progress = searchSlide * searchSlide * (3 - 2 * searchSlide);
		btnSearch.setPos(btnInfoSearch.left(), y - 29 * progress);
		progress = waitSlide * waitSlide * (3 - 2 * waitSlide);
		btnSleep.setPos(btnWait.left(), y - 29 * progress);
	}

	private void updateTutorialGuide() {
		if (tutorialGuide == null) return;
		tutorialGlow.visible = false;
		tutorialGuide.visible = false;
		if (Window.topWindow() != null || !TutorialRun.acceptsInput()) return;
		Tool target;
		switch (TutorialRun.guideAction()) {
			case WAIT: target = btnWait; break;
			case INSPECT: target = btnInfoSearch; break;
			case SEARCH: target = searchOpen ? btnSearch : btnInfoSearch; break;
			case INVENTORY: target = btnInventory; break;
			case SKILLS: target = skillsOpen ? btnTree : btnSkill; break;
			case QUICKSLOT:
				int slot = TutorialRun.guideQuickslot();
				if (slot < 0 || slot >= quickSlots.length) return;
				target = quickSlots[slot];
				break;
			default: return;
		}
		if (!target.visible || !target.active) return;
		guideTime = (guideTime + Game.elapsed) % 2;
		float pulse = .5f - .5f * (float)Math.cos(guideTime * Math.PI);
		tutorialGlow.x = target.left() + 2;
		tutorialGlow.y = target.top() + 2;
		tutorialGlow.size(target.width() - 4, target.height() - 4);
		tutorialGlow.alpha(.18f * pulse);
		tutorialGlow.visible = true;
		tutorialGuide.x = target.left();
		tutorialGuide.y = target.top();
		tutorialGuide.size(target.width(), target.height());
		tutorialGuide.alpha(.2f + .7f * pulse);
		tutorialGuide.visible = true;
	}

	private void updateModeTools() {
		btnSupport.visible = compactControls || Game.width < Game.height;
		btnSupport.enable(btnSupport.visible);
		btnSettings.visible = compactControls || (!Window.isDesktopRuntime() && Game.width < Game.height);
		btnSettings.enable(btnSettings.visible);
		boolean enabled = LocalCoop.active() ? LocalCoop.acceptsInput() && Dungeon.hero.isAlive() : Dungeon.hero.ready;
		btnSkill.enable(compactControls || (enabled && Dungeon.hero.isAlive()));
		btnTree.enable(enabled && Dungeon.hero.isAlive());
		btnSearch.enable(enabled && Dungeon.hero.isAlive());
		btnSleep.enable(enabled && Dungeon.hero.isAlive());
		btnKing.visible = Dungeon.rules().allowsRatKing();
		btnKing.enable(btnKing.visible && enabled);
		btnChallenges.visible = Dungeon.rules().hasChallenges() && (Dungeon.challenges & RebornChallenges.ALL) != 0;
		btnChallenges.enable(btnChallenges.visible);
		if (LocalCoop.active()) {
			Hero partner = LocalCoop.partner(Dungeon.hero);
			btnMerc.visible = true;
			btnMerc.enable(enabled && partner != null
					&& (partner.isAlive() || PartySession.network() || PartySession.splitControls())
					&& GameScene.canAcceptMovement() && Window.topWindow() == null);
		} else if (btnMerc instanceof DailyTool) {
			btnMerc.enable(!DailyRun.finished());
		} else if (btnMerc instanceof GauntletTool) {
			btnMerc.enable(GauntletRun.acceptsInput());
		} else if (btnMerc instanceof JourneyTool) {
			btnMerc.enable(!JourneyRun.dying() && Dungeon.hero.isAlive());
		} else {
			btnMerc.visible = !GauntletRun.active() && !JourneyRun.active();
			btnMerc.enable(btnMerc.visible && enabled && Dungeon.depth != ColdGirl.FROST_DEPTH);
		}
		if (btnMerc instanceof PartnerTool) ((PartnerTool)btnMerc).updateWaiting();
		if (btnParty != null) {
			btnParty.visible = compactControls && btnMerc.visible;
			btnParty.enable(btnParty.visible && btnMerc.active);
			btnParty.updateWaiting();
		}
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
		int left = width >= height ? 82 : 60;
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
            if (instance != null) instance.searchOpen = false;
            closeTrays();

			if (cell == null) {
				return;
			}

			if (cell < 0 || cell >= Level.LENGTH || (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
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
			return Message.key("ui.toolbar.inspect_prompt");
		}
	};

	private static class Tool extends Button {

		private static final int BGCOLOR = 0x7B8073;
		private final int disabledColor = Presentation.reborn() ? 0x6B527C : BGCOLOR;

		protected Image base;
		private ColorBlock selectionBorder;
		private boolean selected;

		public Tool( int x, int y, int width, int height ) {
			this(Assets.TOOLBAR, x, y, width, height);
		}

		public Tool(Object asset, int x, int y, int width, int height) {
			super();

			base.texture(RebornHud.texture(asset));
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
		public void draw() {
			super.draw();
			if (!selected) return;
			if (selectionBorder == null) selectionBorder = new ColorBlock(1, 1, 0xFFD6BE98);
			selectionBorder.camera = camera();
			selectionBorder.alpha(.8f);
			selectionBorder.x = x + 1;
			selectionBorder.y = y + 1;
			selectionBorder.size(width - 2, 1);
			selectionBorder.draw();
			selectionBorder.y = bottom() - 2;
			selectionBorder.draw();
			selectionBorder.y = y + 2;
			selectionBorder.size(1, height - 4);
			selectionBorder.draw();
			selectionBorder.x = right() - 2;
			selectionBorder.draw();
		}

		@Override
		public void destroy() {
			super.destroy();
			if (selectionBorder != null) selectionBorder.destroy();
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

		boolean shortcut() {
			if (!visible || !active) return false;
			onClick();
			return true;
		}
	}

    private static class IconTool extends Tool {
        protected final Image icon;

        IconTool(Image icon) {
            super(83, 7, 22, 25);
            this.icon = icon;
            add(icon);
        }

        @Override protected void layout() {
            super.layout();
            icon.x = PixelScene.align(x + (width - icon.width()) / 2);
            icon.y = PixelScene.align(y + (height - icon.height()) / 2);
        }
    }

    private static class SupportTool extends IconTool {
        SupportTool() {
            super(Icons.SUPPORT.get());
        }

        @Override protected void onClick() {
            closeTrays();
            Game.scene().add(new WndHeroAppearance(
                    PartySession.network() ? PartySession.localHero() : Dungeon.hero));
        }
    }

	private static class ChallengeTool extends Tool {
		private ItemSprite icon;
		private BitmapText count;

		ChallengeTool() {
			super(83, 7, 22, 25);
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			add(icon = new ItemSprite(ItemSpriteSheet.SCROLL_WIPE_OUT, null));
			icon.scale.set(.75f);
			String total = Integer.toString(Integer.bitCount(Dungeon.challenges & RebornChallenges.ALL));
			add(count = new BitmapText(total, PixelScene.font1x));
			count.hardlight(0xFFEBA4);
			count.measure();
		}

		@Override
		protected void layout() {
			super.layout();
			icon.x = PixelScene.align(camera(), x + (width - icon.width()) / 2);
			icon.y = y + 3;
			count.x = PixelScene.align(camera(), x + (width - count.width()) / 2);
			count.y = bottom() - count.height() - 2;
		}

		@Override
		protected void onClick() {
			closeTrays();
			GameScene.show(new WndRebornChallenges(Dungeon.challenges, null));
		}
	}

	private static class GauntletTool extends Tool {
        private Image icon;
        private BitmapText wave, steps;
        private int shownWave = -1, shownSteps = -1;

        GauntletTool() { super(83, 7, 22, 25); }

        @Override protected void createChildren() {
            super.createChildren();
            add(icon = Icons.RAT_KING.get());
            icon.scale.set(10f / Math.max(icon.width, icon.height));
            add(wave = new BitmapText(PixelScene.font1x));
            add(steps = new BitmapText(PixelScene.font1x));
            wave.hardlight(0xFFEBA4);
            steps.hardlight(0xFFD36A);
        }

        @Override public void update() {
            super.update();
            GauntletArena arena = GauntletRun.arena();
            if (arena == null) return;
            boolean preparing = arena.phase() == GauntletArena.Phase.PREPARING;
            int number = arena.wave();
            int remaining = preparing ? arena.stepsRemaining() : -1;
            if (shownWave == number && shownSteps == remaining) return;
            shownWave = number;
            shownSteps = remaining;
            wave.text(number + "/" + GauntletArena.WAVES);
            wave.measure();
            wave.scale.set(Math.min(1, (width - 4) / Math.max(1, wave.width)));
            steps.text(preparing ? Integer.toString(remaining) : "");
            steps.measure();
            steps.scale.set(Math.min(1, 9f / Math.max(1, steps.width)));
            layout();
        }

        @Override protected void layout() {
            super.layout();
            wave.x = PixelScene.align(camera(), x + (width - wave.width()) / 2);
            wave.y = y + 3;
            icon.x = PixelScene.align(camera(), x + (shownSteps < 0 ? (width - icon.width()) / 2 : 2));
            icon.y = bottom() - icon.height() - 2;
            steps.x = right() - steps.width() - 2;
            steps.y = bottom() - steps.height() - 3;
        }

        @Override protected void onClick() {
            closeTrays();
            GauntletArena arena = GauntletRun.arena();
            if (arena == null) return;
            String text = Messages.get("gauntlet.wave", Math.max(1, arena.wave()), GauntletArena.WAVES, arena.themeName());
            if (arena.phase() == GauntletArena.Phase.PREPARING) {
                text += "\n\n" + (arena.stepsRemaining() == 1
                        ? Messages.get("gauntlet.wave_in_step", arena.completed() + 1)
                        : Messages.get("gauntlet.wave_in_steps", arena.completed() + 1, arena.stepsRemaining()));
            }
            GameScene.show(new com.bilboldev.pixeldungeonskills.windows.WndTitledMessage(
                    Icons.RAT_KING.get(), Messages.get("gauntlet.title"), text));
        }
    }

	private static class JourneyTool extends Tool {
		private Image icon;
		private BitmapText floor;
		private int shownFloor = -1;

		JourneyTool() { super(83, 7, 22, 25); }

		@Override protected void createChildren() {
			super.createChildren();
			add(icon = new Image(Assets.JOURNEY));
			icon.scale.set(.5f);
			add(floor = new BitmapText(PixelScene.font1x));
			floor.hardlight(0xFFEBA4);
		}

		@Override public void update() {
			super.update();
			int current = JourneyRun.floorIndex();
			if (current == shownFloor) return;
			shownFloor = current;
			floor.text((current + 1) + "/" + JourneyRun.floorCount());
			floor.measure();
			layout();
		}

		@Override protected void layout() {
			super.layout();
			icon.x = PixelScene.align(camera(), x + (width - icon.width()) / 2);
			icon.y = y + 3;
			floor.x = PixelScene.align(camera(), x + (width - floor.width()) / 2);
			floor.y = bottom() - floor.height() - 2;
		}

		@Override protected void onClick() {
			closeTrays();
			JourneyRoute.Node node = JourneyRun.deployedNode();
			if (node == null) return;
			String text = Messages.get("journey.floor", JourneyRun.floorIndex() + 1, node.floors) + "\n\n";
			if (!JourneyRun.recovering()) text += Messages.get("journey.target_depth", node.depth(node.floors - 1)) + "\n\n";
			if (JourneyRun.recovering()) {
				text += Messages.get("journey.recovery_objective");
			} else if (node.objective != JourneyRoute.Objective.NONE) {
				int guards = JourneyRun.guardsRemaining();
				text += JourneyRun.objectiveComplete() ? Messages.get("journey.guards_cleared")
						: Messages.get(JourneyScene.objectiveKey(node)) + "\n\n"
						+ (guards > 0 ? Messages.get("journey.guards_left", guards)
						: Messages.get(JourneyRun.objectiveExitHint()));
			} else if ((node.gate || node.finish) && !JourneyRun.objectiveComplete()) {
				text += Messages.get(node.finish ? "journey.final_objective" : "journey.boss_objective");
			} else {
				text += Messages.get("journey.entrance_hint");
			}
			GameScene.show(new com.bilboldev.pixeldungeonskills.windows.WndTitledMessage(
					new Image(Assets.JOURNEY), JourneyScene.areaName(node), text));
		}
	}

	private static class DailyTool extends Tool {
		private Image icon;
		private BitmapText turns;
		private int shownTurns = -1;

		DailyTool() {
			super(83, 7, 22, 25);
		}

		@Override protected void createChildren() {
			super.createChildren();
			add(icon = WndDaily.image(DailyRun.day()));
			icon.scale.set(12f / Math.max(icon.width, icon.height));
			add(turns = new BitmapText(PixelScene.font1x));
		}

		@Override public void update() {
			super.update();
			int remaining = DailyRun.remainingTurns();
			if (remaining == shownTurns) return;
			shownTurns = remaining;
			turns.text(Integer.toString(remaining));
			turns.measure();
			turns.scale.set(Math.min(1, (width - 4) / Math.max(1, turns.width)));
			turns.hardlight(Dungeon.level instanceof DailyOutpostLevel
					&& remaining <= 30 ? 0xA7D8B0 : remaining <= 30 ? 0xFF6666
					: remaining <= 100 ? 0xFFD36A : 0xFFEBA4);
			layout();
		}

		@Override protected void layout() {
			super.layout();
			icon.x = PixelScene.align(camera(), x + (width - icon.width()) / 2);
			icon.y = y + 3;
			turns.x = PixelScene.align(camera(), x + (width - turns.width()) / 2);
			turns.y = bottom() - turns.height() - 2;
		}

		@Override protected void onClick() {
			closeTrays();
			GameScene.show(new WndDailyChallenge(DailyRun.day(), true));
		}
	}

	private static class MercTool extends Tool {
		private Image portrait;
		private HiredMerc.MERC_TYPES shownType;

		MercTool() {
			super(83, 7, 22, 25);
			updatePortrait();
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			add(portrait = MercIcons.company());
		}

		@Override
		protected void layout() {
			super.layout();
			portrait.x = PixelScene.align(PixelScene.uiCamera, x + (width - portrait.width()) / 2);
			portrait.y = PixelScene.align(PixelScene.uiCamera, y + (height - portrait.height()) / 2);
		}

		@Override
		public void update() {
			super.update();
			updatePortrait();
		}

		private void updatePortrait() {
			HiredMerc merc = Dungeon.hero.hiredMerc;
			HiredMerc.MERC_TYPES type = merc == null ? null : merc.mercType;
			if (shownType != type) {
				shownType = type;
				portrait.copy(type == null ? MercIcons.company() : MercIcons.portrait(type));
				portrait.alpha(active ? 1 : .35f);
				layout();
			}
		}

		@Override
		public void enable(boolean value) {
			super.enable(value);
			portrait.alpha(value ? 1 : .35f);
		}

		@Override
		protected void onClick() {
			closeTrays();
			if (Dungeon.hero.hiredMerc != null) {
				GameScene.show(new WndMerc(null, null));
			} else if (Dungeon.rules().allowsOnDemandHiring()) {
				GameScene.show(new WndMercs(WndMercs.Mode.ALL));
			} else {
				GameScene.show(new WndMessage(MercenaryRecruitment.noMercMessage()));
			}
		}

		@Override
		protected boolean onLongClick() {
			closeTrays();
			if (Dungeon.hero.hiredMerc == null && Dungeon.rules().allowsOnDemandHiring()) {
				GameScene.show(new WndSkill(null, CurrentSkills.mercMenu));
			} else {
				onClick();
			}
			return true;
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
				portrait.copy(HeroSprite.avatar(partner, shownTier));
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
			closeTrays();
			if (!active || !LocalCoop.acceptsInput() || Window.topWindow() != null) return;
			if (PartySession.network() || PartySession.splitControls()) {
				GameScene.show(new WndPartner(LocalCoop.partner(Dungeon.hero)));
				return;
			}
			Hero partner = LocalCoop.partner(Dungeon.hero);
			LocalCoop.stopFollowing(partner);
			LocalCoop.select(partner);
			updatePortrait();
		}

		@Override
		protected boolean onLongClick() {
			closeTrays();
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
