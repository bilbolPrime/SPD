package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Visual;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PartyVisuals;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.PointF;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;

import static com.bilboldev.pixeldungeonskills.network.PartyMessage.*;

public final class PartyEffects {
    public static final int MISSILE = 1, MAGIC = 2, LIGHTNING = 3, PARTICLES = 4, SPLASH = 5,
            STATUS = 6, SOUND = 7, JUMP = 8, FALL = 9, RAY = 10, WOUND = 11,
            SPELL = 12, FLASH = 13, RIPPLE = 14, FLARE = 15, STAB = 16;
    public static final int MAX_BATCH = 48, MAX_PENDING = 192;
    private static final ArrayDeque<Bundle> outgoing = new ArrayDeque<>();
    private static final ArrayDeque<Pending> incoming = new ArrayDeque<>();
    private static int depth;
    private static float delay;
    private static PartyTraffic traffic = new PartyTraffic(30, 60);
    private static boolean replaySound;

    private static final String[] SOUNDS = {
            Assets.SND_GOLD, Assets.SND_OPEN, Assets.SND_UNLOCK, Assets.SND_ITEM, Assets.SND_DEWDROP,
            Assets.SND_HIT, Assets.SND_MISS, Assets.SND_STEP, Assets.SND_WATER, Assets.SND_DESCEND,
            Assets.SND_EAT, Assets.SND_READ, Assets.SND_LULLABY, Assets.SND_DRINK, Assets.SND_SHATTER,
            Assets.SND_ZAP, Assets.SND_LIGHTNING, Assets.SND_LEVELUP, Assets.SND_DEATH, Assets.SND_CHALLENGE,
            Assets.SND_CURSED, Assets.SND_TRAP, Assets.SND_EVOKE, Assets.SND_TOMB, Assets.SND_ALERT,
            Assets.SND_MELD, Assets.SND_BOSS, Assets.SND_BLAST, Assets.SND_PLANT, Assets.SND_RAY,
            Assets.SND_BEACON, Assets.SND_TELEPORT, Assets.SND_CHARMS, Assets.SND_MASTERY, Assets.SND_PUFF,
            Assets.SND_ROCKS, Assets.SND_BURNING, Assets.SND_FALLING, Assets.SND_GHOST, Assets.SND_SECRET,
            Assets.SND_BONES, Assets.SND_BEE, Assets.SND_DEGRADE, Assets.SND_MIMIC
    };

    private PartyEffects() { }

    public static boolean recording() {
        return PartySession.network() && !PartySession.client() && !PartySession.applyingSnapshot()
                && Game.scene() instanceof GameScene && Dungeon.level != null;
    }

    private static Bundle event(int type) {
        Bundle event = new Bundle();
        event.put("type", type);
        return event;
    }

    private static void queue(Bundle event) {
        if (!recording()) return;
        if (depth != Dungeon.depth) {
            outgoing.clear();
            depth = Dungeon.depth;
        }
        if (outgoing.size() < MAX_PENDING) outgoing.addLast(event);
    }

    static void update() {
        delay -= Game.elapsed;
        if (delay <= 0) {
            flush();
            delay = 0.05f;
        }
        if (PartySession.client() && !PartySession.applyingSnapshot() && Game.scene() instanceof GameScene) {
            long now = System.nanoTime();
            int count = incoming.size();
            while (count-- > 0) {
                Pending pending = incoming.removeFirst();
                if (now - pending.received > 2000000000L) continue;
                if (pending.depth == Dungeon.depth) PartyVisuals.play(pending.event);
                else incoming.addLast(pending);
            }
        }
        PartyVisuals.update();
    }

    static void flush() {
        if (!recording() || outgoing.isEmpty()) return;
        if (depth != Dungeon.depth) {
            outgoing.clear();
            return;
        }
        Bundle message = new Bundle();
        message.put("kind", "effects");
        message.put("depth", depth);
        int count = Math.min(MAX_BATCH, outgoing.size());
        message.put("count", count);
        for (int i = 0; i < count; i++) message.put("effect" + i, outgoing.removeFirst());
        PartySession.sendEffects(message);
    }

    static void receive(Bundle message) throws IOException {
        traffic.receive(message.toString().length());
        int count = message.getInt("count");
        if (incoming.size() + count > MAX_PENDING) throw invalid();
        for (int i = 0; i < count; i++)
            incoming.addLast(new Pending(message.getInt("depth"), message.getBundle("effect" + i)));
    }

    static void clear() {
        outgoing.clear();
        incoming.clear();
        depth = 0;
        delay = 0;
        traffic = new PartyTraffic(30, 60);
        PartyVisuals.clear();
    }

    private static final class Pending {
        final int depth;
        final Bundle event;
        final long received = System.nanoTime();
        Pending(int depth, Bundle event) {
            this.depth = depth;
            this.event = event;
        }
    }

    public static void missile(int from, int to, int image, ItemSprite.Glowing glow) {
        if (!recording()) return;
        Bundle event = path(MISSILE, from, to);
        event.put("image", image);
        event.put("glow", glow == null ? -1 : glow.color & 0xFFFFFF);
        event.put("period", glow == null ? 1 : glow.period);
        queue(event);
    }

    public static void magic(int from, int to, float speed, float size, Emitter.Factory factory, float interval) {
        if (!recording()) return;
        int id = PartyParticles.id(factory);
        if (id < 0) return;
        Bundle event = path(MAGIC, from, to);
        event.put("factory", id);
        event.put("speed", speed);
        event.put("size", size);
        event.put("interval", interval);
        queue(event);
    }

    public static void lightning(int[] cells, int length) {
        if (!recording()) return;
        Bundle event = event(LIGHTNING);
        event.put("cells", Arrays.copyOf(cells, Math.min(length, 64)));
        queue(event);
    }

    public static void particles(Emitter.Factory factory, float interval, int quantity, float x, float y, float width, float height,
                                 Visual anchor) {
        if (!recording() || quantity <= 0) return;
        int id = PartyParticles.id(factory);
        if (id < 0) return;
        Bundle event = position(PARTICLES, x, y);
        event.put("factory", id);
        event.put("light", factory.lightMode());
        event.put("quantity", Math.min(quantity, 64));
        event.put("interval", interval);
        event.put("width", width);
        event.put("height", height);
        event.put("actor", anchor instanceof CharSprite && ((CharSprite)anchor).ch != null
                ? ((CharSprite)anchor).ch.id() : -1);
        queue(event);
    }

    public static void splash(PointF position, float direction, float cone, int color, int quantity) {
        if (!recording() || quantity <= 0) return;
        Bundle event = position(SPLASH, position.x, position.y);
        event.put("direction", direction);
        event.put("cone", cone);
        event.put("color", color & 0xFFFFFF);
        event.put("quantity", Math.min(quantity, 64));
        queue(event);
    }

    public static void status(CharSprite sprite, int color, Message message) {
        if (!recording() || !sprite.visible || sprite.ch == null) return;
        Bundle event = status(sprite, color);
        event.put("text", PartyText.encode(message));
        queue(event);
    }

    public static void number(CharSprite sprite, int color, String text) {
        if (!recording() || !sprite.visible || sprite.ch == null || !text.matches("[+-]?[0-9]{1,9}")) return;
        Bundle event = status(sprite, color);
        event.put("number", Integer.parseInt(text));
        event.put("plus", text.startsWith("+"));
        queue(event);
    }

    private static Bundle status(CharSprite sprite, int color) {
        Bundle event = position(STATUS, sprite.x + sprite.width / 2, sprite.y);
        event.put("cell", sprite.ch.pos);
        event.put("actor", sprite.ch.id());
        event.put("height", sprite.height);
        event.put("color", color & 0xFFFFFF);
        return event;
    }

    public static boolean sound(Object asset, float left, float right, float rate) {
        int id = Arrays.asList(SOUNDS).indexOf(asset);
        if (id < 0) return true;

        if (PartySession.client() && Game.scene() instanceof GameScene) return replaySound;
        if (recording()) {
            Bundle event = event(SOUND);
            event.put("asset", id);
            event.put("left", Math.max(0, Math.min(1, left)));
            event.put("right", Math.max(0, Math.min(1, right)));
            event.put("rate", Math.max(0.5f, Math.min(2, rate)));
            queue(event);
        }
        return true;
    }

    public static void playSound(Bundle event) {
        replaySound = true;
        try {
            com.bilboldev.noosa.audio.Sample.INSTANCE.play(SOUNDS[event.getInt("asset")],
                    event.getFloat("left"), event.getFloat("right"), event.getFloat("rate"));
        } finally {
            replaySound = false;
        }
    }

    public static void jump(CharSprite sprite, int from, int to) {
        if (!recording() || sprite.ch == null || !sprite.visible) return;
        Bundle event = path(JUMP, from, to);
        event.put("actor", sprite.ch.id());
        queue(event);
    }

    public static void actor(int type, CharSprite sprite) {
        if (!recording() || sprite.ch == null || !sprite.visible) return;
        Bundle event = event(type);
        event.put("actor", sprite.ch.id());
        event.put("cell", sprite.ch.pos);
        queue(event);
    }

    public static void ray(PointF from, PointF to) {
        if (!recording()) return;
        Bundle event = position(RAY, from.x, from.y);
        event.put("to_x", to.x);
        event.put("to_y", to.y);
        queue(event);
    }

    public static void wound(int cell, float angle) {
        if (!recording()) return;
        Bundle event = event(WOUND);
        event.put("cell", cell);
        event.put("angle", angle);
        queue(event);
    }

    public static void spell(CharSprite sprite, int icon) {
        if (!recording() || sprite.ch == null || !sprite.visible) return;
        Bundle event = event(SPELL);
        event.put("actor", sprite.ch.id());
        event.put("cell", sprite.ch.pos);
        event.put("icon", icon);
        queue(event);
    }

    public static void ripple(int cell) {
        if (!recording()) return;
        Bundle event = event(RIPPLE);
        event.put("cell", cell);
        queue(event);
    }

    public static void flare(Flare flare, float duration) {
        if (!recording() || duration <= 0) return;
        Bundle event = position(FLARE, flare.x, flare.y);
        event.put("rays", flare.rays());
        event.put("radius", flare.radius());
        event.put("duration", Math.min(duration, 8));
        event.put("color", ((int)(flare.rm * 255) << 16) | ((int)(flare.gm * 255) << 8) | (int)(flare.bm * 255));
        event.put("light", flare.lightMode());
        queue(event);
    }

    public static void stab(int from, int to, int image) {
        if (!recording()) return;
        Bundle event = path(STAB, from, to);
        event.put("image", image);
        queue(event);
    }

    private static Bundle path(int type, int from, int to) {
        Bundle event = event(type);
        event.put("from", from);
        event.put("to", to);
        return event;
    }

    private static Bundle position(int type, float x, float y) {
        Bundle event = event(type);
        event.put("x", x);
        event.put("y", y);
        return event;
    }

    static void validate(JSONObject message) throws IOException, JSONException {
        int count = integer(message, "count", 1, MAX_BATCH);
        String[] names = new String[count + 3];
        names[0] = "kind";
        names[1] = "depth";
        names[2] = "count";
        for (int i = 0; i < count; i++) {
            names[i + 3] = "effect" + i;
            validateEvent(object(message, names[i + 3]));
        }
        fields(message, names);
    }

    private static void validateEvent(JSONObject e) throws IOException, JSONException {
        int type = integer(e, "type", MISSILE, STAB);
        switch (type) {
            case MISSILE:
                fields(e, "type", "from", "to", "image", "glow", "period");
                integer(e, "glow", -1, 0xFFFFFF);
                decimal(e, "period", .05f, 10);

            case STAB:
                if (type == STAB) fields(e, "type", "from", "to", "image");
                integer(e, "image", 0, 255);
                path(e);
                break;
            case MAGIC:
                fields(e, "type", "from", "to", "factory", "speed", "size", "interval");
                path(e);
                factory(e);
                decimal(e, "speed", 32, 1024);
                decimal(e, "size", 0, 32);
                decimal(e, "interval", .01f, 1);
                break;
            case LIGHTNING:
                fields(e, "type", "cells");
                Object raw = e.get("cells");
                if (!(raw instanceof JSONArray)) throw invalid();
                JSONArray cells = (JSONArray)raw;
                if (cells.length() < 2 || cells.length() > 64) throw invalid();
                for (int i = 0; i < cells.length(); i++) {
                    Object cell = cells.get(i);
                    if (!(cell instanceof Integer) || (Integer)cell < 0 || (Integer)cell >= Level.LENGTH) throw invalid();
                }
                break;
            case PARTICLES:
                fields(e, "type", "x", "y", "width", "height", "actor", "factory", "light", "quantity", "interval");
                position(e);
                decimal(e, "width", 0, 64);
                decimal(e, "height", 0, 64);
                integer(e, "actor", -1, Integer.MAX_VALUE - 1);
                integer(e, "quantity", 1, 64);
                decimal(e, "interval", 0, 1);
                bool(e, "light");
                factory(e);
                break;
            case SPLASH:
                fields(e, "type", "x", "y", "direction", "cone", "color", "quantity");
                position(e);
                decimal(e, "direction", -7, 7);
                decimal(e, "cone", 0, 7);
                integer(e, "color", 0, 0xFFFFFF);
                integer(e, "quantity", 1, 64);
                break;
            case STATUS:
                boolean numeric = e.has("number");
                if (numeric) fields(e, "type", "x", "y", "cell", "actor", "height", "color", "number", "plus");
                else fields(e, "type", "x", "y", "cell", "actor", "height", "color", "text");
                position(e);
                cell(e, "cell");
                integer(e, "actor", 0, Integer.MAX_VALUE - 1);
                decimal(e, "height", 0, 64);
                integer(e, "color", 0, 0xFFFFFF);
                if (numeric) {
                    integer(e, "number", -999999999, 999999999);
                    bool(e, "plus");
                } else statusText(object(e, "text"));
                break;
            case SOUND:
                fields(e, "type", "asset", "left", "right", "rate");
                integer(e, "asset", 0, SOUNDS.length - 1);
                decimal(e, "left", 0, 1);
                decimal(e, "right", 0, 1);
                decimal(e, "rate", .5f, 2);
                break;
            case JUMP:
                fields(e, "type", "actor", "from", "to");
                integer(e, "actor", 0, Integer.MAX_VALUE - 1);
                path(e);
                break;
            case FALL:
            case FLASH:
            case SPELL:
                if (type == SPELL) {
                    fields(e, "type", "actor", "cell", "icon");
                    integer(e, "icon", 0, 3);
                } else fields(e, "type", "actor", "cell");
                integer(e, "actor", 0, Integer.MAX_VALUE - 1);
                cell(e, "cell");
                break;
            case RAY:
                fields(e, "type", "x", "y", "to_x", "to_y");
                position(e);
                decimal(e, "to_x", -64, Level.WIDTH * 16 + 64);
                decimal(e, "to_y", -64, Level.HEIGHT * 16 + 64);
                break;
            case WOUND:
                fields(e, "type", "cell", "angle");
                cell(e, "cell");
                decimal(e, "angle", -360, 360);
                break;
            case RIPPLE:
                fields(e, "type", "cell");
                cell(e, "cell");
                break;
            case FLARE:
                fields(e, "type", "x", "y", "rays", "radius", "duration", "color", "light");
                position(e);
                integer(e, "rays", 1, 32);
                decimal(e, "radius", 0, 128);
                decimal(e, "duration", .01f, 8);
                integer(e, "color", 0, 0xFFFFFF);
                bool(e, "light");
                break;
        }
    }

    private static void factory(JSONObject e) throws IOException, JSONException {
        if (!PartyParticles.valid(integer(e, "factory", 0, 255))) throw invalid();
    }

    private static void statusText(JSONObject text) throws IOException, JSONException {
        PartyText.validate(text, 256);
    }

    private static void path(JSONObject e) throws IOException, JSONException {
        cell(e, "from");
        cell(e, "to");
    }

    private static void cell(JSONObject e, String key) throws IOException, JSONException {
        integer(e, key, 0, Level.LENGTH - 1);
    }

    private static void position(JSONObject e) throws IOException, JSONException {
        decimal(e, "x", -64, Level.WIDTH * 16 + 64);
        decimal(e, "y", -64, Level.HEIGHT * 16 + 64);
    }

    private static float decimal(JSONObject e, String key, float min, float max) throws IOException, JSONException {
        Object value = e.get(key);
        if (!(value instanceof Number)) throw invalid();
        double number = ((Number)value).doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number) || number < min - .000001 || number > max + .000001)
            throw invalid();
        return (float)number;
    }
}
