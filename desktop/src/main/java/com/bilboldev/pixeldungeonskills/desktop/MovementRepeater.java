package com.bilboldev.pixeldungeonskills.desktop;

import com.bilboldev.input.GameAction;
import java.util.LinkedHashMap;
import java.util.Map;

final class MovementRepeater {
    private static final float CHORD_DELAY = 0.065f;
    private static final float REPEAT_DELAY = 0.14f;
    private final Map<Integer, GameAction> held = new LinkedHashMap<Integer, GameAction>();
    private float delay;
    private boolean stepped;
    private int[] tap;

    void press(int source, GameAction direction) {
        if (!direction.isMovement() || held.get(source) == direction) return;
        if (held.isEmpty()) {
            delay = CHORD_DELAY;
            stepped = false;
            tap = null;
        }
        held.put(source, direction);
    }

    void release(int source) {
        if (!held.containsKey(source)) return;
        if (!stepped && tap == null) tap = direction();
        held.remove(source);
    }

    void cancel(int source) {
        if (held.remove(source) != null) tap = null;
    }

    int[] poll(float elapsed, boolean ready) {
        delay -= Math.max(0, elapsed);
        if (!ready) {
            tap = null;
            stepped = true;
            return null;
        }
        if (held.isEmpty() && tap == null) return null;
        if (!held.isEmpty() && delay > 0) return null;
        int[] next = tap != null ? tap : direction();
        tap = null;
        stepped = true;
        delay = REPEAT_DELAY;
        return next[0] == 0 && next[1] == 0 ? null : next;
    }

    private int[] direction() {
        int x = 0, y = 0;
        for (GameAction action : held.values()) {
            x += action.dx;
            y += action.dy;
        }
        return new int[]{Integer.signum(x), Integer.signum(y)};
    }

    void clear() {
        held.clear();
        tap = null;
        delay = 0;
        stepped = false;
    }

    static GameAction stickDirection(float x, float y) {
        if (x * x + y * y < 0.55f * 0.55f) return null;
        int sector = (int)Math.round(Math.atan2(y, x) / (Math.PI / 4));
        GameAction[] directions = {GameAction.MOVE_E, GameAction.MOVE_SE, GameAction.MOVE_S,
                GameAction.MOVE_SW, GameAction.MOVE_W, GameAction.MOVE_NW,
                GameAction.MOVE_N, GameAction.MOVE_NE};
        return directions[(sector + 8) % 8];
    }
}
