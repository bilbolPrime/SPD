package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyMineLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyHuntLevel;
import com.bilboldev.pixeldungeonskills.levels.DailySealLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyHoardLevel;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyQuarry;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyConnection;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public final class DailyRun {
    public static final int REVISION = 9;
    static final String HISTORY = "daily-history.dat";
    public static final int EVENTS = 7;
    private static final HeroClass[] HEROES = {HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE,
            HeroClass.HUNTRESS, HeroClass.NECROMANCER, HeroClass.MERCENARY};
    private static final long FORK_SALT = forkSalt(PartyConnection.FORK_ID);
    private static boolean active, finished, won, timedOut, started, startRequested;
    private static long day, requestedDay;
    private static String failure;
    private static float turns;
    private static Runnable restoreDungeon, restoreDifficulty;
    private static float previousSkillTrack;
    private static int previousSummons;
    private static HeroClass previousClass;
    private static Bundle statistics, journal, badges;
    private static int previousSkills;
    private static int previousSkin;

    private DailyRun() {}

    public static long today() {
        long now = System.currentTimeMillis();
        return Math.floorDiv(now + TimeZone.getDefault().getOffset(now), 86_400_000L);
    }
    public static boolean active() { return active; }
    public static boolean finished() { return active && finished; }
    public static boolean won() { return won; }
    public static boolean timedOut() { return active && timedOut; }
    public static boolean startRequested() { return startRequested; }
    public static long day() { return day; }
    public static HeroClass hero(long date) { return HEROES[(int)Math.floorMod(dayHash(date, 2), HEROES.length)]; }
    public static int turns() { return (int)Math.ceil(turns); }
    public static float elapsedTurns() { return turns; }
    public static String failureMessage() { return failure == null ? null : Messages.get(failure); }

    private static long forkSalt(String id) {
        UUID fork = UUID.fromString(id);
        return fork.getMostSignificantBits() ^ Long.rotateLeft(fork.getLeastSignificantBits(), 27);
    }

    public static int scenario(long date) {
        long hash = dayHash(date, 1);
        if ((date & 1) == 0) return (int)Math.floorMod(hash, EVENTS);

        int before = (int)Math.floorMod(dayHash(date - 1, 1), EVENTS);
        int after = (int)Math.floorMod(dayHash(date + 1, 1), EVENTS);
        int choice = (int)Math.floorMod(hash, EVENTS - (before == after ? 1 : 2));
        if (choice >= Math.min(before, after)) choice++;
        if (before != after && choice >= Math.max(before, after)) choice++;
        return choice;
    }

    private static long dayHash(long date, int domain) {
        long value = date ^ FORK_SALT ^ (REVISION * 0x9E3779B97F4A7C15L) ^ ((long)domain << 32);
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public static int requiredTier(long date) {
        long today = today();
        return date > today || date < today - 3 ? -1 : (int)(today - date);
    }

    public static boolean accessible(long date) {
        int tier = requiredTier(date);
        return tier >= 0 && (tier == 0 || SupporterEntitlements.tier() >= tier);
    }

    public static String date(long date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date(date * 86_400_000L));
    }

    public static String title(long date) { return Messages.get("daily.title_" + scenario(date)); }
    public static String briefing(long date) { return Messages.get("daily.brief_" + scenario(date)); }

    public static RunSeed seed(long date) {
        java.util.Random random = new java.util.Random(dayHash(date, 0));
        int[] symbols = new int[RunSeed.SYMBOLS];
        for (int i = 0; i < symbols.length; i++) symbols[i] = random.nextInt(SeedIcons.COUNT);
        return RunSeed.fromSymbols(symbols, true);
    }

    public static void start(long date) {
        if (!accessible(date)) return;
        PartySession.cancelLobby();
        requestedDay = date;
        startRequested = true;
        InterlevelScene.mode = InterlevelScene.Mode.DAILY;
        Game.switchScene(InterlevelScene.class);
    }

    public static boolean prepare() throws IOException {
        if (active && !startRequested) return false;
        if (!accessible(requestedDay)) {
            startRequested = false;
            if (active) return false;
            throw new IllegalStateException("Daily event is unavailable");
        }
        begin();
        return true;
    }

    private static void begin() throws IOException {
        if (active) leave();
        TutorialRun.finish();
        GauntletRun.finish();
        startRequested = false;
        day = requestedDay;
        LocalCoop.clear();
        previousClass = StartScene.curClass;
        previousSkin = HeroSkins.localChoice;
        previousSkills = Skill.availableSkill;
        previousSkillTrack = Skill.skillTrack;
        previousSummons = SummonedPet.summonedPets;
        SummonedPet.summonedPets = 0;
        statistics = new Bundle();
        Statistics.storeInBundle(statistics);
        journal = null;
        if (Journal.records != null) {
            journal = new Bundle();
            Journal.storeInBundle(journal);
        }
        badges = new Bundle();
        Badges.saveLocal(badges);
        restoreDifficulty = Difficulties.NORMAL.useDefaultsTemporarily();
        restoreDungeon = Dungeon.enterTemporaryRun();
        active = true;
        finished = won = timedOut = started = false;
        failure = null;
        turns = 0;
        Dungeon.prepareNewRun(SaveSlots.FIRST, GameplayMode.REBORN, Difficulties.NORMAL.ordinal());
        Dungeon.seed = seed(day);
        StartScene.curClass = hero(day);
        HeroSkins.localChoice = 0;
        Dungeon.init();
        Dungeon.challenges = 0;
        Dungeon.depth = scenario(day) == 6 ? 19 : scenario(day) == 0 ? 4
                : scenario(day) == 1 || scenario(day) == 5 ? 9 : scenario(day) == 4 ? 24 : 14;
        loadout(day).equip(Dungeon.hero);
        try (Random.Scope random = RunSeed.scope(RunSeed.FLOOR, 0)) {
            if (scenario(day) == 6) Dungeon.level = new DailyHoardLevel();
            else if (scenario(day) == 5) Dungeon.level = new DailyOutpostLevel();
            else if (scenario(day) == 4) Dungeon.level = new DailySealLevel();
            else if (scenario(day) == 3) Dungeon.level = new DailyHuntLevel();
            else if (scenario(day) == 2) Dungeon.level = new DailyMineLevel();
            else Dungeon.level = new DailyLevel();
            Dungeon.level.create();
        }
        record(0);
        if (DailyReminders.settings != null) DailyReminders.settings.played(day);
        started = true;
        Dungeon.switchLevel(Dungeon.level, Dungeon.level.entrance);
    }

    public static DailyLoadout loadout(long date) {
        return new DailyLoadout(date, dayHash(date, 4));
    }

    public static int startingLevel(long date) {
        return 5 + 3 * equipmentTier(date);
    }

    public static int armorTier(long date) {
        return 2 + equipmentTier(date);
    }

    public static HeroSubClass subclass(long date) {
        if (scenario(date) < 2) return HeroSubClass.NONE;
        boolean first = (dayHash(date, 3) & 1) == 0;
        switch (hero(date)) {
            case WARRIOR: return first ? HeroSubClass.GLADIATOR : HeroSubClass.BERSERKER;
            case MAGE: return first ? HeroSubClass.BATTLEMAGE : HeroSubClass.WARLOCK;
            case ROGUE: return first ? HeroSubClass.ASSASSIN : HeroSubClass.FREERUNNER;
            case HUNTRESS: return first ? HeroSubClass.SNIPER : HeroSubClass.WARDEN;
            case NECROMANCER: return first ? HeroSubClass.LICH : HeroSubClass.SPIRIT_BINDER;
            case MERCENARY: return first ? HeroSubClass.MARSHAL : HeroSubClass.EXECUTIONER;
            default: return HeroSubClass.NONE;
        }
    }

    private static int equipmentTier(long date) {
        int event = scenario(date);
        return event == 5 ? 1 : event == 4 || event == 6 ? 3 : Math.min(event, 2);
    }

    public static void spend(float time) {
        if (active && !finished && time > 0 && Float.isFinite(time)) turns += time;
    }

    public static int remainingTurns() {
        if (!active || !(Dungeon.level instanceof DailyLevel)) return 0;
        return Math.max(0, (int)Math.ceil(((DailyLevel)Dungeon.level).turnLimit - turns));
    }

    public static String clockText() {
        return Messages.get(Dungeon.level instanceof DailyOutpostLevel ? "daily.outpost_help" : "daily.time_left",
                remainingTurns());
    }

    public static boolean checkTime() {

        if (active && !finished && Dungeon.level instanceof DailyLevel
                && turns >= ((DailyLevel)Dungeon.level).turnLimit) {
            if (Dungeon.level instanceof DailyOutpostLevel) ((DailyOutpostLevel)Dungeon.level).relieve();
            else {
                timedOut = true;
                finish(false);
            }
        }
        return finished();
    }

    public static String objective() {
        if (!(Dungeon.level instanceof DailyLevel)) return "";
        DailyLevel level = (DailyLevel)Dungeon.level;
        if (level instanceof DailyHoardLevel) {
            DailyHoardLevel city = (DailyHoardLevel)level;
            return Messages.get(city.goldTaken() ? "daily.hoard_escape" : city.alarmRaised() ? "daily.hoard_collect"
                    : city.captainDefeated() ? "daily.hoard_find_vault" : "daily.hoard_find_captain");
        }
        if (level instanceof DailyOutpostLevel) {
            DailyOutpostLevel outpost = (DailyOutpostLevel)level;
            if (outpost.waveNumber() > 0 && outpost.waveActive())
                return Messages.get("daily.outpost_progress", outpost.waveNumber(), 5);
            return outpost.waveNumber() < 5 ? Messages.get("daily.outpost_next", outpost.nextWaveTurns())
                    : Messages.get("daily.outpost_hold");
        }
        if (level instanceof DailySealLevel)
            return Messages.get("daily.seal_progress", ((DailySealLevel)level).sitesClosed(), 3);
        if (level instanceof DailyHuntLevel) {
            int remaining = ((DailyHuntLevel)level).quarryRemaining();
            return remaining == 0 ? Messages.get("daily.hunt_reach_exit")
                    : Messages.get("daily.hunt_progress", 3 - remaining, 3);
        }
        if (level instanceof DailyMineLevel) {
            int safe = ((DailyMineLevel)level).minersSafe();
            return safe == 3 ? Messages.get("daily.return_lift") : Messages.get("daily.miners_safe", safe, 3);
        }
        if (scenario(day) == 0) return Messages.get("daily.objective_0");
        int remaining = level.captainsRemaining();
        return remaining == 0 ? Messages.get("daily.caught") : Messages.get("daily.captains", 2 - remaining, 2);
    }

    public static String objectiveDetails() {
        if (Dungeon.level instanceof DailyOutpostLevel) {
            DailyOutpostLevel level = (DailyOutpostLevel)Dungeon.level;
            StringBuilder text = new StringBuilder(objective());
            if (level.captain() != null)
                text.append('\n').append(Messages.get("daily.outpost_captain_health", Math.max(0, level.captain().HP), level.captain().HT));
            text.append('\n').append(Messages.get("daily.outpost_remaining", level.raidersRemaining()));
            if (level.waveNumber() > 0 && level.waveNumber() < 5 && level.waveActive())
                text.append('\n').append(Messages.get("daily.outpost_next", level.nextWaveTurns()));
            return text.toString();
        }
        if (Dungeon.level instanceof DailySealLevel) {
            DailySealLevel level = (DailySealLevel)Dungeon.level;
            StringBuilder text = new StringBuilder(objective());
            for (int site = 0; site < level.siteCells.length; site++) {
                String status = level.siteClosed(site) ? "daily.seal_stopped" : "daily.seal_feeding";
                text.append('\n').append(Messages.get(status, (Object)level.siteName(site)));
            }
            return text.toString();
        }
        if (!(Dungeon.level instanceof DailyHuntLevel)) return objective();
        DailyHuntLevel level = (DailyHuntLevel)Dungeon.level;
        StringBuilder text = new StringBuilder(objective());
        for (int kind = 0; kind < 3; kind++) {
            String status = level.isQuarrySlain(kind) ? "daily.hunt_slain" : "daily.hunt_alive";
            text.append('\n').append(Messages.get(status, (Object)DailyQuarry.quarryName(kind)));
        }
        return text.toString();
    }

    public static void checkObjective() {
        if (!active || finished || !(Dungeon.level instanceof DailyLevel)) return;
        if (Dungeon.level instanceof DailyMineLevel) ((DailyMineLevel)Dungeon.level).updateMiners();
        else ((DailyLevel)Dungeon.level).updatePrisonGate();
    }

    public static void fail(String messageKey) {
        if (!active || finished) return;
        failure = messageKey;
        finish(false);
    }

    public static void finish(boolean victory) {
        if (!active || finished) return;
        finished = true;
        won = victory;
        if (Dungeon.hero != null) {
            Dungeon.hero.interrupt();
            Dungeon.hero.ready = false;
        }
        try {
            record(victory ? 1 : timedOut ? 4 : 2);
        } catch (IOException failure) {
            failure.printStackTrace();
        }
    }

    public static void leave() {
        if (!active) return;
        if (started && !finished) {
            try { record(3); }
            catch (IOException error) { error.printStackTrace(); }
        }
        Actor.clear();
        Dungeon.hero = null;
        Dungeon.level = null;
        restoreDungeon.run();
        restoreDifficulty.run();
        Statistics.restoreFromBundle(statistics);
        if (journal != null) Journal.restoreFromBundle(journal);
        else Journal.records = null;
        Badges.loadLocal(badges);
        StartScene.curClass = previousClass;
        HeroSkins.localChoice = previousSkin;
        Skill.availableSkill = previousSkills;
        Skill.skillTrack = previousSkillTrack;
        SummonedPet.summonedPets = previousSummons;
        active = false;
        restoreDungeon = restoreDifficulty = null;
        statistics = journal = badges = null;
    }

    public static Bundle history() {
        try {
            Bundle saved = Dungeon.gameBundle(HISTORY);
            Bundle history = new Bundle();
            long today = today();
            for (long date = today - 29; date <= today; date++) {
                String key = Long.toString(date);
                Bundle entry = saved.getBundle(key);
                if (entry.isNull()) continue;
                int rules = entry.contains("revision") ? entry.getInt("revision") : 1;
                if (rules == REVISION && PartyConnection.FORK_ID.equals(entry.getString("fork"))) {
                    if (entry.getInt("first") == 0 && !(active && started && day == date)) entry.put("first", 3);
                    history.put(key, entry);
                }
            }
            return history;
        }
        catch (IOException ignored) { return new Bundle(); }
    }

    private static void record(int result) throws IOException {
        Bundle history = history();
        String key = Long.toString(day);
        Bundle entry = history.getBundle(key);
        if (entry.isNull()) entry = new Bundle();
        entry.put("revision", REVISION);
        entry.put("fork", PartyConnection.FORK_ID);
        int attempts = entry.getInt("attempts");
        if (result == 0) entry.put("attempts", ++attempts);
        if (attempts == 1) entry.put("first", result);
        if (result == 1 && (entry.getInt("best") == 0 || turns() < entry.getInt("best"))) entry.put("best", Math.max(1, turns()));
        history.put(key, entry);
        Bundle trimmed = new Bundle();
        long today = today();
        for (long date = today - 29; date <= today; date++) {
            String dateKey = Long.toString(date);
            if (history.contains(dateKey)) trimmed.put(dateKey, history.getBundle(dateKey));
        }
        try (OutputStream out = Game.instance.openFileOutput(HISTORY, Game.MODE_PRIVATE)) {
            if (!Bundle.write(trimmed, out)) throw new IOException("Unable to save daily history");
        }
    }

    public static String result(long date) {
        Bundle entry = history().getBundle(Long.toString(date));
        if (entry.isNull()) return Messages.get("daily.unplayed");
        String first = Messages.get("daily.result_" + entry.getInt("first"));
        String result = Messages.get("daily.history_entry", (Object)first, entry.getInt("attempts"));
        if (entry.getInt("best") > 0) result += "\n" + Messages.get("daily.best", entry.getInt("best"));
        return result;
    }
}
