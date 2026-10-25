package com.bilboldev.pixeldungeonskills.items.armor;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class MercenaryArmor extends ClassArmor {

	private static final String AC_SPECIAL = "SECRET STASH";
	private static final String TXT_NOT_MERCENARY = "custom.generated.necro_merc.only_mercenaries";

	{
		name = "mercenary coat";
		image = ItemSpriteSheet.ARMOR_MERCENARY;
	}

	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public String desc() {
		return "A mercenary's coat with hidden pockets everywhere. Its secret stash restores half ammo to every gun you are carrying.";
	}

	@Override
	public void doSpecial() {
		for (Item item : curUser.belongings) {
			if (item instanceof Gun) {
				Gun gun = (Gun) item;
				gun.restoreAmmo(Math.max(1, gun.ammoCapacity() / 2));
			}
		}

		curUser.HP -= (curUser.HP / 3);
		curUser.spend(Actor.TICK);
		curUser.sprite.operate(curUser.pos);
		curUser.busy();
		Sample.INSTANCE.play(Assets.SND_ITEM);
	}

	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.MERCENARY) {
			return super.doEquip( hero );
		} else {
			GLog.w(TXT_NOT_MERCENARY);
			return false;
		}
	}
}
