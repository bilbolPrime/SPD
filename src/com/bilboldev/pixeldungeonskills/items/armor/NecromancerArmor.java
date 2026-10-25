package com.bilboldev.pixeldungeonskills.items.armor;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class NecromancerArmor extends ClassArmor {

	private static final String AC_SPECIAL = "BLACK MAGIC";
	private static final String TXT_NOT_NECROMANCER = "custom.generated.necro_merc.only_necromancers";

	{
		name = "necromancer robe";
		image = ItemSpriteSheet.ARMOR_NECROMANCER;
	}

	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public String desc() {
		return "A necromancer's robe steeped in black magic. Its power weakens and poisons all nearby enemies within 5 tiles.";
	}

	@Override
	public void doSpecial() {
		for (Mob mob : Dungeon.level.mobs) {
			if (mob == null) {
				continue;
			}
			if (mob.pos < 0 || mob.pos >= Dungeon.level.LENGTH) {
				continue;
			}
			if (com.bilboldev.pixeldungeonskills.levels.Level.distance(curUser.pos, mob.pos) > 5) {
				continue;
			}

			Buff.prolong(mob, NecroWeakened.class, 10f).setLevel(10);
			Buff.affect(mob, Poison.class).set(Poison.durationFactor(mob) * 10f);
		}

		curUser.HP -= (curUser.HP / 3);
		curUser.spend(Actor.TICK);
		curUser.sprite.operate(curUser.pos);
		curUser.busy();
		Sample.INSTANCE.play(Assets.SND_READ);
	}

	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.NECROMANCER) {
			return super.doEquip( hero );
		} else {
			GLog.w(TXT_NOT_NECROMANCER);
			return false;
		}
	}
}
