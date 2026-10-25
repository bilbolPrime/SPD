package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.windows.WndRebornChallenges;

public class ChallengeIcons extends Button {
    private final int challenges;
    private final float size;
    private int columns;
    private int count;

    public ChallengeIcons(int challenges, float size) {
        this(challenges, size, RebornChallenges.MASKS.length, false);
    }

    public ChallengeIcons(int challenges, float size, int columns, boolean showInactive) {
        this.challenges = challenges;
        this.size = size;
        this.columns = columns;
        for (int i = 0; i < RebornChallenges.MASKS.length; i++) {
            boolean active = (challenges & RebornChallenges.MASKS[i]) != 0;
            if (!active && !showInactive) continue;
            ItemSprite icon = new ItemSprite(RebornChallenges.icon(i), null);
            icon.scale.set(size / 16f);
            if (!active) icon.alpha(.4f);
            add(icon);
            count++;
        }
        setSize(Math.min(count, columns) * size, ((count + columns - 1) / columns) * size);
    }

    public void fitWidth(float available) {
        columns = Math.max(1, Math.min(count, (int)(available / size)));
        setSize(Math.min(count, columns) * size, ((count + columns - 1) / columns) * size);
    }

    @Override protected void layout() {
        super.layout();
        int index = 0;
        for (Gizmo child : members) {
            if (!(child instanceof ItemSprite)) continue;
            ItemSprite icon = (ItemSprite)child;
            icon.x = x + (index % columns) * size + (size - icon.width()) / 2;
            icon.y = y + (index / columns) * size + (size - icon.height()) / 2;
            index++;
        }
    }

    public void useScrollPaneInput() {
        Touchscreen.event.remove(hotArea);
    }

    public void open() {
        Game.scene().add(new WndRebornChallenges(challenges, null));
    }

    @Override protected void onClick() {
        open();
    }
}
