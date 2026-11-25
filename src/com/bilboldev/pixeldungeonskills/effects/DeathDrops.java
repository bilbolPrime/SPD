package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Group;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import java.util.ArrayList;
import java.util.Collections;

public final class DeathDrops extends Group {
    public DeathDrops(int from, Iterable<Item> items) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int offset : Level.NEIGHBOURS8) {
            int cell = from + offset;
            if (Dungeon.level.insideMap(cell) && Level.adjacent(from, cell)
                    && Level.passable[cell] && !Level.pit[cell] && Dungeon.level.heaps.get(cell) == null)
                cells.add(cell);
        }
        Collections.shuffle(cells);
        int next = 0;
        for (Item item : items) {
            if (next == cells.size()) break;

            Heap appearance = new Heap();
            appearance.pos = cells.get(next++);
            appearance.items.add(item);
            ItemSprite sprite = new ItemSprite();
            sprite.link(appearance);
            add(sprite);
            sprite.drop(from);
        }
    }
}
