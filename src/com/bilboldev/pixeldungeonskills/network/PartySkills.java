package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.actors.skills.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

final class PartySkills {
    private static final Map<Integer, Supplier<Skill>> skills = new HashMap<>();

    static {
        add(Accuracy.class, Accuracy::new);
        add(ActiveSkill.class, ActiveSkill::new);
        add(ActiveSkill1.class, ActiveSkill1::new);
        add(ActiveSkill2.class, ActiveSkill2::new);
        add(ActiveSkill3.class, ActiveSkill3::new);
        add(Aggression.class, Aggression::new);
        add(AggressiveMinions.class, AggressiveMinions::new);
        add(AimedShot.class, AimedShot::new);
        add(Assassin.class, Assassin::new);
        add(Awareness.class, Awareness::new);
        add(Bandit.class, Bandit::new);
        add(BattleMage.class, BattleMage::new);
        add(Berserker.class, Berserker::new);
        add(Bombvoyage.class, Bombvoyage::new);
        add(BranchSkill.class, BranchSkill::new);
        add(CorpseExplosionSkill.class, CorpseExplosionSkill::new);
        add(CurseSkill.class, CurseSkill::new);
        add(DarkBolt.class, DarkBolt::new);
        add(DeadEye.class, DeadEye::new);
        add(Dominance.class, Dominance::new);
        add(DoubleShot.class, DoubleShot::new);
        add(DoubleStab.class, DoubleStab::new);
        add(DrainLifeSkill.class, DrainLifeSkill::new);
        add(Echo.class, Echo::new);
        add(EnabledSoulFury.class, EnabledSoulFury::new);
        add(Endurance.class, Endurance::new);
        add(Enrage.class, Enrage::new);
        add(Evasion.class, Evasion::new);
        add(ExecuteSkill.class, ExecuteSkill::new);
        add(ExecutionerSkill.class, ExecutionerSkill::new);
        add(FirmHand.class, FirmHand::new);
        add(Fletching.class, Fletching::new);
        add(Freerunner.class, Freerunner::new);
        add(Gladiator.class, Gladiator::new);
        add(HeadShotSkill.class, HeadShotSkill::new);
        add(Hunting.class, Hunting::new);
        add(HuntressActive.class, HuntressActive::new);
        add(HuntressPassiveA.class, HuntressPassiveA::new);
        add(HuntressPassiveB.class, HuntressPassiveB::new);
        add(IAmTheLawSkill.class, IAmTheLawSkill::new);
        add(IronTip.class, IronTip::new);
        add(KOArrow.class, KOArrow::new);
        add(KeenEye.class, KeenEye::new);
        add(KneeShot.class, KneeShot::new);
        add(KnockBack.class, KnockBack::new);
        add(KnockBackSkill.class, KnockBackSkill::new);
        add(LegendActiveA.class, LegendActiveA::new);
        add(LegendPassiveA.class, LegendPassiveA::new);
        add(LichSkill.class, LichSkill::new);
        add(LockSmith.class, LockSmith::new);
        add(MageActive.class, MageActive::new);
        add(MagePassiveA.class, MagePassiveA::new);
        add(MagePassiveB.class, MagePassiveB::new);
        add(MarshalSkill.class, MarshalSkill::new);
        add(MasterOfDeathSkill.class, MasterOfDeathSkill::new);
        add(Mastery.class, Mastery::new);
        add(Meditation.class, Meditation::new);
        add(MeditationSkill.class, MeditationSkill::new);
        add(MercArcherSkillA.class, MercArcherSkillA::new);
        add(MercArcherSkillB.class, MercArcherSkillB::new);
        add(MercBruteSkillA.class, MercBruteSkillA::new);
        add(MercThiefSkillA.class, MercThiefSkillA::new);
        add(MercWizardSkillA.class, MercWizardSkillA::new);
        add(MercenaryActive.class, MercenaryActive::new);
        add(MercenaryPassiveA.class, MercenaryPassiveA::new);
        add(MercenaryPassiveB.class, MercenaryPassiveB::new);
        add(Negotiations.class, Negotiations::new);
        add(NinjaBomb.class, NinjaBomb::new);
        add(NoWitnessesSkill.class, NoWitnessesSkill::new);
        add(PackratSkill.class, PackratSkill::new);
        add(PassiveSkillA1.class, PassiveSkillA1::new);
        add(PassiveSkillA2.class, PassiveSkillA2::new);
        add(PassiveSkillA3.class, PassiveSkillA3::new);
        add(PassiveSkillB1.class, PassiveSkillB1::new);
        add(PassiveSkillB2.class, PassiveSkillB2::new);
        add(PassiveSkillB3.class, PassiveSkillB3::new);
        add(QuickDrawSkill.class, QuickDrawSkill::new);
        add(RaiseSkeletonArcherSkill.class, RaiseSkeletonArcherSkill::new);
        add(RaiseSkeletonSkill.class, RaiseSkeletonSkill::new);
        add(Rampage.class, Rampage::new);
        add(Regeneration.class, Regeneration::new);
        add(RogueActive.class, RogueActive::new);
        add(RoguePassiveA.class, RoguePassiveA::new);
        add(RoguePassiveB.class, RoguePassiveB::new);
        add(Scorpion.class, Scorpion::new);
        add(ShadowCloneNew.class, ShadowCloneNew::new);
        add(ShadowCloneSkill.class, ShadowCloneSkill::new);
        add(SilentDeath.class, SilentDeath::new);
        add(Skill.class, Skill::new);
        add(Slow.class, Slow::new);
        add(SlowSkill.class, SlowSkill::new);
        add(Smash.class, Smash::new);
        add(Smite.class, Smite::new);
        add(SmokeBomb.class, SmokeBomb::new);
        add(Sniper.class, Sniper::new);
        add(Sorcerer.class, Sorcerer::new);
        add(SoulFury.class, SoulFury::new);
        add(SoulSpark.class, SoulSpark::new);
        add(Spark.class, Spark::new);
        add(SpiritArmor.class, SpiritArmor::new);
        add(SpiritArrow.class, SpiritArrow::new);
        add(SpiritBinderSkill.class, SpiritBinderSkill::new);
        add(Spirituality.class, Spirituality::new);
        add(SpiritualitySkill.class, SpiritualitySkill::new);
        add(SteadyAimSkill.class, SteadyAimSkill::new);
        add(Stealth.class, Stealth::new);
        add(SummonCrab.class, SummonCrab::new);
        add(SummonGhostSkill.class, SummonGhostSkill::new);
        add(SummonRat.class, SummonRat::new);
        add(SummonSkeleton.class, SummonSkeleton::new);
        add(SummonSkeletonArcher.class, SummonSkeletonArcher::new);
        add(SummonSkeletonSkill.class, SummonSkeletonSkill::new);
        add(Summoner.class, Summoner::new);
        add(SummonerSkill.class, SummonerSkill::new);
        add(Toughness.class, Toughness::new);
        add(ToughnessSkill.class, ToughnessSkill::new);
        add(TripleShot.class, TripleShot::new);
        add(Venom.class, Venom::new);
        add(WantedSkill.class, WantedSkill::new);
        add(Warden.class, Warden::new);
        add(Warlock.class, Warlock::new);
        add(WarriorActive.class, WarriorActive::new);
        add(WarriorPassiveA.class, WarriorPassiveA::new);
        add(WarriorPassiveB.class, WarriorPassiveB::new);
        add(Wizard.class, Wizard::new);
    }

    private PartySkills() { }

    private static void add(Class<? extends Skill> type, Supplier<Skill> factory) {
        skills.put(PartySymbols.id(type.getName()), factory);
    }

    static void require(int id) {
        if (!skills.containsKey(id)) throw new IllegalArgumentException("Unknown party skill");
    }

    static Skill create(int id) {
        require(id);
        return skills.get(id).get();
    }
}
