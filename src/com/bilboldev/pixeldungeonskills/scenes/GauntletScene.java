package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.*;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.gauntlet.GauntletArena;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonWallsTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.RaisedTerrainTilemap;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.ui.GauntletHeckle;
import com.bilboldev.pixeldungeonskills.windows.WndGauntletVictory;
import java.util.ArrayList;
import java.util.HashMap;

public class GauntletScene extends GameScene {
    public static int level = 1;
    public static boolean scenePause;
    private GauntletArena arena;
    private int renderedTheme;
    private RenderedTextMultiline waveLabel;
    private final ArrayList<Spectator> spectators = new ArrayList<>();
    private final HashMap<Integer, ItemSprite> rewardMarkers = new HashMap<>();
    private Group rewardOverlay;
    private float outroTime;
    private boolean victoryShown;
    private enum HeckleEvent {
        DAMAGE("damage", 6, 45), KILL("kill", 3, 45), WAVE("wave", 3, 65);
        final String key;
        final int lines, chance;
        HeckleEvent(String key, int lines, int chance) {
            this.key = key;
            this.lines = lines;
            this.chance = chance;
        }
    }

    private final java.util.Random heckleRandom = new java.util.Random();
    private GauntletHeckle heckle;
    private int noticedWave, noticedKills, noticedHits, lastHeckle = -1;
    private HeckleEvent lastHeckleEvent;
    private float heckleTime;

    @Override
    public void create() {
        arena = GauntletRun.arena();
        if (arena == null) throw new IllegalStateException("Missing Gauntlet arena");
        InterlevelScene.mode = InterlevelScene.Mode.NONE;
        super.create();
        Camera.main.zoom(Math.max(minZoom, Math.min(maxZoom, defaultZoom + GauntletRun.zoomOffset())));
        Camera.main.focusOn(Dungeon.hero.sprite);
        for (int cell = 0; cell < Level.LENGTH; cell++) if (!arena.crowdDeparted() && arena.isSpectatorTile(cell)) {
            Spectator rat = new Spectator(cell);
            rat.updateVisibility();
            spectators.add(rat);
            mobs.add(rat);
        }
        rewardOverlay = new Group();
        add(rewardOverlay);

        members.remove(rewardOverlay);
        members.add(members.indexOf(fogNew) + 1, rewardOverlay);
        waveLabel = PixelScene.renderMultiline("", 7);
        waveLabel.camera = uiCamera;
        waveLabel.hardlight(Window.TITLE_COLOR);
        waveLabel.maxWidth(Math.max(1, uiCamera.width - 60));
        add(waveLabel);
        heckle = new GauntletHeckle();
        heckle.camera = uiCamera;
        int heckleWidth = Math.min(150, uiCamera.width - 70);
        heckle.setRect((uiCamera.width - heckleWidth) / 2f, 34, heckleWidth, 0);
        add(heckle);

        noticedWave = arena.phase() == GauntletArena.Phase.FIGHTING ? arena.wave() : arena.completed();
        noticedKills = Statistics.enemiesSlain;
        noticedHits = ((GauntletHero)Dungeon.hero).hitsTaken();
        renderedTheme = arena.theme();
        updateRewards();
        updateHud();
        if (arena.phase() == GauntletArena.Phase.LOST)
            show(new com.bilboldev.pixeldungeonskills.windows.WndGauntletDie());
    }

    private void refreshArenaTheme() {
        if (renderedTheme == arena.theme()) return;
        renderedTheme = arena.theme();
        refreshTheme(this);
        water.texture(arena.waterTex());
        water.size(Level.WIDTH * 16, Level.HEIGHT * 16);
        if (isPerspectiveView()) firstPersonView().refreshTerrain();
        GameScene.updateMap();
        Dungeon.observe();
    }

    private void refreshTheme(Group group) {
        for (int i = 0; i < group.length; i++) {
            Gizmo member = group.memberAt(i);
            if (member instanceof DungeonTerrainTilemap || member instanceof DungeonWallsTilemap || member instanceof RaisedTerrainTilemap)
                ((DungeonTilemap)member).refreshTexture();
            else if (member instanceof Group) refreshTheme((Group)member);
        }
    }

    @Override
    public synchronized void update() {
        super.update();
        if (arena == null) return;
        if (Window.topWindow() == null) arena.releaseWave();
        refreshArenaTheme();
        arena.checkCompletion();
        if (arena.phase() == GauntletArena.Phase.VICTORY && !victoryShown) {
            if (outroTime == 0) frameArena();
            outroTime += Game.elapsed;
            if (arena.crowdDeparted() || outroTime >= 0.55f) {
                for (Spectator rat : spectators) rat.flee();
                if (remainingSpectators() == 0) {
                    arena.finishVictoryAnimation();
                    victoryShown = true;
                    show(new WndGauntletVictory());
                }
            }
        }
        for (Spectator rat : spectators) rat.updateVisibility();
        updateRewards();
        cellSelector.enabled = GauntletRun.acceptsInput() && Dungeon.hero.ready;
        busy.visible = GauntletRun.acceptsInput() && !Dungeon.hero.ready;
        updateHud();
        updateHeckling();
    }

    private void updateHeckling() {
        boolean kill = Statistics.enemiesSlain > noticedKills;
        noticedKills = Statistics.enemiesSlain;
        int hits = ((GauntletHero)Dungeon.hero).hitsTaken();
        boolean hurt = hits > noticedHits;
        noticedHits = hits;
        boolean waveStarted = arena.phase() == GauntletArena.Phase.FIGHTING && arena.wave() != noticedWave;
        if (waveStarted) noticedWave = arena.wave();

        if (!GauntletRun.acceptsInput() || arena.wave() == GauntletArena.WAVES) {
            heckleTime = 0;
            heckle.visible = false;
            return;
        }
        heckleTime = Math.max(0, heckleTime - Game.elapsed);
        boolean unobstructed = Window.topWindow() == null;
        HeckleEvent event = waveStarted ? HeckleEvent.WAVE : kill ? HeckleEvent.KILL : hurt ? HeckleEvent.DAMAGE : null;
        if (unobstructed && heckleTime == 0 && event != null
                && heckleRandom.nextInt(100) < event.chance) {
            int previous = lastHeckleEvent == event ? lastHeckle : -1;
            int line = heckleRandom.nextInt(event.lines - (previous < 0 ? 0 : 1));
            if (previous >= 0 && line >= previous) line++;
            lastHeckle = line;
            lastHeckleEvent = event;
            heckle.say(Messages.get("gauntlet.heckle_" + event.key + "_" + line));
            heckleTime = 3f;
        }

        heckle.visible = unobstructed && heckleTime > 0;
    }

    private void updateRewards() {
        ArrayList<Integer> cells = arena.rewardCells();
        for (int cell : new ArrayList<>(rewardMarkers.keySet())) if (!cells.contains(cell))
            rewardMarkers.remove(cell).killAndErase();
        for (int cell : cells) {
            Heap heap = arena.heaps.get(cell);
            ItemSprite marker = rewardMarkers.get(cell);
            if (marker == null) {
                marker = new ItemSprite();
                rewardMarkers.put(cell, marker);
                rewardOverlay.add(marker);
            }
            marker.view(heap.image(), heap.glowing());
            marker.place(cell);
            marker.visible = !Dungeon.visible[cell];
        }
    }

    private void updateHud() {
        String label = arena.phase() == GauntletArena.Phase.PREPARING
                ? (arena.stepsRemaining() == 1
                    ? Messages.get("gauntlet.wave_in_step", arena.completed() + 1)
                    : Messages.get("gauntlet.wave_in_steps", arena.completed() + 1, arena.stepsRemaining()))
                : Messages.get("gauntlet.wave", arena.wave(), GauntletArena.WAVES, arena.themeName());
        if (!label.equals(waveLabel.text())) waveLabel.text(label);
        waveLabel.setPos((uiCamera.width - waveLabel.width()) / 2f, 19);
    }

    private void frameArena() {

        setViewMode(2);
        Camera.main.target = null;
        float span = (GauntletArena.STANDS_RADIUS * 2 + 3) * 16f;
        float uiZoom = uiCamera.zoom;
        float availableWidth = Game.width - 38 * uiZoom;
        float availableHeight = Game.height - 54 * uiZoom;
        float target = Math.min(availableWidth, availableHeight) / span;
        target = Math.max(0.4f, target);
        Camera.main.zoom(target);
        float center = (GauntletArena.CENTER + 0.5f) * 16;
        Camera.main.scroll.set(center - Camera.main.width / 2f, center - Camera.main.height / 2f);
    }

    public int remainingSpectators() {
        int count = 0;
        for (Spectator rat : spectators) if (!rat.departed) count++;
        return count;
    }

    void drawSpectators(FirstPersonView view) {
        for (int i = 0; i < spectators.size(); i++) {
            Spectator rat = spectators.get(i);
            view.decorationSprite(rat, rat.cell, rat.height / 16.0);
        }
    }

    private static class Spectator extends MovieClip {
        private final int cell;
        private final int dx, dy;
        private boolean departed;
        Spectator(int cell) {
            super(Assets.RAT);
            this.cell = cell;
            int tileX = cell % Level.WIDTH, tileY = cell / Level.WIDTH;
            int offsetX = tileX - GauntletArena.CENTER, offsetY = tileY - GauntletArena.CENTER;

            dx = offsetX < 0 ? -1 : 1;
            dy = Math.abs(offsetX) >= Math.abs(offsetY) ? 0 : Integer.signum(offsetY);
            TextureFilm film = new TextureFilm(texture, 16, 15);
            play(new Animation(8 + cell % 5, true).frames(film, 6, 7, 8, 9, 10));
            origin.set(width / 2f, height / 2f);
            x = (tileX + 0.5f) * 16 - width / 2f;
            y = (tileY + 1) * 16 - height - 6;
            face(-dx);
        }

        private void face(int xDirection) {
            flipHorizontal = xDirection < 0;
        }
        void updateVisibility() {
            int tileX = (int)Math.floor((x + width / 2f) / 16f);
            int tileY = (int)Math.floor((y + height / 2f + 6) / 16f);
            visible = !departed && tileX >= 0 && tileX < Level.WIDTH && tileY >= 0 && tileY < Level.HEIGHT
                    && Dungeon.visible[tileX + tileY * Level.WIDTH];
        }
        void flee() {
            if (departed) return;
            face(dx);
            float distance = 160 * Game.elapsed / (dy == 0 ? 1f : (float)Math.sqrt(2));
            x += dx * distance;
            y += dy * distance;
            Camera view = Camera.main;
            if (x + width < view.scroll.x || x > view.scroll.x + view.width
                    || y + height < view.scroll.y || y > view.scroll.y + view.height) departed = true;
        }
    }
}
