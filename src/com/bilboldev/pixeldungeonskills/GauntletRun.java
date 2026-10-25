package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.levels.gauntlet.GauntletArena;
import com.bilboldev.pixeldungeonskills.levels.gauntlet.GauntletProfile;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.utils.Bundle;

public final class GauntletRun {
    private static boolean active;
    private static Runnable restoreDungeon, restoreDifficulty;
    private static Bundle statistics, journal, badges;
    private static boolean previous3d;
    private static int previousSkills;
    private static int previousSummonedPets;
    private static int zoomOffset;
    private static float previousSkillTrack;
    private static HeroClass previousClass;

    private GauntletRun() {}

    public static boolean active() {
        return active;
    }

    public static int zoomOffset() {
        return zoomOffset;
    }

    public static void zoomOffset(int value) {
        zoomOffset = value;
    }

    public static GauntletArena arena() {
        return active && Dungeon.level instanceof GauntletArena ? (GauntletArena)Dungeon.level : null;
    }

    public static boolean acceptsInput() {
        GauntletArena arena = arena();
        return arena != null && arena.acceptsInput() && Dungeon.hero != null && Dungeon.hero.isAlive();
    }

    public static void start() {
        InterlevelScene.mode = InterlevelScene.Mode.GUANTLET;
        Game.switchScene(InterlevelScene.class);
    }

    public static void prepare() {
        if (active) return;
        GauntletProfile.forHero(StartScene.curClass);
        statistics = new Bundle();
        Statistics.storeInBundle(statistics);

        journal = null;
        if (Journal.records != null) {
            journal = new Bundle();
            Journal.storeInBundle(journal);
        }
        badges = new Bundle();
        Badges.saveLocal(badges);
        previousSkills = Skill.availableSkill;
        previousSummonedPets = SummonedPet.summonedPets;
        previousSkillTrack = Skill.skillTrack;
        previousClass = StartScene.curClass;
        previous3d = Difficulties.is3d;
        zoomOffset = 0;
        active = true;
        restoreDifficulty = Difficulties.NORMAL.useDefaultsTemporarily();
        restoreDungeon = Dungeon.enterGauntlet();
        try {
            Difficulties.is3d = true;
            SummonedPet.summonedPets = 0;
            Dungeon.initGauntlet();
            GauntletScene.level = 1;
            Dungeon.level = new GauntletArena();
            Dungeon.level.create();
            Dungeon.switchLevel(Dungeon.level, Dungeon.level.entrance);
        } catch (RuntimeException failure) {
            finish();
            throw failure;
        }
    }

    public static void leave() {
        Game.switchScene(TitleScene.class);
    }

    public static void finish() {
        if (!active) return;
        Actor.clear();
        Dungeon.hero = null;
        Dungeon.level = null;
        restoreDungeon.run();
        restoreDifficulty.run();
        Difficulties.is3d = previous3d;
        Statistics.restoreFromBundle(statistics);
        if (journal != null) Journal.restoreFromBundle(journal);
        else Journal.records = null;
        Badges.loadLocal(badges);
        Skill.availableSkill = previousSkills;
        SummonedPet.summonedPets = previousSummonedPets;
        Skill.skillTrack = previousSkillTrack;
        StartScene.curClass = previousClass;
        InterlevelScene.mode = InterlevelScene.Mode.NONE;
        active = false;
        restoreDungeon = restoreDifficulty = null;
        statistics = journal = badges = null;
    }
}
