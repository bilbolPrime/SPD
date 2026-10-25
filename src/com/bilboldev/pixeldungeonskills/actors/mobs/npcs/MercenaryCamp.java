package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import com.bilboldev.pixeldungeonskills.windows.WndMercs;

public class MercenaryCamp extends NPC {
    private HiredMerc.MERC_TYPES type = HiredMerc.MERC_TYPES.Brute;
    {
        name = "mercenary";
        spriteClass = MercSprite.class;
        rooted = true;
    }

    public void chooseOffer() {
        ArrayList<HiredMerc.MERC_TYPES> choices = new ArrayList<>();
        for (HiredMerc.MERC_TYPES candidate : HiredMerc.MERC_TYPES.values()) {
            if (MercenaryRecruitment.eligible(Dungeon.hero.heroClass, candidate, false)) choices.add(candidate);
        }
        type = Random.element(choices);
    }

    public HiredMerc.MERC_TYPES offer() {
        return type == HiredMerc.MERC_TYPES.Archer && MercenaryRecruitment.maidenAvailable()
                ? HiredMerc.MERC_TYPES.ArcherMaiden : type;
    }

    @Override
    public CharSprite sprite() {
        MercSprite result = (MercSprite)super.sprite();
        result.updateArmor(offer());
        return result;
    }

    @Override
    public String displayName() {
        return MercenaryRecruitment.displayName(offer());
    }

    @Override public com.bilboldev.pixeldungeonskills.messages.Message nameMessage() {
        return MercenaryRecruitment.nameText(offer());
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("offer", type.name());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        type = HiredMerc.MERC_TYPES.valueOf(bundle.getString("offer"));
    }

    @Override
    public void interact() {
        if (!MercenaryRecruitment.atRecruiter(this)) return;
        GameScene.show(new WndMercs(WndMercs.modeFor(offer()), this));
    }

    @Override
    public void damage(int damage, Object source) {}

    @Override
    public void add(Buff buff) {}

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public String description() {
        return Messages.maybeTranslate(offer().getDescription()) + "\n\n" + Messages.get(this, "recruitment");
    }

    @Override
    public String displayDescription() {
        return description();
    }
}
