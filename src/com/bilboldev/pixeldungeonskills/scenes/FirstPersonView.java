package com.bilboldev.pixeldungeonskills.scenes;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.effects.Ripple;
import com.bilboldev.pixeldungeonskills.effects.TenguWarnings;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Vertigo;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.RectF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;

import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;

final class FirstPersonView {

    private static final double PLANE = 1.15;
    private static final double EYE_HEIGHT = 0.5;

    private static final double CEILING_HEIGHT = 1.5;
    private static final double THIRD_DEFAULT_TILT = Math.toRadians(50);
    private static final double THIRD_RADIUS = 2.2 / Math.cos(THIRD_DEFAULT_TILT);
    private static final double THIRD_TARGET_HEIGHT = CEILING_HEIGHT * 2 - 2.2 * Math.tan(THIRD_DEFAULT_TILT);
    static final double NEAR = 0.12;
    private static final int MAX_POOLED_BILLBOARDS = 4096;
    private static final int MAX_CACHED_SOURCE_PIXELS = 1024 * 1024;
    private Level level;
    private final PerspectiveFog terrainFog;
    private final GameScene scene;
    private final FirstPersonEffects effects;
    private final FirstPersonCombat combat;
    private final FirstPersonKeys keys;
    private final FirstPersonWeapon heldWeapon;
    private final Camera ui;
    final Camera mapCamera;
    private final FrameTexture texture, indicatorTexture;
    private final Image image, indicatorImage;
    private final FirstPersonRadar radar;
    private final Image mapHeading, mapHeadingOutline;
    private final RenderedTextMultiline hint;
    private final boolean desktopControls;
    private boolean mouseCaptured, mouseLookAvailable = true;
    private boolean controllerControls, controllerLooking;
    private static final GameAction[] GUIDE_ACTIONS = {GameAction.MOVE_N, GameAction.MOVE_W, GameAction.MOVE_S, GameAction.MOVE_E};
    private static final String[] STICK_GUIDES = {"^", "<", "v", ">"};
    private final int[] guideCells = {-1, -1, -1, -1};
    private final BitmapText[] movementKeys;
    private final ColorBlock movementFill;
    private final ColorBlock[] aimOutline, reticle;
    private int aimedCell = -1;
    private final int width, height;

    private final int[] pixels, cells, indicatorPixels;
    private boolean indicatorsVisible;
    private final double[] depth, surfaceDepth, pickDepth;
    private final int[] floorTiles;
    private final int[] terrainSnapshot, backgroundPixels, backgroundCells;
    private final boolean[] visibleSnapshot;
    private boolean backgroundValid, backgroundThirdPerson, visibleWater, destroyed;
    private double backgroundX, backgroundY, backgroundAngle, backgroundTilt, lightFalloff;
    private int backgroundViewDistance, sourcePixelCount;
    private float backgroundWaterX, backgroundWaterY;
    private final Image[] terrainSprites = new Image[256];
    private final Image grassAlt;
    private Pixels tiles, water;
    private final IdentityHashMap<SmartTexture, Pixels> sources = new IdentityHashMap<>();
    private final IdentityHashMap<CharSprite, PickBody> pickBodies = new IdentityHashMap<>();
    private final ArrayList<Billboard> sprites = new ArrayList<>();
    private final ArrayList<Billboard> billboardPool = new ArrayList<>();
    private final ArrayList<Ripple> ripples = new ArrayList<>();
    private final ArrayList<RectF> rippleFrames = new ArrayList<>();
    private final ArrayList<Pixels> rippleSources = new ArrayList<>();
    private double angle, px, py, dx, dy, heroX, heroY;
    private double eyeHeight = EYE_HEIGHT, horizon, cameraBack, pitch, pitchSin, pitchCos = 1, focal;
    private double firstPersonTilt, thirdPersonTilt = THIRD_DEFAULT_TILT;
    private boolean thirdPerson;
    private Touch pointer;
    private double startAngle, startTilt;
    private boolean dragged;

    FirstPersonView(GameScene scene, Camera ui) {
        this.scene = scene;
        this.ui = ui;
        desktopControls = GameActions.enabled && ControlSettings.backend != null;
        keys = desktopControls ? new FirstPersonKeys(ui) : null;
        heldWeapon = new FirstPersonWeapon(ui);
        level = Dungeon.level;
        effects = new FirstPersonEffects(scene, this);
        int position = Dungeon.hero.pos;
        width = ui.width < ui.height ? 180 : 320;
        height = Math.max(100, Math.min(360, Math.round(width * (float)ui.height / ui.width)));
        combat = new FirstPersonCombat(scene, this, ui, width, height);
        pixels = new int[width * height];
        indicatorPixels = new int[pixels.length];
        cells = new int[pixels.length];
        depth = new double[width];
        surfaceDepth = new double[pixels.length];
        pickDepth = new double[pixels.length];
        floorTiles = new int[level.map.length];
        terrainSnapshot = new int[level.map.length];
        Arrays.fill(terrainSnapshot, -1);
        visibleSnapshot = new boolean[Dungeon.visible.length];
        terrainFog = new PerspectiveFog(level);
        backgroundPixels = new int[pixels.length];
        backgroundCells = new int[cells.length];

        SmartTexture atlas = TextureCache.get(raisedAtlas(level.tilesTex()));
        tiles = source(atlas);
        SmartTexture terrainAtlas = TextureCache.get(terrainAtlas(level.tilesTex()));
        int[] terrains = {Terrain.SIGN, Terrain.STATUE, Terrain.STATUE_SP, Terrain.HIGH_GRASS,
                Terrain.ENTRANCE, Terrain.STORAGE, Terrain.BARRICADE};
        for (int i = 0; i < terrains.length; i++) {
            Image art = terrainSprites[terrains[i]] = new Image(terrainAtlas);
            art.frame((terrains[i] == Terrain.BARRICADE ? 7 : i) * 16, 0, 16, 16);
        }
        grassAlt = new Image(terrainAtlas);
        grassAlt.frame(6 * 16, 0, 16, 16);
        water = source(TextureCache.get(level.waterTex()));
        float mapWidth = Math.min(104, ui.width * 0.32f);
        float mapHeight = Math.min(78, ui.height * 0.32f);
        float mapX = ui.width - mapWidth - 5, mapY = 34;
        float zoom = Math.max(0.25f, mapWidth * ui.zoom / (16 * 13));
        mapCamera = new Camera(ui.x + Math.round(mapX * ui.zoom), ui.y + Math.round(mapY * ui.zoom),
                Math.round(mapWidth * ui.zoom / zoom), Math.round(mapHeight * ui.zoom / zoom), zoom);
        mapHeadingOutline = new HeadingArrow(0xFF251E16, 4, 13, 4.5f);
        mapHeading = new HeadingArrow(0xFFFFD878, 5, 11, 3);
        mapHeading.camera = mapHeadingOutline.camera = ui;
        mapHeading.alpha(0.8f);
        mapHeadingOutline.alpha(0.8f);
        if (desktopControls) {

            mapHeading.scale.set(0.45f);
            mapHeadingOutline.scale.set(0.45f);
            movementFill = new ColorBlock(1, 1, 0xFF69BFD5);
            movementFill.alpha(0.28f);
            movementFill.camera = ui;
            movementKeys = new BitmapText[GUIDE_ACTIONS.length];
            for (int i = 0; i < movementKeys.length; i++) {
                movementKeys[i] = PixelScene.createText("", 5);
                movementKeys[i].camera = ui;
            }
            aimOutline = new ColorBlock[4];
            reticle = new ColorBlock[4];
            for (int i = 0; i < 4; i++) {
                aimOutline[i] = new ColorBlock(1, 1, 0xFFFFFFFF);
                aimOutline[i].camera = ui;
                aimOutline[i].alpha(0.8f);
                reticle[i] = new ColorBlock(1, 1, 0xFFFFFFFF);
                reticle[i].camera = ui;
                reticle[i].alpha(0.65f);
            }
        } else {
            movementFill = null;
            movementKeys = null;
            aimOutline = reticle = null;
        }
        hint = desktopControls ? PixelScene.renderMultiline("", 6) : null;
        if (hint != null) {
            hint.camera = ui;

            hint.maxWidth(Math.max(1, ui.width - 56));
            setHint(Messages.get(FirstPersonView.class, "pointer_free"));
            hint.hardlight(0xE4D5AC);
        }
        texture = new FrameTexture(width, height);
        image = new Image(texture);
        image.camera = ui;
        image.scale.set((float)ui.width / width, (float)ui.height / height);
        indicatorTexture = new FrameTexture(width, height);
        indicatorImage = new Image(indicatorTexture);
        indicatorImage.camera = ui;
        indicatorImage.scale.set(image.scale);
        TextureCache.add(indicatorTexture, indicatorTexture);

        double nearest = Double.MAX_VALUE;
        for (Mob mob : level.mobs) {
            if (!seen(mob.pos) || mob.sprite == null || !mob.sprite.visible) continue;
            double x = mob.pos % level.width() - position % level.width();
            double y = mob.pos / level.width() - position / level.width();
            if (x * x + y * y < nearest && x * x + y * y > 0) {
                nearest = x * x + y * y;
                angle = Math.atan2(y, x);
            }
        }
        TextureCache.add(texture, texture);
        radar = new FirstPersonRadar(ui, mapCamera);
    }

    void rebind(Level level) {
        this.level = level;
        terrainFog.rebind(level);
    }

    void draw() {
        if (destroyed || Dungeon.level != level || Dungeon.hero == null || !valid(Dungeon.hero.pos)) {
            throw new IllegalStateException("First-person view belongs to a different level");
        }
        int pos = Dungeon.hero.pos;

        heroX = px = pos % level.width() + 0.5 + Dungeon.hero.sprite.movementOffsetX();
        heroY = py = pos / level.width() + 0.5 + Dungeon.hero.sprite.movementOffsetY();
        dx = Math.cos(angle);
        dy = Math.sin(angle);
        positionCamera();
        lightFalloff = Math.max(0.065, 0.7 / Math.max(1, Dungeon.hero.viewDistance));
        for (Pixels source : sources.values()) source.used = false;
        tiles.used = water.used = true;
        com.bilboldev.pixeldungeonskills.actors.mobs.RebornTengu.warnings(level, tenguWarnings);
        if (!Arrays.equals(tenguWarnings, previousTenguWarnings)) {
            backgroundValid = false;
            System.arraycopy(tenguWarnings, 0, previousTenguWarnings, 0, tenguWarnings.length);
        }
        warningTexture = scene.tenguWarnings != null && scene.tenguWarnings.count() > 0
                ? source(scene.tenguWarnings.at(0).texture) : null;

        if (warningTexture != null) backgroundValid = false;
        int bossMarks = scene.bossWarnings == null ? 0 : scene.bossWarnings.count();
        if (bossMarks > 0 || previousBossMarks > 0) backgroundValid = false;
        previousBossMarks = bossMarks;
        for (int i = 0; i < bossMarks; i++) source(scene.bossWarnings.at(i).texture);
        drawSurfaces(prepareSurfaces());
        billboards();
        effects.drawImmersion();

        for (Iterator<Pixels> it = sources.values().iterator(); it.hasNext();) {
            Pixels source = it.next();
            if (!source.used) {
                sourcePixelCount -= source.cachedPixels();
                it.remove();
            }
        }
        texture.upload(pixels);
        image.draw();
        if (!thirdPerson) heldWeapon.draw();
        combat.draw();
        mapCamera.focusOn((float)heroX * 16, (float)heroY * 16);
        mapCamera.update();
    }

    void update() {
        if (!thirdPerson) heldWeapon.update();
    }

    void drawIndicators() {
        if (!indicatorsVisible) return;

        for (int i = 0; i < indicatorPixels.length; i++) {
            if (indicatorPixels[i] != 0) indicatorPixels[i] = pixels[i];
        }
        indicatorTexture.upload(indicatorPixels);
        indicatorImage.draw();
    }

    void refreshTerrain() {
        SmartTexture atlas = TextureCache.get(raisedAtlas(level.tilesTex()));
        tiles = source(atlas);
        water = source(TextureCache.get(level.waterTex()));
        SmartTexture terrainAtlas = TextureCache.get(terrainAtlas(level.tilesTex()));
        for (Image art : terrainSprites) if (art != null) {
            RectF frame = art.frame();
            art.texture(terrainAtlas);
            art.frame(frame);
        }
        RectF frame = grassAlt.frame();
        grassAlt.texture(terrainAtlas);
        grassAlt.frame(frame);
        backgroundValid = false;
    }

    void drawHint() {
        if (hint != null) hint.draw();
    }
    Camera beginRadar() {
        return radar.begin();
    }
    void endRadar() {
        radar.end();
    }
    void drawRadar() {
        radar.draw();
    }

    void drawMapHeading() {
        if (desktopControls) drawGuides();
        Point screen = mapCamera.cameraToScreen((float)heroX * 16, (float)heroY * 16);
        PointF point = ui.screenToCamera(screen.x, screen.y);
        mapHeading.x = mapHeadingOutline.x = point.x;
        mapHeading.y = mapHeadingOutline.y = point.y;
        mapHeading.angle = mapHeadingOutline.angle = (float)Math.toDegrees(angle);
        mapHeadingOutline.draw();
        mapHeading.draw();
    }

    boolean isThirdPerson() {
        return thirdPerson;
    }
    void thirdPerson() {
        thirdPerson = true;
        pointer = null;
        dragged = false;
    }

    private void setHint(String text) {
        if (hint == null || text.equals(hint.text())) return;
        hint.text(text, false);
        hint.setPos(Math.max(4, (ui.width - hint.width()) / 2), scene.toolbar.top() - 4 - hint.height());
    }

    void mouseState(boolean captured, boolean available) {
        mouseState(captured, available, false);
    }

    void mouseState(boolean captured, boolean available, boolean awaitingClick) {
        if (!desktopControls || controllerControls) return;
        mouseCaptured = captured;
        mouseLookAvailable = available;
        setHint(available ? Messages.get(FirstPersonView.class, awaitingClick ? "pointer_resume" : "pointer_free") : "");
    }

    void controllerState(boolean active, boolean looking) {
        if (!desktopControls || (controllerControls == active && controllerLooking == looking)) return;
        controllerControls = active;
        controllerLooking = looking;
        if (active) {
            mouseCaptured = looking;
            mouseLookAvailable = true;
            String binding = ControlSettings.backend.bindingName(GameAction.POINTER_SECONDARY, true);
            setHint(Messages.get(FirstPersonView.class, looking ? "controller_look" : "controller_cursor", binding));
        }
    }

    void lookBy(double radians) {
        lookBy(radians, 0);
    }

    double[] direction() {
        return new double[]{angle, firstPersonTilt, thirdPersonTilt};
    }

    void direction(double[] value) {
        angle = value[0];
        firstPersonTilt = value[1];
        thirdPersonTilt = value[2];
    }

    void lookBy(double radians, double tilt) {
        if (!desktopControls) return;
        if (!Double.isNaN(radians) && !Double.isInfinite(radians)) {
            angle = Math.atan2(Math.sin(angle + radians), Math.cos(angle + radians));
        }
        if (!Double.isNaN(tilt) && !Double.isInfinite(tilt)) setTilt((thirdPerson ? thirdPersonTilt : firstPersonTilt) + tilt);
    }

    private void setTilt(double tilt) {
        if (thirdPerson) thirdPersonTilt = Math.max(Math.toRadians(35), Math.min(Math.toRadians(70), tilt));
        else firstPersonTilt = Math.max(Math.toRadians(-20), Math.min(Math.toRadians(20), tilt));
    }

    int[] movementStep(int x, int y) {
        if (!desktopControls || (x == 0 && y == 0)) return new int[]{x, y};

        double snapped = Math.round((angle + Math.atan2(x, -y)) / (Math.PI / 4)) * (Math.PI / 4);
        return new int[]{(int)Math.round(Math.cos(snapped)), (int)Math.round(Math.sin(snapped))};
    }

    private int movementHintCell(GameAction action) {
        if (!GameActions.canMove() || Dungeon.hero.rooted || Dungeon.hero.buff(Vertigo.class) != null) return -1;
        int[] step = movementStep(action.dx, action.dy);
        int pos = Dungeon.hero.pos;
        int cell = cellAt(pos % level.width() + 0.5 + step[0], pos / level.width() + 0.5 + step[1]);
        if (!seen(cell) || !(Level.passable[cell] || Level.avoid[cell]) || (Level.pit[cell] && !Dungeon.hero.flying)) return -1;
        int terrain = level.map[cell];
        if (terrain == Terrain.DOOR || terrain == Terrain.LOCKED_DOOR || terrain == Terrain.LOCKED_EXIT
                || terrain == Terrain.ALCHEMY || cell == level.entrance || cell == level.exit || cell == level.storage) return -1;
        Char occupant = Actor.findChar(cell);

        if (occupant != null && occupant.invisible <= 0 && occupant.sprite != null && occupant.sprite.visible) return -1;
        Heap heap = level.heaps.get(cell);
        if (heap != null && heap.type != Heap.Type.HEAP && heap.type != Heap.Type.HIDDEN) return -1;
        return cell;
    }

    private void drawGuides() {
        Arrays.fill(guideCells, -1);
        if (ControlSettings.backend != null) for (int i = 0; i < GUIDE_ACTIONS.length; i++) {
            int cell = movementHintCell(GUIDE_ACTIONS[i]);
            if (cell < 0) continue;
            String name = controllerControls ? STICK_GUIDES[i] : ControlSettings.backend.keyboardKeyName(GUIDE_ACTIONS[i]);
            if (name.isEmpty()) continue;
            RectF tile = mapTile(cell);
            if (tile == null) continue;
            guideCells[i] = cell;
            float opacity = radarOpacity(cell);
            movementFill.alpha(0.28f * opacity);
            movementFill.x = tile.left;
            movementFill.y = tile.top;
            movementFill.size(tile.width(), tile.height());
            movementFill.draw();
            BitmapText label = movementKeys[i];
            label.alpha(opacity);
            if (!name.equals(label.text())) {
                label.text(name, false);
                label.measure();
            }
            label.scale.set(Math.min(1, (tile.width() - 1) / Math.max(1, Math.max(label.width, label.height))));
            label.x = tile.left + (tile.width() - label.width()) / 2;
            label.y = tile.top + (tile.height() - label.height()) / 2;
            label.draw();
        }
        aimedCell = -1;
        if (!mouseCaptured || !GameScene.canLookAround()) return;
        drawReticle(ui.width / 2f, ui.height / 2f);

        int cell = pickedCell(width / 2 + (height / 2) * width);
        if (!seen(cell) || cell == Dungeon.hero.pos) return;
        aimedCell = cell;
        RectF tile = mapTile(cell);
        if (tile == null) return;
        for (int i = 0; i < 4; i++) {
            aimOutline[i].alpha(0.8f * radarOpacity(cell));
            aimOutline[i].x = tile.left + (i == 1 ? tile.width() - 0.7f : 0);
            aimOutline[i].y = tile.top + (i == 3 ? tile.height() - 0.7f : 0);
            aimOutline[i].size(i < 2 ? 0.7f : tile.width(), i < 2 ? tile.height() : 0.7f);
            aimOutline[i].draw();
        }
    }

    void drawTargetCursor(int screenX, int screenY) {
        if (!desktopControls || mouseCaptured || !ui.hitTest(screenX, screenY)
                || mapCamera.hitTest(screenX, screenY)) return;
        PointF p = ui.screenToCamera(screenX, screenY);
        drawReticle(p.x, p.y);
    }

    private void drawReticle(float cx, float cy) {
        for (int i = 0; i < 4; i++) {
            reticle[i].x = cx + (i == 0 ? -4 : i == 1 ? 2 : 0);
            reticle[i].y = cy + (i == 2 ? -4 : i == 3 ? 2 : 0);
            reticle[i].size(i < 2 ? 2 : 0.7f, i < 2 ? 0.7f : 2);
            reticle[i].draw();
        }
    }

    private RectF mapTile(int cell) {
        Point screen = mapCamera.cameraToScreen((cell % level.width()) * 16, (cell / level.width()) * 16);
        PointF corner = ui.screenToCamera(screen.x, screen.y);
        float size = 16 * mapCamera.zoom / ui.zoom;
        if (corner.x < radar.x || corner.y < radar.y
                || corner.x + size > radar.x + radar.width()
                || corner.y + size > radar.y + radar.height()) return null;
        return new RectF(corner.x, corner.y, corner.x + size, corner.y + size);
    }

    private float radarOpacity(int cell) {
        double radius = Math.min(mapCamera.width, mapCamera.height) / 32.0;
        double distance = Math.hypot(cell % level.width() + 0.5 - heroX, cell / level.width() + 0.5 - heroY);
        double t = Math.max(0, Math.min(1, (distance / radius - 0.12) / 0.88));
        return (float)(0.8 - 0.6 * t * t * (3 - 2 * t));
    }

    private void positionCamera() {
        pitch = thirdPerson ? thirdPersonTilt : 0;
        cameraBack = thirdPerson ? THIRD_RADIUS * Math.cos(pitch) : 0;
        if (thirdPerson) {

            px = heroX - dx * cameraBack;
            py = heroY - dy * cameraBack;
        }

        eyeHeight = thirdPerson ? THIRD_TARGET_HEIGHT + THIRD_RADIUS * Math.sin(pitch) : EYE_HEIGHT;
        pitchSin = Math.sin(pitch);
        pitchCos = Math.cos(pitch);

        focal = thirdPerson ? Math.max(width / 2.20, height / 1.30) : Math.min(width / (2 * PLANE), height * 0.8);
        horizon = height * (thirdPerson ? 0.62 : 0.46);

        if (!thirdPerson) horizon -= focal * Math.tan(firstPersonTilt);
    }

    private void drawSurfaces(boolean terrainChanged) {
        double tilt = thirdPerson ? thirdPersonTilt : firstPersonTilt;
        float waterX = scene.water.offsetX(), waterY = scene.water.offsetY();
        boolean reuse = backgroundValid && !terrainChanged && ripples.isEmpty()
                && backgroundX == heroX && backgroundY == heroY && backgroundAngle == angle
                && backgroundThirdPerson == thirdPerson && backgroundTilt == tilt
                && backgroundViewDistance == Dungeon.hero.viewDistance
                && Arrays.equals(visibleSnapshot, Dungeon.visible)
                && (!visibleWater || (backgroundWaterX == waterX && backgroundWaterY == waterY));
        if (reuse) {
            System.arraycopy(backgroundPixels, 0, pixels, 0, pixels.length);
            System.arraycopy(backgroundCells, 0, cells, 0, cells.length);
            return;
        }
        Arrays.fill(cells, -1);
        if (thirdPerson) angledSurfaces();
        else {
            floors();
            walls();
        }
        System.arraycopy(pixels, 0, backgroundPixels, 0, pixels.length);
        System.arraycopy(cells, 0, backgroundCells, 0, cells.length);
        System.arraycopy(Dungeon.visible, 0, visibleSnapshot, 0, visibleSnapshot.length);
        backgroundX = heroX;
        backgroundY = heroY;
        backgroundAngle = angle;
        backgroundThirdPerson = thirdPerson;
        backgroundTilt = tilt;
        backgroundViewDistance = Dungeon.hero.viewDistance;
        backgroundWaterX = waterX;
        backgroundWaterY = waterY;

        backgroundValid = ripples.isEmpty();
    }

    private boolean prepareSurfaces() {
        if (terrainFog.update(Dungeon.visible, PixelDungeon.brightnessNew())) backgroundValid = false;
        boolean changed = !Arrays.equals(terrainSnapshot, level.map);
        if (changed) for (int cell = 0; cell < floorTiles.length; cell++) {
            int t = level.map[cell];
            if (isWater(t)) {
                int x = cell % level.width(), y = cell / level.width();
                floorTiles[cell] = DungeonTileSheet.stitchWaterTile(
                        y > 0 ? level.map[cell - level.width()] : Terrain.WALL,
                        x + 1 < level.width() ? level.map[cell + 1] : Terrain.WALL,
                        y + 1 < level.height() ? level.map[cell + level.width()] : Terrain.WALL,
                        x > 0 ? level.map[cell - 1] : Terrain.WALL);
            } else {
                int tile = DungeonTileSheet.directVisuals.get(t, DungeonTileSheet.FLOOR);
                if (t == Terrain.HIGH_GRASS) tile = DungeonTileSheet.GRASS;
                if (t == Terrain.STATUE_SP) tile = DungeonTileSheet.FLOOR_SP;
                if (t == Terrain.INACTIVE_TRAP || isTrap(t)) tile = t + 256;

                if (t == Terrain.ENTRANCE || t == Terrain.STORAGE) tile = DungeonTileSheet.FLOOR;
                if ((Terrain.flags[t] & Terrain.SECRET) != 0) tile = DungeonTileSheet.FLOOR;
                floorTiles[cell] = tile;
            }
        }
        if (changed) System.arraycopy(level.map, 0, terrainSnapshot, 0, terrainSnapshot.length);
        visibleWater = false;
        for (int cell = 0; cell < level.map.length; cell++) {
            if (terrainFog.hasTerrain(cell) && isWater(level.map[cell])) {
                visibleWater = true;
                break;
            }
        }
        ripples.clear();
        rippleFrames.clear();
        rippleSources.clear();
        for (int i = 0; i < scene.ripples.length; i++) {
            Gizmo g = scene.ripples.memberAt(i);
            if (g instanceof Ripple && g.exists && g.visible) {
                Ripple ripple = (Ripple)g;
                ripples.add(ripple);

                rippleFrames.add(ripple.frame());
                rippleSources.add(source(ripple.texture));
            }
        }
        return changed;
    }

    private void floors() {
        for (int y = 0; y < height; y++) {
            boolean ceiling = y < horizon;
            double clearance = ceiling ? CEILING_HEIGHT - eyeHeight : eyeHeight;
            double distance = focal * clearance / Math.max(0.01, Math.abs(y - horizon));
            for (int x = 0; x < width; x++) {
                double side = (x - width * 0.5) / focal;
                double wx = px + distance * (dx - dy * side);
                double wy = py + distance * (dy + dx * side);
                int cell = cellAt(wx, wy), index = x + y * width;
                if (!terrainFog.hasTerrain(cell)) {
                    pixels[index] = 0xFF000000;
                    continue;
                }
                pixels[index] = terrainFog.apply(shade(surfacePixel(cell, wx, wy, ceiling),
                        terrainLight(cell, wx, wy) * (ceiling ? 0.53 : 0.82)), wx, wy);
                if (!ceiling && seen(cell)) cells[index] = cell;
            }
        }
    }

    private void angledSurfaces() {
        for (int y = 0; y < height; y++) {
            double up = (horizon - y) / focal;
            double along = pitchCos + up * pitchSin, rz = -pitchSin + up * pitchCos;

            double planeDistance = rz < -0.00001 ? -eyeHeight / rz : 20;
            double roofDistance = rz < -0.00001 ? (CEILING_HEIGHT - eyeHeight) / rz : Double.POSITIVE_INFINITY;
            for (int x = 0; x < width; x++) {
                double lateral = (x - width * 0.5) / focal;
                double rx = dx * along - dy * lateral, ry = dy * along + dx * lateral;
                int mx = (int)Math.floor(px), my = (int)Math.floor(py), cell = -1;
                int sx = rx < 0 ? -1 : 1, sy = ry < 0 ? -1 : 1;
                double deltaX = Math.abs(1 / rx), deltaY = Math.abs(1 / ry);
                double nextX = (rx < 0 ? px - mx : mx + 1 - px) * deltaX;
                double nextY = (ry < 0 ? py - my : my + 1 - py) * deltaY;
                double distance = Math.min(20, planeDistance);
                boolean hit = false, vertical = false, roof = false;
                double entry = 0;
                for (int step = 0; step < 96; step++) {
                    cell = cellAt(mx, my);
                    double next = Math.min(nextX, nextY);

                    if (terrainFog.hasTerrain(cell) && solidWall(level.map[cell])) {
                        if (entry >= roofDistance) {
                            distance = entry;
                            hit = true;
                            break;
                        }
                        if (roofDistance < next && roofDistance < distance) {
                            distance = roofDistance;
                            hit = roof = true;
                            break;
                        }
                    }
                    if (next >= distance) break;
                    vertical = nextX < nextY;
                    if (vertical) {
                        mx += sx;
                        nextX += deltaX;
                    }
                    else {
                        my += sy;
                        nextY += deltaY;
                    }
                    entry = next;
                }
                int index = x + y * width;
                surfaceDepth[index] = Math.max(0.001, distance);
                if (y == height / 2) depth[x] = surfaceDepth[index];
                double wx = px + rx * distance, wy = py + ry * distance;
                if (hit) {
                    double u = vertical ? wy : wx;
                    int tx = (int)((u - Math.floor(u)) * 16);
                    int terrain = valid(cell) ? level.map[cell] : Terrain.WALL;
                    int color = roof ? surfacePixel(cell, wx, wy, true)
                            : wallPixel(terrain, tx, eyeHeight + rz * distance);
                    pixels[index] = terrainFog.apply(shade(color,
                            terrainLight(cell, wx, wy) * (roof ? 0.65 : vertical ? 0.8 : 1)), wx, wy);
                    cells[index] = !seen(cell) ? -1 : !roof && door(terrain) ? cell : -2 - cell;
                } else {
                    cell = cellAt(wx, wy);
                    if (!terrainFog.hasTerrain(cell) || distance >= 20) pixels[index] = 0xFF000000;
                    else {
                        pixels[index] = terrainFog.apply(shade(surfacePixel(cell, wx, wy, false),
                                terrainLight(cell, wx, wy) * 0.82), wx, wy);
                        if (seen(cell)) cells[index] = cell;
                    }
                }
            }
        }
    }

    private final boolean[] tenguWarnings = new boolean[Level.LENGTH];
    private final boolean[] previousTenguWarnings = new boolean[Level.LENGTH];
    private Pixels warningTexture;
    private int previousBossMarks;

    private int surfacePixel(int cell, double wx, double wy, boolean ceiling) {
        int color = terrainPixel(cell, wx, wy, ceiling);
        if (!ceiling && Dungeon.visible[cell] && scene.bossWarnings != null) {
            for (com.bilboldev.pixeldungeonskills.effects.BossWarnings.Mark mark = scene.bossWarnings.atCell(cell);
                 mark != null;
                 mark = mark.next) {
                double x = wx * 16 - mark.centerX, y = wy * 16 - mark.centerY;
                double u = (x * mark.cosine + y * mark.sine) / mark.sizeX + 0.5;
                double v = (-x * mark.sine + y * mark.cosine) / mark.sizeY + 0.5;
                if (u < 0 || u >= 1 || v < 0 || v >= 1) continue;
                Pixels texture = sources.get(mark.texture);
                RectF frame = mark.frame();
                int ink = texture.at((int)((frame.left + u * frame.width()) * texture.width),
                        (int)((frame.top + v * frame.height()) * texture.height));
                color = blend(color, litColor(ink, mark, -1), (ink >>> 24) / 255.0 * mark.am);
            }
        }
        if (ceiling || warningTexture == null || !Dungeon.visible[cell]) return color;
        TenguWarnings.WarningShuriken shuriken = scene.tenguWarnings.atCell(cell);
        if (shuriken == null) return color;
        double x = wx * 16 - shuriken.centerX, y = wy * 16 - shuriken.centerY;
        double u = (x * shuriken.cosine + y * shuriken.sine) / shuriken.size + 0.5;
        double v = (-x * shuriken.sine + y * shuriken.cosine) / shuriken.size + 0.5;
        if (u < 0 || u >= 1 || v < 0 || v >= 1) return color;
        RectF frame = shuriken.frame();
        int ink = warningTexture.at((int)((frame.left + u * frame.width()) * warningTexture.width),
                (int)((frame.top + v * frame.height()) * warningTexture.height));
        return blend(color, ink, (ink >>> 24) / 255.0 * shuriken.am);
    }

    private int terrainPixel(int cell, double wx, double wy, boolean ceiling) {
        int tx = (int)((wx - Math.floor(wx)) * 16), ty = (int)((wy - Math.floor(wy)) * 16);
        if (ceiling) return tilePixel(DungeonTileSheet.FLOOR, tx, ty);
        int terrain = level.map[cell];
        if (isWater(terrain)) {
            int color = water.at((int)Math.floor(wx * 16 + scene.water.offsetX()), (int)Math.floor(wy * 16 + scene.water.offsetY()));
            for (int i = 0; i < ripples.size(); i++) {
                color = ripplePixel(ripples.get(i), rippleSources.get(i), rippleFrames.get(i), wx * 16, wy * 16, color);
            }
            int shore = tilePixel(floorTiles[cell], tx, ty);
            return blend(color, shore, (shore >>> 24) / 255.0);
        }
        if (terrain == Terrain.CHASM || terrain >= Terrain.CHASM_FLOOR && terrain <= Terrain.CHASM_WATER) {
            int above = cell >= level.width() ? level.map[cell - level.width()] : Terrain.CHASM;
            int edge = tilePixel(DungeonTileSheet.stitchChasmTile(above), tx, ty);
            return blend(0xFF050609, edge, (edge >>> 24) / 255.0);
        }
        return tilePixel(floorTiles[cell], tx, ty);
    }

    private int wallPixel(int terrain, int tx, double z) {
        int tile = z >= 1 ? DungeonTileSheet.RAISED_WALL
                : terrain == Terrain.DOOR ? DungeonTileSheet.FLAT_DOOR
                : terrain == Terrain.LOCKED_DOOR || terrain == Terrain.LOCKED_EXIT ? DungeonTileSheet.FLAT_DOOR_LOCKED
                : terrain == Terrain.BOOKSHELF ? DungeonTileSheet.FLAT_BOOKSHELF
                : terrain == Terrain.BARRICADE ? DungeonTileSheet.FLAT_BARRICADE
                : terrain == Terrain.WALL_DECO ? DungeonTileSheet.RAISED_WALL_DECO : DungeonTileSheet.RAISED_WALL;
        int ty = (int)Math.floor((1 - z) * 16) & 15;
        boolean raised = tile == DungeonTileSheet.RAISED_WALL || tile == DungeonTileSheet.RAISED_WALL_DECO;
        return tilePixel(tile, tx, raised ? 4 + ty * 12 / 16 : ty);
    }

    private static boolean door(int terrain) {
        return terrain == Terrain.DOOR || terrain == Terrain.LOCKED_DOOR || terrain == Terrain.LOCKED_EXIT;
    }

    private int ripplePixel(Ripple ripple, Pixels p, RectF f, double wx, double wy, int below) {
        if (ripple.scale.x <= 0 || ripple.scale.y <= 0) return below;
        double u = ((wx - ripple.x - ripple.origin.x) / ripple.scale.x + ripple.origin.x) / ripple.width;
        double v = ((wy - ripple.y - ripple.origin.y) / ripple.scale.y + ripple.origin.y) / ripple.height;
        if (u < 0 || v < 0 || u >= 1 || v >= 1) return below;
        int color = p.at((int)((f.left + u * f.width()) * p.width), (int)((f.top + v * f.height()) * p.height));
        return blend(below, color, (color >>> 24) / 255.0 * ripple.am);
    }

    private void walls() {
        for (int x = 0; x < width; x++) {
            double side = (x - width * 0.5) / focal;
            double rx = dx - dy * side, ry = dy + dx * side;
            int mx = (int)px, my = (int)py;
            int sx = rx < 0 ? -1 : 1, sy = ry < 0 ? -1 : 1;
            double deltaX = Math.abs(1 / rx), deltaY = Math.abs(1 / ry);
            double nextX = (rx < 0 ? px - mx : mx + 1 - px) * deltaX;
            double nextY = (ry < 0 ? py - my : my + 1 - py) * deltaY;
            double distance = 16;
            int cell = -1;
            boolean vertical = false;
            for (int step = 0; step < 96; step++) {
                vertical = nextX < nextY;
                if (vertical) {
                    mx += sx;
                    distance = nextX;
                    nextX += deltaX;
                }
                else {
                    my += sy;
                    distance = nextY;
                    nextY += deltaY;
                }
                cell = cellAt(mx, my);
                if (!terrainFog.hasTerrain(cell) || solidWall(level.map[cell]) || distance > 16) break;
            }
            distance = Math.max(0.05, distance);
            depth[x] = distance;
            double unitHeight = focal / distance;
            double top = horizon - unitHeight * (CEILING_HEIGHT - eyeHeight);
            double bottom = horizon + unitHeight * eyeHeight;
            double u = vertical ? py + distance * ry : px + distance * rx;
            int tx = (int)((u - Math.floor(u)) * 16);
            int terrain = valid(cell) ? level.map[cell] : Terrain.WALL;
            int tile = terrain == Terrain.DOOR ? DungeonTileSheet.FLAT_DOOR
                    : terrain == Terrain.LOCKED_DOOR || terrain == Terrain.LOCKED_EXIT ? DungeonTileSheet.FLAT_DOOR_LOCKED
                    : terrain == Terrain.BOOKSHELF ? DungeonTileSheet.FLAT_BOOKSHELF
                    : terrain == Terrain.BARRICADE ? DungeonTileSheet.FLAT_BARRICADE
                    : terrain == Terrain.WALL_DECO ? DungeonTileSheet.RAISED_WALL_DECO
                    : DungeonTileSheet.RAISED_WALL;
            boolean known = terrainFog.hasTerrain(cell);
            double wx = px + distance * rx, wy = py + distance * ry;
            double light = terrainLight(cell, wx, wy) * (vertical ? 0.80 : 1.0);
            for (int y = Math.max(0, (int)top); y < Math.min(height, bottom); y++) {
                double z = eyeHeight + (horizon - y) / unitHeight;

                int face = z >= 1 ? DungeonTileSheet.RAISED_WALL : tile;
                int ty = (int)Math.floor((1 - z) * 16) & 15;

                boolean raised = face == DungeonTileSheet.RAISED_WALL || face == DungeonTileSheet.RAISED_WALL_DECO;
                int color = known ? tilePixel(face, tx, raised ? 4 + ty * 12 / 16 : ty) : 0xFF000000;
                int index = x + y * width;
                pixels[index] = known ? terrainFog.apply(shade(color, light), wx, wy) : color;
                cells[index] = !seen(cell) ? -1 : door(terrain) ? cell : -2 - cell;
            }
        }
    }

    private void billboards() {
        sprites.clear();
        Arrays.fill(indicatorPixels, 0);
        indicatorsVisible = false;
        Arrays.fill(pickDepth, Double.POSITIVE_INFINITY);

        for (int i = 0; i < scene.mobs.length; i++) {
            Gizmo g = scene.mobs.memberAt(i);
            if (g instanceof PartyPresentation.Death) {
                PartyPresentation.Death death = (PartyPresentation.Death)g;
                addSprite(death, death.cell, Math.max(0.5, death.height / 16.0) * death.scale.y, false);
                continue;
            }
            if (!(g instanceof CharSprite)) continue;
            CharSprite sprite = (CharSprite)g;
            if (sprite.ch instanceof Mob || LocalCoop.contains(sprite.ch) && sprite.ch != Dungeon.hero)
                addSprite(sprite, sprite.ch.pos, 1.0);
        }
        if (scene instanceof GauntletScene) ((GauntletScene)scene).drawSpectators(this);
        for (Heap heap : level.heaps.values()) if (heap.type != Heap.Type.HIDDEN) addSprite(heap.sprite, heap.pos, 0.45);
        for (Plant plant : level.plants.values()) addSprite(plant.sprite, plant.pos, 0.55);
        for (int cell = 0; cell < level.map.length; cell++) {
            int terrain = level.map[cell];
            Image art = terrainSprites[terrain];
            if (terrain == Terrain.HIGH_GRASS && DungeonTileSheet.getVisualWithAlts(
                    DungeonTileSheet.FLAT_HIGH_GRASS, cell) == DungeonTileSheet.FLAT_HIGH_GRASS_ALT) {
                art = grassAlt;
            }
            if (art != null) addSprite(art, cell, terrain == Terrain.HIGH_GRASS ? 0.7
                    : terrain == Terrain.BARRICADE ? 1.0 : 0.85, true, true);
        }
        if (thirdPerson) addSprite(Dungeon.hero.sprite, Dungeon.hero.pos, 1.0);
        effects.collect();

        for (int i = sprites.size(); i < billboardPool.size(); i++) billboardPool.get(i).sprite = null;
        for (Billboard b : sprites) b.projectedDepth = cameraDepth(b.distance, b.z);
        Collections.sort(sprites, FAR_FIRST);
        for (Billboard b : sprites) {
            if (b.projectedDepth < NEAR) continue;
            Image s = b.sprite;
            boolean indicator = s instanceof EmoIcon;
            boolean creature = s instanceof CharSprite;
            Pixels source = source(s.texture);
            RectF frame = s.frame();
            double size = focal * b.size / b.projectedDepth;
            double spriteWidth = size * b.aspect;
            double cx = width * 0.5 + b.side * focal / b.projectedDepth;
            double cy = screenY(b.distance, b.z);
            double light = b.emissive ? 1 : illumination(
                    px + b.distance * dx - b.side * dy, py + b.distance * dy + b.side * dx);
            if (b.terrainCell >= 0 && !seen(b.terrainCell)) light = 1;
            double cosine = Math.cos(b.rotation), sine = Math.sin(b.rotation);
            double halfWidth = (Math.abs(cosine) * spriteWidth + Math.abs(sine) * size) * 0.5;
            double halfHeight = (Math.abs(sine) * spriteWidth + Math.abs(cosine) * size) * 0.5;
            double left = cx - halfWidth, right = cx + halfWidth, top = cy - halfHeight, bottom = cy + halfHeight;
            boolean upright = thirdPerson && b.upright;
            if (upright) {
                double zTop = Math.min(b.z + b.size * 0.5, eyeHeight + (pitchCos * b.distance - NEAR) / pitchSin);
                double zBottom = b.z - b.size * 0.5;
                double near = cameraDepth(b.distance, zTop), far = cameraDepth(b.distance, zBottom);
                double a = b.side - b.size * b.aspect * 0.5, c = b.side + b.size * b.aspect * 0.5;
                left = width * 0.5 + focal * Math.min(a / near, a / far);
                right = width * 0.5 + focal * Math.max(c / near, c / far);
                top = Math.min(screenY(b.distance, zTop), screenY(b.distance, zBottom));
                bottom = Math.max(screenY(b.distance, zTop), screenY(b.distance, zBottom));
            }
            int fx = Math.round(frame.left * source.width), fy = Math.round(frame.top * source.height);
            int fw = Math.max(1, Math.round(frame.width() * source.width));
            int fh = Math.max(1, Math.round(frame.height() * source.height));
            for (int x = Math.max(0, (int)left); x < Math.min(width, right); x++) {
                if (!thirdPerson && b.distance >= depth[x]) continue;
                for (int y = Math.max(0, (int)top); y < Math.min(height, bottom); y++) {
                    int index = x + y * width;
                    double pixelDepth = b.projectedDepth;
                    double u = ((x + 0.5 - cx) * cosine + (y + 0.5 - cy) * sine) / spriteWidth + 0.5;
                    double v = (-(x + 0.5 - cx) * sine + (y + 0.5 - cy) * cosine) / size + 0.5;
                    if (upright) {
                        double up = (horizon - y) / focal;
                        pixelDepth = b.distance / (pitchCos + up * pitchSin);
                        double z = eyeHeight + pixelDepth * (-pitchSin + up * pitchCos);
                        u = (pixelDepth * (x - width * 0.5) / focal - b.side) / (b.size * b.aspect) + 0.5;
                        v = 0.5 - (z - b.z) / b.size;
                    }
                    if (thirdPerson && (pixelDepth < NEAR || pixelDepth >= surfaceDepth[index])) continue;
                    if (u < 0 || v < 0 || u >= 1 || v >= 1) continue;
                    int tx = Math.min(fw - 1, (int)((s.flipHorizontal ? 1 - u : u) * fw));
                    int ty = Math.min(fh - 1, (int)((s.flipVertical ? 1 - v : v) * fh));
                    int color = source.at(fx + tx, fy + ty);
                    double alpha = Math.min(1, (color >>> 24) / 255.0 * s.am + s.aa) * b.opacity;
                    if (alpha <= 0.01) continue;
                    b.visiblePixels = true;
                    color = litColor(color, s, b.tint);
                    color = shade(color, light);
                    if (b.terrainCell >= 0) {
                        double lateral = b.side + (u - 0.5) * b.size * b.aspect;
                        color = terrainFog.apply(color, px + b.distance * dx - lateral * dy,
                                py + b.distance * dy + lateral * dx);
                    }
                    pixels[index] = b.additive ? addLight(pixels[index], color, alpha) : blend(pixels[index], color, alpha);
                    if (indicator) {
                        indicatorPixels[index] = 1;
                        indicatorsVisible = true;
                    } else if (alpha >= 1 && !b.additive) {
                        indicatorPixels[index] = 0;
                    }

                    if (b.cell >= 0 && (creature || alpha > 0.3) && pixelDepth < pickDepth[index]) {
                        cells[index] = s == terrainSprites[Terrain.BARRICADE] ? -2 - b.cell : b.cell;
                        pickDepth[index] = pixelDepth;
                    }
                }
            }
        }

        for (Billboard b : sprites) if (b.cell >= 0 && b.visiblePixels && b.sprite instanceof CharSprite) pickBody(b);
        for (Iterator<CharSprite> it = pickBodies.keySet().iterator(); it.hasNext();) {
            CharSprite s = it.next();
            if (!s.exists || s.ch == null || !s.ch.isAlive()) it.remove();
        }
    }

    private void pickBody(Billboard b) {
        CharSprite sprite = (CharSprite)b.sprite;
        PickBody body = pickBodies.get(sprite);
        RectF[] idle = sprite.idleFrames();
        if (body == null || body.texture != sprite.texture || body.frames != idle) {
            body = new PickBody(sprite, source(sprite.texture));
            pickBodies.put(sprite, body);
        }
        double zTop = body.top;
        if (thirdPerson) zTop = Math.min(zTop, eyeHeight + (pitchCos * b.distance - NEAR) / pitchSin);
        if (zTop <= body.bottom) return;
        double near = cameraDepth(b.distance, zTop), far = cameraDepth(b.distance, body.bottom);
        if (near < NEAR || far < NEAR) return;
        double a = b.side - body.halfWidth, c = b.side + body.halfWidth;
        double left = width * 0.5 + focal * Math.min(a / near, a / far);
        double right = width * 0.5 + focal * Math.max(c / near, c / far);
        double top = screenY(b.distance, zTop), bottom = screenY(b.distance, body.bottom);
        for (int x = Math.max(0, (int)left); x < Math.min(width, right); x++) {
            if (!thirdPerson && b.distance >= depth[x]) continue;
            for (int y = Math.max(0, (int)Math.min(top, bottom)); y < Math.min(height, Math.max(top, bottom)); y++) {
                double up = (horizon - (thirdPerson ? y : y + 0.5)) / focal;
                double distance = thirdPerson ? b.distance / (pitchCos + up * pitchSin) : b.distance;
                int index = x + y * width;
                if (distance < NEAR || distance >= pickDepth[index]
                        || (thirdPerson && distance >= surfaceDepth[index])) continue;
                double side = distance * ((thirdPerson ? x : x + 0.5) - width * 0.5) / focal;
                double z = eyeHeight + distance * (-pitchSin + up * pitchCos);
                if (Math.abs(side - b.side) > body.halfWidth || z < body.bottom || z > zTop) continue;
                cells[index] = b.cell;
                pickDepth[index] = distance;
            }
        }
    }

    void decorationSprite(Image sprite, int cell, double size) {
        addSprite(sprite, cell, size, false);
    }

    private void addSprite(Image sprite, int cell, double size) {
        addSprite(sprite, cell, size, true);
    }

    private void addSprite(Image sprite, int cell, double size, boolean targetable) {
        addSprite(sprite, cell, size, targetable, false);
    }

    private void addSprite(Image sprite, int cell, double size, boolean targetable, boolean terrain) {
        if (!(terrain ? terrainFog.hasTerrain(cell) : seen(cell))
                || sprite == null || !sprite.exists || !sprite.visible || sprite.texture == null) return;
        double x = cell % level.width() + 0.5 - px, y = cell / level.width() + 0.5 - py;
        if (sprite instanceof CharSprite) {
            x += ((CharSprite)sprite).movementOffsetX();
            y += ((CharSprite)sprite).movementOffsetY();
        }
        double distance = x * dx + y * dy;
        if (sprite instanceof CharSprite) size *= Math.max(0.5, sprite.height / 16.0);
        if (thirdPerson ? cameraDepth(distance, size * 0.5) <= NEAR : distance <= 0.15) return;
        boolean hero = sprite == Dungeon.hero.sprite;
        int target = !targetable || !seen(cell) || hero
                || sprite instanceof CharSprite && !((CharSprite)sprite).ch.isAlive() ? -1 : cell;
        double opacity = 1;
        if (hero) {

            double heroSide = (-x * dy + y * dx) / cameraDepth(distance, size * 0.5);
            double heroHalf = size * sprite.width / Math.max(1, sprite.height) / cameraDepth(distance, size) * 0.5;
            double heroTop = screenY(distance, size), heroBottom = screenY(distance, 0);
            for (Billboard b : sprites) {
                if (!(b.sprite instanceof CharSprite) || b.cell < 0 || b.distance <= distance) continue;
                double half = b.size * b.aspect / cameraDepth(b.distance, b.z + b.size * 0.5) * 0.5;
                if (Math.abs(heroSide - b.side / cameraDepth(b.distance, b.z)) < heroHalf + half
                        && screenY(b.distance, b.z + b.size * 0.5) < heroBottom
                        && screenY(b.distance, b.z - b.size * 0.5) > heroTop) {
                    opacity *= 0.3;
                    break;
                }
            }
        }
        double jump = sprite instanceof CharSprite ? ((CharSprite)sprite).jumpHeight() : 0;
        double rotation = sprite instanceof PartyPresentation.Death ? Math.toRadians(sprite.angle) : 0;
        Billboard b = billboard().set(sprite, target, distance, -x * dy + y * dx, size, size * 0.5 + jump,
                sprite.width / Math.max(1, sprite.height), rotation, opacity, -1, false, true);
        if (terrain) b.terrainCell = cell;
        sprites.add(b);
    }

    void attachedParticle(Image particle, CharSprite owner) {
        attachedParticle(particle, owner, false, false);
    }

    void attachedParticle(Image particle, CharSprite owner, boolean emissive, boolean additive) {
        if (particle == null || !particle.exists || !particle.visible || owner.ch == null
                || !owner.exists || !owner.visible || !seen(owner.ch.pos)) return;
        double offset = (particle.x + particle.origin.x - owner.x - owner.width * 0.5) / 16;
        double z = (owner.y + owner.height - particle.y - particle.origin.y) / 16;
        if (!thirdPerson && owner == Dungeon.hero.sprite) {
            selfParticle(particle, offset, z, additive);
            return;
        }
        int cell = owner.ch.pos;

        effectSprite(particle, cell % level.width() + 0.5 + owner.movementOffsetX() - dy * offset,
                cell / level.width() + 0.5 + owner.movementOffsetY() + dx * offset,
                z, Math.abs(particle.height()) / 16, Math.toRadians(particle.angle), 1, -1, emissive, additive);
    }

    void selfEffect(Image sprite) {
        if (sprites.size() >= 4096 || sprite.texture == null || sprite.am <= 0 || sprite.height() == 0) return;
        sprites.add(billboard().set(sprite, -1, 0.3, 0, Math.abs(sprite.height()) / 64,
                eyeHeight - 0.12, effectAspect(sprite), Math.toRadians(sprite.angle), 1, -1, true, false));
    }

    void selfParticle(Image particle, double offset, double z, boolean additive) {
        if (sprites.size() >= MAX_POOLED_BILLBOARDS || particle.texture == null
                || particle.am <= 0 || particle.height() == 0) return;
        Billboard b = billboard().set(particle, -1, 0.4, offset * 0.2,
                Math.abs(particle.height()) / 80, eyeHeight - 0.24 + z * 0.18,
                effectAspect(particle), Math.toRadians(particle.angle), 1, -1, true, false);
        b.additive = additive;
        sprites.add(b);
    }

    private double effectAspect(Image sprite) {
        return Math.abs(sprite.width()) / Math.max(0.0001, Math.abs(sprite.height()));
    }

    void planeParticle(Image particle, double x, double y, double offset, double z, boolean additive) {
        effectSprite(particle, x - dy * offset, y + dx * offset, z,
                Math.abs(particle.height()) / 16, Math.toRadians(particle.angle), 1, -1, true, additive);
    }

    void effectSprite(Image sprite, double x, double y, double z, double size, double rotation,
                      double opacity, int tint, boolean emissive) {
        effectSprite(sprite, x, y, z, size, rotation, opacity, tint, emissive, false);
    }

    void effectSprite(Image sprite, double x, double y, double z, double size, double rotation,
                      double opacity, int tint, boolean emissive, boolean additive) {
        if (sprites.size() >= 4096 || sprite == null || sprite.texture == null || size <= 0 || opacity <= 0) return;
        if (!seen(cellAt(x, y))) return;
        double distance = forward(x, y);
        if ((thirdPerson ? cameraDepth(distance, z) : distance) < NEAR || Math.abs(distance) > 20) return;
        Billboard effect = billboard().set(sprite, -1, distance, side(x, y), size, z,
                effectAspect(sprite), rotation, opacity, tint, emissive, false);
        effect.additive = additive;
        sprites.add(effect);
    }

    double forward(double x, double y) {
        return (x - px) * dx + (y - py) * dy;
    }
    double side(double x, double y) {
        return -(x - px) * dy + (y - py) * dx;
    }

    private double cameraDepth(double distance, double z) {
        return pitchCos * distance - pitchSin * (z - eyeHeight);
    }

    private double screenY(double distance, double z) {
        return horizon - focal * (pitchSin * distance + pitchCos * (z - eyeHeight)) / cameraDepth(distance, z);
    }

    StatusPoint statusPoint(int cell, double actorHeight) {
        return statusPoint(cell, actorHeight, null);
    }

    StatusPoint statusPoint(int cell, double actorHeight, CharSprite owner) {
        return combatPoint(cell, actorHeight, owner, false);
    }

    StatusPoint healthPoint(int cell, double actorHeight, CharSprite owner) {
        return combatPoint(cell, actorHeight, owner, true);
    }

    private StatusPoint combatPoint(int cell, double actorHeight, CharSprite owner, boolean healthBar) {
        if (!seen(cell)) return null;
        double x = cell % level.width() + 0.5, y = cell / level.width() + 0.5;
        if (owner != null) {
            x += owner.movementOffsetX();
            y += owner.movementOffsetY();
        }
        double distance = forward(x, y), sideways = side(x, y);
        double head = actorHeight * 0.85, headDepth = cameraDepth(distance, head);
        if (headDepth < NEAR || distance > 20) return null;
        double sx = width * 0.5 + sideways * focal / headDepth;
        double sy = screenY(distance, head);
        if (sx < 0 || sx >= width) return null;
        int ix = (int)sx, iy = Math.max(0, Math.min(height - 1, (int)sy));
        if (thirdPerson ? headDepth >= surfaceDepth[ix + iy * width] : distance >= depth[ix]) return null;
        if (healthBar) {

            double top = thirdPerson
                    ? Math.min(actorHeight, eyeHeight + (pitchCos * distance - NEAR) / pitchSin)
                    : actorHeight * 0.5;
            double labelDepth = cameraDepth(distance, top);
            if (labelDepth < NEAR) return null;
            sx = width * 0.5 + sideways * focal / labelDepth;
            sy = thirdPerson ? Math.min(screenY(distance, top), screenY(distance, 0))
                    : screenY(distance, top) - focal * actorHeight / labelDepth * 0.5;
            return new StatusPoint(sx, sy - 2, focal / labelDepth);
        }
        double labelDepth = cameraDepth(distance, actorHeight + 0.12);
        if (labelDepth < NEAR) return null;
        sx = width * 0.5 + sideways * focal / labelDepth;
        sy = screenY(distance, actorHeight + 0.12);

        if (!thirdPerson) sy = Math.max(36.0 * height / ui.height, sy);
        return new StatusPoint(sx, sy, focal / labelDepth);
    }

    static final class StatusPoint {
        final double x, y, pixelsPerTile;
        StatusPoint(double x, double y, double pixelsPerTile) {
            this.x = x;
            this.y = y;
            this.pixelsPerTile = pixelsPerTile;
        }
    }

    private double illumination(double x, double y) {

        double xDistance = x - heroX, yDistance = y - heroY;

        double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        return Math.max(0.18, 1 - distance * lightFalloff);
    }

    private double terrainLight(int cell, double x, double y) {

        return seen(cell) ? illumination(x, y) : 1;
    }

    void terrainFlame(Image flame, int cell, double offsetX, double offsetY, double size) {
        if (!seen(cell)) return;
        double x = cell % level.width() + 0.5 + offsetX, y = cell / level.width() + 0.5 + offsetY;
        if (solidWall(level.map[cell])) {

            wallParticle(flame, cell, 0.5 + offsetX, 0.5 + offsetY, size, true);

            if (thirdPerson) effectSprite(flame, x, y, CEILING_HEIGHT + size * 0.25,
                    size, 0, 0.9, -1, true);
        } else {

            double base = level.map[cell] == Terrain.HIGH_GRASS ? 0.55
                    : level.map[cell] == Terrain.BARRICADE ? 0.8 : 0;
            effectSprite(flame, x, y, base + size * 0.5, size, 0, 0.9, -1, true);
        }
    }

    void wallParticle(Image particle, int cell, double u, double z, double size, boolean emissive) {
        if (!seen(cell)) return;
        double x = cell % level.width(), y = cell / level.width();

        if (px < x && !wallAt(x - 0.02, y + 0.5)) effectSprite(particle, x - 0.02, y + u, z, size, 0, 1, -1, emissive);
        if (px > x + 1 && !wallAt(x + 1.02, y + 0.5)) effectSprite(particle, x + 1.02, y + u, z, size, 0, 1, -1, emissive);
        if (py < y && !wallAt(x + 0.5, y - 0.02)) effectSprite(particle, x + u, y - 0.02, z, size, 0, 1, -1, emissive);
        if (py > y + 1 && !wallAt(x + 0.5, y + 1.02)) effectSprite(particle, x + u, y + 1.02, z, size, 0, 1, -1, emissive);
    }

    private boolean wallAt(double x, double y) {
        int cell = cellAt(x, y);
        return !seen(cell) || wall(level.map[cell]);
    }

    double flightAngle(double x, double y, double vx, double vy, double z) {
        double distance = forward(x, y), lateral = side(x, y);
        double vd = vx * dx + vy * dy, vs = -vx * dy + vy * dx;

        double projected = cameraDepth(distance, z);
        return Math.atan2(-(eyeHeight - z) * vd, vs * projected - lateral * pitchCos * vd);
    }

    void immersion(int color, double alpha) {
        for (int i = 0; i < pixels.length; i++) pixels[i] = blend(pixels[i], color, alpha);
    }

    private static int litColor(int c, Image s, int tint) {
        int r = Math.min(255, Math.max(0, (int)(((c >> 16) & 255) * s.rm + 255 * s.ra)));
        int g = Math.min(255, Math.max(0, (int)(((c >> 8) & 255) * s.gm + 255 * s.ga)));
        int b = Math.min(255, Math.max(0, (int)((c & 255) * s.bm + 255 * s.ba)));
        if (s instanceof HeroSprite
                && ((HeroSprite)s).partyTint()) {
            r = (int)(r * 0.72f);
            g = (int)(g * 0.86f);
        }
        if (tint != -1) {
            r = r * ((tint >> 16) & 255) / 255;
            g = g * ((tint >> 8) & 255) / 255;
            b = b * (tint & 255) / 255;
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int addLight(int below, int above, double alpha) {

        alpha = Math.max(0, Math.min(1, alpha));
        return 0xFF000000 | Math.min(255, (int)(((below >> 16) & 255) + ((above >> 16) & 255) * alpha)) << 16
                | Math.min(255, (int)(((below >> 8) & 255) + ((above >> 8) & 255) * alpha)) << 8
                | Math.min(255, (int)((below & 255) + (above & 255) * alpha));
    }

    private static int blend(int below, int above, double alpha) {
        alpha = Math.max(0, Math.min(1, alpha));
        double back = 1 - alpha;
        return 0xFF000000 | (int)(((below >> 16) & 255) * back + ((above >> 16) & 255) * alpha) << 16
                | (int)(((below >> 8) & 255) * back + ((above >> 8) & 255) * alpha) << 8
                | (int)((below & 255) * back + (above & 255) * alpha);
    }

    void pointerDown(Touch touch) {
        if (pointer == touch) return;
        if (pointer != null && pointer.down) {
            dragged = true;
            return;
        }
        pointer = touch;
        startAngle = angle;
        startTilt = thirdPerson ? thirdPersonTilt : firstPersonTilt;
        dragged = false;
    }

    void pointerDrag(Touch touch) {
        if (touch != pointer) return;
        float delta = touch.current.x - touch.start.x;
        if (Math.max(Math.abs(delta), Math.abs(touch.current.y - touch.start.y)) > 4 * ui.zoom) dragged = true;
        if (dragged && (!desktopControls || !mouseLookAvailable)
                && !mapCamera.hitTest(touch.start.x, touch.start.y)) {
            angle = startAngle - delta / (ui.width * ui.zoom) * Math.PI * 1.5;
            setTilt(startTilt + (touch.current.y - touch.start.y) / (ui.height * ui.zoom));
        }
    }

    int clickedCell(Touch touch) {
        if (touch != pointer) return -1;
        pointerDrag(touch);

        if (dragged || mapCamera.hitTest(touch.start.x, touch.start.y)
                || mapCamera.hitTest(touch.current.x, touch.current.y)) return -1;
        PointF p = ui.screenToCamera((int)touch.current.x, (int)touch.current.y);
        int x = (int)(p.x * width / ui.width), y = (int)(p.y * height / ui.height);
        return x >= 0 && y >= 0 && x < width && y < height ? pickedCell(x + y * width) : -1;
    }

    private int pickedCell(int index) {
        int cell = cells[index];

        return cell < -1 ? (GameScene.isPerspectiveTargeting() ? -2 - cell : -1) : cell;
    }

    void drawKeys() {
        if (keys != null && !controllerControls) keys.draw();
    }

    void destroy() {
        if (destroyed) return;
        destroyed = true;
        image.destroy();
        indicatorImage.destroy();
        TextureCache.remove(indicatorTexture);
        radar.destroy();
        if (hint != null) hint.destroy();
        mapHeading.destroy();
        mapHeadingOutline.destroy();
        if (desktopControls) {
            movementFill.destroy();
            for (BitmapText key : movementKeys) key.destroy();
            for (ColorBlock line : aimOutline) line.destroy();
            for (ColorBlock line : reticle) line.destroy();
        }
        effects.destroy();
        combat.destroy();
        heldWeapon.destroy();
        if (keys != null) keys.destroy();
        for (Image art : terrainSprites) if (art != null) art.destroy();
        grassAlt.destroy();
        TextureCache.remove(texture);
        mapCamera.destroy();
        sources.clear();
        sprites.clear();
        pickBodies.clear();
        billboardPool.clear();
        ripples.clear();
        rippleFrames.clear();
        rippleSources.clear();
        sourcePixelCount = 0;
        backgroundValid = false;
    }

    private boolean valid(int cell) {
        return cell >= 0 && cell < level.map.length;
    }

    private boolean seen(int cell) {
        return valid(cell) && cell < Dungeon.visible.length && Dungeon.visible[cell];
    }

    private int cellAt(double x, double y) {
        return x >= 0 && y >= 0 && x < level.width() && y < level.height() ? (int)x + (int)y * level.width() : -1;
    }

    private static boolean wall(int t) {
        return t == Terrain.WALL || t == Terrain.WALL_DECO || t == Terrain.SECRET_DOOR || t == Terrain.DOOR
                || t == Terrain.LOCKED_DOOR || t == Terrain.LOCKED_EXIT || t == Terrain.BOOKSHELF || t == Terrain.BARRICADE;
    }

    private static boolean solidWall(int t) {

        return t != Terrain.BARRICADE && wall(t);
    }

    private static boolean isWater(int t) {
        return t >= Terrain.WATER_TILES && t <= Terrain.WATER;
    }

    private static boolean isTrap(int t) {
        return t == Terrain.TOXIC_TRAP || t == Terrain.FIRE_TRAP || t == Terrain.PARALYTIC_TRAP || t == Terrain.POISON_TRAP
                || t == Terrain.ALARM_TRAP || t == Terrain.LIGHTNING_TRAP || t == Terrain.GRIPPING_TRAP || t == Terrain.SUMMONING_TRAP;
    }

    private int tilePixel(int tile, int x, int y) {
        return tiles.at((tile % 16) * 16 + x, (tile / 16) * 16 + y);
    }

    private Pixels source(SmartTexture texture) {
        Pixels result = sources.get(texture);
        if (result == null) {

            boolean copy = (long)texture.width * texture.height <= MAX_CACHED_SOURCE_PIXELS - sourcePixelCount;
            result = new Pixels(texture, copy);
            sources.put(texture, result);
            sourcePixelCount += result.cachedPixels();
        }
        result.used = true;
        return result;
    }

    private static int shade(int c, double light) {
        return 0xFF000000 | ((int)(((c >> 16) & 255) * light) << 16)
                | ((int)(((c >> 8) & 255) * light) << 8) | (int)((c & 255) * light);
    }

    private static String raisedAtlas(String atlas) {
        if (Assets.TILES_SEWERS.equals(atlas)) return Assets.TILES_SEWERS_3D;
        if (Assets.TILES_PRISON.equals(atlas)) return Assets.TILES_PRISON_3D;
        if (Assets.TILES_CAVES.equals(atlas)) return Assets.TILES_CAVES_3D;
        if (Assets.TILES_CITY.equals(atlas)) return Assets.TILES_CITY_3D;
        if (Assets.TILES_HALLS.equals(atlas)) return Assets.TILES_HALLS_3D;
        if (Assets.TILES_SNOW.equals(atlas)) return Assets.TILES_SNOW_3D;
        return atlas;
    }

    private static String terrainAtlas(String atlas) {
        atlas = raisedAtlas(atlas);
        if (Assets.TILES_SEWERS_3D.equals(atlas)) return Assets.TERRAIN_SEWERS;
        if (Assets.TILES_PRISON_3D.equals(atlas)) return Assets.TERRAIN_PRISON;
        if (Assets.TILES_CAVES_3D.equals(atlas)) return Assets.TERRAIN_CAVES;
        if (Assets.TILES_CITY_3D.equals(atlas)) return Assets.TERRAIN_CITY;
        if (Assets.TILES_HALLS_3D.equals(atlas)) return Assets.TERRAIN_HALLS;
        if (Assets.TILES_SNOW_3D.equals(atlas)) return Assets.TERRAIN_SNOW;
        throw new IllegalArgumentException("Unsupported terrain atlas: " + atlas);
    }
    private static final class Pixels {
        final int width, height;
        final int widthMask, heightMask;
        final int[] data;
        final Bitmap bitmap;
        boolean used;
        Pixels(SmartTexture texture, boolean copy) {
            width = texture.width;
            height = texture.height;
            widthMask = (width & (width - 1)) == 0 ? width - 1 : -1;
            heightMask = (height & (height - 1)) == 0 ? height - 1 : -1;
            bitmap = copy ? null : texture.bitmap;
            data = copy ? new int[width * height] : null;
            if (copy) texture.bitmap.getPixels(data, 0, width, 0, 0, width, height);
        }
        int cachedPixels() {
            return data == null ? 0 : data.length;
        }
        int at(int x, int y) {
            if (widthMask >= 0) x &= widthMask;
            else if (x < 0 || x >= width) {
                x %= width;
                if (x < 0) x += width;
            }
            if (heightMask >= 0) y &= heightMask;
            else if (y < 0 || y >= height) {
                y %= height;
                if (y < 0) y += height;
            }
            return data == null ? bitmap.getPixel(x, y) : data[x + y * width];
        }
    }

    private static final class PickBody {
        final SmartTexture texture;
        final RectF[] frames;
        double halfWidth, bottom = Double.POSITIVE_INFINITY, top;
        PickBody(CharSprite sprite, Pixels pixels) {
            texture = sprite.texture;
            frames = sprite.idleFrames();
            RectF[] samples = frames == null || frames.length == 0 ? new RectF[]{sprite.frame()} : frames;
            for (RectF frame : samples) {
                if (frame == null) continue;
                int fx = Math.round(frame.left * pixels.width), fy = Math.round(frame.top * pixels.height);
                int fw = Math.max(1, Math.round(frame.width() * pixels.width));
                int fh = Math.max(1, Math.round(frame.height() * pixels.height));
                for (int y = 0; y < fh; y++) for (int x = 0; x < fw; x++) {
                    if ((pixels.at(fx + x, fy + y) >>> 24) <= 8) continue;
                    halfWidth = Math.max(halfWidth, Math.max(Math.abs(x - fw * 0.5), Math.abs(x + 1 - fw * 0.5)) / 16);
                    bottom = Math.min(bottom, (fh - y - 1) / 16.0);
                    top = Math.max(top, (fh - y) / 16.0);
                }
            }
            if (bottom == Double.POSITIVE_INFINITY) {
                halfWidth = sprite.width / 32.0;
                bottom = 0;
                top = Math.max(0.5, sprite.height / 16.0);
            }
            halfWidth += 1.0 / 16;
            bottom = Math.max(0, bottom - 1.0 / 16);
            top += 1.0 / 16;
        }
    }

    private Billboard billboard() {
        int index = sprites.size();
        if (index < billboardPool.size()) return billboardPool.get(index);
        Billboard result = new Billboard();
        if (index < MAX_POOLED_BILLBOARDS) billboardPool.add(result);
        return result;
    }

    private static final class Billboard {
        Image sprite;
        int cell;
        int terrainCell;
        double distance, side, size, z, aspect, rotation, opacity;
        int tint;
        boolean emissive, upright, additive;
        double projectedDepth;
        boolean visiblePixels;
        Billboard set(Image sprite, int cell, double distance, double side, double size, double z,
                  double aspect, double rotation, double opacity, int tint, boolean emissive, boolean upright) {
            this.sprite = sprite;
            this.cell = cell;
            terrainCell = -1;
            this.distance = distance;
            this.side = side;
            this.size = size;
            this.z = z;
            this.aspect = aspect;
            this.rotation = rotation;
            this.opacity = opacity;
            this.tint = tint;
            this.emissive = emissive;
            this.upright = upright;
            projectedDepth = 0;
            visiblePixels = false;
            additive = false;
            return this;
        }
    }
    private static final Comparator<Billboard> FAR_FIRST = new Comparator<Billboard>() {
        @Override
        public int compare(Billboard a, Billboard b) {
            return Double.compare(b.projectedDepth, a.projectedDepth);
        }
    };

    private static final class HeadingArrow extends Image {
        HeadingArrow(int color, float base, float tip, float halfWidth) {
            super(TextureCache.createSolid(color));
            float[] corners = {base, -halfWidth, tip, 0, base, halfWidth, base, halfWidth};
            for (int i = 0; i < 4; i++) {
                vertices[i * 4] = corners[i * 2];
                vertices[i * 4 + 1] = corners[i * 2 + 1];
            }
            dirty = true;
        }
    }

    private static final class FrameTexture extends SmartTexture {
        private final IntBuffer buffer;
        FrameTexture(int width, int height) {
            this.width = width;
            this.height = height;
            buffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        }

        @Override
        protected void generate() {
            int[] names = new int[1];
            GLES20.glGenTextures(1, names, 0);
            id = names[0];
            bind();
            filter(NEAREST, NEAREST);
            wrap(CLAMP, CLAMP);
            buffer.position(0);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }
        void upload(int[] argb) {
            buffer.position(0);
            for (int color : argb) buffer.put((color & 0xFF00FF00) | ((color >>> 16) & 255) | ((color & 255) << 16));
            buffer.position(0);
            bind();
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }
    }
}
