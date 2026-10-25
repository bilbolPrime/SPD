package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.nio.charset.CodingErrorAction;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class PartySnapshot {
    static final int MAX_BYTES = 4 * 1024 * 1024;

    private PartySnapshot() { }

    static Bundle capture() {
        Bundle state = new Bundle();
        Dungeon.storePartyWorld(state);
        LocalCoop.storeParty(state);
        PartySession.storeDeaths(state);
        state.put("actor_time", Actor.partyTime());
        state.put("night_mode", Dungeon.nightMode);
        Actor respawner = Dungeon.level.respawner();
        if (respawner != null) {
            for (Actor actor : Actor.all()) {
                if (actor.getClass() == respawner.getClass()) {
                    Bundle runtime = new Bundle();
                    actor.storeInBundle(runtime);
                    state.put("respawner", runtime);
                    break;
                }
            }
        }
        state.put("visible", Dungeon.visible);
        LocalCoop.rememberLevel(Dungeon.depth, Dungeon.level);
        int[] depths = LocalCoop.floorDepths();
        state.put("floors", depths);
        Hero[] heroes = LocalCoop.heroes();
        Hero turn = PartySession.turnHero();
        state.put("turn_hero", turn == heroes[0] ? 0 : turn == heroes[1] ? 1 : -1);
        for (int index = 0; index < heroes.length; index++) {
            Hero hero = heroes[index];
            int[] visible = new int[hero.visibleEnemies()];
            for (int enemy = 0; enemy < visible.length; enemy++) visible[enemy] = hero.visibleEnemy(enemy).id();
            state.put("enemies_" + index, visible);
        }
        for (int depth : depths) {
            Level level = LocalCoop.level(depth);
            Bundle floor = new Bundle();
            floor.put("feeling", level.feeling);
            floor.put("view_distance", level.viewDistance);
            for (Mob mob : level.mobs) {
                Bundle runtime = new Bundle();
                mob.storePartyRuntime(runtime);
                if (mob instanceof NPC) runtime.put("owner", ((NPC)mob).owner() == heroes[1] ? 1 : 0);
                floor.put("mob_" + mob.id(), runtime);
            }
            floor.put("level", level);
            state.put("floor_" + depth, floor);
        }
        return state;
    }

    static void restore(Bundle state, int player) throws IOException {
        LocalCoop.preparePartyRestore(PartyConnection.hero(state.getString("second_class")));
        Dungeon.restorePartyWorld(state);
        Dungeon.nightMode = state.getBoolean("night_mode");
        int[] depths = state.getIntArray("floors");
        if (depths.length < 1 || depths.length > 40) throw new IOException("Invalid party floors");
        Actor.clear();
        LocalCoop.clearLevels();

        for (int depth : depths) {
            if (depth != Dungeon.depth) restoreFloor(state, depth);
        }
        Dungeon.level = restoreFloor(state, Dungeon.depth);
        LocalCoop.restoreParty(state, player);
        Hero[] heroes = LocalCoop.heroes();
        if (heroes.length != 2) throw new IOException("Invalid party heroes");
        Dungeon.level.rebuildFlags();

        Actor.clear();
        Actor.init();
        if (state.contains("respawner")) {
            Actor respawner = Dungeon.level.respawner();
            if (respawner == null) throw new IOException("Unexpected party respawner");
            respawner.restoreFromBundle(state.getBundle("respawner"));
            Actor.add(respawner);
        }
        Actor.partyTime(state.getFloat("actor_time"));
        for (int index = 0; index < heroes.length; index++) {
            heroes[index].restorePartyRuntime(state.getBundle("hero_" + index).getBundle("runtime"));
            heroes[index].restorePartyVisibleEnemies(state.getIntArray("enemies_" + index));
        }
        for (int depth : depths) {
            Bundle floor = state.getBundle("floor_" + depth);
            Level level = LocalCoop.level(depth);
            for (Mob mob : level.mobs) {
                Bundle runtime = floor.getBundle("mob_" + mob.id());
                mob.restorePartyRuntime(runtime);
                int enemyId = runtime.getInt("enemy");
                Char enemy = null;
                for (Hero hero : heroes) {
                    if (hero.id() == enemyId) enemy = hero;
                }
                for (Mob other : level.mobs) {
                    if (other.id() == enemyId) enemy = other;
                }
                mob.restorePartyEnemy(enemy);
                if (mob instanceof NPC) ((NPC)mob).partyOwner(heroes[runtime.getInt("owner") == 1 ? 1 : 0]);
            }
        }
        LocalCoop.restoreActions();
        Dungeon.visible = state.getBooleanArray("visible");
        if (Dungeon.visible.length != Level.LENGTH) throw new IOException("Invalid party visibility");
        System.arraycopy(Dungeon.visible, 0, Level.fieldOfView, 0, Level.LENGTH);
    }

    private static Level restoreFloor(Bundle state, int depth) throws IOException {
        if (depth < 1 || depth > 26 && depth != 1000) throw new IOException("Invalid floor number");
        Bundle floor = state.getBundle("floor_" + depth);
        Bundle data = floor.getBundle("level");
        if (data.getIntArray("map").length != Level.LENGTH
                || data.getBooleanArray("visited").length != Level.LENGTH
                || data.getBooleanArray("mapped").length != Level.LENGTH) throw new IOException("Invalid floor map");
        Level level = (Level)floor.get("level");
        if (level == null) throw new IOException("Missing party floor");
        level.feeling = floor.getEnum("feeling", Level.Feeling.class);
        level.viewDistance = floor.getInt("view_distance");
        LocalCoop.rememberLevel(depth, level);
        return level;
    }

    static String encode(Bundle state) throws IOException {
        try {
            JSONObject data = PartyConnection.parse(state.toString(), MAX_BYTES);
            PartyNames.encode(data);
            PartySymbols.encode(data);
            return compress(data.toString());
        } catch (JSONException | IllegalArgumentException invalid) {
            throw new IOException("Could not describe party names", invalid);
        }
    }

    static String compress(String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_BYTES) throw new IOException("Party state is too large");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(bytes)) {
            zip.write(data);
        }
        if (bytes.size() > (PartyConnection.MAX_MESSAGE_BYTES - 1024) / 2)
            throw new IOException("Compressed party state is too large");
        return PartySecurity.hex(bytes.toByteArray());
    }

    static Bundle decode(String encoded) throws IOException {
        try {
            return PartyState.read(unpack(encoded));
        } catch (JSONException | IllegalArgumentException invalid) {
            throw new IOException("Invalid party state", invalid);
        }
    }

    static JSONObject unpack(String encoded) throws IOException {
        try {
            byte[] compressed = PartySecurity.unhex(encoded, PartyConnection.MAX_MESSAGE_BYTES / 2);
            try (InputStream zip = new LimitedInput(new GZIPInputStream(new ByteArrayInputStream(compressed)));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(zip,
                         StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                                 .onUnmappableCharacter(CodingErrorAction.REPORT)))) {
                return PartyJson.snapshot(reader);
            }
        } catch (JSONException | GeneralSecurityException | IllegalArgumentException invalid) {
            throw new IOException("Invalid party state", invalid);
        }
    }

    private static final class LimitedInput extends FilterInputStream {
        private int remaining = MAX_BYTES;

        LimitedInput(InputStream input) {
            super(input);
        }

        @Override public int read() throws IOException {
            int value = in.read();
            if (value != -1 && --remaining < 0) throw new IOException("Party state is too large");
            return value;
        }

        @Override public int read(byte[] bytes, int offset, int length) throws IOException {
            int count = in.read(bytes, offset, Math.min(length, remaining + 1));
            if (count > 0) remaining -= count;
            if (remaining < 0) throw new IOException("Party state is too large");
            return count;
        }
    }
}
