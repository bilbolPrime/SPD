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

import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatScout;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.ClothArmor;
import com.bilboldev.pixeldungeonskills.items.food.ChargrilledMeat;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.food.FrozenCarpaccio;
import com.bilboldev.pixeldungeonskills.items.food.MysteryMeat;
import com.bilboldev.pixeldungeonskills.items.food.OverpricedRation;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.ui.CheckBox;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.RectF;

import java.util.ArrayList;

public class WndRatKingGauntlet extends Window {

    private static final String TXT_NO_GOLD = "Insufficient Gold";


    protected static final int COLS_P	= 4;
    protected static final int COLS_L	= 6;

    protected static final int SLOT_SIZE	= 28;
    protected static final int SLOT_MARGIN	= 1;

    protected static final int TAB_WIDTH	= 25;

	private static final String TXT_PLUS = "+";
	private static final String TXT_MINUS = "-";
	private static final String TXT_CHAMPION_CHANCE = "Champions: %s";


    public static enum Mode {
        INTRO,
        WAITING,
        NEWLEVEL
    }

	private static final String TXT_TRUE_KING_TITLE	= "A King.. I Think";

    private static final String TXT_PEONS	= "Useless peons";

    private static final String TXT_SHOP	= "Not Stolen Items";

	private static final int WIDTH		= 112;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP 		= 2;


    private Mode mode;


	public WndRatKingGauntlet(Mode mode) {
		super();

    this.mode = mode;
        int maxHeight = 20;
            Component titlebar = new IconTitle(Icons.RAT_KING.get(), TXT_TRUE_KING_TITLE);
            titlebar.setRect(0, 0, WIDTH, 0);
            add(titlebar);

            String description = getDescription(mode);
            BitmapTextMultiline txtInfo = PixelScene.createMultiline( description, 6 );
            txtInfo.maxWidth = WIDTH;
            txtInfo.measure();
            txtInfo.x = titlebar.left();
            txtInfo.y = titlebar.bottom() + GAP;
            add( txtInfo );

            if(maxHeight < (int) txtInfo.y + (int)txtInfo.height() )
                maxHeight = (int) txtInfo.y + (int)txtInfo.height();

            if(mode == Mode.NEWLEVEL){
                // Armor
                Item armor =  Generator.randomGauntletArmor();
                try
                {
                    if(((Armor)armor).multiplicity()){
                        ((Armor)armor).inscribe( null );;
                    }
                }
                catch (Exception e){

                }
                String descriptionItem = armor.coreName();
                BitmapTextMultiline txtInfoItem = PixelScene.createMultiline( descriptionItem, 6 );
                txtInfoItem.maxWidth = WIDTH;
                txtInfoItem.measure();
                txtInfoItem.x = SLOT_MARGIN * 6 + (SLOT_SIZE - txtInfoItem.width()) / 2;
                txtInfoItem.y = txtInfo.y + txtInfo.height() + GAP;
                add( txtInfoItem );

                // Weapon
                Item weapon =  Generator.randomGauntletWeapon();
                descriptionItem = weapon.coreName();
                txtInfoItem = PixelScene.createMultiline( descriptionItem, 6 );
               txtInfoItem.maxWidth = WIDTH;
               txtInfoItem.measure();
               txtInfoItem.x = SLOT_MARGIN * 6 + SLOT_SIZE + SLOT_MARGIN * 7 + (SLOT_SIZE - txtInfoItem.width()) / 2;
               txtInfoItem.y = txtInfo.y + txtInfo.height() + GAP;
                add( txtInfoItem );

                // Misc
                Item misc =  Generator.randomGauntletMisc();
                descriptionItem = misc.coreName();
                txtInfoItem = PixelScene.createMultiline( descriptionItem, 6 );
                txtInfoItem.maxWidth = WIDTH;
                txtInfoItem.measure();
                txtInfoItem.x = SLOT_MARGIN * 6 + SLOT_SIZE + SLOT_MARGIN * 7 + SLOT_SIZE + SLOT_MARGIN * 7 + (SLOT_SIZE - txtInfoItem.width()) / 2;
                txtInfoItem.y = txtInfo.y + txtInfo.height() + GAP;
                add( txtInfoItem );

                if(maxHeight < (int) txtInfoItem.y + (int)txtInfoItem.height() )
                    maxHeight = (int) txtInfoItem.y + (int)txtInfoItem.height();


                add( new ItemButton(armor, false).setPos( SLOT_MARGIN * 6 , txtInfoItem.y + txtInfoItem.height() + GAP) );
                add( new ItemButton(weapon, false).setPos(  SLOT_MARGIN * 6 + SLOT_SIZE + SLOT_MARGIN * 7, txtInfoItem.y + txtInfoItem.height() + GAP) );
                add( new ItemButton( misc, false).setPos(  SLOT_MARGIN * 6 + SLOT_SIZE + SLOT_MARGIN * 7 + SLOT_SIZE + SLOT_MARGIN * 7 , txtInfoItem.y + txtInfoItem.height() + GAP) );
                maxHeight += SLOT_SIZE + SLOT_MARGIN * 2;
            }


            resize(WIDTH, maxHeight);
	}

	@Override
    public void onBackPressed() {
	    if(mode != Mode.NEWLEVEL){
            hide();
        }
    }

    private String getDescription(Mode mode){
	    if(mode == Mode.INTRO){
            return "Welcome to my dungeon non-rodent. Here no one questions my rule.\n\n" +
                    "My army is almost ready to invade the world. However, I need one more peon for my scheme.\n" +
                    "Do not feel blessed fool, you are just means to an end.\n" +
                    "I must put my army to the test before unleashing my wrath on all the non-rodent cheese eaters.\n" +
                    "In the next room you will find the weakest of my army.\n\n" +
                    "IF you destroy him, I will reward you. And then we do this all over again till you die...";
        }

        if(mode == Mode.WAITING){
            return "I am waiting pet... go and fight!";
        }

        if(mode == Mode.NEWLEVEL){
            return "YOU SURVIVED?!\n" +
                    "FINE... choose your reward and go die!\n\nONLY ONE!!!\n";
        }

	    return "I don't know what to say.. talk to the dev";
    }


    private class previewInformation extends Window
    {
        public previewInformation(Image image, String title, String description)
        {

            IconTitle titlebar = new IconTitle();
            titlebar.icon( image);
            titlebar.label( Utils.capitalize(title), TITLE_COLOR );
            titlebar.setRect( 0, 0, WIDTH - GAP, 0 );
            add( titlebar );

            BitmapTextMultiline txtInfo = PixelScene.createMultiline( description, 6 );
            txtInfo.maxWidth = WIDTH - GAP * 2;
            txtInfo.measure();
            txtInfo.x = titlebar.left();
            txtInfo.y = titlebar.bottom() + GAP;
            add( txtInfo );

            resize( 100, (int) txtInfo.y + (int) txtInfo.height() + (int)GAP);
        }
    }

    private class ItemButton extends ItemSlot implements WndBag.Listener{

        private static final int NORMAL		= 0xFF4A4D44;
        private static final int EQUIPPED	= 0xFF63665B;

        private static final int NBARS	= 3;

        private Item item;
        private ColorBlock bg;

        private ColorBlock durability[];

        public  boolean holdOnly = false;
        public ItemButton( Item item , boolean holdOnly) {

            super( item );

            this.holdOnly = holdOnly;

            this.item = item;
            if (item instanceof Gold) {
                bg.visible = false;
            }

            width = height = SLOT_SIZE;
        }

        @Override
        protected void createChildren() {
            bg = new ColorBlock( SLOT_SIZE, SLOT_SIZE, NORMAL );
            add( bg );

            super.createChildren();
        }

        @Override
        protected void layout() {
            bg.x = x;
            bg.y = y;



            super.layout();

            topRight.visible = false;
        }



        @Override
        public void item( Item item ) {

            super.item( item );
            if (item != null) {

                bg.texture( TextureCache.createSolid( EQUIPPED  ) );


                enable( true );



            } else {
                bg.color(NORMAL);
            }
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
            if(item instanceof Armor){
                Dungeon.hero.belongings.armor = (Armor)item;
                ((HeroSprite)Dungeon.hero.sprite).updateArmor();
                Dungeon.hero.spendAndNext( 0.1f );
            }

            if(item instanceof Weapon){
                Dungeon.hero.belongings.weapon = (Weapon)item;
            }

            if(item instanceof Potion){
                ((Potion)item).execute( Dungeon.hero, Potion.AC_DRINK );
            }

            if(item instanceof Food){
                ((Food)item).execute( Dungeon.hero, Food.AC_EAT );
            }

            if(item instanceof Scroll){
                ((Scroll)item).execute( Dungeon.hero, Scroll.AC_READ );
            }

            WndRatKingGauntlet.this.hide();
        }

        @Override
        protected boolean onLongClick() {
            return true;
        }

        @Override
        public void onSelect( Item item ) {

        }
    }
}
