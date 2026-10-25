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
package com.bilboldev.pixeldungeonskills.items.weapon.melee;

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.ArrayList;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.Smite;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBag;

public class ShortSword extends MeleeWeapon {

	public static final String AC_REFORGE	= "REFORGE";

	private static final String TXT_SELECT_WEAPON	= "Select a weapon to upgrade";

	private static final String TXT_REFORGED =
		"custom.generated.entry_you_reforged_the_short_sword_to_upgrade_your_s_d0a50eb763";
	private static final String TXT_NOT_BOOMERANG =
		"custom.generated.entry_you_can_t_upgrade_a_boomerang_this_way_39db284b08";

	private static final float TIME_TO_REFORGE	= 2f;

	private boolean  equipped;

	{
		name = "short sword";
		image = ItemSpriteSheet.SHORT_SWORD;
        weaponEffect = new Smite();
	}

	public ShortSword() {
		super( 1, 1f, 1f );

		STR = 11;
	}

	@Override
	protected int max0() {
		return 12;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (level() > 0) {
			actions.add( AC_REFORGE );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action == AC_REFORGE) {

			if (hero.belongings.weapon == this) {
				equipped = true;
				hero.belongings.weapon = null;
			} else {
				equipped = false;
				detach( hero.belongings.backpack );
			}

			curUser = hero;

			GameScene.selectItem(itemSelector, WndBag.Mode.WEAPON, Message.key("custom.generated.entry_select_a_weapon_to_upgrade_3991d96a75"));

		} else {

			super.execute( hero, action );

		}
	}

	@Override
	public String desc() {
		return
			"It is indeed quite short, just a few inches longer, than a dagger.";
	}

	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null && !(item instanceof Boomerang)) {

				Sample.INSTANCE.play( Assets.SND_EVOKE );
				ScrollOfUpgrade.upgrade( curUser );
				evoke( curUser );

				GLog.w(TXT_REFORGED, Message.itemName(item));

				((MeleeWeapon)item).safeUpgrade();
				curUser.spendAndNext( TIME_TO_REFORGE );

				Badges.validateItemLevelAquired( item );

			} else {

				if (item instanceof Boomerang) {
					GLog.w(TXT_NOT_BOOMERANG);
				}

				if (equipped) {
					curUser.belongings.weapon = ShortSword.this;
				} else {
					collect( curUser.belongings.backpack );
				}
			}
		}
	};
}
