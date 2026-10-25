package com.bilboldev.pixeldungeonskills.scenes;

import java.util.Arrays;
import java.util.Random;

import com.bilboldev.glwrap.Matrix;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.SkinnedBlock;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.Tilemap;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.BadgeBanner;
import com.bilboldev.pixeldungeonskills.effects.Halo;
import com.bilboldev.pixeldungeonskills.effects.particles.FlameParticle;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.PointF;

final class TitleBackdrop extends Group {
    private static final int COLS = 100, ROWS = 56, TILE = 16;
    private final int[] cells = new int[COLS * ROWS];
    private final Camera view;
    private final SkinnedBlock water, wellWater;
    private final boolean portrait;
    private final HeroFigure[] heroes = new HeroFigure[TitleScene.HERO_CLASSES.length];
    private float originX, originY, fromX, fromY, targetX, targetY, travel = 1;
    private float time;

    TitleBackdrop(boolean portrait) {
        this.portrait = portrait;
        float zoom = portrait ? Math.round(Game.height / 400f)
                : (float)Math.ceil(Math.max(Game.width / 560f, Game.height / 330f));
        view = Camera.add(Camera.createFullscreen(Math.max(1, zoom)));
        camera = view;
        originX = 300 - view.width * (portrait ? 0.5f : 0.70f);
        originY = 166 - view.height * (portrait ? 0.30f : 0.50f);
        positionCamera();

        Arrays.fill(cells, Terrain.WALL);
        room(12, 5, 25, 16);
        room(17, 0, 19, 23);
        room(0, 10, 12, 12);
        room(25, 8, 31, 10);
        room(8, 5, 12, 8);
        room(11, 0, 24, 2);

        room(26, 8, 46, 10);
        room(46, 6, 61, 21);
        room(17, 22, 19, 35);
        room(30, 39, 47, 41);
        room(52, 20, 54, 34);
        room(63, 8, 80, 10);
        for (int x : new int[]{15, 23}) {
            cells[x + 8 * COLS] = Terrain.WALL_DECO;
            cells[x + 14 * COLS] = Terrain.WALL;
        }
        for (int y = 12; y <= 14; y++) for (int x = 18; x <= 20; x++)
            cells[x + y * COLS] = Terrain.WATER;
        cells[14 + 5 * COLS] = Terrain.STATUE;
        cells[23 + 5 * COLS] = Terrain.STATUE;
        furnishLodge();
        furnishMemorial();
        furnishTrophyRoom();
        furnishArchive();

        water = new SkinnedBlock(3 * TILE, 3 * TILE, Assets.WATER_PRISON);
        water.x = 18 * TILE;
        water.y = 12 * TILE;
        water.hardlight(0x677E91);
        add(water);
        wellWater = new SkinnedBlock(TILE, TILE, Assets.WATER_CITY);
        wellWater.x = 53 * TILE;
        wellWater.y = (portrait ? 18 : 14) * TILE;
        wellWater.hardlight(0x749CA7);
        add(wellWater);

        int[] floor = new int[cells.length], walls = new int[cells.length], caps = new int[cells.length];
        Arrays.fill(floor, -1);
        Arrays.fill(walls, -1);
        Arrays.fill(caps, -1);
        Random random = new Random(732193);
        for (int y = 0; y < ROWS; y++) for (int x = 0; x < COLS; x++) {
            int index = x + y * COLS, tile = cells[index];
            if (!solid(x, y)) {
                int variant = random.nextInt(18);
                floor[index] = variant == 0 ? DungeonTileSheet.FLOOR_ALT_1
                        : variant == 1 ? DungeonTileSheet.FLOOR_ALT_2 : DungeonTileSheet.FLOOR;
                if (tile == Terrain.WATER) {
                    floor[index] = DungeonTileSheet.stitchWaterTile(cell(x, y - 1),
                            cell(x + 1, y), cell(x, y + 1), cell(x - 1, y));
                } else if (tile == Terrain.STATUE) {
                    walls[index] = DungeonTileSheet.RAISED_STATUE;
                } else if (tile == Terrain.PEDESTAL) {
                    floor[index] = DungeonTileSheet.PEDESTAL;
                } else if (tile == Terrain.EMPTY_SP) {
                    floor[index] = DungeonTileSheet.FLOOR_SP;
                } else if (tile == Terrain.ALCHEMY) {
                    floor[index] = DungeonTileSheet.ALCHEMY_POT;
                } else if (tile == Terrain.WELL) {
                    floor[index] = DungeonTileSheet.WELL;
                } else if (tile == Terrain.HIGH_GRASS) {
                    walls[index] = DungeonTileSheet.RAISED_HIGH_GRASS;
                } else if (tile == Terrain.GRASS) {
                    floor[index] = DungeonTileSheet.GRASS;
                }
            } else {
                walls[index] = (tile == Terrain.BOOKSHELF ? DungeonTileSheet.RAISED_WALL_BOOKSHELF
                        : tile == Terrain.WALL_DECO ? DungeonTileSheet.RAISED_WALL_DECO
                        : DungeonTileSheet.RAISED_WALL) + (solid(x + 1, y) ? 0 : 1)
                        + (solid(x - 1, y) ? 0 : 2);
            }
            if (solid(x, y) && solid(x, y + 1)) {
                caps[index] = DungeonTileSheet.stitchInternalWallTile(tile, cell(x + 1, y),
                        cell(x + 1, y + 1), cell(x - 1, y + 1), cell(x - 1, y));
            } else if (solid(x, y + 1)) {
                caps[index] = DungeonTileSheet.stitchWallOverhangTile(tile,
                        cell(x + 1, y + 1), cell(x, y + 1), cell(x - 1, y + 1));
            } else if (cell(x, y + 1) == Terrain.STATUE) {
                caps[index] = DungeonTileSheet.STATUE_OVERHANG;
            }
        }
        addTiles(floor, 0x8993A6);
        addTiles(walls, 0xA0A1AE);
        addLodgeProps();
        addMemorialProps();
        addTrophyProps();
        addArchiveProps();

        add(new Walker(Assets.SKELETON, 12, 15, new int[]{4, 5, 6, 7, 8, 9}, 6.0f, 1.8f,
                new float[][]{{17, 10}, {21, 10}, {21, 11}, {17, 11}}));
        add(new Walker(Assets.RAT, 16, 15, new int[]{6, 7, 8, 9, 10}, 8.5f, 0.6f,
                new float[][]{{13, 11}, {16, 11}, {16, 15}, {13, 15}}));
        add(new Walker(Assets.GNOLL, 12, 15, new int[]{4, 5, 6, 7}, 5.0f, 5f,
                new float[][]{{24, 10}, {28, 10}, {28, 8}, {24, 8}}));
        addRoomWalkers();
        for (int i = 0; i < heroes.length; i++) {
            float hx = heroX(i), hy = heroY(i);
            heroes[i] = new HeroFigure(TitleScene.HERO_CLASSES[i], hx, hy);
            add(heroes[i].light);
            add(heroes[i]);
        }
        addTiles(caps, 0x7C8191);

        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == Terrain.WALL_DECO) add(new Torch(i));
        }
        for (int i = 0; i < 18; i++) add(new Mote(random));
    }

    private void room(int left, int top, int right, int bottom) {
        for (int y = top; y <= bottom; y++) for (int x = left; x <= right; x++)
            cells[x + y * COLS] = Terrain.EMPTY;
    }

    private float heroX(int index) {
        return portrait ? 856 + (index % 3 - 1) * 48 : (51 + index % 3 * 3) * TILE;
    }

    private float heroY(int index) {
        return portrait ? 208 + index / 3 * 60 : (12 + index / 3 * 5) * TILE;
    }

    private void cell(int x, int y, int tile) {
        cells[x + y * COLS] = tile;
    }

    private void furnishLodge() {

        for (int y = 4; y <= 23; y++) for (int x = 45; x <= 63; x++) cell(x, y, Terrain.WALL);
        room(43, 9, 44, 13);
        room(43, 12, 49, 13);
        room(53, 19, 55, 24);
        if (portrait) {
            room(48, 10, 59, 18);
            room(50, 9, 57, 10);
            for (int x = 50; x <= 57; x++) cell(x, 9, Terrain.BOOKSHELF);
            cell(48, 9, Terrain.WALL_DECO);
            cell(59, 9, Terrain.WALL_DECO);
            cell(49, 11, Terrain.STATUE);
            cell(58, 11, Terrain.STATUE);
            cell(48, 16, Terrain.BOOKSHELF);
            cell(59, 16, Terrain.BOOKSHELF);
            cell(53, 18, Terrain.WELL);
        } else {
            room(48, 9, 60, 19);
            room(50, 8, 58, 10);
            room(46, 12, 48, 15);
            room(60, 12, 62, 15);
            for (int x = 50; x <= 58; x++) cell(x, 8, Terrain.BOOKSHELF);
            for (int x : new int[]{46, 62}) for (int y = 12; y <= 14; y++) cell(x, y, Terrain.BOOKSHELF);
            cell(48, 8, Terrain.WALL_DECO);
            cell(60, 8, Terrain.WALL_DECO);
            cell(48, 17, Terrain.WALL_DECO);
            cell(60, 17, Terrain.WALL_DECO);
            cell(48, 10, Terrain.STATUE);
            cell(60, 10, Terrain.STATUE);
            cell(53, 14, Terrain.WELL);
        }
    }

    private void addLodgeProps() {

        if (portrait) {
            carpet(792, 188, 128, 40);
            carpet(792, 248, 128, 44);
            prop(ItemSpriteSheet.CHEST, 784, 294, 1);
            prop(ItemSpriteSheet.CHEST, 936, 294, 1);
        } else {
            carpet(792, 172, 144, 42);
            carpet(792, 252, 144, 46);
            prop(ItemSpriteSheet.CHEST, 764, 244, 1);
            prop(ItemSpriteSheet.CHEST, 984, 244, 1);
        }
        add(new Halo(18, 0x86B7C6, .10f).point(wellWater.x + 8, wellWater.y + 8));
    }

    private void furnishMemorial() {
        room(17, 30, 19, 38);
        room(27, 39, 33, 41);
        room(portrait ? 17 : 16, portrait ? 37 : 35, portrait ? 27 : 28, portrait ? 42 : 45);
        room(20, portrait ? 36 : 33, 24, 37);
        int altarRow = portrait ? 37 : 35;
        cell(22, altarRow, Terrain.STATUE);
        for (int x : new int[]{20, 24}) cell(x, altarRow - 1, Terrain.WALL_DECO);
        if (portrait) {
            for (int y = 39; y <= 42; y++) {
                for (int x : new int[]{18, 19, 25, 26}) cell(x, y, Terrain.GRASS);
            }
        } else {
            cell(17, 35, Terrain.STATUE);
            cell(27, 35, Terrain.STATUE);
            for (int y = 38; y <= 45; y++) {
                for (int x : new int[]{17, 18, 19, 25, 26, 27}) cell(x, y, Terrain.GRASS);
            }
            cell(16, 42, Terrain.WALL_DECO);
            cell(28, 42, Terrain.WALL_DECO);
        }
    }

    private void furnishTrophyRoom() {
        room(43, 39, 48, 41);
        room(52, 31, 54, 37);
        room(portrait ? 48 : 47, portrait ? 37 : 35, portrait ? 58 : 59, portrait ? 42 : 44);
        room(51, portrait ? 36 : 33, 55, 37);
        int backRow = portrait ? 37 : 35;
        cell(portrait ? 49 : 48, backRow, Terrain.STATUE);
        cell(portrait ? 57 : 58, backRow, Terrain.STATUE);
        if (!portrait) {
            room(45, 37, 48, 40);
            room(58, 37, 60, 40);
            for (int x : new int[]{45, 46, 47, 59, 60}) cell(x, 37, Terrain.BOOKSHELF);
            cell(47, 42, Terrain.STATUE);
        }
    }

    private void furnishArchive() {
        room(78, 9, 81, 13);
        if (portrait) {
            room(81, 10, 92, 15);
            room(83, 9, 90, 10);
            room(80, 12, 82, 14);
            room(91, 12, 93, 14);
            for (int x = 83; x <= 90; x++) cell(x, 9, Terrain.BOOKSHELF);
            cell(80, 12, Terrain.BOOKSHELF);
            cell(93, 12, Terrain.BOOKSHELF);
            cell(81, 9, Terrain.WALL_DECO);
            cell(92, 9, Terrain.WALL_DECO);
        } else {
            room(80, 7, 92, 18);
            room(84, 5, 88, 8);
            room(78, 10, 81, 14);
            room(91, 10, 93, 14);
            room(85, 18, 87, 22);
            for (int x = 84; x <= 88; x++) cell(x, 5, Terrain.BOOKSHELF);
            for (int x : new int[]{81, 82, 83, 89, 90, 91}) cell(x, 7, Terrain.BOOKSHELF);
            for (int x : new int[]{78, 79, 80, 91, 92, 93}) cell(x, 10, Terrain.BOOKSHELF);
            cell(80, 7, Terrain.WALL_DECO);
            cell(92, 7, Terrain.WALL_DECO);
            cell(80, 16, Terrain.WALL_DECO);
            cell(92, 16, Terrain.WALL_DECO);
        }
    }

    private void addMemorialProps() {
        carpet(344, portrait ? 606 : 578, 32, portrait ? 76 : 158).tint(0x333E58, .65f);
        int[] rows = portrait ? new int[]{39, 41} : new int[]{38, 41, 44};
        int[] columns = portrait ? new int[]{19, 25} : new int[]{18, 26};
        for (int row : rows) {
            for (int column : columns) {
                prop(ItemSpriteSheet.TOMB, (column + .5f) * TILE, (row + 1) * TILE, 1)
                        .hardlight(0xA4AAB9);
            }
        }
        float altarY = (portrait ? 38 : 36) * TILE;
        prop(ItemSpriteSheet.ROSE, 360, altarY + 9, 1).hardlight(0xB9A0B9);
        add(new Halo(28, 0xA3B8D8, .08f).point(360, altarY - 8));
    }

    private void addTrophyProps() {
        carpet(832, portrait ? 600 : 560, 48, portrait ? 86 : 160).hardlight(0xB39AA8);
        float altarY = (portrait ? 39 : 36) * TILE;
        pedestal(856, altarY, 2);
        prop(ItemSpriteSheet.AMULET, 856, altarY - 7, 1.5f);
        add(new Halo(35, 0xF1CB7F, .16f).point(856, altarY - 12));
        if (!portrait) {
            pedestal(808, 37 * TILE, 1);
            prop(ItemSpriteSheet.SWORD, 808, 37 * TILE - 7, 1);
            pedestal(904, 37 * TILE, 1);
            prop(ItemSpriteSheet.ARMOR_PLATE, 904, 37 * TILE - 7, 1);
            prop(ItemSpriteSheet.CHEST, 808, 44 * TILE, 1);
            prop(ItemSpriteSheet.CHEST, 904, 44 * TILE, 1);
        }

        Badges.Badge[] badges = {
                Badges.Badge.MONSTERS_SLAIN_4, Badges.Badge.VICTORY,
                Badges.Badge.GRIM_WEAPON, Badges.Badge.CHAMPION,
                Badges.Badge.NIGHT_HUNTER, Badges.Badge.RARE
        };
        int[] rows = portrait ? new int[]{39, 41} : new int[]{38, 40, 42};
        int[] columns = portrait ? new int[]{50, 56} : new int[]{49, 57};
        int index = 0;
        for (int row : rows) {
            for (int column : columns) {
                badge(badges[index++], (column + .5f) * TILE, (row + 1) * TILE);
            }
        }
    }

    private void badge(Badges.Badge badge, float x, float y) {
        Image medal = BadgeBanner.image(badge.image);
        medal.scale.set(1.5f);
        medal.x = x - medal.width() / 2;
        medal.y = y - 4 - medal.height();
        medal.hardlight(0xE7DCC2);
        add(medal);
    }

    private void addArchiveProps() {
        carpet(1352, portrait ? 184 : 148, 64, portrait ? 68 : 136).tint(0x3D344C, .45f);
        float deskY = portrait ? 192 : 144;
        pedestal(1384, deskY, 3);
        prop(ItemSpriteSheet.SCROLL_KAUNAN, 1368, deskY - 7, 1);
        prop(ItemSpriteSheet.MASTERY, 1384, deskY - 7, 1);
        prop(ItemSpriteSheet.SCROLL_SOWILO, 1400, deskY - 7, 1);
        if (!portrait) {
            prop(ItemSpriteSheet.CHEST, 1312, 284, 1);
            prop(ItemSpriteSheet.CHEST, 1456, 284, 1);
        }
    }

    private void addRoomWalkers() {
        add(new Walker(Assets.RATKING, 16, 16, new int[]{2, 3, 4, 5, 6}, 8, .5f,
                portrait ? new float[][]{{54, 40}, {54, 41}, {52, 41}, {52, 40}}
                        : new float[][]{{54.5f, 41}, {54.5f, 43}, {51.5f, 43}, {51.5f, 38}, {54.5f, 38}}));
        add(new Walker(Assets.GHOST, 14, 15, new int[]{0, 1}, 5, 1,
                portrait ? new float[][]{{23.5f, 39.5f}, {23.5f, 41}, {21, 41}, {21, 39.5f}}
                        : new float[][]{{24, 40}, {24, 44}, {20, 44}, {20, 38}, {24, 38}}));
        int[] monkFrames = {11, 12, 13, 14, 15, 16};
        add(new Walker(Assets.MONK, 15, 14, monkFrames, 5, .8f,
                portrait ? new float[][]{{83, 12}, {82, 12}, {82, 14}, {83, 14}}
                        : new float[][]{{85, 10}, {88, 10}, {88, 12}, {85, 12}}));
        add(new Walker(Assets.MONK, 15, 14, monkFrames, 4.5f, 2.8f,
                portrait ? new float[][]{{90, 14}, {91, 14}, {91, 12}, {90, 12}}
                        : new float[][]{{90, 14}, {90, 16}, {89, 16}, {89, 14}}));
        if (!portrait) {
            add(new Walker(Assets.MONK, 15, 14, monkFrames, 5.5f, 4.4f,
                    new float[][]{{83, 16}, {82, 16}, {82, 13}, {83, 13}}));
        }
    }

    private void pedestal(float x, float y, float width) {
        Image pedestal = new Image(Assets.TILES_CITY_3D, 80, 16, 16, 16);
        pedestal.scale.set(width, 1);
        pedestal.x = x - pedestal.width() / 2;
        pedestal.y = y - pedestal.height();
        pedestal.hardlight(0xB6ADA5);
        add(pedestal);
    }

    private NinePatch carpet(float x, float y, float width, float height) {
        NinePatch carpet = new NinePatch(Assets.TILES_CITY_3D, 64, 0, 16, 16, 2);
        carpet.x = x;
        carpet.y = y;
        carpet.size(width, height);
        carpet.hardlight(0x9A7F8A);
        add(carpet);
        return carpet;
    }

    private ItemSprite prop(int image, float x, float y, float scale) {
        ItemSprite prop = new ItemSprite(image, null) {{ renderShadow = true; }};
        prop.scale.set(scale);
        prop.x = x - prop.width() / 2;
        prop.y = y - prop.height();
        prop.hardlight(0xCDC6B8);
        add(prop);
        return prop;
    }

    private int cell(int x, int y) {
        return x < 0 || y < 0 || x >= COLS || y >= ROWS ? Terrain.WALL : cells[x + y * COLS];
    }

    private boolean solid(int x, int y) {
        return DungeonTileSheet.wallStitcheable(cell(x, y));
    }

    private void addTiles(int[] data, int color) {
        int[] city = new int[data.length];
        Arrays.fill(city, -1);
        for (int y = 6; y <= 48; y++) {
            if (y > 23 && y < 32) continue;
            for (int x = 45; x <= 63; x++) {
                int pos = x + y * COLS;
                city[pos] = data[pos];
                data[pos] = -1;
            }
        }
        addTiles(data, color, Assets.TILES_PRISON_3D);
        addTiles(city, 0xB6ADA5, Assets.TILES_CITY_3D);
    }

    private void addTiles(int[] data, int color, String texture) {
        Tilemap tiles = new Tilemap(texture, new TextureFilm(texture, TILE, TILE)) {
            @Override
            protected boolean needsRender(int pos) {
                return this.data[pos] >= 0;
            }
        };
        tiles.camera = view;
        tiles.map(data, COLS);
        tiles.hardlight(color);
        add(tiles);
    }

    private void positionCamera() {
        view.scroll.set(PixelScene.align(view, originX + (float)Math.sin(time * 0.095f) * 2.5f),
                PixelScene.align(view, originY + (float)Math.sin(time * 0.071f) * 1.5f));
    }

    void visit(TitleScene.Page page, boolean immediate) {
        float cx = 300, cy = 166;
        switch (page) {
            case HEROES: cx = portrait ? 856 : 864;
            cy = portrait ? 208 : 224;
            break;
            case RANKINGS: cx = 360;
            cy = 640;
            break;
            case BADGES: cx = portrait ? 856 : 864;
            cy = 640;
            break;
            case ABOUT: cx = portrait ? 1384 : 1392;
            cy = 208;
            break;
            default: break;
        }
        fromX = originX;
        fromY = originY;
        targetX = cx - view.width * (portrait ? .5f : .70f);
        targetY = cy - view.height * (portrait ? (page == TitleScene.Page.HEROES ? .32f : .27f) : .52f);
        if (page == TitleScene.Page.HOME) targetY = 166 - view.height * (portrait ? .30f : .50f);
        travel = immediate || !PixelDungeon.titleAnimation() ? 1 : 0;
        if (travel == 1) {
            originX = targetX;
            originY = targetY;
            positionCamera();
        }
    }

    void selectHero(HeroClass selected) {
        selectHeroes(selected, null);
    }

    void selectHeroes(HeroClass selected, HeroClass companion) {
        for (HeroFigure hero : heroes) hero.select(hero.heroClass == selected || hero.heroClass == companion);
    }

    PointF heroAnchor(int index) {
        HeroFigure hero = heroes[index];
        Point screen = view.cameraToScreen(hero.feetX, hero.feetY);
        return Camera.main.screenToCamera(screen.x, screen.y);
    }

    float heroScale() {
        return view.zoom / Camera.main.zoom;
    }

    @Override
    public void update() {
        float dt = Math.min(Game.elapsed, 0.1f);
        if (PixelDungeon.titleAnimation()) {
            time += dt;
            super.update();
        }
        if (travel < 1) {
            travel = PixelDungeon.titleAnimation() ? Math.min(1, travel + dt / .85f) : 1;
            float ease = travel * travel * (3 - 2 * travel);
            originX = fromX + (targetX - fromX) * ease;
            originY = fromY + (targetY - fromY) * ease;
        }
        water.offsetTo(time * 0.65f, time * 0.35f);
        wellWater.offsetTo(time * .25f, time * .15f);
        positionCamera();
    }

    private static final class HeroFigure extends CharSprite {
        final HeroClass heroClass;
        final float feetX, feetY;
        final Halo light;
        private boolean selected;
        private float step;

        HeroFigure(HeroClass heroClass, float x, float y) {
            this.heroClass = heroClass;
            feetX = x;
            feetY = y;
            texture(heroClass.spritesheet());
            renderShadow = true;
            listener = null;
            TextureFilm film = new TextureFilm(new TextureFilm(texture, texture.width, 15), 1, 12, 15);
            idle = new Animation(2, true).frames(film, 0, 0, 0, 1, 0, 0, 1, 1);
            run = new Animation(10, true).frames(film, 2, 3, 4, 5, 6, 7);
            scale.set(1.5f);
            light = new Halo(17, 0xB19CE3, .20f);
            play(idle);
            placeFigure();
        }

        void select(boolean value) {
            selected = value;
            if (!PixelDungeon.titleAnimation()) step = value ? 1 : 0;
            placeFigure();
        }

        private void placeFigure() {
            x = feetX - width() / 2;
            y = feetY + step * 10 - height();
            light.point(feetX, feetY + step * 10 - 3);
            light.visible = selected;
            brightness(selected ? 1.15f : .72f);
        }

        @Override
        protected void updateMatrix() {
            super.updateMatrix();

            Matrix.copy(matrix, shadowMatrix);
            Matrix.translate(shadowMatrix, width * (1f - shadowWidth) / 2f,
                    height * (1f - shadowHeight));
            Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
        }

        @Override
        public void update() {
            float target = selected ? 1 : 0;
            float delta = Math.min(Game.elapsed, .1f) / .35f;
            step = target > step ? Math.min(target, step + delta) : Math.max(target, step - delta);
            play(Math.abs(step - target) > .001f ? run : idle);
            placeFigure();
            super.update();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        Camera.remove(view);
        view.destroy();
    }

    private static final class Walker extends CharSprite {
        private final Animation walk;
        private final float[][] route;
        private final float pace;
        private int target = 1;
        private float wait, feetX, feetY;

        Walker(String texture, int frameWidth, int frameHeight, int[] frames, float pace, float wait, float[][] route) {
            texture(texture);

            renderShadow = true;
            if (Assets.GHOST.equals(texture)) {
                renderShadow = false;
                alpha(.6f);
            }
            listener = null;
            this.route = route;
            this.pace = pace;
            this.wait = wait;
            TextureFilm film = new TextureFilm(texture, frameWidth, frameHeight);
            Object[] indices = new Object[frames.length];
            for (int i = 0; i < frames.length; i++) indices[i] = frames[i];
            walk = new Animation(7, true).frames(film, indices);
            idle = new Animation(2, true).frames(film, 0, 0, 0, 1);
            feetX = (route[0][0] + 0.5f) * TILE;
            feetY = (route[0][1] + 0.75f) * TILE;
            play(idle);
            hardlight(0xBDC1CF);
            position();
        }

        private void position() {
            x = feetX - width / 2;
            y = feetY - height;
        }

        @Override
        public void update() {
            float dt = Math.min(Game.elapsed, 0.1f);
            if (wait > 0) {
                wait -= dt;
                play(idle);
            } else {
                float dx = (route[target][0] + 0.5f) * TILE - feetX;
                float dy = (route[target][1] + 0.75f) * TILE - feetY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                if (distance <= pace * dt) {
                    feetX += dx;
                    feetY += dy;
                    target = (target + 1) % route.length;
                    wait = 2.5f + target * 0.7f;
                } else {
                    feetX += dx / distance * pace * dt;
                    feetY += dy / distance * pace * dt;
                    if (Math.abs(dx) > 0.01f) flipHorizontal = dx < 0;
                    play(walk);
                }
                position();
            }
            super.update();
        }
    }

    private static final class Torch extends Emitter {
        private final Random random;

        Torch(int cell) {
            random = new Random(732193L + cell);
            float centerX = (cell % COLS + 0.5f) * TILE;
            float centerY = (cell / COLS + 0.5f) * TILE;

            pos(centerX - 1, centerY + 3, 2, 0);
            factory = FlameParticle.FACTORY;
            lightMode = factory.lightMode();
            interval = 0.15f;
            time = random.nextFloat() * interval;
            on = true;
            add(new Halo(16, 0xFFFFCC, 0.2f).point(centerX, centerY));

            FlameParticle initial = new FlameParticle();
            initial.reset(centerX, y);
            initial.size(3);
            add(initial);
        }

        @Override
        protected void emit(int index) {

            factory.emit(this, index, x + random.nextFloat() * width, y);
        }
    }

    private static final class Mote extends ColorBlock {
        private final float startX, startY, phase;
        private float time;
        Mote(Random random) {
            super(0.55f, 0.55f, 0xFFD3C3A4);
            startX = 200 + random.nextFloat() * 240;
            startY = 80 + random.nextFloat() * 210;
            phase = random.nextFloat() * 6.28f;
            time = random.nextFloat() * 30;
            move();
        }

        private void move() {
            x = startX + (float)Math.sin(time * 0.15f + phase) * 8;
            y = startY - (time * 1.2f % 45);
            alpha(Math.max(0, 0.13f + 0.16f * (float)Math.sin(time * 0.5f + phase)));
        }

        @Override
        public void update() {
            time += Math.min(Game.elapsed, 0.1f);
            move();
        }
    }
}
