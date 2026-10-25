package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.messages.Message;
import android.util.Log;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.ClassicStartScene;
import com.bilboldev.pixeldungeonskills.scenes.ClassicTitleScene;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.PartyPresentation;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.scenes.SurfaceScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.utils.Bundle;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public final class PartySession {
    public static final int MAX_DEATHS = 256;
    private static int actorIdCeiling;
    public enum LobbyState { IDLE, HOSTING, DISCOVERING, CONNECTING, WAITING, CONNECTED, ERROR }
    public enum Ping {
        HELP(1), WAIT(2), FOLLOW(3), READY(4), THANKS(5), ARE_YOU_THERE(6), YES(7), NO(8);

        final int id;

        Ping(int id) {
            this.id = id;
        }

        static Ping fromId(int id) {
            for (Ping ping : values()) if (ping.id == id) return ping;
            throw new IllegalArgumentException("Unknown party ping");
        }
    }
    public enum StopReason { ENEMY, INTERRUPTED, CANCELLED }

    public static final class StartRequest {
        public final HeroClass first, second;
        StartRequest(HeroClass first, HeroClass second) {
            this.first = first;
            this.second = second;
        }
    }

    private static final Object connectionLock = new Object();
    private static volatile PartyConnection connection;
    private static volatile boolean foreground = true;
    private static volatile boolean foregroundLost;
    private static PartyTraffic snapshots = new PartyTraffic(10, 24);
    private static PartyUpdates updates = new PartyUpdates();
    private static final LinkedHashMap<String, PartyConnection.Host> discovered = new LinkedHashMap<>();
    private static final ArrayDeque<Bundle> commands = new ArrayDeque<>();
    private static final ArrayDeque<String> logs = new ArrayDeque<>();
    private static final ArrayDeque<PendingAnimation> animations = new ArrayDeque<>();
    private static final ArrayDeque<Bundle> moves = new ArrayDeque<>();
    private static int moveDepth;
    private static float moveDelay;

    private static final LinkedHashMap<Integer, Integer> deaths = new LinkedHashMap<>();
    private static int deathDepth;
    private static LobbyState state = LobbyState.IDLE;
    private static String status = "";
    private static List<String> hostAddresses = Collections.emptyList();
    private static int hostPort;
    private static HeroClass chosen;
    private static PartyConnection.Peer request;
    private static StartRequest start;
    private static boolean hosting, joined, split, applying, switching, executing;
    private static boolean started, departurePending;
    private static boolean departureRequested, lanAllowed;
    private static int fallenHero = -1;
    private static boolean ending, surfaceEnding;
    private static int localIndex;
    private static int generation;
    private static int sequence, receivedSequence;
    private static int appliedSequence, sentStep, receivedStep;
    private static boolean awaitingState, fullStateRequested;
    private static long nextStateRequest, nextFullState;
    private static String lastStateText;
    private static float snapshotDelay;
    private static Bundle pendingState;
    private static int remainsDepth, remainsCell;
    private static Bundle appliedState;
    private static String notice;
    private static CellSelector.Listener remoteTarget;
    private static int targetToken;
    private static Message targetPrompt;
    private static int clientTargetToken;
    private static float pingDelay;
    private static Object runIdentity = new Object();

    private PartySession() { }

    public static void setForeground(boolean active) {
        synchronized (connectionLock) {
            foreground = active;
            if (!active) {
                foregroundLost = true;
                if (connection != null) connection.close();
            }
        }
    }

    public static void host(HeroClass hero) {
        open(hero, true);
    }

    public static void discover(HeroClass hero) {
        open(hero, false);
    }

    private static void open(HeroClass hero, boolean host) {
        synchronized (connectionLock) {
            cancelLobby();
            if (!foreground) return;
            chosen = hero;
            hosting = host;
            localIndex = host ? 0 : 1;
            state = host ? LobbyState.HOSTING : LobbyState.DISCOVERING;
            final int attempt = generation;
            NetworkAccess.request(granted -> {
                synchronized (connectionLock) {
                    if (attempt != generation || !foreground || foregroundLost) return;
                    if (!granted) {
                        fail(text("permission"));
                        return;
                    }
                    lanAllowed = true;
                    connection = new PartyConnection(new Events());
                    if (host) connection.host(hero, Messages.maybeTranslate(hero.title()));
                    else connection.discover();
                }
            });
        }
    }

    public static void join(PartyConnection.Host host, String code) {
        connect(host.address, host.port, code);
    }

    public static void joinAddress(String address, int port, String code) {
        connect(address.trim(), port, code);
    }

    private static void connect(String address, int port, String code) {
        synchronized (connectionLock) {
            if (!foreground || foregroundLost || chosen == null || hosting || joined || !lanAllowed) return;
            generation++;
            if (connection != null) connection.close();
            connection = new PartyConnection(new Events());
            state = LobbyState.CONNECTING;
            status = "";
            try {
                connection.connect(address, port, chosen, code);
            } catch (IllegalArgumentException invalid) {
                fail(text("address"));
            }
        }
    }

    public static LobbyState lobbyState() {
        return state;
    }

    public static String status() {
        return status;
    }

    public static List<String> hostAddresses() {
        return hostAddresses;
    }

    public static int hostPort() {
        return hostPort;
    }

    public static String pairingCode() {
        return connection == null ? "" : connection.pairingCode();
    }

    public static List<PartyConnection.Host> hosts() {
        return Collections.unmodifiableList(new ArrayList<>(discovered.values()));
    }

    public static PartyConnection.Peer request() {
        return request;
    }

    public static void accept() {
        synchronized (connectionLock) {
            if (!foreground || foregroundLost) return;
            if (connection != null && request != null) connection.accept(request);
            request = null;
        }
    }

    public static void reject() {
        if (connection != null && request != null) connection.reject(request, "declined");
        request = null;
    }

    public static StartRequest takeStart() {
        if (!foreground || foregroundLost) return null;
        StartRequest result = start;
        start = null;
        return result;
    }

    public static void cancelLobby() {
        PartyPresentation.clear();
        clearSnapshots();
        synchronized (connectionLock) {
            generation++;
            foregroundLost = false;
            if (connection != null) connection.close();
            connection = null;
        }
        runIdentity = new Object();
        PartyWindows.clear();
        discovered.clear();
        commands.clear();
        logs.clear();
        state = LobbyState.IDLE;
        status = "";
        hostAddresses = Collections.emptyList();
        hostPort = 0;
        chosen = null;
        request = null;
        start = null;
        joined = hosting = split = started = departurePending = switching = false;
        departureRequested = lanAllowed = false;
        fallenHero = -1;
        ending = surfaceEnding = false;
        localIndex = 0;
        remoteTarget = null;
        targetPrompt = null;
        clientTargetToken = 0;
        sequence = receivedSequence = 0;
        appliedSequence = sentStep = receivedStep = 0;
    }

    private static void clearSnapshots() {
        awaitingState = fullStateRequested = false;
        nextStateRequest = nextFullState = 0;
        actorIdCeiling = 0;
        animations.clear();
        moves.clear();
        moveDelay = 0;
        PartyEffects.clear();
        snapshots = new PartyTraffic(10, 24);
        updates = new PartyUpdates();
        pendingState = null;
        appliedState = null;
        remainsDepth = 0;
        lastStateText = null;
        deaths.clear();
        deathDepth = 0;
    }

    public static void sameDevice(boolean separateControls) {
        cancelLobby();
        split = separateControls;
    }

    public static boolean network() {
        return joined;
    }

    public static Object runIdentity() {
        return runIdentity;
    }

    public static boolean client() {
        return joined && !hosting;
    }

    public static boolean splitControls() {
        return split && LocalCoop.active();
    }

    public static boolean applyingSnapshot() {
        return applying || switching;
    }

    public static Hero localHero() {
        Hero[] party = LocalCoop.heroes();
        return party.length == 2 ? party[localIndex] : Dungeon.hero;
    }

    public static Hero partner() {
        return LocalCoop.partner(network() ? localHero() : Dungeon.hero);
    }

    public static Hero turnHero() {
        if (!LocalCoop.active()) return null;
        if (client()) {
            if (appliedState == null || !appliedState.contains("turn_hero")) return null;
            int index = appliedState.getInt("turn_hero");
            Hero first = LocalCoop.primary();
            Hero hero = index == 0 ? first : index == 1 ? LocalCoop.partner(first) : null;
            return hero != null && hero.ready ? hero : null;
        }
        Actor current = Actor.current();
        return current instanceof Hero && LocalCoop.contains((Hero)current) && ((Hero)current).ready
                ? (Hero)current : null;
    }

    public static void observeActorId(int id) {
        if (network()) actorIdCeiling = Math.max(actorIdCeiling, id);
    }

    public static int nextActorId(int currentMaximum) {
        if (!network()) return currentMaximum + 1;

        actorIdCeiling = Math.max(actorIdCeiling, currentMaximum);
        if (actorIdCeiling >= Integer.MAX_VALUE - 1) throw new IllegalStateException("Party actor IDs exhausted");
        return ++actorIdCeiling;
    }

    public static boolean waitingForPartner() {
        if (!joined || ending || departurePending || departureRequested || applying || switching) return false;
        Hero local = localHero();
        Hero other = partner();
        return local != null && local.isAlive() && other != null && other.isAlive()
                && other.ready && other.curAction == null && !other.restoreHealth && !LocalCoop.hasCommand(other)
                && (client() || Actor.current() == other);
    }

    public static boolean remoteContext() {
        return joined && hosting && LocalCoop.active() && Dungeon.hero != localHero()
                && LocalCoop.contains(Dungeon.hero);
    }

    public static void withRemote(Runnable action) {
        Hero remote = LocalCoop.partner(localHero());
        if (remote == null) return;
        boolean previous = executing;
        Level level = Dungeon.level;
        boolean[] view = Level.fieldOfView.clone();
        executing = true;
        try {
            LocalCoop.withHero(remote, () -> {
                if (level != null) level.updateFieldOfView(remote);
                action.run();
            });
        } finally {
            if (Dungeon.level == level) System.arraycopy(view, 0, Level.fieldOfView, 0, view.length);
            executing = previous;
        }
    }

    public static void update() {
        synchronized (connectionLock) {
            if (foregroundLost) {
                foregroundLost = false;

                if (connection != null || state == LobbyState.HOSTING || state == LobbyState.DISCOVERING
                        || state == LobbyState.CONNECTING || state == LobbyState.WAITING) disconnect(text("background"));
                else generation++;
            }
        }
        NetworkAccess.poll();
        if (connection != null) connection.poll();
        if (!switching) {
            if (hosting && (moveDelay -= Game.elapsed) <= 0) {
                flushMoves();
                moveDelay = 0.05f;
            }
            playAnimations();
            PartyEffects.update();
        }
        pingDelay = Math.max(0, pingDelay - Game.elapsed);
        if (hosting) snapshotDelay -= Game.elapsed;
        boolean menu = Game.scene() instanceof TitleScene || Game.scene() instanceof ClassicTitleScene
                || Game.scene() instanceof ClassicStartScene;
        if (notice != null && menu) {
            Game.scene().add(new WndMessage(notice));
            notice = null;
        }
        if (menu && started) {
            cancelLobby();
            return;
        }
        if (!joined) return;
        if (awaitingState && client()) requestFullState();
        if (LocalCoop.active() && Game.scene() instanceof GameScene) started = true;
        if (!started) return;
        if (ending) return;
        if (departureRequested) {
            if (settled() && !switching) finishDeparture();
            else return;
        }
        if (departurePending && client() && pendingState != null && !switching) {
            PartyWindows.clear();
            switching = true;
            PartyPresentation.capture(pendingState);
            Game.switchScene(GameScene.class);
            return;
        }
        if (departurePending && settled() && !switching) leavePartner();
        if (!joined) return;
        PartyWindows.update();
        if (client()) {
            if (pendingState != null && !switching && Window.topWindow() == null
                    && !PartyWindows.busy() && clientTargetToken == 0) {
                if (Game.scene() instanceof GameScene && Dungeon.depth == pendingState.getInt("depth")) {
                    refreshScene();
                } else {
                    switching = true;
                    PartyPresentation.capture(pendingState);
                    Game.switchScene(GameScene.class);
                }
            }
            return;
        }
    }

    public static void beforeActors() {
        if (!joined || !hosting || ending) return;
        if (!settled()) return;
        if (fullStateRequested && System.nanoTime() >= nextFullState) sendSnapshot();
        if (departureRequested) finishDeparture();
        if (departurePending) leavePartner();
        if (!joined) return;
        if (Window.topWindow() != null || GameScene.hasCellSelection()) return;
        int count = 0;
        while (!commands.isEmpty() && count++ < 8 && settled()) {
            Bundle command = commands.removeFirst();
            try {
                withRemote(() -> execute(command));
            } catch (RuntimeException invalid) {
                Log.e("Party", "Could not apply peer action: " + invalid);
                disconnect(text("invalid_data"));
                return;
            }
        }
        if (snapshotDelay <= 0 && settled()) {
            snapshotDelay = 0.15f;
            sendSnapshot();
        }
    }

    public static boolean settled() {
        if (!(Game.scene() instanceof GameScene) || Dungeon.level == null || applying) return false;
        if (client()) return !switching;
        Actor actor = Actor.current();
        if (LocalCoop.alive() && actor != null && (!(actor instanceof Hero)
                || !((Hero)actor).ready && remoteTarget == null && !PartyWindows.busy())) return false;
        for (Hero hero : LocalCoop.heroes()) {
            if (hero.sprite != null && hero.sprite.isMoving) return false;
        }
        for (Mob mob : Dungeon.level.mobs) {
            if (mob.sprite != null && mob.sprite.isMoving) return false;
        }
        return true;
    }

    private static void sendSnapshot() {
        if (connection == null || !connection.connected()) return;
        flushMoves();
        PartyEffects.flush();
        try {
            Bundle snapshot = PartySnapshot.capture();
            String text = snapshot.toString();
            boolean full = fullStateRequested && System.nanoTime() >= nextFullState;
            if (!full && text.equals(lastStateText)) return;
            Bundle message = new Bundle();
            message.put("kind", "state");
            message.put("state", updates.encode(snapshot, full));
            sequence = updates.revision();
            message.put("sequence", sequence);
            if (!connection.send(PartyWire.encode(message))) throw new IOException("Party state was not queued");
            lastStateText = text;
            if (full) {
                fullStateRequested = false;
                nextFullState = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            }
        } catch (IOException | RuntimeException failure) {
            Log.e("Party", "Could not send party state: " + Log.getStackTraceString(failure));
            disconnect(text("sync_failed"));
        }
    }

    public static boolean prepareScene() {
        if (!client() || pendingState == null) return true;
        applying = true;
        try {
            if (!started) Generator.reset();
            PartySnapshot.restore(pendingState, 1);
            appliedState = pendingState;
            appliedSequence = receivedSequence;
            StartScene.finishNetworkStart();
            pendingState = null;
            InterlevelScene.mode = InterlevelScene.Mode.NONE;
            started = true;
        } catch (IOException | RuntimeException failure) {
            Log.e("Party", "Could not restore party state: " + android.util.Log.getStackTraceString(failure));
            if (appliedState != null) {
                try {
                    PartySnapshot.restore(appliedState, 1);
                    pendingState = null;
                    disconnect(text("sync_failed"));
                    return true;
                } catch (IOException | RuntimeException restoreFailure) {
                    Log.e("Party", "Could not restore the last party state: " + restoreFailure);
                }
            }
            cancelLobby();
            LocalCoop.clear();
            StartScene.finishNetworkStart();
            notice = text("sync_failed");
            Game.switchScene(TitleScene.class);
            return false;
        } finally {
            applying = false;
            switching = false;
        }
        return true;
    }

    private static void refreshScene() {
        boolean wasAlive = LocalCoop.alive();
        applying = true;
        try {
            PartyPresentation.Update visuals = new PartyPresentation.Update(pendingState, receivedSequence);
            PartySnapshot.restore(pendingState, 1);
            visuals.apply();
            appliedState = pendingState;
            appliedSequence = receivedSequence;
            pendingState = null;
        } catch (IOException | RuntimeException failure) {
            Log.e("Party", "Could not update party state: " + Log.getStackTraceString(failure));

            pendingState = appliedState;
            disconnect(text("sync_failed"));
            switching = true;
            Game.switchScene(GameScene.class);
            return;
        } finally {
            applying = false;
        }
        clientReady(wasAlive);
    }

    public static void sceneReady() {
        if (joined && hosting) {
            sendSnapshot();
            snapshotDelay = 0.15f;
            return;
        }
        if (!client()) return;
        PartyPresentation.restore();
        clientReady(true);
    }

    private static void clientReady(boolean showGameOver) {
        playAnimations();
        if (remainsDepth != 0) {
            if (remainsDepth == Dungeon.depth) GameScene.bossRemains(remainsDepth, remainsCell);
            remainsDepth = 0;
        }
        while (!logs.isEmpty()) GLog.update.dispatch(logs.removeFirst());
        if (clientTargetToken != 0) showTarget();
        PartyWindows.update();
        if (showGameOver && !LocalCoop.alive()) GameScene.gameOver();
        if (ending) Game.switchScene(surfaceEnding ? SurfaceScene.class : TitleScene.class);
    }

    public static boolean input(Hero hero, String action, int value) {
        if (!client() || executing || applying || hero != localHero()) return false;
        Bundle payload = new Bundle();
        payload.put("value", value);
        command(action, payload);
        return true;
    }

    public static void command(String action, Bundle payload) {
        if (!client() || switching || awaitingState || localHero() == null) return;
        Bundle message = new Bundle();
        message.put("kind", "command");
        message.put("action", action);
        message.put("depth", Dungeon.depth);
        message.put("sequence", receivedSequence);
        message.put("payload", payload);
        send(message);
    }

    private static void execute(Bundle message) {
        Hero hero = Dungeon.hero;
        if (message.getInt("depth") != Dungeon.depth) return;
        Bundle payload = message.getBundle("payload");
        int value = payload.getInt("value");
        String action = message.getString("action");
        if (!hero.isAlive() && !action.startsWith("ui_")) return;
        if (PartyWindows.busy() && !action.startsWith("ui_") && !action.equals("ping")) return;
        if (remoteTarget != null && !action.equals("target") && !action.equals("cancel") && !action.equals("ping")) return;
        switch (action) {
            case "move":
                if (remoteTarget == null && value >= 0 && value < Level.LENGTH
                        && (Dungeon.level.visited[value] || Dungeon.level.mapped[value] || Dungeon.visible[value])) hero.handle(value);
                break;
            case "rest":
                hero.rest(value != 0);
                break;
            case "search":
                hero.search(true);
                break;
            case "resume":
                hero.resume();
                break;
            case "cancel":
                if (remoteTarget != null) {
                    CellSelector.Listener listener = remoteTarget;
                    remoteTarget = null;
                    listener.onSelect(null);
                    Bundle cleared = new Bundle();
                    cleared.put("kind", "target");
                    cleared.put("token", 0);
                    send(cleared);
                }
                hero.interrupt(StopReason.CANCELLED);
                hero.restoreHealth = false;
                break;
            case "inventory":
                GameScene.selectItem(null, WndBag.Mode.ALL, null);
                break;
            case "skills":
                GameScene.show(new WndSkillsNew(null, null));
                break;
            case "last_skill":
                if (!LocalCoop.defer(hero, () -> hero.heroSkills.showLastUsed())) hero.heroSkills.showLastUsed();
                break;
            case "quick":
                QuickSlot.partyUse(value, false);
                break;
            case "quick_assign":
                QuickSlot.partyUse(value, true);
                break;
            case "go":
                walkToPartner(hero);
                break;
            case "ping":
                if (pingDelay == 0) announce(hero, Ping.fromId(value));
                break;
            case "target":
                if (remoteTarget != null && payload.getInt("token") == targetToken
                        && (value == -1 || value >= 0 && value < Level.LENGTH)) {
                    CellSelector.Listener listener = remoteTarget;
                    remoteTarget = null;
                    listener.onSelect(value == -1 ? null : value);
                }
                break;
            case "ui_choose":
                PartyWindows.choose(token(payload), payload.getInt("index"));
                break;
            case "ui_close":
                PartyWindows.close(token(payload));
                break;
            default:
                break;
        }
    }

    private static long token(Bundle payload) {
        try {
            return payload.getInt("token");
        } catch (NumberFormatException invalid) {
            return -1;
        }
    }

    public static boolean openInventory() {
        return input(localHero(), "inventory", 0);
    }

    public static boolean openSkills() {
        return input(localHero(), "skills", 0);
    }

    public static boolean blocksLocalInput() {
        return network() && hosting && Dungeon.hero == localHero() && (remoteTarget != null || PartyWindows.busy());
    }

    public static void goToPartner() {
        Hero hero = network() ? localHero() : Dungeon.hero;
        if (!input(hero, "go", 0)) walkToPartner(hero);
    }

    private static void walkToPartner(Hero hero) {
        Hero partner = LocalCoop.partner(hero);
        if (partner == null || !partner.isAlive()) return;
        int best = -1, distance = Integer.MAX_VALUE;
        for (int offset : Level.NEIGHBOURS8) {
            int cell = partner.pos + offset;
            if (cell < 0 || cell >= Level.LENGTH
                    || !Level.passable[cell] || Actor.findChar(cell) != null) continue;
            int length = Level.distance(hero.pos, cell);
            if (length < distance) {
                best = cell;
                distance = length;
            }
        }
        if (best != -1) hero.handle(best);
    }

    public static void ping(Ping ping) {
        if (!input(network() ? localHero() : Dungeon.hero, "ping", ping.id) && pingDelay == 0)
            announce(Dungeon.hero, ping);
    }

    private static void announce(Hero hero, Ping ping) {
        pingDelay = 1;
        showPing(hero.heroClass, ping);
        if (!network() || !hosting) return;
        Bundle message = new Bundle();
        message.put("kind", "ping");
        message.put("hero", hero.heroClass.name());
        message.put("ping", ping.id);
        send(message);
    }

    private static void showPing(HeroClass hero, Ping ping) {
        String line = GLog.HIGHLIGHT + Messages.maybeTranslate(hero.title()) + ": "
                + text("ping_" + ping.name().toLowerCase(Locale.ROOT));
        receiveLog(line);
    }

    public static boolean captureTarget(CellSelector.Listener listener, boolean ready) {
        if (!remoteContext()) return false;
        if (ready) {
            QuickSlot.cancel();
            return true;
        }
        remoteTarget = listener;
        Bundle message = new Bundle();
        message.put("kind", "target");
        message.put("token", ++targetToken);
        try {
            message.put("prompt", PartyText.encode(listener.prompt()));
            send(message);
        } catch (IllegalArgumentException failure) {
            textFailure(failure);
        }
        return true;
    }

    private static void showTarget() {
        final int token = clientTargetToken;
        GameScene.selectCell(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                Bundle payload = new Bundle();
                payload.put("token", token);
                payload.put("value", cell == null ? -1 : cell);
                command("target", payload);
                clientTargetToken = 0;
                GameScene.ready();
            }

            @Override
            public Message prompt() {
                return targetPrompt;
            }
        });
    }

    public static void sendUi(Bundle payload) {
        Bundle message = new Bundle();
        message.put("kind", "ui");
        message.put("payload", payload);
        send(message);
    }

    public static void finishRun(boolean surface) {
        if (!joined || !hosting || ending) return;
        Bundle message = new Bundle();
        message.put("kind", "finished");
        message.put("surface", surface);
        try {
            message.put("state", updates.encode(PartySnapshot.capture(), true));
            message.put("sequence", updates.revision());
        } catch (IOException | RuntimeException failure) {
            Log.e("Party", "Could not send final party state: " + failure);
        }
        ending = true;
        finishConnection(message);
        Game.switchScene(surface ? SurfaceScene.class : TitleScene.class);
    }

    public static void leave() {
        if (!joined || ending || departurePending) return;
        if (hosting) departureRequested = true;
        else disconnect(text("left"));
    }

    public static void heroDied(Hero hero) {
        if (!joined || !hosting || applying || ending || departurePending) return;
        fallenHero = hero == LocalCoop.primary() ? 0 : 1;
        Bundle message = new Bundle();
        message.put("kind", "fallen");
        message.put("hero", fallenHero);
        finishConnection(message);
        disconnect(text("left"));
    }

    private static void finishDeparture() {
        Bundle message = new Bundle();
        message.put("kind", "departed");
        try {
            message.put("state", updates.encode(PartySnapshot.capture(), true));
            message.put("sequence", updates.revision());
            finishConnection(message);
        } catch (IOException | RuntimeException failure) {
            Log.e("Party", "Could not send the last party state: " + failure);
        }
        disconnect(text("left"));
    }

    private static void finishConnection(Bundle message) {
        PartyConnection departing = connection;
        connection = null;
        if (departing == null) return;
        try {
            if (!departing.finish(PartyWire.encode(message))) departing.close();
        } catch (RuntimeException failure) {
            departing.close();
            Log.e("Party", "Could not finish the party connection: " + failure);
        }
    }

    private static final String[] LOG_TONES = {"", GLog.POSITIVE, GLog.NEGATIVE, GLog.WARNING, GLog.HIGHLIGHT};

    public static void log(String prefix, Message text) {
        if (!network() || !hosting || applying) return;
        int tone = 0;
        while (tone < LOG_TONES.length && !LOG_TONES[tone].equals(prefix)) tone++;
        if (tone == LOG_TONES.length) throw new IllegalArgumentException("Unknown log tone");
        Bundle message = new Bundle();
        message.put("kind", "log");
        message.put("tone", tone);
        try {
            message.put("text", PartyText.encode(text));
            send(message);
        } catch (IllegalArgumentException failure) {
            textFailure(failure);
        }
    }

    static void textFailure(IllegalArgumentException failure) {
        Log.e("Party", "Could not describe a party message: " + Log.getStackTraceString(failure));
        disconnect(text("invalid_data"));
    }

    private static void receiveLog(String line) {
        if (!network() || started && !switching) GLog.update.dispatch(line);
        else {
            if (logs.size() >= 40) logs.removeFirst();
            logs.addLast(line);
        }
    }

    public static void interrupted(Hero hero, StopReason reason) {
        if (!joined || !hosting || applying || !LocalCoop.contains(hero)) return;
        if (hero.curAction == null && !hero.restoreHealth && !LocalCoop.hasCommand(hero)) return;
        snapshotDelay = 0;
        if (hero == localHero()) return;
        Bundle message = new Bundle();
        message.put("kind", "stopped");
        message.put("reason", reason.name());
        send(message);
    }

    public static void animation(int actorId, String kind, int cell) {
        if (!network() || !hosting || applying) return;
        flushMoves();
        Bundle message = new Bundle();
        message.put("kind", "animation");
        message.put("actor", actorId);
        message.put("animation", kind);
        message.put("cell", cell);
        message.put("depth", Dungeon.depth);
        send(message);
    }

    public static void move(int actorId, int from, int to) {
        if (!network() || !hosting || applying) return;
        if (moveDepth != Dungeon.depth) {
            flushMoves();
            moveDepth = Dungeon.depth;
        }
        Bundle message = new Bundle();
        message.put("actor", actorId);
        message.put("from", from);
        message.put("cell", to);
        message.put("step", ++sentStep);
        moves.addLast(message);
        if (moves.size() == 48) flushMoves();
    }

    private static void flushMoves() {
        if (moves.isEmpty()) return;
        Bundle message = new Bundle();
        message.put("kind", "animation");
        message.put("animation", "move");
        message.put("depth", moveDepth);
        message.put("sequence", sequence);
        message.put("count", moves.size());
        for (int i = 0; !moves.isEmpty(); i++) message.put("move" + i, moves.removeFirst());
        send(message);
    }

    public static void bossRemains(int depth, int cell) {
        if (!network() || !hosting || applying) return;
        Bundle message = new Bundle();
        message.put("kind", "boss_remains");
        message.put("depth", depth);
        message.put("cell", cell);
        send(message);
    }

    public static void mobDied(Mob mob) {
        if (!joined || !hosting || applying) return;
        if (deathDepth != Dungeon.depth) {
            deaths.clear();
            deathDepth = Dungeon.depth;
        }
        if (deaths.size() == MAX_DEATHS && !deaths.containsKey(mob.id()))
            deaths.remove(deaths.keySet().iterator().next());
        deaths.put(mob.id(), mob.pos);
    }

    static void storeDeaths(Bundle state) {
        if (deathDepth != Dungeon.depth) {
            deaths.clear();
            deathDepth = Dungeon.depth;
        }
        int[] ids = new int[deaths.size()], cells = new int[deaths.size()];
        int index = 0;
        for (java.util.Map.Entry<Integer, Integer> death : deaths.entrySet()) {
            ids[index] = death.getKey();
            cells[index++] = death.getValue();
        }
        state.put("dead_mobs", ids);
        state.put("dead_cells", cells);
    }

    private static void send(Bundle message) {
        if (connection != null && connection.connected()) connection.send(PartyWire.encode(message));
    }

    static void sendEffects(Bundle message) {
        send(message);
    }

    private static void receive(String json) {
        try {
            JSONObject data = PartyConnection.parse(json);
            PartyWire.decode(data);
            PartyMessage.validate(data, hosting, sequence);
            Bundle message = Bundle.read(data.toString().getBytes(StandardCharsets.UTF_8));
            String kind = message.getString("kind");
            if (!hosting && (kind.equals("state") || kind.equals("departed") || kind.equals("finished")))
                snapshots.receive(json.length());
            if (hosting) {
                if (kind.equals("resync")) {
                    fullStateRequested = true;
                    return;
                }
                if (commands.size() >= 32) throw new IOException("Too many queued party actions");
                commands.addLast(message);
                return;
            }
            switch (kind) {
                case "boss_remains":
                    remainsDepth = message.getInt("depth");
                    remainsCell = message.getInt("cell");
                    if (started && !switching && remainsDepth == Dungeon.depth) {
                        GameScene.bossRemains(remainsDepth, remainsCell);
                        remainsDepth = 0;
                    }
                    break;
                case "effects":
                    PartyEffects.receive(message);
                    break;
                case "departed":
                    Bundle departure = receiveState(message.getString("state"), message.getInt("sequence"));
                    if (departure != null) pendingState = departure;
                    disconnect(text("left"));
                    break;
                case "fallen":
                    int fallen = message.getInt("hero");
                    if (fallen < 0 || fallen > 1) throw new IOException("Invalid party member");
                    fallenHero = fallen;
                    disconnect(text("left"));
                    break;
                case "animation":
                    if (message.getString("animation").equals("move")) {
                        for (int i = 0; i < message.getInt("count"); i++) {
                            Bundle move = message.getBundle("move" + i);
                            int step = move.getInt("step");
                            if (step <= receivedStep) continue;
                            if (step != receivedStep + 1) {
                                awaitingState = true;
                                requestFullState();
                            }
                            receivedStep = step;
                            if (animations.size() == 192) animations.removeFirst();
                            animations.addLast(new PendingAnimation(message, move));
                        }
                        break;
                    }
                    if (animations.size() == 192) animations.removeFirst();
                    animations.addLast(new PendingAnimation(message, message));
                    break;
                case "finished":
                    Bundle finalState = message.contains("state")
                            ? receiveState(message.getString("state"), message.getInt("sequence")) : null;
                    ending = true;
                    surfaceEnding = message.getBoolean("surface");
                    PartyWindows.clear();
                    clientTargetToken = 0;
                    if (finalState != null) pendingState = finalState;
                    switching = true;
                    Game.switchScene(GameScene.class);
                    break;
                case "state":
                    Bundle next = receiveState(message.getString("state"), message.getInt("sequence"));
                    if (next == null) break;
                    pendingState = next;
                    if (!started && !switching) {
                        switching = true;
                        PartyPresentation.capture(pendingState);
                        Game.switchScene(GameScene.class);
                    }
                    break;
                case "ui":
                    PartyWindows.receive(message.getBundle("payload"));
                    break;
                case "stopped":
                    StopReason reason = StopReason.valueOf(message.getString("reason"));
                    if (reason != StopReason.CANCELLED) {
                        String line = text(reason == StopReason.ENEMY ? "stopped_enemy" : "stopped");
                        if (started && !switching) GLog.localW(line);
                        else {
                            if (logs.size() >= 40) logs.removeFirst();
                            logs.addLast(GLog.WARNING + line);
                        }
                    }
                    break;
                case "target":
                    clientTargetToken = message.getInt("token");
                    targetPrompt = clientTargetToken == 0 ? Message.empty() : PartyText.decoded(message.getBundle("prompt"));
                    if (started && !switching) {
                        if (clientTargetToken == 0) GameScene.ready();
                        else showTarget();
                    }
                    break;
                case "log":
                    receiveLog(LOG_TONES[message.getInt("tone")] + PartyText.render(message.getBundle("text")));
                    break;
                case "ping":
                    showPing(PartyConnection.hero(message.getString("hero")), Ping.fromId(message.getInt("ping")));
                    break;
                default:
                    break;
            }
        } catch (IOException | JSONException | RuntimeException invalid) {
            Log.w("Party", "Rejected peer data: " + Log.getStackTraceString(invalid));
            disconnect(text("invalid_data"));
        }
    }

    private static void disconnect(String reason) {
        synchronized (connectionLock) {
            generation++;
            if (connection != null) connection.close();
            connection = null;
        }
        start = null;
        request = null;
        hostAddresses = Collections.emptyList();
        hostPort = 0;
        discovered.clear();
        lanAllowed = false;
        commands.clear();
        if (ending) return;
        status = reason;
        departureRequested = false;
        if (started || switching && pendingState != null) departurePending = true;
        else {
            state = LobbyState.ERROR;
            joined = false;
            clearSnapshots();
        }
    }

    private static Bundle receiveState(String encoded, int revision) throws IOException {
        try {
            Bundle state = updates.decode(encoded, revision);
            if (state != null) {
                receivedSequence = updates.revision();
                awaitingState = false;
            }
            return state;
        } catch (PartyUpdates.MissingBase gap) {
            awaitingState = true;
            requestFullState();
            return null;
        }
    }

    private static void requestFullState() {
        long now = System.nanoTime();
        if (now < nextStateRequest || connection == null || !connection.connected()) return;
        Bundle message = new Bundle();
        message.put("kind", "resync");
        message.put("sequence", receivedSequence);
        send(message);
        nextStateRequest = now + TimeUnit.SECONDS.toNanos(2);
    }

    private static void playAnimations() {
        if (!client() || !started || switching || !(Game.scene() instanceof GameScene)) return;
        long now = System.nanoTime();
        for (int count = animations.size(); count > 0; count--) {
            PendingAnimation animation = animations.removeFirst();
            if (now - animation.received > TimeUnit.SECONDS.toNanos(2)) continue;
            if (animation.depth == Dungeon.depth) {
                if (animation.kind.equals("move")) {
                    if (animation.sequence >= appliedSequence) {
                        PartyPresentation.move(animation.actor, animation.from, animation.cell,
                                animation.sequence, animation.step);
                    }
                } else {
                    PartyPresentation.animate(animation.actor, animation.kind, animation.cell);
                }
            } else {
                animations.addLast(animation);
            }
        }
    }

    private static final class PendingAnimation {
        final int depth, actor, cell, from, sequence, step;
        final String kind;
        final long received = System.nanoTime();

        PendingAnimation(Bundle message, Bundle actorData) {
            depth = message.getInt("depth");
            actor = actorData.getInt("actor");
            cell = actorData.getInt("cell");
            kind = message.getString("animation");
            from = actorData.getInt("from");
            sequence = message.getInt("sequence");
            step = actorData.getInt("step");
        }
    }

    private static void leavePartner() {
        departurePending = false;

        if (client()) PartyPresentation.release();
        Hero survivor = localHero();
        Hero departed = LocalCoop.partner(survivor);
        PartyWindows.clear();
        remoteTarget = null;
        targetPrompt = null;
        clientTargetToken = 0;
        clearSnapshots();
        commands.clear();
        logs.clear();
        joined = false;
        split = false;
        if (fallenHero == localIndex) removeHero(survivor);
        if (departed != null) removeHero(departed);
        LocalCoop.cancelCommand(survivor);
        GameScene.ready();
        LocalCoop.select(survivor);
        if (fallenHero < 0) GLog.localW(status.isEmpty() ? text("left") : status);
        if (!survivor.isAlive()) GameScene.gameOver();
    }

    private static void removeHero(Hero hero) {
        boolean alive = hero.isAlive();
        LocalCoop.cancelCommand(hero);
        hero.curAction = null;
        hero.HP = 0;
        LocalCoop.withHero(hero, () -> {
            for (Buff buff : new ArrayList<>(hero.buffs())) buff.detach();
        });
        Actor.remove(hero);
        if (Actor.findChar(hero.pos) == hero) Actor.freeCell(hero.pos);
        hero.next();
        if (alive && hero.sprite != null) hero.sprite.die();
    }

    private static void fail(String message) {
        disconnect(message);
        state = LobbyState.ERROR;
    }

    private static String text(String key) {
        return Messages.get(PartySession.class, key);
    }

    private static String failureMessage(String reason, String fallback) {
        switch (reason) {
            case "incompatible":
            case "different_fork":
            case "invalid_data":
            case "permission":
            case "connect_timeout":
            case "host_unreachable":
            case "pairing_failed":
            case "declined":
            case "party_full":
            case "request_expired":
                return text(reason);
            case "This party is full":
                return text("party_full");
            case "The join request expired":
                return text("request_expired");
            case "The connection stopped responding":
                return text("connect_timeout");
            case "Connection closed":
                return text("host_left");
            default:
                return text(fallback);
        }
    }

    private static class Events implements PartyConnection.Listener {
        private final int session = generation;

        private boolean current() {
            return session == generation && foreground && !foregroundLost;
        }

        @Override
        public void onHostReady(int port, List<String> addresses) {
            if (!current()) return;
            hostAddresses = Collections.unmodifiableList(new ArrayList<>(addresses));
            hostPort = port;
            state = LobbyState.HOSTING;
        }

        @Override
        public void onDiscovered(PartyConnection.Host host) {
            if (!current()) return;
            if (discovered.size() < 64) discovered.put(host.id, host);
        }

        @Override
        public void onJoinRequest(PartyConnection.Peer peer) {
            if (!current()) return;
            if (request == null) request = peer;
            else if (connection != null) connection.reject(peer, text("waiting"));
        }

        @Override
        public void onJoinWithdrawn(PartyConnection.Peer peer) {
            if (!current()) return;
            if (request == peer) request = null;
        }

        @Override
        public void onWaiting() {
            if (current() && state == LobbyState.CONNECTING) state = LobbyState.WAITING;
        }

        @Override
        public void onConnected(PartyConnection.Peer peer) {
            if (!current()) return;
            joined = true;
            state = LobbyState.CONNECTED;
            status = "";
            if (hosting) start = new StartRequest(chosen, peer.heroClass);
        }

        @Override
        public void onMessage(String json) {
            if (current()) receive(json);
        }

        @Override
        public void onDisconnected(String reason) {
            if (!current()) return;
            boolean inGame = started || switching && pendingState != null;
            String key;
            if ("incompatible".equals(reason) || "invalid_data".equals(reason)) {
                key = reason;
            } else if (inGame) {
                key = "left";
            } else {
                key = "join_failed";
            }
            disconnect(inGame ? text(key) : failureMessage(reason, key));
        }

        @Override
        public void onError(String reason) {
            if (!current()) return;
            fail(failureMessage(reason, "unavailable"));
        }

        @Override
        public void onNotice(String key) {
            if (current()) status = text(key);
        }
    }
}
