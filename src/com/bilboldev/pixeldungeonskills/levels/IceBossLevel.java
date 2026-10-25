package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.IceCaves;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.OldRime;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class IceBossLevel extends Level {
    private static final int GATE = 16 + 25 * WIDTH;
    private boolean entered;

    {
        viewDistance = 8;
        color1 = 0x739CAD;
        color2 = 0xCAE4EB;
    }

    @Override
    public String tilesTex() {
        return Difficulties.is3d ? Assets.TILES_ICE_BOSS_3D : Assets.TILES_ICE_BOSS;
    }

    @Override
    public String waterTex() {
        return Assets.WATER_ICE_CAVES;
    }

    @Override
    public String tileName(int tile) {
        if (tile == Terrain.EXIT) return Messages.get(IceBossLevel.class, "exit_name");
        if (tile == Terrain.LOCKED_EXIT) return Messages.get(IceBossLevel.class, "blocked_name");
        return super.tileName(tile);
    }

    @Override
    public String tileDesc(int tile) {
        if (tile == Terrain.EXIT) return Messages.get(IceBossLevel.class, "exit_desc");
        if (tile == Terrain.LOCKED_EXIT) return Messages.get(IceBossLevel.class, "blocked_desc");
        return super.tileDesc(tile);
    }

    @Override
    protected boolean build() {
        Painter.fill(this, 5, 5, 23, 20, Terrain.EMPTY);
        for (int y = 5; y < 25; y++) for (int x = 5; x < 28; x++) {
            if ((x < 8 || x > 24) && (y < 8 || y > 21)) map[x + y * WIDTH] = Terrain.WALL;
        }
        Painter.fill(this, 14, 26, 5, 4, Terrain.EMPTY);
        map[GATE] = Terrain.DOOR;
        entrance = 16 + 28 * WIDTH;
        map[entrance] = Terrain.ENTRANCE;
        exit = 16 + 4 * WIDTH;
        map[exit] = IceCaves.bossSlain() ? Terrain.EXIT : Terrain.LOCKED_EXIT;
        for (int cell : new int[]{10 + 10 * WIDTH, 22 + 10 * WIDTH,
                10 + 19 * WIDTH, 22 + 19 * WIDTH}) map[cell] = Terrain.WALL_DECO;
        return true;
    }

    @Override
    protected void decorate() {
        for (int cell = 0; cell < LENGTH; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(12) == 0) map[cell] = Terrain.EMPTY_DECO;
        }
    }

    @Override
    protected void createMobs() {
        if (IceCaves.bossSlain()) return;
        OldRime boss = new OldRime();
        boss.pos = 16 + 9 * WIDTH;
        mobs.add(boss);
    }

    @Override
    protected void createItems() { }

    @Override
    public Actor respawner() {
        return null;
    }

    @Override
    public int randomRespawnCell() {
        for (int cell = entrance - 1; cell <= entrance + 1; cell++) {
            if (Actor.findChar(cell) == null) return cell;
        }
        return -1;
    }

    @Override
    public void press(int cell, Char ch) {
        super.press(cell, ch);
        if (entered || IceCaves.bossSlain() || cell / WIDTH >= 25 || !(ch == Dungeon.hero || LocalCoop.contains(ch))) return;
        if (!gatherParty(ch, cell, GATE)) return;
        entered = true;
        set(GATE, Terrain.WALL);
        GameScene.updateMap(GATE);
        for (Mob mob : mobs) if (mob instanceof OldRime) {
            mob.state = mob.HUNTING;
            mob.beckon(cell);
        }
        Dungeon.observe();
    }

    public void openExit() {
        set(GATE, Terrain.EMPTY);
        set(exit, Terrain.EXIT);
        GameScene.updateMap(GATE);
        GameScene.updateMap(exit);
        Dungeon.observe();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("ice_entered", entered);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        entered = bundle.getBoolean("ice_entered");
    }
}
