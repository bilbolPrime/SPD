package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.DrainLifeSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SpiritBinderSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.CorpseExplosionSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.MeditationSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SlowSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SummonGhostSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.MasterOfDeathSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.SpiritualitySkill;
import com.bilboldev.pixeldungeonskills.actors.skills.CurseSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.LichSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.RaiseSkeletonArcherSkill;

import java.util.ArrayList;

public class NecromancerSkillTree extends SkillTree {
    {
        skillTreeType = SkillTreeType.NECROMANCER;
    }
    public NecromancerSkillTree(float x, float y) {
        super(x, y);
    }

    @Override
    public ArrayList<ColorBlock> getBackground() {
        ArrayList<ColorBlock> toReturn = new ArrayList<>();

        ColorBlock bg = new ColorBlock( SLOT_SIZE * 7, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add( bg );

        bg = buildBlock(1, 1);
        toReturn.add( bg );

        bg = buildBlock(2, 1);
        toReturn.add( bg );

        bg = buildBlock(4, 1);
        toReturn.add( bg );

        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y =   y  + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add( bg );

        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE  * 5;
        bg.y =  y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        bg = buildBlock(1, 2);
        toReturn.add( bg );

        bg = buildBlock(2, 2);
        toReturn.add( bg );

        bg = buildBlock(3, 2);
        toReturn.add( bg );

        bg = buildBlock(4, 2);
        toReturn.add( bg );

        bg = new ColorBlock( SLOT_SIZE * 7, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 3 + SLOT_SIZE  + 6;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        bg = buildBlock(2, 3);
        toReturn.add( bg );

        bg = buildBlock(4, 3);
        toReturn.add( bg );

        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE  * 5;
        bg.y = y  + SLOT_SIZE * 3 + SLOT_SIZE  + 6 + SLOT_SIZE *3/2;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y =  y  + SLOT_SIZE * 4 + SLOT_SIZE / 2;
        toReturn.add( bg );

        bg = buildBlock(1, 4);

        bg = buildBlock(3, 4);
        toReturn.add( bg );

        bg = buildBlock(4, 4);
        toReturn.add( bg );

        return toReturn;
    }

    @Override
    public SkillTree build(){
        skillDictionary.clear();
        addSkill(new RaiseSkeletonSkill());
        addSkill(new RaiseSkeletonArcherSkill());
        addSkill(new SummonGhostSkill());

        addSkill(new SpiritualitySkill());
        addSkill(new MeditationSkill());
        addSkill(new SpiritBinderSkill());
        addSkill(new MasterOfDeathSkill());

        addSkill(new CurseSkill());
        addSkill(new DrainLifeSkill());

        addSkill(new SlowSkill());
        addSkill(new LichSkill());
        addSkill(new CorpseExplosionSkill());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();

        toReturn.add(new SkillContainer(getSkill(RaiseSkeletonSkill.class), 1, 1));
        toReturn.add(new SkillContainer(getSkill(DrainLifeSkill.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(SummonGhostSkill.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(SpiritualitySkill.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(MeditationSkill.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(SpiritBinderSkill.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(MasterOfDeathSkill.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(CurseSkill.class), 2, 3));
        toReturn.add(new SkillContainer(getSkill(RaiseSkeletonArcherSkill.class), 4, 3));

        toReturn.add(new SkillContainer(getSkill(LichSkill.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(CorpseExplosionSkill.class), 4, 4));

        return toReturn;
    }

    @Override
    public float getWandRechargeSpeedModifier(){
        return 1f;
    }

    @Override
    public float getWandDamageModifier(){
        return 1f;
    }

    @Override
    public float getSparkDamage() {
        return ((RaiseSkeletonSkill)getSkill(RaiseSkeletonSkill.class)).getDamage()
                + getSkill(LichSkill.class).sparkBonusDamage();
    }

    @Override
    public int SpiritArmorAbsorb(int damage) {
        return ((MasterOfDeathSkill)getSkill(MasterOfDeathSkill.class)).incomingDamageReduction(damage);
    }

    @Override
    public int getManaRegenerationBonus(){
        return ((MeditationSkill)getSkill(MeditationSkill.class)).manaRegenerationBonus();
    };

    @Override
    public float getMinionDamageModifier() {
        return ((DrainLifeSkill)getSkill(DrainLifeSkill.class)).damageModifier()
                + (((CorpseExplosionSkill)getSkill(CorpseExplosionSkill.class)).damageModifier() - 1f);
    }

    @Override
    public float getMinionIncomingDamageModifier(){
        return ((CorpseExplosionSkill)getSkill(CorpseExplosionSkill.class)).incomingDamageModifier();
    }

    @Override
    public int summoningLimitBonus() {
        return getSkill(LichSkill.class).level > 0 ? 1 : 0;
    }

    @Override
    public boolean canMasterOfDeathRevive() {
        return ((MasterOfDeathSkill)getSkill(MasterOfDeathSkill.class)).canRevive();
    }

    @Override
    public void triggerMasterOfDeathCooldown() {
        ((MasterOfDeathSkill)getSkill(MasterOfDeathSkill.class)).triggerReviveCooldown();
    }

    @Override
    public void castSpark(){
        Dungeon.hero.MP -= getSkill(RaiseSkeletonSkill.class).getManaCost();
        getSkill(RaiseSkeletonSkill.class).castTextYell();
    }
}
