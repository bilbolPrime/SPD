package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.IceCaves;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.mobs.Iceclaw;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Prospector;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Torch;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.utils.PathFinder;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Comparator;

public class IceCavesLevel extends CavesLevel {
    {
        color1 = 0x739CAD;
        color2 = 0xCAE4EB;
        viewDistance = 6;
    }

    @Override
    public String tilesTex() {
        return Difficulties.is3d ? Assets.TILES_ICE_CAVES_3D : Assets.TILES_ICE_CAVES;
    }

    @Override
    public String waterTex() {
        return Assets.WATER_ICE_CAVES;
    }

    @Override
    protected boolean[] grass() {
        return new boolean[LENGTH];
    }

    @Override
    protected void paintGrass() { }

    @Override
    protected void finishBuild() {
        super.finishBuild();
        if (feeling == Feeling.GRASS) feeling = Feeling.NONE;
        for (int cell = 0; cell < LENGTH; cell++) {
            if (map[cell] == Terrain.GRASS || map[cell] == Terrain.HIGH_GRASS) map[cell] = Terrain.EMPTY;
        }
        if (Dungeon.depth == IceCaves.FIRST) map[entrance] = Terrain.EMPTY_SP;
    }

    @Override
    protected void createItems() {
        super.createItems();
        if (Dungeon.depth != IceCaves.FIRST) return;
        try (Random.Scope random = RunSeed.scope(RunSeed.QUEST, Dungeon.levelKey())) {
            createProspector();
        }
    }

    private void createProspector() {
        PathFinder.buildDistanceMap(entrance, passable);
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if ((map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_DECO)
                    && cell != storage && cell != entrance && cell != exit
                    && PathFinder.distance[cell] != Integer.MAX_VALUE
                    && heaps.get(cell) == null && Actor.findChar(cell) == null) cells.add(cell);
        }
        cells.sort(Comparator.comparingInt(cell -> PathFinder.distance[cell]));
        Prospector prospector = new Prospector();
        prospector.pos = cells.remove(0);
        prospector.job = Random.Int(2);
        mobs.add(prospector);
        Actor.occupyCell(prospector);

        if (prospector.job == Prospector.TORCHES) {
            int cell = cells.get(Random.Int(cells.size() / 2, cells.size()));
            Torch torches = new Torch();
            torches.quantity(3);
            drop(torches, cell).type = Heap.Type.CHEST;
        } else {
            int anchor = cells.get(Random.Int(cells.size() / 2, cells.size()));
            cells.sort(Comparator.comparingInt(cell -> distance(cell, anchor)));
            for (int i = 0; i < 3; i++) {
                Iceclaw claw = new Iceclaw();
                claw.pos = cells.get(i);
                mobs.add(claw);
                Actor.occupyCell(claw);
            }
        }
    }

    @Override
    public void addVisuals(Scene scene) { }

    @Override
    public String tileName(int tile) {
        if (tile == Terrain.WATER) return Messages.get(IceCavesLevel.class, "water_name");
        return super.tileName(tile);
    }

    @Override
    public String tileDesc(int tile) {
        if (tile == Terrain.WATER) return Messages.get(IceCavesLevel.class, "water_desc");
        if (tile == Terrain.WALL_DECO) return Messages.get(IceCavesLevel.class, "wall_desc");
        return super.tileDesc(tile);
    }
}
