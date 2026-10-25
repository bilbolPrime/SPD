package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.actors.skills.NoWitnessesSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.PackratSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.ExecutionerSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SteadyAimSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.MarshalSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.QuickDrawSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.ExecuteSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.HeadShotSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.WantedSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.IAmTheLawSkill;

import java.util.ArrayList;

public class MercenarySkillTree extends SkillTree {
    {
        skillTreeType = SkillTreeType.MERCENARY;
    }

    public MercenarySkillTree(float x, float y) {
        super(x, y);
    }

    @Override
    public ArrayList<ColorBlock> getBackground() {
        ArrayList<ColorBlock> toReturn = new ArrayList<>();

        ColorBlock bg = new ColorBlock(SLOT_SIZE * 7, 2, NORMAL);
        bg.x = x + SLOT_SIZE;
        bg.y = y + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add(bg);

        bg = buildBlock(1, 1);
        toReturn.add(bg);
        bg = buildBlock(2, 1);
        toReturn.add(bg);
        bg = buildBlock(4, 1);
        toReturn.add(bg);

        bg = new ColorBlock(SLOT_SIZE * 2, 2, NORMAL);
        bg.x = x + SLOT_SIZE;
        bg.y = y + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add(bg);

        bg = new ColorBlock(2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL);
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y = y + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add(bg);

        bg = new ColorBlock(SLOT_SIZE * 2, 2, NORMAL);
        bg.x = x + SLOT_SIZE * 5;
        bg.y = y + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add(bg);

        bg = buildBlock(1, 2);
        toReturn.add(bg);
        bg = buildBlock(2, 2);
        toReturn.add(bg);
        bg = buildBlock(3, 2);
        toReturn.add(bg);
        bg = buildBlock(4, 2);
        toReturn.add(bg);

        bg = new ColorBlock(SLOT_SIZE * 7, 2, NORMAL);
        bg.x = x + SLOT_SIZE;
        bg.y = y + SLOT_SIZE * 3 + SLOT_SIZE + 6;
        toReturn.add(bg);

      bg = new ColorBlock(2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL);
      bg.x = x + SLOT_SIZE;
      bg.y = y + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
       toReturn.add(bg);

        bg = buildBlock(4, 3);
        toReturn.add(bg);

        bg = new ColorBlock(SLOT_SIZE * 2, 2, NORMAL);
        bg.x = x + SLOT_SIZE * 5;
        bg.y = y + SLOT_SIZE * 3 + SLOT_SIZE + 6 + SLOT_SIZE * 3 / 2;
        toReturn.add(bg);

        bg = new ColorBlock(2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL);
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y = y + SLOT_SIZE * 4 + SLOT_SIZE / 2;
        toReturn.add(bg);

        bg = new ColorBlock(2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL);
        bg.x = x + SLOT_SIZE * 3 - 1;
        bg.y = y + SLOT_SIZE * 4 + SLOT_SIZE / 2;

        bg = buildBlock(3, 4);
        toReturn.add(bg);
        bg = buildBlock(4, 4);
        toReturn.add(bg);

        return toReturn;
    }

    @Override
    public SkillTree build() {
        skillDictionary.clear();
        addSkill(new WantedSkill());
        addSkill(new SteadyAimSkill());
        addSkill(new QuickDrawSkill());

        addSkill(new PackratSkill());
        addSkill(new HeadShotSkill());
        addSkill(new MarshalSkill());
        addSkill(new IAmTheLawSkill());

        addSkill(new NoWitnessesSkill());
        addSkill(new ExecutionerSkill());
        addSkill(new ExecuteSkill());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();

        toReturn.add(new SkillContainer(getSkill(WantedSkill.class), 1, 1));
        toReturn.add(new SkillContainer(getSkill(SteadyAimSkill.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(QuickDrawSkill.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(PackratSkill.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(HeadShotSkill.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(MarshalSkill.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(IAmTheLawSkill.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(NoWitnessesSkill.class), 4, 3));

        toReturn.add(new SkillContainer(getSkill(ExecutionerSkill.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(ExecuteSkill.class), 4, 4));

        return toReturn;
    }

    private Skill safeSkill(Class<? extends Skill> clazz) {
        Skill skill = getSkill(clazz);
        return skill == null ? new Skill() : skill;
    }

    @Override
    public int getGunAmmoBonus() {
        return safeSkill(PackratSkill.class).gunAmmoBonus()
                + safeSkill(MarshalSkill.class).gunAmmoBonus();
    }

    @Override
    public int getGunAttackSkillBonus() {
        return safeSkill(SteadyAimSkill.class).gunToHitBonus()
                + safeSkill(MarshalSkill.class).gunToHitBonus()
                + safeSkill(HeadShotSkill.class).headShotToHitBonus();
    }

    @Override
    public float getGunDamageModifier() {
        return 1f;
    }

    @Override
    public float getGunAttackSpeedModifier() {
        return safeSkill(QuickDrawSkill.class).gunAttackSpeedModifier()
                * safeSkill(ExecutionerSkill.class).gunAttackSpeedModifier();
    }

    @Override
    public float getGunRearmSpeedModifier() {
        return safeSkill(ExecutionerSkill.class).gunRearmSpeedModifier();
    }

    @Override
    public boolean shouldFleeOnGunHit() {
        return false;
    }

    @Override
    public float getGunExecuteThreshold() {
        return safeSkill(ExecuteSkill.class).executeThreshold();
    }

    @Override
    public int getWantedGoldBonus() {
        return safeSkill(WantedSkill.class).wantedGoldBonus();
    }

    @Override
    public GunShotData prepareGunShot(com.bilboldev.pixeldungeonskills.actors.hero.Hero hero) {
        GunShotData shotData = new GunShotData();
        shotData.attackSkillBonus += safeSkill(SteadyAimSkill.class).gunToHitBonus();
        shotData.attackSkillBonus += safeSkill(MarshalSkill.class).gunToHitBonus();
        safeSkill(HeadShotSkill.class).applyToGunShot(hero, shotData);
        safeSkill(NoWitnessesSkill.class).applyToGunShot(hero, shotData);
        return shotData;
    }
}
