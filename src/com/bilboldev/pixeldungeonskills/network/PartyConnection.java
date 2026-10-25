package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PartyConnection implements Closeable {
    public static final int DEFAULT_PORT = 47864;
    public static final int DISCOVERY_PORT = 47865;

    public static final int PROTOCOL = 1;

    public static final String FORK_ID = "00000000-0000-0000-0000-000000000000";
    public static final int MAX_MESSAGE_BYTES = 4 * 1024 * 1024;
    private static final int MAX_QUEUED_BYTES = 16 * 1024 * 1024;
    private static final int MAX_PENDING = 4;
    private static final long HEARTBEAT_MS = 2000;
    private static final long TIMEOUT_MS = 15000;
    private static final long APPROVAL_MS = 120000;
    private static final long FAILURE_WINDOW_MS = 60000;
    private static final String SERVICE = "skillful-pixel-dungeon-party";

    public interface Listener {
        default void onHostReady(int port, List<String> addresses) { }
        default void onDiscovered(Host host) { }
        default void onJoinRequest(Peer peer) { }
        default void onJoinWithdrawn(Peer peer) { }
        default void onWaiting() { }
        default void onConnected(Peer peer) { }
        default void onMessage(String json) { }
        default void onDisconnected(String reason) { }
        default void onError(String reason) { }
        default void onNotice(String key) { }
    }

    public static final class Host {
        public final String name;
        public final HeroClass heroClass;
        public final String address;
        public final int port;
        public final String id;

        public Host(String name, HeroClass heroClass, String address, int port, String id) {
            this.name = name;
            this.heroClass = heroClass;
            this.address = address;
            this.port = port;
            this.id = id;
        }
    }

    public static final class Peer {
        public final int id;
        public final HeroClass heroClass;
        public final String address;

        public Peer(int id, HeroClass heroClass, String address) {
            this.id = id;
            this.heroClass = heroClass;
            this.address = address;
        }
    }

    private static final class Event {
        final Runnable callback;
        final int bytes;

        Event(Runnable callback, int bytes) {
            this.callback = callback;
            this.bytes = bytes;
        }
    }

    private static final class Frame {
        final byte[] bytes;
        final String closeReason;
        final boolean authenticated;

        Frame(byte[] bytes, String closeReason, boolean authenticated) {
            this.bytes = bytes;
            this.closeReason = closeReason;
            this.authenticated = authenticated;
        }
    }

    private static final class Attempts {
        long since = System.nanoTime();
        int failures;
    }

    private final Listener listener;
    private final String id = UUID.randomUUID().toString();
    private final ConcurrentHashMap<Integer, Link> links = new ConcurrentHashMap<>();
    private final AtomicInteger nextPeer = new AtomicInteger();
    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<>();
    private final AtomicInteger eventBytes = new AtomicInteger();
    private final AtomicInteger eventCount = new AtomicInteger();
    private final Object eventLock = new Object();
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(task -> daemon(task, "Party heartbeat"));
    private volatile boolean closed;
    private volatile boolean hosting;
    private volatile boolean accepting, searching;
    private volatile HeroClass localHero;
    private volatile String pairingCode;
    private final LinkedHashMap<String, Attempts> attempts = new LinkedHashMap<>();
    private volatile ServerSocket server;
    private Socket connecting;
    private volatile DatagramSocket discovery;
    private volatile Link partner;
    private volatile int listeningPort;
    private Thread pollThread;

    public PartyConnection(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("A party needs a listener");
        this.listener = listener;
        heartbeat.scheduleAtFixedRate(this::tick, HEARTBEAT_MS, HEARTBEAT_MS, TimeUnit.MILLISECONDS);
    }

    public void host(HeroClass hero, String name) {
        host(hero, name, DEFAULT_PORT);
    }

    public synchronized void host(HeroClass hero, String name, int port) {
        requireIdle();
        requireHero(hero);
        if (port < 0 || port > 65535) throw new IllegalArgumentException("Invalid party port");
        localHero = hero;
        pairingCode = PartySecurity.newCode();
        hosting = true;
        accepting = true;
        worker("Party host", () -> listen(port));
    }

    public synchronized void connect(String address, int port, HeroClass hero, String code) {
        requireIdle();
        requireHero(hero);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid party port");
        byte[] ip = ipv4(address);
        pairingCode = PartySecurity.normalize(code);
        stopDiscovery();
        localHero = hero;
        Socket socket = new Socket();
        connecting = socket;
        worker("Party connect", () -> {
            try {
                socket.connect(new InetSocketAddress(InetAddress.getByAddress(ip), port), 5000);
                synchronized (this) {
                    if (closed) return;
                    Link link = new Link(socket, false);
                    links.put(link.number, link);
                    link.security = new PartySecurity(pairingCode, false);
                    link.start();
                    link.offer(message("hello").put("hero", PartySymbols.id(hero.name())).put("key", link.security.publicKey())
                            .put("nonce", link.security.nonce()), null);
                }
            } catch (IOException | JSONException | GeneralSecurityException | SecurityException failure) {
                closeSocket(socket);
                String reason;
                if (failure instanceof SocketTimeoutException) {
                    reason = "connect_timeout";
                } else if (failure instanceof ConnectException || failure instanceof NoRouteToHostException) {
                    reason = "host_unreachable";
                } else if (failure instanceof SecurityException) {
                    reason = "permission";
                } else {
                    reason = "unavailable";
                }
                post(() -> listener.onError(reason), 0);
            } finally {
                synchronized (this) {
                    if (connecting == socket) connecting = null;
                }
            }
        });
    }

    public synchronized void accept(Peer peer) {
        Link link = peer == null ? null : links.get(peer.id);
        if (!hosting || link == null) return;
        synchronized (link) {
            if (link.peer != peer || !link.authenticated || link.approved || link.ended || partner != null) return;
            link.approved = true;
            partner = link;
            try {
                if (!link.offer(message("accepted").put("hero", PartySymbols.id(localHero.name())), null)) return;
            } catch (JSONException impossible) {
                link.end("Could not approve the connection");
                return;
            }
        }
        stopHosting();
        post(() -> {
            if (!link.ended) listener.onConnected(peer);
        }, 0);
        for (Link other : links.values()) {
            if (other != link) other.reject("This party is full");
        }
    }

    public void reject(Peer peer, String reason) {
        Link link = peer == null ? null : links.get(peer.id);
        if (hosting && link != null && link.peer == peer && !link.approved) link.reject(reason);
    }

    public boolean send(String json) {
        return send(json, false);
    }

    public boolean finish(String json) {
        boolean queued = send(json, true);
        Link link = partner;
        if (!queued && (link == null || !link.finishing)) close();
        return queued;
    }

    private boolean send(String json, boolean finish) {
        Link link = partner;
        if (closed || link == null || !link.approved || link.ended) return false;
        try {
            JSONObject payload = message("data").put("data", parse(json));
            if (payload.toString().getBytes(StandardCharsets.UTF_8).length > MAX_MESSAGE_BYTES - 25) {
                throw new IOException("Message too large");
            }
            synchronized (link) {
                if (link.ended || link.finishing) return false;
                link.finishing = finish;
                return link.offer(payload, finish ? "Party finished" : null);
            }
        } catch (JSONException | IOException invalid) {
            throw new IllegalArgumentException("Party payload must be a bounded JSON object", invalid);
        }
    }

    public boolean connected() {
        Link link = partner;
        return !closed && link != null && link.approved && !link.ended;
    }

    public boolean hosting() {
        return hosting && !closed;
    }

    public int port() {
        return listeningPort;
    }

    public String pairingCode() {
        return hosting ? pairingCode : "";
    }

    public void poll() {
        if (pollThread == null) pollThread = Thread.currentThread();
        if (pollThread != Thread.currentThread()) throw new IllegalStateException("Poll the party from one game thread");
        for (int i = 0; i < 64; i++) {
            Event event;
            synchronized (eventLock) {
                event = events.poll();
                if (event == null) break;
                eventBytes.addAndGet(-event.bytes);
                eventCount.decrementAndGet();
            }
            if (!closed) event.callback.run();
        }
    }

    public synchronized void discover() {
        if (closed || hosting || localHero != null || searching) return;
        searching = true;
        worker("Party discovery", this::findHosts);
    }

    public synchronized void stopDiscovery() {
        searching = false;
        DatagramSocket socket = discovery;
        discovery = null;
        if (socket != null) socket.close();
    }

    private synchronized void stopHosting() {
        accepting = false;
        stopDiscovery();
        ServerSocket socket = server;
        server = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        hosting = false;
        stopHosting();
        if (connecting != null) closeSocket(connecting);
        connecting = null;
        heartbeat.shutdownNow();
        for (Link link : links.values()) link.end("Party closed");
        partner = null;
        synchronized (eventLock) {
            events.clear();
            eventBytes.set(0);
            eventCount.set(0);
        }
    }

    private void requireIdle() {
        if (closed || hosting || localHero != null) throw new IllegalStateException("Party connection already started");
    }

    private void listen(int port) {
        ServerSocket socket = null;
        try {
            synchronized (this) {
                if (closed || !accepting) return;
                socket = new ServerSocket();
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(port), MAX_PENDING);
                server = socket;
                listeningPort = socket.getLocalPort();
            }
            List<String> addresses;
            try {
                addresses = localAddresses();
            } catch (SocketException unavailableInterfaces) {
                addresses = Collections.emptyList();
            }
            final List<String> hostAddresses = addresses;
            post(() -> listener.onHostReady(listeningPort, hostAddresses), 0);
            worker("Party advertisement", this::advertise);
            while (!closed && accepting) {
                Socket incoming = socket.accept();
                synchronized (this) {
                    if (closed || !accepting || links.size() >= MAX_PENDING || throttled(incoming.getInetAddress().getHostAddress())) {
                        incoming.close();
                        continue;
                    }
                    try {
                        Link link = new Link(incoming, true);
                        links.put(link.number, link);
                        link.start();
                    } catch (IOException failure) {
                        incoming.close();
                        throw failure;
                    }
                }
            }
        } catch (IOException | SecurityException failure) {
            if (!closed && accepting) post(() -> listener.onError("Could not host a party: " + detail(failure)), 0);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void tick() {
        long now = System.nanoTime();
        for (Link link : links.values()) {
            if (millis(now - link.lastReceived) > TIMEOUT_MS) {
                link.end("The connection stopped responding");
            } else if (!link.approved && millis(now - link.created) > APPROVAL_MS) {
                link.reject("The join request expired");
            } else if (link.authenticated && !link.finishing) {
                link.offer(message("ping"), null);
            }
        }
    }

    private synchronized boolean throttled(String address) {
        Attempts previous = attempts.get(address);
        if (previous == null) return false;
        if (millis(System.nanoTime() - previous.since) >= FAILURE_WINDOW_MS) {
            attempts.remove(address);
            return false;
        }
        return previous.failures >= 5;
    }

    private synchronized void failed(String address) {
        Attempts previous = attempts.get(address);
        if (previous == null || millis(System.nanoTime() - previous.since) >= FAILURE_WINDOW_MS) {
            if (attempts.size() >= 128) attempts.remove(attempts.keySet().iterator().next());
            previous = new Attempts();
            attempts.put(address, previous);
        }
        previous.failures++;
    }

    private boolean post(Runnable callback, int bytes) {
        synchronized (eventLock) {
            if (closed || eventCount.get() >= 256 || eventBytes.get() + bytes > MAX_QUEUED_BYTES) return false;
            eventCount.incrementAndGet();
            eventBytes.addAndGet(bytes);
            events.add(new Event(callback, bytes));
            return true;
        }
    }

    private void disconnected(String reason) {
        synchronized (eventLock) {
            if (closed) return;
            if (eventCount.get() >= 256) {

                events.clear();
                eventBytes.set(0);
                eventCount.set(0);
            }
            post(() -> listener.onDisconnected(reason), 0);
        }
    }

    private final class Link {
        final int number = nextPeer.incrementAndGet();
        final Socket socket;
        final boolean incoming;
        final long created = System.nanoTime();
        final ArrayBlockingQueue<Frame> outgoing = new ArrayBlockingQueue<>(64);
        final AtomicInteger queuedBytes = new AtomicInteger();
        volatile long lastReceived = created;
        volatile boolean approved;
        volatile boolean authenticated;
        volatile boolean ended;
        volatile boolean finishing;
        volatile Peer peer;
        volatile PartySecurity security;
        HeroClass requestedHero;
        final PartyTraffic traffic = new PartyTraffic();

        Link(Socket socket, boolean incoming) throws SocketException {
            this.socket = socket;
            this.incoming = incoming;
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000);
        }

        void start() {
            worker("Party receive", this::read);
            worker("Party send", this::write);
        }

        boolean offer(JSONObject message, String closeReason) {
            if (ended || closed) return false;
            byte[] bytes = message.toString().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > MAX_MESSAGE_BYTES - 25 || queuedBytes.addAndGet(bytes.length) > MAX_QUEUED_BYTES) {
                if (bytes.length <= MAX_MESSAGE_BYTES - 25) queuedBytes.addAndGet(-bytes.length);
                end("The connection cannot keep up");
                return false;
            }
            if (!outgoing.offer(new Frame(bytes, closeReason, authenticated))) {
                queuedBytes.addAndGet(-bytes.length);
                end("The connection cannot keep up");
                return false;
            }
            return true;
        }

        void write() {
            try {
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                while (!closed && !ended) {
                    Frame frame = outgoing.poll(1000, TimeUnit.MILLISECONDS);
                    if (frame == null) continue;
                    queuedBytes.addAndGet(-frame.bytes.length);
                    byte[] bytes = frame.authenticated ? security.encrypt(frame.bytes)
                            : PartySecurity.encryptSetup(frame.bytes, false);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                    output.flush();
                    if (frame.closeReason != null) {
                        end(frame.closeReason);
                        return;
                    }
                }
            } catch (IOException | InterruptedException | GeneralSecurityException failure) {
                end("Connection closed");
            }
        }

        void read() {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                while (!closed && !ended) {
                    byte[] header = new byte[4];
                    readFully(input, header);
                    int length = ByteBuffer.wrap(header).getInt();
                    if (length <= 1 || length > MAX_MESSAGE_BYTES || !approved && length > 4096) {
                        throw new IOException("Invalid message size");
                    }
                    traffic.receive(length);
                    byte[] bytes = new byte[length];
                    readFully(input, bytes);
                    byte[] plaintext = authenticated ? security.decrypt(bytes) : PartySecurity.decryptSetup(bytes, false);
                    JSONObject message = parse(decode(plaintext));
                    lastReceived = System.nanoTime();
                    receive(message, length);
                }
            } catch (IOException | JSONException | GeneralSecurityException | IllegalArgumentException failure) {
                if (incoming && !authenticated) failed(socket.getInetAddress().getHostAddress());
                end(failure instanceof SocketTimeoutException ? "The connection stopped responding"
                        : failure instanceof EOFException || failure instanceof SocketException ? "Connection closed" : "invalid_data");
            }
        }

        void readFully(DataInputStream input, byte[] bytes) throws IOException {
            int offset = 0;
            while (offset < bytes.length && !ended && !closed) {
                try {
                    int count = input.read(bytes, offset, bytes.length - offset);
                    if (count < 0) throw new EOFException();
                    offset += count;
                } catch (SocketTimeoutException timeout) {
                    if (millis(System.nanoTime() - lastReceived) > TIMEOUT_MS) throw timeout;
                }
            }
            if (offset != bytes.length) throw new EOFException();
        }

        void receive(JSONObject message, int length) throws IOException, JSONException, GeneralSecurityException {
            if (!PartySecurity.MAGIC.equals(PartyMessage.string(message, "app", 64))) throw PartyMessage.invalid();
            int revision = PartyMessage.integer(message, "v", 1, Integer.MAX_VALUE);
            String kind = PartySymbols.name(PartyMessage.integer(message, "kind", 1, 65535));
            if (revision != PROTOCOL || "incompatible".equals(kind) && !incoming && !approved) {
                if (incoming && !approved) {
                    offer(PartyConnection.message("incompatible"), "incompatible");
                    post(() -> listener.onNotice("incompatible"), 0);
                } else end("incompatible");
                return;
            }
            if (!FORK_ID.equals(PartyMessage.string(message, "fork", 36))
                    || "different_fork".equals(kind) && !incoming && !approved) {
                if (incoming && !approved) offer(PartyConnection.message("different_fork"), "different_fork");
                else end("different_fork");
                return;
            }
            validateFrame(message, kind);
            if ("ping".equals(kind) && authenticated) {
                offer(PartyConnection.message("pong"), null);
                return;
            }
            if ("pong".equals(kind) && authenticated) return;
            if ("waiting".equals(kind) && !incoming && authenticated && !approved) {
                post(listener::onWaiting, 0);
                return;
            }
            if ("hello".equals(kind) && incoming && security == null && !authenticated) {
                requestedHero = hero(PartySymbols.name(PartyMessage.integer(message, "hero", 1, 65535)));
                security = new PartySecurity(pairingCode, true);
                security.agree(message.getString("key"), message.getString("nonce"), requestedHero.name(), localHero.name());
                offer(PartyConnection.message("challenge").put("hero", PartySymbols.id(localHero.name())).put("key", security.publicKey())
                        .put("nonce", security.nonce()).put("proof", security.proof()), null);
            } else if ("challenge".equals(kind) && !incoming && security != null && !authenticated) {
                requestedHero = hero(PartySymbols.name(PartyMessage.integer(message, "hero", 1, 65535)));
                security.agree(message.getString("key"), message.getString("nonce"), localHero.name(), requestedHero.name());
                try {
                    security.verify(message.getString("proof"));
                } catch (GeneralSecurityException failure) {
                    end("pairing_failed");
                    return;
                }
                offer(PartyConnection.message("proof").put("proof", security.proof()), null);
                authenticated = true;
            } else if ("proof".equals(kind) && incoming && security != null && !authenticated) {
                security.verify(message.getString("proof"));
                authenticated = true;
                peer = new Peer(number, requestedHero, socket.getInetAddress().getHostAddress());
                offer(PartyConnection.message("waiting"), null);
                if (!post(() -> {
                    if (!ended && !approved) listener.onJoinRequest(peer);
                }, 0)) {
                    throw new IOException("Too many pending events");
                }
            } else if ("accepted".equals(kind) && !incoming && authenticated && !approved) {
                if (hero(PartySymbols.name(PartyMessage.integer(message, "hero", 1, 65535))) != requestedHero) throw new IOException("Host hero changed during pairing");
                peer = new Peer(number, requestedHero, socket.getInetAddress().getHostAddress());
                synchronized (this) {
                    if (ended) return;
                    approved = true;
                    partner = this;
                }
                post(() -> {
                    if (!ended) listener.onConnected(peer);
                }, 0);
            } else if ("rejected".equals(kind) && !incoming && authenticated && !approved) {
                int reason = PartyMessage.integer(message, "reason", 1, 3);
                end(reason == 2 ? "party_full" : reason == 3 ? "request_expired" : "declined");
            } else if ("data".equals(kind) && approved) {
                String json = message.getJSONObject("data").toString();
                if (!post(() -> listener.onMessage(json), length)) throw new IOException("The game cannot keep up with this connection");
            } else {
                throw new IOException("Unexpected party message");
            }
        }

        void reject(String reason) {
            if (approved || ended) return;
            if (!authenticated) {
                end("Pairing was not completed");
                return;
            }
            int code = "This party is full".equals(reason) ? 2 : "The join request expired".equals(reason) ? 3 : 1;
            try {
                offer(message("rejected").put("reason", code), "Request declined");
            } catch (JSONException impossible) {
                end("Request declined");
            }
        }

        synchronized void end(String reason) {
            if (ended) return;
            ended = true;
            closeSocket(socket);
            outgoing.clear();
            links.remove(number);
            if (partner == this) partner = null;
            if (approved || !incoming) disconnected(reason);
            else if (peer != null) post(() -> listener.onJoinWithdrawn(peer), 0);
            if (finishing && !closed) {
                try {
                    heartbeat.execute(PartyConnection.this::close);
                } catch (RejectedExecutionException alreadyClosing) {
                }
            } else if (approved || !incoming) {
                heartbeat.shutdownNow();
            }
        }
    }

    private void advertise() {
        DatagramSocket socket = null;
        PartyTraffic traffic = new PartyTraffic(20, 40);
        try {
            synchronized (this) {
                if (closed || !accepting) return;
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(DISCOVERY_PORT));
                socket.setSoTimeout(1000);
                discovery = socket;
            }

            byte[] buffer = new byte[2049];
            while (!closed && discovery == socket) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException timeout) {
                    continue;
                }
                if (partner != null || !traffic.allow(packet.getLength())) continue;
                try {
                    JSONObject query = readDiscovery(packet);
                    PartyMessage.fields(query, "app", "v", "fork", "kind", "service");
                    if (!PartySecurity.MAGIC.equals(PartyMessage.string(query, "app", 64))
                            || !FORK_ID.equals(PartyMessage.string(query, "fork", 36))
                            || !SERVICE.equals(PartyMessage.string(query, "service", 64))
                            || PartyMessage.integer(query, "v", 1, Integer.MAX_VALUE) != PROTOCOL
                            || !"discover".equals(PartySymbols.name(PartyMessage.integer(query, "kind", 1, 65535)))) continue;
                    byte[] answer = PartySecurity.encryptSetup(message("host").put("service", SERVICE)
                            .put("hero", PartySymbols.id(localHero.name())).put("port", listeningPort).put("id", id)
                            .toString().getBytes(StandardCharsets.UTF_8), true);
                    socket.send(new DatagramPacket(answer, answer.length, packet.getAddress(), packet.getPort()));
                } catch (JSONException | IOException | GeneralSecurityException | IllegalArgumentException malformed) {

                }
            }
        } catch (IOException | SecurityException failure) {
            if (!closed && accepting) post(() -> listener.onError("Party discovery is unavailable"), 0);
        } finally {
            if (socket != null) socket.close();
        }
    }

    private void findHosts() {
        DatagramSocket socket = null;
        PartyTraffic traffic = new PartyTraffic(20, 40);
        try {
            synchronized (this) {
                if (closed || !searching || localHero != null || discovery != null) return;
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(500);
                discovery = socket;
            }
            Set<InetAddress> broadcasts = broadcasts();
            byte[] query = message("discover").put("service", SERVICE).toString().getBytes(StandardCharsets.UTF_8);
            byte[] buffer = new byte[2049];
            long sentAt = 0;
            while (!closed && discovery == socket) {
                long now = System.nanoTime();
                if (sentAt == 0 || millis(now - sentAt) >= 2000) {
                    byte[] encrypted = PartySecurity.encryptSetup(query, true);
                    for (InetAddress address : broadcasts) {
                        try {
                            socket.send(new DatagramPacket(encrypted, encrypted.length, address, DISCOVERY_PORT));
                        } catch (IOException unavailableInterface) {
                        }
                    }
                    sentAt = now;
                }
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException timeout) {
                    continue;
                }
                try {
                    if (!traffic.allow(packet.getLength())) continue;
                    JSONObject answer = readDiscovery(packet);
                    PartyMessage.fields(answer, "app", "v", "fork", "kind", "service", "port", "id", "hero");
                    if (!PartySecurity.MAGIC.equals(PartyMessage.string(answer, "app", 64))
                            || !FORK_ID.equals(PartyMessage.string(answer, "fork", 36))
                            || !SERVICE.equals(PartyMessage.string(answer, "service", 64))
                            || !"host".equals(PartySymbols.name(PartyMessage.integer(answer, "kind", 1, 65535)))
                            || !(packet.getAddress() instanceof Inet4Address)) continue;
                    if (PartyMessage.integer(answer, "v", 1, Integer.MAX_VALUE) != PROTOCOL) {
                        post(() -> listener.onNotice("incompatible"), 0);
                        continue;
                    }
                    int port = PartyMessage.integer(answer, "port", 1, 65535);
                    String hostId = PartyMessage.string(answer, "id", 64);
                    HeroClass hostHero = hero(PartySymbols.name(PartyMessage.integer(answer, "hero", 1, 65535)));
                    Host host = new Host(hostHero.name(), hostHero, packet.getAddress().getHostAddress(), port, hostId);
                    post(() -> listener.onDiscovered(host), 0);
                } catch (JSONException | IOException | GeneralSecurityException | IllegalArgumentException malformed) { }
            }
        } catch (IOException | JSONException | GeneralSecurityException | SecurityException failure) {
            if (!closed && searching && localHero == null) post(() -> listener.onError("Could not search for parties: " + detail(failure)), 0);
        } finally {
            if (socket != null) socket.close();
        }
    }

    private static JSONObject readDiscovery(DatagramPacket packet) throws GeneralSecurityException, IOException, JSONException {
        byte[] bytes = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
        return parse(decode(PartySecurity.decryptSetup(bytes, true)));
    }

    public static List<String> localAddresses() throws SocketException {
        List<String> result = new ArrayList<>();
        for (NetworkInterface network : interfaces()) {
            for (InterfaceAddress address : network.getInterfaceAddresses()) {
                if (address.getAddress() instanceof Inet4Address) result.add(address.getAddress().getHostAddress());
            }
        }
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    private static List<NetworkInterface> interfaces() throws SocketException {
        List<NetworkInterface> result = new ArrayList<>();
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        if (networks == null) return result;
        for (NetworkInterface network : Collections.list(networks)) {
            String name = network.getName().toLowerCase(Locale.ROOT);
            if (!network.isUp() || network.isLoopback() || network.isPointToPoint() || network.isVirtual()
                    || name.startsWith("tun") || name.startsWith("tap") || name.startsWith("utun")
                    || name.startsWith("wg") || name.startsWith("ppp")) continue;
            result.add(network);
        }
        return result;
    }

    private static Set<InetAddress> broadcasts() throws IOException {
        Set<InetAddress> result = new HashSet<>();
        for (NetworkInterface network : interfaces()) {
            for (InterfaceAddress address : network.getInterfaceAddresses()) {
                if (address.getAddress() instanceof Inet4Address && address.getBroadcast() != null) result.add(address.getBroadcast());
            }
        }
        result.add(InetAddress.getByAddress(new byte[]{-1, -1, -1, -1}));
        result.add(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        return result;
    }

    public static boolean validAddress(String address) {
        try {
            ipv4(address);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    public static boolean validCode(String code) {
        try {
            PartySecurity.normalize(code);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private static byte[] ipv4(String address) {
        if (address == null) throw new IllegalArgumentException("Enter an IPv4 host address");
        String[] parts = address.trim().split("\\.", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Enter an IPv4 host address");
        byte[] result = new byte[4];
        try {
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isEmpty() || parts[i].length() > 3) throw new NumberFormatException();
                for (int j = 0; j < parts[i].length(); j++) {
                    if (!Character.isDigit(parts[i].charAt(j))) throw new NumberFormatException();
                }
                int octet = Integer.parseInt(parts[i]);
                if (octet < 0 || octet > 255) throw new NumberFormatException();
                result[i] = (byte)octet;
            }
            if ((result[0] & 255) == 0 || (result[0] & 255) >= 224) throw new NumberFormatException();
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("Enter an IPv4 host address");
        }
        return result;
    }

    private static JSONObject message(String kind) {
        try {
            return new JSONObject().put("app", PartySecurity.MAGIC).put("v", PROTOCOL)
                    .put("fork", FORK_ID).put("kind", PartySymbols.id(kind));
        } catch (JSONException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    static JSONObject parse(String json) throws JSONException, IOException {
        return parse(json, MAX_MESSAGE_BYTES);
    }

    static JSONObject parse(String json, int maximumLength) throws JSONException, IOException {
        return PartyJson.parse(json, maximumLength);
    }

    private static void validateFrame(JSONObject message, String kind) throws IOException, JSONException {
        switch (kind) {
            case "hello":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "hero", "key", "nonce");
                break;
            case "challenge":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "hero", "key", "nonce", "proof");
                break;
            case "proof":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "proof");
                break;
            case "accepted":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "hero");
                break;
            case "rejected":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "reason");
                PartyMessage.integer(message, "reason", 1, 3);
                break;
            case "data":
                PartyMessage.fields(message, "app", "v", "fork", "kind", "data");
                PartyMessage.object(message, "data");
                break;
            case "ping": case "pong": case "waiting":
                PartyMessage.fields(message, "app", "v", "fork", "kind");
                break;
            default: throw PartyMessage.invalid();
        }
        if (message.has("hero")) hero(PartySymbols.name(PartyMessage.integer(message, "hero", 1, 65535)));
        if (message.has("key")) PartyMessage.string(message, "key", 512);
        if (message.has("nonce")) PartyMessage.string(message, "nonce", 64);
        if (message.has("proof")) PartyMessage.string(message, "proof", 64);
    }

    static HeroClass hero(String name) throws IOException {
        try {
            HeroClass hero = HeroClass.valueOf(name);
            requireHero(hero);
            return hero;
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Unknown hero");
        }
    }

    static void requireHero(HeroClass hero) {
        if (hero == null) throw new IllegalArgumentException("Choose a hero");
        switch (hero) {
            case WARRIOR:
            case MAGE:
            case ROGUE:
            case HUNTRESS:
            case NECROMANCER:
            case MERCENARY:
                return;
            default:
                throw new IllegalArgumentException("Choose a playable hero");
        }
    }

    private static String decode(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
    }

    private static String detail(Exception failure) {
        return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
    }

    private static long millis(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    private static Thread daemon(Runnable task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        return thread;
    }

    private static void worker(String name, Runnable task) {
        daemon(task, name).start();
    }

    private static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
