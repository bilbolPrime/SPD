package com.bilboldev.pixeldungeonskills.items.weapon.missiles;

import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Bullet extends Item {

	{
		name = "bullet";
		image = ItemSpriteSheet.BULLET;
		unique = true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String desc() {
		return "A gun bullet. It exists only long enough to be fired.";
	}
}
