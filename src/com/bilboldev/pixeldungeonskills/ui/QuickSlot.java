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

import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.input.GameAction;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Belongings;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndQuickSlot;
import com.bilboldev.utils.Bundle;
import java.util.Arrays;
import java.util.ArrayList;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.ActiveSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SpiritArmor;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.TalentSkill;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.Presentation;

public class QuickSlot extends Button implements WndBag.Listener {

	public static final int MAX_SLOTS = 10;
	public static Object[] values = new Object[MAX_SLOTS];
	private static final QuickSlot[] slots = new QuickSlot[MAX_SLOTS];
	private int index;

	private Item itemInSlot;
	private ItemSlot slot;
    private SkillSprite skillIcon;
    private BitmapText skillCount;
    private ColorBlock countBack;
    private ColorBlock[] activeFrame;
    private Skill shownSkill;
    private int shownCooldown = -1;
    private Hero skillHero;
    private Object skillTree;
    private int skillRef = -1;
    private Skill cachedSkill;

	private Image crossB;
	private Image crossM;

	private boolean targeting = false;

	private static Char lastTarget= null;

	public static void resetSlots() {
		Arrays.fill(values, null);
		lastTarget = null;
	}

	public static Char lastTarget() {
		return lastTarget;
	}

	public static void lastTarget(Char target) {
		lastTarget = target;
	}

	public void primary() {
		index(0);
	}

	public void secondary() {
		index(1);
	}

	public void index(int value) {
		index = value;
		slots[index] = this;
		item( select() );
		bindAction(GameAction.valueOf("QUICKSLOT_" + (index + 1)));
	}

	@Override
	public boolean triggerAction(boolean alternate) {
		if (!acceptsAction()) return false;

		if (!alternate && slot.acceptsAction()) return slot.triggerAction(false);
		return super.triggerAction(alternate);
	}

	@Override
	public void destroy() {
		super.destroy();

		if (slots[index] == this) slots[index] = null;
		crossM.remove();

		lastTarget = null;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new ItemSlot() {
			@Override
			protected void onClick() {
				if (confirmSkillTarget()) return;
				if (PartySession.input(Dungeon.hero, "quick", index)) {
                    suggestSkillTarget();
                    return;
                }
				if (selectedSkill() != null) {
					useSkill();
					return;
				}
				final Item item = select();
				if (item == null || item.quantity() <= 0) return;
				if (targeting) {
					if (lastTarget != null && lastTarget.isAlive()) GameScene.handleCell(lastTarget.pos);
					else QuickSlot.cancel();
				} else {
					if (LocalCoop.active()) {
						final Hero user = Dungeon.hero;
						final int slotIndex = index;
						if (LocalCoop.defer(user, () -> {
							if (user.isAlive() && (user.belongings.backpack.contains(item) || item.isEquipped(user))) {
								QuickSlot current = slots[slotIndex];
								if (current != null) current.useTargeting();
								item.execute(user);
							}
						})) return;
					}
					useTargeting();
					item.execute(Dungeon.hero);
				}
			}
			@Override
			protected boolean onLongClick() {
				return QuickSlot.this.onLongClick();
			}
			@Override
			protected void onTouchDown() {
				icon.lightness( 0.7f );
			}
			@Override
			protected void onTouchUp() {
				icon.resetColor();
			}
		};
		add( slot );
        skillIcon = new SkillSprite(0);
        skillIcon.visible = false;
        add(skillIcon);
        activeFrame = new ColorBlock[4];
        for (int i = 0; i < activeFrame.length; i++) {
            activeFrame[i] = new ColorBlock(1, 1, 0xFFDBC789);
            activeFrame[i].visible = false;
            add(activeFrame[i]);
        }
        countBack = new ColorBlock(1, 1, 0xCC17131C);
        countBack.visible = false;
        add(countBack);
        skillCount = new BitmapText(PixelScene.font1x);
        add(skillCount);

		crossB = Icons.TARGET.get();
		crossB.visible = false;
		add( crossB );

		crossM = new Image();
		crossM.copy( crossB );
	}

	@Override
	protected void layout() {
		super.layout();

		slot.fill( this );
        skillIcon.x = PixelScene.align(x + (width - skillIcon.width()) / 2);
        skillIcon.y = PixelScene.align(y + (height - skillIcon.height()) / 2);
        skillCount.x = PixelScene.align(PixelScene.uiCamera, x + (width - skillCount.width()) / 2);
        skillCount.y = PixelScene.align(PixelScene.uiCamera, y + height - skillCount.height() - 3);
        countBack.x = skillCount.x - 1;
        countBack.y = skillCount.y - 1;
        countBack.size(skillCount.width() + 2, skillCount.height() + 2);
        for (int i = 0; i < activeFrame.length; i++) {
            boolean horizontal = i < 2;
            activeFrame[i].size(horizontal ? width - 2 : 1, horizontal ? 1 : height - 3);
            activeFrame[i].x = x + (i == 3 ? width - 2 : 1);
            activeFrame[i].y = y + (i == 1 ? height - 3 : 1);
        }

		crossB.x = PixelScene.align( x + (width - crossB.width) / 2 );
		crossB.y = PixelScene.align( y + (height - crossB.height) / 2 );
	}

	@Override
	protected void onClick() {
		if (PartySession.input(Dungeon.hero, "quick_assign", index)) return;
		choose();
	}

	@Override
	protected boolean onLongClick() {
		if (PartySession.input(Dungeon.hero, "quick_assign", index)) return true;
		choose();
		return true;
	}

    private void choose() {
        GameScene.show(new WndQuickSlot(index));
    }

	@SuppressWarnings("unchecked")
	private Item select() {
		Object content = values[index];
		if (content instanceof Item) {

			return (Item)content;

		} else if (content instanceof Class) {

			for (Item item : Dungeon.hero.belongings) {
				if (item.getClass() == content) return item;
			}
			return Item.virtual((Class<? extends Item>)content);

		} else {

			return null;

		}
	}

	public static void partyUse(int index, boolean assign) {
		if (index < 0 || index >= MAX_SLOTS) return;
		QuickSlot slot = slots[index];
		if (slot == null) return;
		if (assign) {
			slot.choose();
			return;
		}
		Hero hero = Dungeon.hero;
        if (slot.selectedSkill() != null) { slot.useSkill(); return; }
		Item item = slot.select();
		if (item == null || item.quantity() <= 0
				|| !(hero.belongings.backpack.contains(item) || item.isEquipped(hero))) return;
		if (LocalCoop.defer(hero, () -> partyUse(index, false))) return;
		item.execute(hero);
	}

	@Override
	public void onSelect( Item item ) {
		if (item != null) {
			values[index] = item.stackable ? item.getClass() : item;
			refresh();
		}
	}

	public void item( Item item ) {
		slot.item( item );
		itemInSlot = item;
        Skill skill = selectedSkill();
        skillIcon.visible = skill != null;
        if (skill != null) skillIcon.view(skill.image());
        shownCooldown = -1;
		enableSlot();
	}

    private Skill selectedSkill() {
        if (!(values[index] instanceof Integer) || Dungeon.hero == null) return null;
        int ref = (Integer)values[index];
        if (skillHero != Dungeon.hero || skillTree != Dungeon.hero.skillTree || skillRef != ref) {
            skillHero = Dungeon.hero;
            skillTree = skillHero.skillTree;
            skillRef = ref;
            Skill[] skills = LocalCoop.skills(skillHero);
            cachedSkill = ref >= 0 && ref < skills.length ? skills[ref] : null;
        }
        return cachedSkill;
    }

    public static boolean canAssign(Skill skill) {
        if (skill == null) return false;
        if (skill instanceof TalentSkill) return !((TalentSkill)skill).talent.passive() && ((TalentSkill)skill).rank() > 0;
        return skill.level > 0 && (skill instanceof ActiveSkill || skill.quickCast || toggle(skill, Dungeon.hero));
    }

    public static void assign(Skill skill) {
        if (!canAssign(skill)) return;
        GameScene.show(new WndQuickSlot(skill));
    }

    public static void bind(Skill skill, int slot) {
        if (!canAssign(skill) || slot < 0 || slot >= visibleSlots()) return;
        Skill[] skills = LocalCoop.skills(Dungeon.hero);
        for (int ref = 0; ref < skills.length; ref++) {
            if (skills[ref] != skill) continue;
            for (int i = 0; i < MAX_SLOTS; i++) if (Integer.valueOf(ref).equals(values[i])) values[i] = null;
            values[slot] = ref;
            refresh();
            return;
        }
    }

    public static void bind(Item item, int slot) {
        if (item == null || item.defaultAction == null || slot < 0 || slot >= visibleSlots()
                || !(item.isEquipped(Dungeon.hero) || Dungeon.hero.belongings.backpack.contains(item))) return;
        values[slot] = item.stackable ? item.getClass() : item;
        refresh();
    }

    public static int visibleSlots() {
        if (PartySession.remoteContext()) return PartySession.remoteQuickSlots();
        if (PixelScene.uiCamera == null) return Presentation.quickSlots();
        return Math.min(Presentation.quickSlots(), Toolbar.maxQuickSlots());
    }

    public static int freeSlot() {
        for (int i = 0; i < visibleSlots(); i++) if (values[i] == null) return i;
        return -1;
    }

    private static boolean bindFree(Skill skill) {
        Skill[] skills = LocalCoop.skills(Dungeon.hero);
        for (int ref = 0; ref < skills.length; ref++) {
            if (skills[ref] != skill) continue;
            for (int i = 0; i < visibleSlots(); i++) if (Integer.valueOf(ref).equals(values[i])) return true;
            int slot = freeSlot();
            if (slot < 0) return false;
            values[slot] = ref;
            refresh();
            return true;
        }
        return false;
    }

    public static void learned(Skill skill) {
        if (canAssign(skill) && !bindFree(skill)) {
            GameScene.show(new WndMessage(Message.key("skills4.no_slots")));
        }
    }

    private static boolean toggle(Skill skill, Hero hero) {
        if (hero == null) return false;
        if (skill instanceof TalentSkill) return ((TalentSkill)skill).talent.toggle();
        ArrayList<String> actions = skill.actions(hero);
        return actions.contains(Skill.AC_ACTIVATE) || actions.contains(Skill.AC_DEACTIVATE);
    }

    public static void remember(Hero hero, Skill skill) {
        Skill previous = hero.heroSkills.lastUsed;
        if (previous != null && previous != skill && previous.active && toggle(previous, hero)
                && !(previous instanceof SpiritArmor)
                && (!(previous instanceof TalentSkill) || ((TalentSkill)previous).talent.automatic())) {
            previous.execute(hero, Skill.AC_DEACTIVATE);
        }
        hero.heroSkills.lastUsed = skill;
    }

    public static void useLastSkill() {
        Hero hero = Dungeon.hero;
        if (hero == null || !hero.isAlive()) return;
        Skill skill = hero.heroSkills.lastUsed;
        if (!canAssign(skill)) return;
        if (!PartySession.remoteContext()) {
            for (int i = 0; i < visibleSlots(); i++) {
                QuickSlot slot = slots[i];
                if (slot == null || slot.selectedSkill() != skill) continue;
                if (slot.confirmSkillTarget()) return;
                if (PartySession.input(hero, "last_skill", 0)) slot.suggestSkillTarget();
                else slot.useSkill();
                return;
            }
        }
        if (PartySession.input(hero, "last_skill", 0)) return;
        if (!PartySession.remoteContext()) cancel();
        useSkill(hero, skill);
    }

    private void useSkill() {
        if (confirmSkillTarget()) return;
        if (!PartySession.remoteContext()) cancel();
        useSkill(Dungeon.hero, selectedSkill());
        if (GameScene.hasCellSelection()) suggestSkillTarget();
    }

    private boolean aimsAtEnemy() {
        Skill skill = selectedSkill();
        return skill instanceof TalentSkill && ((TalentSkill)skill).talent.aimsAtEnemy();
    }

    private void suggestSkillTarget() {
        Skill skill = selectedSkill();
        if (!PartySession.remoteContext() && aimsAtEnemy() && canAssign(skill)
                && !skill.coolDown() && Dungeon.hero.canUseSkill(skill.getManaCost())) {
            cancel();
            useTargeting();
        }
    }

    private boolean confirmSkillTarget() {
        if (PartySession.remoteContext() || !aimsAtEnemy() || !targeting || !GameScene.hasCellSelection()) return false;
        if (canTarget(lastTarget)) GameScene.handleCell(lastTarget.pos);
        else {
            cancel();
            useTargeting();
        }
        return true;
    }

    private static void useSkill(Hero hero, Skill skill) {
        if (skill == null || !hero.isAlive() || !canAssign(skill)) return;
        Runnable use = () -> executeSkill(hero, skill);
        boolean selectsTarget = skill instanceof TalentSkill && ((TalentSkill)skill).talent.targeted();
        if (toggle(skill, hero) || selectsTarget || !LocalCoop.defer(hero, use)) use.run();
    }

    private static void executeSkill(Hero hero, Skill skill) {
        if (!hero.isAlive() || !canAssign(skill)) return;
        remember(hero, skill);
        if (toggle(skill, hero)) {
            TutorialRun.quickslot(hero, skill);
            skill.execute(hero, skill.active ? Skill.AC_DEACTIVATE : Skill.AC_ACTIVATE);
            refresh();
            return;
        }
        if (skill.coolDown()) {
            GLog.w("skills4.wait", (int)Math.ceil(skill.availableAfter - Skill.skillTrack));
            return;
        }
        if (!hero.canUseSkill(skill.getManaCost())) {
            GLog.w("skills4.no_mana");
            return;
        }
        for (String action : skill.actions(hero)) {
            if (!Skill.AC_ADVANCE.equals(action)) {
                TutorialRun.quickslot(hero, skill);
                skill.execute(hero, action);
                return;
            }
        }
    }

    @Override public void update() {
        super.update();
        Skill skill = selectedSkill();
        if (skill != null) slot.icon.visible = false;
        if (skill != shownSkill) {
            shownSkill = skill;
            skillIcon.visible = skill != null;
            if (skill != null) skillIcon.view(skill.image());
            shownCooldown = -1;
            enableSlot();
        }
        boolean usable = skill != null && canAssign(skill);
        for (ColorBlock edge : activeFrame) edge.visible = usable && skill.active;
        skillCount.visible = countBack.visible = skill != null && skill.coolDown();
        if (skill == null) return;
        int remaining = Math.max(0, (int)Math.ceil(skill.availableAfter - Skill.skillTrack));
        if (remaining != shownCooldown) {
            shownCooldown = remaining;
            skillCount.text(remaining > 0 ? Integer.toString(remaining) : "");
            skillCount.measure();
            layout();
        }
        skillIcon.resetColor();
        if (!Dungeon.hero.canUseSkill(skill.getManaCost())) skillIcon.hardlight(0x728AA9);
        skillIcon.alpha(usable && remaining == 0 ? 1 : .4f);
    }

	public void enable( boolean value ) {
		active = value;
		if (value) {
			enableSlot();
		} else {
			slot.enable( false );
		}
	}

	private void enableSlot() {
        if (selectedSkill() != null) { slot.enable(true); return; }
		slot.enable( (!(itemInSlot instanceof Arrow) || Dungeon.hero.belongings.bow != null) &&
			itemInSlot != null &&
			itemInSlot.quantity() > 0 &&
			(Dungeon.hero.belongings.backpack.contains( itemInSlot ) || itemInSlot.isEquipped( Dungeon.hero )));
	}

	private void useTargeting() {

		targeting = canTarget(lastTarget);

		if (!targeting) {
			int n = Dungeon.hero.visibleEnemies();
			for (int i=0; i < n; i++) {
				Mob enemy = Dungeon.hero.visibleEnemy( i );
				if (canTarget(enemy)) {
					lastTarget = enemy;
					targeting = true;
					break;
				}
			}
		}

		if (targeting) {
			if (Actor.all().contains( lastTarget )) {
				lastTarget.sprite.parent.add( crossM );
				crossM.point(Difficulties.is3d ? DungeonTerrainTilemap.raisedTileToWorld( lastTarget.pos ) :  DungeonTilemapOld.tileToWorld( lastTarget.pos ));
				crossB.visible = true;
			} else {
				lastTarget = null;
			}
		}
	}

    private boolean canTarget(Char target) {
        if (target == null || !target.isAlive() || target.pos < 0 || target.pos >= Level.LENGTH
                || !Dungeon.visible[target.pos] || !Actor.all().contains(target)
                || target.sprite == null || target.sprite.parent == null) return false;
        if (aimsAtEnemy() && (!(target instanceof Mob) || !((Mob)target).hostile || target.invisible > 0
                || RebornChallenges.active(RebornChallenges.POINT_BLANK) && !Level.adjacent(Dungeon.hero.pos, target.pos))) return false;
        return Ballistica.cast(Dungeon.hero.pos, target.pos, false, true) == target.pos;
    }

	public static void refresh() {
		if (LocalCoop.isRestoring()) return;
		for (QuickSlot slot : slots) {
			if (slot != null) {
                slot.skillRef = -1;
                slot.item(slot.select());
            }
		}
	}

	public static void target( Item item, Char target ) {
		if (target != Dungeon.hero) {
			lastTarget = target;
			HealthIndicator.instance.target( target );
		}
	}

	public static void cancel() {
		for (QuickSlot slot : slots) {
			if (slot != null) slot.clearTarget();
		}
	}

    private void clearTarget() {
        crossB.visible = false;
        crossM.remove();
        targeting = false;
    }

	private static final String QUICKSLOTS = "quick_slots";

	@SuppressWarnings("unchecked")
	public static void save( Bundle bundle ) {
		Belongings stuff = Dungeon.hero.belongings;
		String[] types = new String[MAX_SLOTS];
		Arrays.fill(types, "");
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (values[i] instanceof Class && stuff.getItem((Class<? extends Item>)values[i]) != null) {
				types[i] = ((Class<?>)values[i]).getName();
			}
		}
		bundle.put(QUICKSLOTS, types);
        int[] skills = new int[MAX_SLOTS];
        Arrays.fill(skills, -1);
        for (int i = 0; i < MAX_SLOTS; i++) if (values[i] instanceof Integer) skills[i] = (Integer)values[i];
        bundle.put("quick_skills", skills);
	}

	public static void save( Bundle bundle, Item item ) {
		int[] assigned = new int[MAX_SLOTS];
		int count = 0;
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (values[i] == item) assigned[count++] = i;
		}
		if (count > 0) bundle.put(QUICKSLOTS, Arrays.copyOf(assigned, count));
	}

	public static void restore( Bundle bundle ) {
		resetSlots();
        int[] skills = bundle.getIntArray("quick_skills");
        if (skills != null) {
            for (int i = 0; i < Math.min(skills.length, MAX_SLOTS); i++) {
                if (skills[i] >= 0 && skills[i] < 64) values[i] = skills[i];
            }
        }
		String[] types = bundle.getStringArray(QUICKSLOTS);
		if (types == null) return;
		for (int i = 0; i < Math.min(types.length, MAX_SLOTS); i++) {
			if (types[i].isEmpty()) continue;
			try {
				values[i] = Class.forName(types[i], false, Item.class.getClassLoader()).asSubclass(Item.class);
			} catch (ClassNotFoundException | ClassCastException ignored) {
			}
		}
	}

	public static void restore( Bundle bundle, Item item ) {
		int[] assigned = bundle.getIntArray(QUICKSLOTS);
		if (assigned == null) return;
		for (int index : assigned) {
			if (index >= 0 && index < MAX_SLOTS) values[index] = item.stackable ? item.getClass() : item;
		}
	}
}
