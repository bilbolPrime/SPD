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
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.noosa.Group;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.Regeneration;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillContainer;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.bags.Keyring;
import com.bilboldev.pixeldungeonskills.items.bags.ScrollHolder;
import com.bilboldev.pixeldungeonskills.items.bags.SeedPouch;
import com.bilboldev.pixeldungeonskills.items.bags.WandHolster;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.SkillSlot;
import com.bilboldev.pixeldungeonskills.ui.SkillSlotNew;
import com.bilboldev.utils.RectF;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.TalentSkill;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RebornSlot;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import java.util.ArrayList;

public class WndSkillsNew extends WndTabbed {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

	protected static final int COLS_P	= 4;
	protected static final int COLS_L	= 6;

	protected static final int SLOT_SIZE	= 14;
	protected static final int SLOT_MARGIN	= 1;

	protected static final int TAB_WIDTH	= 25;

	protected static final int TITLE_HEIGHT	= 12;

	private Listener listener;
	private Message title;
    private int treeTab;
    private final Hero owner;
    private final boolean readOnly;
    private final boolean quickslot;
    private final int previewPoints;

	private int nCols;
	private int nRows;

	protected int count;
	protected int col;
	protected int row;

    public boolean noDegrade = PixelDungeon.itemDeg();
	public WndSkillsNew(Listener listener, Message title) {
		this(listener, title, 0);
	}

    public WndSkillsNew(Listener listener, Message title, int treeTab) {
        this(Dungeon.hero, listener, title, treeTab, false, 0, false);
    }

    public WndSkillsNew(Hero hero, int freePoints) {
        this(hero, null, null, 0, true, freePoints, false);
    }

    public static WndSkillsNew quickslot(Listener listener, int treeTab) {
        return new WndSkillsNew(Dungeon.hero, listener, Message.key("skills4.quickslot"), treeTab, false, 0, true);
    }

    private WndSkillsNew(Hero hero, Listener listener, Message title, int treeTab, boolean readOnly, int freePoints,
                         boolean quickslot) {

		super();

		this.listener = listener;
        owner = hero;
        this.readOnly = readOnly;
        this.quickslot = quickslot;
        previewPoints = freePoints;
		this.title = title;
        menuModel = new MenuModel(title != null ? title : Message.key("windows.wndskills.points_title", points()), Message.empty());
        menuModel.view = quickslot ? MenuModel.View.QUICK_SKILLS : MenuModel.View.SKILLS;
        menuModel.selection = listener != null;
        menuModel.prompt = title;
        this.treeTab = Math.max(0, Math.min(2, treeTab));
        if (owner.talents() != null) {
            menuModel.mode = this.treeTab;
            placeTalents();
            return;
        }

		nCols = 8;
		nRows = 6;

		int slotsWidth = SLOT_SIZE * nCols + SLOT_MARGIN * (nCols - 1);
		int slotsHeight = SLOT_SIZE * nRows + SLOT_MARGIN * (nRows - 1);

		BitmapText txtTitle = PixelScene.createText( buildTitleText(), 9 );
		txtTitle.hardlight( titleColor() );
		txtTitle.measure();
		txtTitle.x = (int)(slotsWidth - txtTitle.width()) / 2;
		txtTitle.y = (int)(TITLE_HEIGHT - txtTitle.height()) / 2;
		add( txtTitle );

		placeSkills();

		resize( slotsWidth, slotsHeight + TITLE_HEIGHT );
	}

	private String buildTitleText() {
		if (title != null) {
			return title.toString();
		}

		String heading = Messages.capitalize( Messages.maybeTranslate( "Skills" ) );
		if (points() > 0) {
			return Messages.get(WndSkillsNew.class, "title_points", heading,
					points(), Messages.maybeTranslate("points)"));
		}
		return heading;
	}

    private int points() { return readOnly ? previewPoints : Skill.availableSkill; }

    public void reopen() {
        hide();
        GameScene.show(new WndSkillsNew(listener, title, treeTab));
    }

    private void placeTalents() {
        int w = boundedWidth(176);
        int h = Math.min(178, maxFrameHeight() - frameHeight());
        ArrayList<HeroSubClass> branches = new ArrayList<>();
        for (TalentSkill skill : owner.talents().talents()) {
            if (!branches.contains(skill.talent.subclass)) branches.add(skill.talent.subclass);
        }
        HeroSubClass branch = branches.get(treeTab);
        String name = (branch == HeroSubClass.NONE ? Message.heroClass(owner.heroClass) : Message.subclass(branch, false)).toString();
        HighlightedText heading = new HighlightedText(9);
        heading.text(title != null ? title.toString() : Messages.capitalize(name), w - 40, false);
        heading.setColor(titleColor(), titleColor());
        heading.setPos(28 + (w - 28 - heading.width()) / 2, 0);
        add(heading);
        BitmapText points = PixelScene.createText(Messages.get("skills4.points", points()), 7);
        points.measure();
        points.hardlight(0xB7ADA3);
        points.x = PixelScene.align((w - points.width()) / 2);
        points.y = heading.bottom() + 3;
        add(points);

        for (HeroSubClass tabBranch : branches) {
            TreeTab tab = new TreeTab(treeIcon(tabBranch));
            tab.setSize(36, tabHeight());
            add(tab);
            menuModel.add(tab, tabBranch == HeroSubClass.NONE ? Message.heroClass(owner.heroClass) : Message.subclass(tabBranch, false));
        }
        resize(w, h);
        select(treeTab);

        ArrayList<TalentButton> nodes = new ArrayList<>();
        float top = points.y + points.height() + 9;
        float rowHeight = (height - top - 30) / 3;
        for (TalentSkill skill : owner.talents().talents()) {
            if (skill.talent.subclass != branch) continue;
            TalentButton node = new TalentButton(skill);
            float center = w * (.18f + skill.talent.column * .32f);
            node.setRect(PixelScene.align(center - 12), PixelScene.align(top + skill.talent.row * rowHeight), 24, 24);
            nodes.add(node);
        }
        for (TalentButton node : nodes) {
            TalentSkill previous = owner.talents().prerequisite(node.skill.talent);
            if (previous == null) continue;
            TalentButton from = null;
            for (TalentButton candidate : nodes) if (candidate.skill == previous) from = candidate;
            if (from == null) continue;
            float x = from.left() + from.width() / 2;
            float y = from.bottom();
            float targetX = node.left() + node.width() / 2;
            float bend = PixelScene.align(y + (node.top() - y) / 2);
            boolean learned = previous.level > 0 && (branch == HeroSubClass.NONE || branch == owner.subClass);
            connection(x, y, x, bend, learned);
            connection(x, bend, targetX, bend, learned);
            connection(targetX, bend, targetX, node.top(), learned);
        }
        for (TalentButton node : nodes) {
            add(node);
            menuModel.add(node, Message.skillName(node.skill));
        }
        Button identity = new Button() {
            final Image icon = treeIcon(branch);
            { add(icon); }
            @Override protected void layout() {
                super.layout();
                icon.x = x + (width - icon.width()) / 2;
                icon.y = y + (height - icon.height()) / 2;
            }
            @Override protected void onClick() {
                if (branch == HeroSubClass.NONE || !readOnly && PartyWindows.select(this)) return;
                WndTitledMessage description = new WndTitledMessage(treeIcon(branch), Message.subclass(branch, false),
                        Message.key("skills4.subclass." + branch.name().toLowerCase(java.util.Locale.ROOT)));
                if (readOnly) WndSkillsNew.this.parent.add(description);
                else GameScene.show(description);
            }
        };
        identity.setRect(0, 0, 24, 24);
        add(identity);
        if (branch != HeroSubClass.NONE) menuModel.add(identity, Message.subclass(branch, false));
    }

    private void connection(float x1, float y1, float x2, float y2, boolean learned) {
        float w = Math.max(1, Math.abs(x2 - x1));
        float h = Math.max(1, Math.abs(y2 - y1));
        ColorBlock shadow = new ColorBlock(w + 2, h + 2, 0xFF17151D);
        shadow.x = Math.min(x1, x2) - 1;
        shadow.y = Math.min(y1, y2) - 1;
        add(shadow);
        ColorBlock line = new ColorBlock(w, h, learned ? 0xFFB49C6F : 0xFF554E61);
        line.x = Math.min(x1, x2);
        line.y = Math.min(y1, y2);
        add(line);
    }

    private Image treeIcon(HeroSubClass branch) {
        if (branch == HeroSubClass.NONE) return LibraryCatalog.heroImage(owner.spritesheet(), owner.tier());
        int icon;
        switch (branch) {
            case GLADIATOR: icon = 128; break;
            case BERSERKER: icon = 129; break;
            case BATTLEMAGE: icon = 130; break;
            case WARLOCK: icon = 131; break;
            case FREERUNNER: icon = 153; break;
            case ASSASSIN: icon = 154; break;
            case WARDEN: icon = 145; break;
            case SNIPER: icon = 146; break;
            case SPIRIT_BINDER: icon = 176; break;
            case LICH: icon = 177; break;
            case MARSHAL: icon = 161; break;
            case EXECUTIONER: icon = 160; break;
            default: throw new IllegalArgumentException("Missing subclass icon");
        }
        return new SkillSprite(icon);
    }

    private class TreeTab extends Tab {
        private final Image icon;

        TreeTab(Image icon) {
            this.icon = icon;
            add(icon);
        }

        @Override protected void layout() {
            super.layout();
            if (icon == null || bg == null) return;
            icon.x = PixelScene.align(WndSkillsNew.this.camera, x + (width - icon.width()) / 2);
            icon.y = PixelScene.align(WndSkillsNew.this.camera,
                    y + bg.marginTop() + (height - bg.marginVer() - icon.height()) / 2);
            icon.alpha(selected ? 1 : .6f);
        }
    }

    private class TalentButton extends Button {
        private final TalentSkill skill;
        private final ColorBlock well;
        private final SkillSprite icon;
        private final BitmapText rank;

        TalentButton(TalentSkill skill) {
            this.skill = skill;
            well = RebornSlot.create(24, 24, 0xFF46404D);
            add(well);
            icon = new SkillSprite(skill.image());
            if (quickslot) {
                active = QuickSlot.canAssign(skill);
                icon.alpha(active ? 1 : .25f);
                well.color(active ? 0xFFB49C6F : 0xFF46404D);
            } else icon.alpha(skill.rank() > 0 ? 1 : skill.tree.canLevel(skill, points()) ? .8f : .6f);
            add(icon);
            rank = new BitmapText(PixelScene.font1x);
            int extra = Math.max(0, skill.rank() - skill.level);
            rank.text(extra > 0 ? skill.level + "+" + extra : Integer.toString(skill.rank()));
            rank.measure();
            rank.visible = skill.rank() > 0;
            rank.hardlight(skill.rank() > skill.level ? 0x86B9DE : 0xE5D1A7);
            add(rank);
        }

        @Override protected void layout() {
            super.layout();
            if (icon == null) return;
            well.x = x;
            well.y = y;
            icon.x = x + 4;
            icon.y = y + 3;
            rank.scale.set(1);
            if (rank.width() > width - 4) rank.scale.set((width - 4) / rank.width());
            rank.x = x + width - rank.width() - 2;
            rank.y = y + height - rank.height() - 1;
        }

        @Override protected void onTouchDown() {
            well.brightness(1.4f);
            Sample.INSTANCE.play(Assets.SND_CLICK);
        }

        @Override protected void onTouchUp() { well.resetColor(); }

        @Override protected void onClick() {
            if (quickslot && !QuickSlot.canAssign(skill)) return;
            if (readOnly) {
                WndSkillsNew.this.parent.add(new WndTitledMessage(new SkillSprite(skill.image()), skill.talent.title(),
                        skill.info(previewPoints, true)));
                return;
            }
            if (PartyWindows.select(this)) return;
            if (listener != null) {
                hide();
                listener.onSelect(skill);
            } else GameScene.show(new WndSkillNew(WndSkillsNew.this, skill));
        }
    }

    protected void placeSkills() {
        placeSkillLine();
        for (SkillContainer skillContainer : owner.skillTree.getSkills()) {
            placeSkill(skillContainer, false);
        }
    }

	protected void placeSkill(final SkillContainer skill, boolean showBackground ) {

		int x = (skill.x != 4 ? SLOT_SIZE / 2 + SLOT_SIZE * 2 * (skill.x - 1) : SLOT_SIZE * 7);
		int y =  SLOT_SIZE + (skill.y - 1) * 3 / 2 * SLOT_SIZE + (skill.y - 1) * SLOT_SIZE / 2;

		if(skill.y == 3){
			y = 4 * SLOT_SIZE;
		}

		if(skill.y == 4){
			y =SLOT_SIZE * 3 + SLOT_SIZE+ SLOT_SIZE *3/2;
		}

		SkillButton button = new SkillButton(skill.skill);
        add(button.setPos(x + 2, y + 2));
        menuModel.add(button, Message.skillName(skill.skill));

		if (++col >= nCols) {
			col = 0;
			row++;
		}

		count++;
	}

	protected void placeSkillLine( ) {

		int x = col * (SLOT_SIZE );
		int y = TITLE_HEIGHT + row * (SLOT_SIZE);

		add( new SkillButtonLine(  ).setPos( x, y ) );

		if (++col >= nCols) {
			col = 0;
			row++;
		}

		count++;
	}

	@Override
	public void onMenuPressed() {
		if (!readOnly && PartyWindows.close(this)) return;
		if (listener == null) {
			hide();
		}
	}

	@Override
	public void onBackPressed() {
		if (!readOnly && PartyWindows.close(this)) return;
		if (listener != null) {
			listener.onSelect( null );
		}
		super.onBackPressed();
	}

	@Override
	protected void onClick( Tab tab ) {
        if (!readOnly && PartyWindows.select(tab)) return;
        Group host = parent;
        hide();
        WndSkillsNew next = new WndSkillsNew(owner, listener, title, tab instanceof TreeTab ? tabs.indexOf(tab) : 0,
                readOnly, previewPoints, quickslot);
        if (readOnly) host.add(next);
        else GameScene.show(next);
	}

	@Override
	protected int tabHeight() {
		return owner != null && owner.talents() != null ? 29 : 20;
	}

	private class BagTab extends Tab {

		private Image icon;

		private Bag bag;

		public BagTab( Bag bag ) {
			super();

			this.bag = bag;

			icon = icon();
			add( icon );
		}

		@Override
		protected void select( boolean value ) {
			super.select( value );
			icon.am = selected ? 1.0f : 0.6f;
		}

		@Override
		protected void layout() {
			super.layout();

			icon.copy( icon() );
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1);
			if (!selected && icon.y < y + CUT) {
				RectF frame = icon.frame();
				frame.top += (y + CUT - icon.y) / icon.texture.height;
				icon.frame( frame );
				icon.y = y + CUT;
			}
		}

		private Image icon() {
			if (bag instanceof SeedPouch) {
				return Icons.get( Icons.SEED_POUCH );
			} else if (bag instanceof ScrollHolder) {
				return Icons.get( Icons.SCROLL_HOLDER );
			} else if (bag instanceof WandHolster) {
				return Icons.get( Icons.WAND_HOLSTER );
			} else if (bag instanceof Keyring) {
				return Icons.get( Icons.KEYRING );
			} else {
				return Icons.get( Icons.BACKPACK );
			}
		}
	}

	private static class Placeholder extends Skill {
		{
			name = null;
		}

		public Placeholder( int image ) {
			this.image = image;
		}
	}

	private class SkillButton extends SkillSlotNew {

		private static final int NORMAL		= 0xFF4A4D44;
		private static final int EQUIPPED	= 0xFF63665B;

		private Skill skill;
		private ColorBlock bg;

		private ColorBlock durability[];

		public SkillButton(Skill skill) {

			super( skill );

			this.skill = skill;

			width = height = SLOT_SIZE;

            durability = new ColorBlock[Skill.MAX_LEVEL];

            if(skill != null && skill.name != null && skill.level > 0  && skill.level <= Skill.MAX_LEVEL) {
                for (int i = 0; i < skill.level; i++) {
                    durability[i] = new ColorBlock(2, 2, 0xFF00EE00);

                }
                for (int i = skill.level; i < Skill.MAX_LEVEL; i++) {
                    durability[i] = new ColorBlock(2, 2, 0x4000EE00);

                }
            }

			bg.visible = false;
		}

		@Override
		protected void createChildren() {
			bg = com.bilboldev.pixeldungeonskills.ui.RebornSlot.create(SLOT_SIZE, SLOT_SIZE, NORMAL);
			add( bg );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

            if(skill != null && skill.name != null && skill.level > 0  && skill.level <= Skill.MAX_LEVEL) {
                for (int i = 0; i < Skill.MAX_LEVEL; i++) {
                    durability[i].x = x + width - 9 + i * 3;
                    durability[i].y = y + 3;

                }
            }

			super.layout();
		}

		@Override
		protected void onTouchDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
		};

		protected void onTouchUp() {
			bg.brightness( 1.0f );
		};

		@Override
		protected void onClick() {
            if (PartyWindows.select(this)) return;
            if (listener != null) {

                hide();
                listener.onSelect(skill);

            } else {

				GameScene.show( new WndSkillNew( WndSkillsNew.this, skill ) );

            }
		}

		@Override
		protected boolean onLongClick() {
				return true;
		}
	}

	private class SkillButtonLine extends SkillSlot {

		private static final int NORMAL		= 0xFF4A4D44;
		private static final int EQUIPPED	= 0xFF63665B;

		private ColorBlock bg;

		private ColorBlock durability[];

		public SkillButtonLine() {

			super( new Regeneration(){
				@Override
				public int image() {
					return 5;
				}
			});

			width = height = SLOT_SIZE;

			bg.visible = true;
		}

		@Override
		protected void createChildren() {

			bg = com.bilboldev.pixeldungeonskills.ui.RebornSlot.create(SLOT_SIZE, SLOT_SIZE, NORMAL);
			bg.x = x + SLOT_SIZE / 2;
			bg.y = y  + SLOT_SIZE ;

			for(ColorBlock bg : owner.skillTree.setCoords(x, y).getBackground()){
				add(bg);
			}

			super.createChildren();
		}

		@Override
		protected void layout() {

			super.layout();
		}

		@Override
		protected void onTouchDown() {

		};

		protected void onTouchUp() {
			bg.brightness( 1.0f );
		};

		@Override
		protected void onClick() {

		}

		@Override
		protected boolean onLongClick() {
			return true;
		}
	}

	public interface Listener {
		void onSelect(Skill skill);
	}
}
