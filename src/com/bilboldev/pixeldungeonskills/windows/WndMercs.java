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

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryCamp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryRecruitment;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillB;
import com.bilboldev.pixeldungeonskills.actors.skills.MercBruteSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercThiefSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercWizardSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.ClothArmor;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.food.ChargrilledMeat;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Dagger;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Knuckles;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.FrostBow;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.RectF;
import java.util.ArrayList;

public class WndMercs extends WndTabbed {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.PARCHMENT;
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return equipmentControls.scrollPane(content);
    }

	public static enum Mode {
		ALL,
		BRUTE,
		WIZARD,
		THIEF,
		ARCHER,
		ARCHERMAIDEN
	}

    float pos = 5;
    float GAP = 2;

    private static final int WIDTH_P	= 120;
    private static final int WIDTH_L	= 144;

    protected static final int TAB_WIDTH	= 25;

    private static final String TXT_TITLE =  "Hire A Mercenary";

    private static final String TXT_MERCENARIES_DETAIL = "Mercenaries will fight for you in exchange for a fee.\n \n"
            + "There are five mercenary classes each with strengths and weaknesses. \n"
            + "Each class has one skill with the exception of the Archer Maiden who knows two.\n"
            + "Skills level with the mercenary and are capped at level 3. \n"
            + "Mercenaries have the same level as the hero and level with him. \n \n"
            + "You can unequip an item from a merc by tapping on it and holding down. \n \n"
            + "Mercs will consume any healing potion equipped on them if about to die. \n \n"
            + "You cannot hire the mercenary equivalent of your class.";

    private static final String TXT_NO_GOLD = "Insufficient Gold";
    private final ControlRow equipmentControls = new ControlRow();

    private final MercenaryCamp camp;

    public static Mode modeFor(HiredMerc.MERC_TYPES type) {
        switch (type) {
            case Brute: return Mode.BRUTE;
            case Wizard: return Mode.WIZARD;
            case Thief: return Mode.THIEF;
            case Archer: return Mode.ARCHER;
            case ArcherMaiden: return Mode.ARCHERMAIDEN;
            default: throw new IllegalArgumentException("Unknown mercenary");
        }
    }

    public WndMercs(final Mode mode) {
        this(mode, null);
    }

    public WndMercs(final Mode mode, MercenaryCamp camp)
    {
        super();
        this.camp = camp;
        Message nameText = MercenaryRecruitment.nameText(getMercType(mode));
        menuModel = mode == Mode.ALL
                ? new MenuModel(Message.key("custom.generated.entry_hire_a_mercenary_719a392723"),
                        Message.key("custom.generated.entry_mercenaries_will_fight_for_you_in_exchange_for_a_45fbaccd78"))
                : new MenuModel(Message.key(mode == Mode.ARCHER || mode == Mode.ARCHERMAIDEN ? "custom.merc.hire_title_an" : "custom.merc.hire_title_a", nameText),
                        Message.paragraphs(getMercDetails(mode), getMercStats(mode)));

        int width = boundedWidth(landscapeLayout() ? WIDTH_L : WIDTH_P);
        Component titlebar;
        Component content = new Component();
        ArrayList<RedButton> actions = new ArrayList<>();

        if(mode == Mode.ALL) {
            titlebar = new IconTitle(new SkillSprite(96), TXT_TITLE);
            titlebar.setRect(0, 0, width, 0);
            add(titlebar);

            pos = 0;

            BitmapTextMultiline info = PixelScene.createMultiline(6);
            content.add(info);

            info.text(TXT_MERCENARIES_DETAIL);
            info.maxWidth = textWidth(width);
            info.measure();
            info.y = pos;

            pos = (int) info.y + (int) info.height() + GAP * 2;

        }
        else
        {
            String mercName = MercenaryRecruitment.displayName(getMercType(mode));
            titlebar = new IconTitle(new SkillSprite(getImage(mode)), Messages.get(
                    mode == Mode.ARCHER || mode == Mode.ARCHERMAIDEN ? "custom.merc.hire_title_an" : "custom.merc.hire_title_a",
                    new Object[]{mercName}));
            titlebar.setRect(0, 0, width, 0);
            add(titlebar);

            pos = 0;

            BitmapTextMultiline info = PixelScene.createMultiline(6);
            content.add(info);

            info.text(getMercDetails(mode).toString());
            info.maxWidth = textWidth(width);
            info.measure();
            info.y = pos;

            pos = (int) info.y + (int) info.height() + GAP * 2;

            BitmapText stats = PixelScene.createText(Messages.get(WndMercs.class, "stats", mercName), 9);
            stats.hardlight( titleColor() );
            stats.measure();
            content.add( stats );

            stats.y = pos;

            pos = stats.y + stats.height() + GAP;

            BitmapTextMultiline infoStats = PixelScene.createMultiline(6);
            content.add(infoStats);

            infoStats.text(getMercStats(mode).toString());
            infoStats.maxWidth = textWidth(width);
            infoStats.measure();
            infoStats.y = pos;

            pos = infoStats.y + infoStats.height() + 2 * GAP;

            content.setSize(textWidth(width), pos);
            pos = 0;
            BitmapText equipment = PixelScene.createText( Messages.maybeTranslate( "Standard Layout" ), 9 );
            equipment.hardlight( titleColor() );
            equipment.measure();
            equipmentControls.add( equipment );

            equipment.y = pos;

            pos = equipment.y + equipment.height() + GAP;

            if(mode == Mode.BRUTE) {
                final Image imageWeapon = new ItemSprite(new Mace());
                equipmentControls.add(imageWeapon);

                imageWeapon.x = 0;
                imageWeapon.y = pos;

                TouchArea hotArea_imageWeapon = new TouchArea(imageWeapon) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageWeapon);
                        WndMercs.this.add(new previewInformation(tmp, new Mace()));
                    }
                };
                equipmentControls.add(hotArea_imageWeapon);

                final Image imageArmor = new ItemSprite(new LeatherArmor());
                equipmentControls.add(imageArmor);

                imageArmor.x = imageWeapon.x + imageWeapon.width() + GAP;
                imageArmor.y = pos;

                TouchArea hotArea_imageArmo = new TouchArea(imageArmor) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageArmor);
                        WndMercs.this.add(new previewInformation(tmp, new LeatherArmor()));
                    }
                };
                equipmentControls.add(hotArea_imageArmo);

                final Image image = new ItemSprite(new ChargrilledMeat());
                equipmentControls.add(image);

                image.x = imageArmor.x + imageArmor.width() + GAP;
                image.y = pos + 2;

                TouchArea hotArea_image = new TouchArea(image) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(image);
                        WndMercs.this.add(new previewInformation(tmp, new ChargrilledMeat()));
                    }
                };
                equipmentControls.add(hotArea_image);

                final Image imageSkill = new SkillSprite(3);
                equipmentControls.add(imageSkill);

                imageSkill.x = image.x + image.width() + GAP;
                imageSkill.y = pos;

                TouchArea hotArea_imageSkill = new TouchArea(imageSkill) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkill);
                        WndMercs.this.add(new previewInformation(tmp, new MercBruteSkillA()));
                    }
                };
                equipmentControls.add(hotArea_imageSkill);

                pos = image.y + image.height() + GAP * 3;
            }
            else if(mode == Mode.WIZARD)
            {
                final Image imageWeapon = new ItemSprite(new Knuckles());
                equipmentControls.add(imageWeapon);

                imageWeapon.x = 0;
                imageWeapon.y = pos + 2;

                TouchArea hotArea_imageWeapon = new TouchArea(imageWeapon) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageWeapon);
                        WndMercs.this.add(new previewInformation(tmp, new Knuckles()));
                    }
                };
                equipmentControls.add(hotArea_imageWeapon);

                final Image imageArmor = new ItemSprite(new ClothArmor());
                equipmentControls.add(imageArmor);

                imageArmor.x = imageWeapon.x + imageWeapon.width() + GAP;
                imageArmor.y = pos + 2;

                TouchArea hotArea_imageArmo = new TouchArea(imageArmor) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageArmor);
                        WndMercs.this.add(new previewInformation(tmp, new ClothArmor()));
                    }
                };
                equipmentControls.add(hotArea_imageArmo);

                final Image image = new ItemSprite(new PotionOfHealing());
                equipmentControls.add(image);

                image.x = imageArmor.x + imageArmor.width() + GAP;
                image.y = pos + 2;

                TouchArea hotArea_image = new TouchArea(image) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(image);
                        WndMercs.this.add(new previewInformation(tmp, new PotionOfHealing()));
                    }
                };
                equipmentControls.add(hotArea_image);

                final Image imageSkill = new SkillSprite(41);
                equipmentControls.add(imageSkill);

                imageSkill.x = image.x + image.width() + GAP;
                imageSkill.y = pos;

                TouchArea hotArea_imageSkill = new TouchArea(imageSkill) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkill);
                        WndMercs.this.add(new previewInformation(tmp, new MercWizardSkillA()));
                    }
                };
                equipmentControls.add(hotArea_imageSkill);

                pos = image.y + image.height() + GAP * 3;
            }
            else if(mode == Mode.THIEF)
            {
                final Image imageWeapon = new ItemSprite(new Dagger());
                equipmentControls.add(imageWeapon);

                imageWeapon.x = 0;
                imageWeapon.y = pos + 1;

                TouchArea hotArea_imageWeapon = new TouchArea(imageWeapon) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageWeapon);
                        WndMercs.this.add(new previewInformation(tmp, new Dagger()));
                    }
                };
                equipmentControls.add(hotArea_imageWeapon);

                final Image imageArmor = new ItemSprite(new ClothArmor());
                equipmentControls.add(imageArmor);

                imageArmor.x = imageWeapon.x + imageWeapon.width() + GAP;
                imageArmor.y = pos + 2;

                TouchArea hotArea_imageArmo = new TouchArea(imageArmor) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageArmor);
                        WndMercs.this.add(new previewInformation(tmp, new ClothArmor()));
                    }
                };
                equipmentControls.add(hotArea_imageArmo);

                final Image image = new ItemSprite(new PotionOfHealing());
                equipmentControls.add(image);

                image.x = imageArmor.x + imageArmor.width() + GAP;
                image.y = pos + 2;

                TouchArea hotArea_image = new TouchArea(image) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(image);
                        WndMercs.this.add(new previewInformation(tmp, new PotionOfHealing()));
                    }
                };
                equipmentControls.add(hotArea_image);

                final Image imageSkill = new SkillSprite(57);
                equipmentControls.add(imageSkill);

                imageSkill.x = image.x + image.width() + GAP;
                imageSkill.y = pos;

                TouchArea hotArea_imageSkill = new TouchArea(imageSkill) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkill);
                        WndMercs.this.add(new previewInformation(tmp, new MercThiefSkillA()));
                    }
                };
                equipmentControls.add(hotArea_imageSkill);

                pos = image.y + image.height() + GAP * 3;
            }
            else if(mode == Mode.ARCHER)
            {
                final Image imageWeapon = new ItemSprite(new Bow());
                equipmentControls.add(imageWeapon);

                imageWeapon.x = 0;
                imageWeapon.y = pos + 1;

                TouchArea hotArea_imageWeapon = new TouchArea(imageWeapon) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageWeapon);
                        WndMercs.this.add(new previewInformation(tmp, new Bow()));
                    }
                };
                equipmentControls.add(hotArea_imageWeapon);

                final Image imageArmor = new ItemSprite(new ClothArmor());
                equipmentControls.add(imageArmor);

                imageArmor.x = imageWeapon.x + imageWeapon.width() + GAP;
                imageArmor.y = pos + 2;

                TouchArea hotArea_imageArmo = new TouchArea(imageArmor) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageArmor);
                        WndMercs.this.add(new previewInformation(tmp, new ClothArmor()));
                    }
                };
                equipmentControls.add(hotArea_imageArmo);

                final Image image = new ItemSprite(new PotionOfHealing());
                equipmentControls.add(image);

                image.x = imageArmor.x + imageArmor.width() + GAP;
                image.y = pos + 2;

                TouchArea hotArea_image = new TouchArea(image) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(image);
                        WndMercs.this.add(new previewInformation(tmp, new PotionOfHealing()));
                    }
                };
                equipmentControls.add(hotArea_image);

                final Image imageSkill = new SkillSprite(82);
                equipmentControls.add(imageSkill);

                imageSkill.x = image.x + image.width();
                imageSkill.y = pos + 1;

                TouchArea hotArea_imageSkill = new TouchArea(imageSkill) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkill);
                        WndMercs.this.add(new previewInformation(tmp, new MercArcherSkillA()));
                    }
                };
                equipmentControls.add(hotArea_imageSkill);

                pos = image.y + image.height() + GAP * 3;
            }
            else if(mode == Mode.ARCHERMAIDEN)
            {
                final Image imageWeapon = new ItemSprite(new FrostBow());
                equipmentControls.add(imageWeapon);

                imageWeapon.x = 0;
                imageWeapon.y = pos + 1;

                TouchArea hotArea_imageWeapon = new TouchArea(imageWeapon) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageWeapon);
                        WndMercs.this.add(new previewInformation(tmp, new FrostBow()));
                    }
                };
                equipmentControls.add(hotArea_imageWeapon);

                final Image image = new ItemSprite(new PotionOfHealing());
                equipmentControls.add(image);

                image.x = imageWeapon.x + imageWeapon.width() + GAP;
                image.y = pos + 2;

                TouchArea hotArea_image = new TouchArea(image) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(image);
                        WndMercs.this.add(new previewInformation(tmp, new PotionOfHealing()));
                    }
                };
                equipmentControls.add(hotArea_image);

                final Image imageSkill = new SkillSprite(82);
                equipmentControls.add(imageSkill);

                imageSkill.x = image.x + image.width();
                imageSkill.y = pos + 1;

                TouchArea hotArea_imageSkill = new TouchArea(imageSkill) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkill);
                        WndMercs.this.add(new previewInformation(tmp, new MercArcherSkillA()));
                    }
                };
                equipmentControls.add(hotArea_imageSkill);

                final Image imageSkillB = new SkillSprite(106);
                equipmentControls.add(imageSkillB);

                imageSkillB.x = imageSkill.x + imageSkill.width() + GAP;
                imageSkillB.y = pos + 1;

                TouchArea hotArea_imageSkillB = new TouchArea(imageSkillB) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        Image tmp = new Image();
                        tmp.copy(imageSkillB);
                        WndMercs.this.add(new previewInformation(tmp, new MercArcherSkillB()));
                    }
                };
                equipmentControls.add(hotArea_imageSkillB);

                pos = image.y + image.height() + GAP * 3;
            }

            equipmentControls.setSize(textWidth(width), pos);
            equipmentControls.setPos(0, content.height());
            content.add(equipmentControls);
            content.setSize(textWidth(width), equipmentControls.bottom());
            if ((Dungeon.rules().allowsOnDemandHiring() || MercenaryRecruitment.atRecruiter(camp))
                    && (mode != Mode.ARCHERMAIDEN || MercenaryRecruitment.maidenAvailable())) {
                RedButton btnHire = new ActionButton(Messages.get("custom.merc.hire_button",
                        new Object[]{mercName, getGoldCost(mode)})) {
                    @Override
                    protected void onClick() {
                        final HiredMerc previous = Dungeon.hero.hiredMerc;
                        if (WndMercs.this.camp != null && previous != null) {
                            GameScene.show(new WndOptions(Message.key(WndMercs.class, "replace_title"),
                                    Message.key(WndMercs.class, "replace_message", nameText, getGoldCost(mode)),
                                    Message.key(WndMercs.class, "replace_confirm"),
                                    Message.key("scenes.startscene.cancel_run")) {
                                @Override
                                protected void onSelect(int index) {
                                    if (index == 0) completeHire(mode, previous);
                                }
                            });
                        } else completeHire(mode, null);
                    }
                };

                btnHire.setSize(Math.min(width, 120), 20);
                actions.add(btnHire);
                menuModel.add(btnHire, Message.key("custom.merc.hire_button", nameText, getGoldCost(mode)));

            }
            else if (Dungeon.rules().allowsOnDemandHiring())
            {
                RedButton btnHire = new ActionButton("Unlock") {
                    @Override
                    protected void onClick() {
                        Image tmp = new  SkillSprite(104);
                        parent.add(new previewInformation(tmp,
                                Message.key("custom.generated.entry_archer_maiden_760746f689"),
                                Message.key("custom.generated.entry_please_consider_donating_to_unlock_this_feature_5b14e9ead8")));
                    }
                };

                btnHire.setSize(Math.min(width, 120), 20);
                actions.add(btnHire);
                menuModel.add(btnHire, Message.key("custom.generated.entry_unlock_1526a17ee7"));

            }

        }

        if (mode == Mode.ALL) content.setSize(textWidth(width), pos);
        layoutContent(width, titlebar, content, GAP * 2, actions.toArray(new RedButton[0]));

        if (camp != null) return;

        MercenaryTab tabAll = new MercenaryTab(Mode.ALL);
        tabAll.setSize(TAB_WIDTH, tabHeight());
        add(tabAll);
        menuModel.add(tabAll, Message.key("custom.generated.entry_hire_a_mercenary_719a392723"));
        tabAll.select(mode == Mode.ALL);

        if (Dungeon.hero.heroClass != HeroClass.WARRIOR && Dungeon.hero.heroClass != HeroClass.MERCENARY) {
            MercenaryTab tabBrute = new MercenaryTab(Mode.BRUTE);
            tabBrute.setSize(TAB_WIDTH, tabHeight());
            add(tabBrute);
        menuModel.add(tabBrute, MercenaryRecruitment.nameText(getMercType(tabBrute.mode)));
            tabBrute.select(mode == Mode.BRUTE);
        }

        if (Dungeon.hero.heroClass != HeroClass.MAGE && Dungeon.hero.heroClass != HeroClass.NECROMANCER) {
        MercenaryTab tabWizard = new MercenaryTab(Mode.WIZARD);
        tabWizard.setSize(TAB_WIDTH, tabHeight());
        add(tabWizard);
        menuModel.add(tabWizard, MercenaryRecruitment.nameText(getMercType(tabWizard.mode)));
        tabWizard.select(mode == Mode.WIZARD);
        }

        if(Dungeon.hero.heroClass != HeroClass.ROGUE) {
        MercenaryTab tabThief = new MercenaryTab(Mode.THIEF);
        tabThief.setSize(TAB_WIDTH, tabHeight());
        add(tabThief);
        menuModel.add(tabThief, MercenaryRecruitment.nameText(getMercType(tabThief.mode)));
        tabThief.select(mode == Mode.THIEF);
        }

        if (Dungeon.hero.heroClass != HeroClass.HUNTRESS
                && (!Dungeon.rules().hasRebornEncounters() || !MercenaryRecruitment.maidenAvailable())) {
        MercenaryTab tabArcher = new MercenaryTab(Mode.ARCHER);
        tabArcher.setSize(TAB_WIDTH, tabHeight());
        add(tabArcher);
        menuModel.add(tabArcher, MercenaryRecruitment.nameText(getMercType(tabArcher.mode)));
        tabArcher.select(mode == Mode.ARCHER);
        }

        if (!Dungeon.rules().hasRebornEncounters() || (Dungeon.hero.heroClass != HeroClass.HUNTRESS
                && MercenaryRecruitment.maidenAvailable())) {
        MercenaryTab tabMaiden = new MercenaryTab(Mode.ARCHERMAIDEN);
        tabMaiden.setSize(TAB_WIDTH, tabHeight());
        add(tabMaiden);
        menuModel.add(tabMaiden, MercenaryRecruitment.nameText(getMercType(tabMaiden.mode)));
        tabMaiden.select(mode == Mode.ARCHERMAIDEN);
        }

    }

    private void completeHire(Mode mode, HiredMerc replacement) {
        Message error = MercenaryRecruitment.hire(getMercType(mode), camp, replacement);
        if (error != null) GameScene.show(new WndMessage(error));
        else hide();
    }

    private Message getMercDetails(Mode mode)
    {
        switch(mode)
        {
            case BRUTE: return Message.key("custom.generated.entry_brutes_are_strong_mercenaries_who_rely_in_physic_fd81660656");
            case WIZARD: return Message.key("custom.generated.entry_wizards_choose_the_path_of_magic_they_are_physic_55a9e927c6");
            case THIEF: return Message.key("custom.generated.entry_thieves_rely_on_stealth_and_poison_in_combat_eac_3b37bfa564");
            case ARCHER: return Message.key("custom.generated.entry_archers_are_physically_weak_so_they_strike_from_537fd3fc47");
            case ARCHERMAIDEN: return Message.key("custom.generated.entry_archer_maidens_are_elite_archers_only_the_select_4286165e61");
        }

        return Message.key("custom.generated.entry_brutes_are_strong_mercenaries_who_rely_in_physic_fd81660656");
    }

    private Message getMercStats(Mode mode)
    {
        switch(mode)
        {
            case BRUTE: return Message.key("custom.generated.entry_health_20_level_x_3_strength_13_level_x_0_33_spe_e7e4bca58b");

            case WIZARD: return Message.key("custom.generated.entry_health_10_level_strength_10_level_x_0_2_speed_no_071549bb80");
            case THIEF:  return Message.key("custom.generated.entry_health_15_level_x_2_strength_13_level_x_0_25_spe_a03557c072");
            case ARCHER:  return Message.key("custom.generated.entry_health_15_level_x_2_strength_11_level_x_0_25_spe_246d573315");
            case ARCHERMAIDEN:  return Message.key("custom.generated.entry_health_17_level_x_2_strength_12_level_x_0_25_spe_a604f99651");
        }

        return Message.key("custom.generated.entry_health_20_level_x_3_strength_13_level_x_0_33_spe_e7e4bca58b");
    }
    private int getImage(Mode mode)
    {
        switch (mode)
        {
            case BRUTE: return 0;
            case WIZARD: return 24;
            case THIEF: return 48;
            case ARCHER: return 72;
            case ARCHERMAIDEN: return 104;
        }

        return 0;
    }

    private String getName(Mode mode)
    {
        switch (mode)
        {
            case BRUTE: return "Brute";
            case WIZARD: return "Wizard";
            case THIEF: return "Thief";
            case ARCHER: return "Archer";
            case ARCHERMAIDEN: return "ArcherMaiden";
        }

        return "Brute";
    }

    private HiredMerc.MERC_TYPES getMercType(Mode mode)
    {
        switch (mode)
        {
            case BRUTE: return HiredMerc.MERC_TYPES.Brute;
            case WIZARD: return HiredMerc.MERC_TYPES.Wizard;
            case THIEF: return HiredMerc.MERC_TYPES.Thief;
            case ARCHER: return HiredMerc.MERC_TYPES.Archer;
            case ARCHERMAIDEN: return HiredMerc.MERC_TYPES.ArcherMaiden;
        }

        return HiredMerc.MERC_TYPES.Brute;
    }

    private int getGoldCost(Mode mode)
    {
        return mode == Mode.ALL ? 0 : MercenaryRecruitment.cost(getMercType(mode), Dungeon.hero.lvl);
    }

    @Override
    protected void onClick( Tab tab ) {

        parent.add(new WndMercs(((MercenaryTab)tab).mode, camp));
        hide();
    }

    private class MercenaryTab extends Tab {

        private Image icon;

        Mode mode = Mode.BRUTE;

        public MercenaryTab(Mode mode  ) {
            super();

            this.mode = mode;

            switch (mode)
            {
                case ALL:  icon = Icons.get( Icons.ALL_MERCS ); break;
                case ARCHER:  icon = Icons.get( Icons.ARCHER ); break;
                case BRUTE: icon = Icons.get( Icons.BRUTE ); break;
                case WIZARD: icon = Icons.get( Icons.WIZARD ); break;
                case THIEF: icon = Icons.get( Icons.THIEF ); break;
                case ARCHERMAIDEN: icon = Icons.get( Icons.ARCHER_MAIDEN ); break;

            }

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

            icon.copy( icon );
            icon.x = x + (width - icon.width) / 2;
            icon.y = y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1);
            if (!selected && icon.y < y + CUT) {
                RectF frame = icon.frame();
                frame.top += (y - icon.y) / icon.texture.height;
                icon.frame( frame );
                icon.y = y;
            }
        }
    }

    private static class MercenaryTitle extends Component {

        private static final int GAP	= 2;

        private SkillSprite image;
        private BitmapText title;

        public MercenaryTitle(int image, String name) {

            this.image = new SkillSprite(image);

            title = PixelScene.createText( Utils.capitalize(name ), 9 );
            title.hardlight( titleColor() );
            title.measure();
            add( title );
            add(this.image);

        }

        @Override
        protected void layout() {

            image.x = 0;
            image.y = Math.max( 0, title.height() + GAP - image.height );

            title.x = image.width + GAP;
            title.y = image.height - GAP - title.baseLine();

            height = image.y + image.height();
        }
    }

    private static String localizePreviewText( String text ) {
        if (text == null || text.length() == 0) {
            return "";
        }

        if (text.indexOf('\n') < 0) {
            return Messages.maybeTranslate( text );
        }

        String[] lines = text.split("\\n", -1);
        StringBuilder builder = new StringBuilder( text.length() );
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                builder.append( '\n' );
            }
            if (lines[i].length() == 0) {
                continue;
            }
            builder.append( Messages.maybeTranslate( lines[i] ) );
        }
        return builder.toString();
    }

    private class previewInformation extends WndScrollable
    {
        public previewInformation(Image image, Item item)
        {
            this( image, item == null ? Message.empty() : Message.itemName(item), item == null ? Message.empty() : Message.description(item) );
        }

        public previewInformation(Image image, Skill skill)
        {
            this( image, skill == null ? Message.empty() : Message.skillName(skill), skill == null ? Message.empty() : Message.skillDescription(skill) );
        }

        public previewInformation(Image image, Message title, Message description)
        {

            menuModel = new MenuModel(title, description);
            IconTitle titlebar = new IconTitle();
            titlebar.icon( image);
            titlebar.label( Utils.capitalize(title.toString()), titleColor() );
            int windowWidth = boundedWidth(100);
            titlebar.setRect( 0, 0, windowWidth, 0 );
            add( titlebar );

            BitmapTextMultiline txtInfo = PixelScene.createMultiline( description.toString(), 6 );
            txtInfo.maxWidth = textWidth(windowWidth);
            txtInfo.measure();
            layoutContent(windowWidth, titlebar, textContent(txtInfo), GAP);
        }
    }

}
