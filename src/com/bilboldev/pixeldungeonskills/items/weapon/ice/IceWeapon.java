package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;

public interface IceWeapon {
    HeroClass heroClass();

    static boolean canUse(Item item, Hero hero) {
        if (item instanceof IceWeapon && ((IceWeapon)item).heroClass() != hero.heroClass) {
            GLog.w("items.weapon.ice.wrong_hero");
            GameScene.show(new WndMessage(Message.key("items.weapon.ice.wrong_hero")));
            return false;
        }
        return true;
    }

    static boolean magicAllowed() {
        return !RebornChallenges.active(RebornChallenges.PLAIN_STEEL);
    }

    static Item reward(HeroClass heroClass) {
        Item item;
        switch (heroClass) {
            case WARRIOR: item = new GlacierCleaver(); break;
            case MAGE: item = new HoarfrostStaff(); break;
            case ROGUE: item = new RimePick(); break;
            case HUNTRESS: item = new FrostBow(); break;
            case NECROMANCER: item = new GravefrostScythe(); break;
            case MERCENARY: item = new FrostbiteRifle(); break;
            default: throw new IllegalArgumentException("No ice weapon for " + heroClass);
        }
        return item.identify();
    }
}
