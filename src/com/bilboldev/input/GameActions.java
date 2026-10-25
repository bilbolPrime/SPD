package com.bilboldev.input;

import java.util.ArrayList;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndHero;
import com.bilboldev.pixeldungeonskills.windows.WndJournal;

public final class GameActions {

    public static boolean enabled = false;

    private static final ArrayList<Binding> bindings = new ArrayList<Binding>();
    private static Object lastScene;
    private static Object lastWindow;
    private static Object lastLevel;
    private static Object lastSelector;
    private static Object lastHero;
    private static boolean lastAvailable;
    private static long contextVersion;

    private GameActions() {
    }

    public static void bind(Button button, GameAction action, boolean alternate) {
        if (!enabled || button == null || action == null) return;
        for (int i = bindings.size() - 1; i >= 0; i--) {
            Binding binding = bindings.get(i);
            if (binding.button == button && binding.alternate == alternate) {
                bindings.remove(i);
            }
        }
        bindings.add(new Binding(button, action, alternate));
    }

    public static void unbind(Button button) {
        for (int i = bindings.size() - 1; i >= 0; i--) {
            if (bindings.get(i).button == button) bindings.remove(i);
        }
    }

    public static Button visibleButton(GameAction action) {
        if (!enabled || action == null) return null;
        for (int i = bindings.size() - 1; i >= 0; i--) {
            Binding binding = bindings.get(i);
            if (binding.action != action || binding.alternate) continue;
            for (Gizmo ancestor = binding.button; ancestor != null; ancestor = ancestor.parent) {
                if (!ancestor.exists || !ancestor.visible || ancestor instanceof Window) break;
                if (ancestor == Game.scene()) return binding.button;
            }
        }
        return null;
    }

    public static boolean perform(GameAction action) {
        if (!enabled || action == null) return false;
        if (action == GameAction.BACK) {
            Keys.processKeyEvent(Keys.BACK, true);
            Keys.processKeyEvent(Keys.BACK, false);
            return true;
        }
        if (action.isMovement()) return move(action.dx, action.dy);

        if (action == GameAction.VIEW_MODE) {
            Button button = visibleButton(action);
            return button != null && button.triggerAction(false);
        }
        if (!gameplayAvailable()) return false;

        if (action == GameAction.ZOOM_IN || action == GameAction.ZOOM_OUT) {
            return GameScene.zoomByAmount(action == GameAction.ZOOM_IN ? 1 : -1);
        }
        if (action == GameAction.HERO_INFO) {
            if (Dungeon.hero.sprite != null && !Dungeon.hero.sprite.isVisible()) {
                Camera.main.focusOn(Dungeon.hero.sprite);
            }
            GameScene.show(new WndHero());
            return true;
        }
        if (action == GameAction.JOURNAL) {
            GameScene.show(new WndJournal());
            return true;
        }

        boolean ready = LocalCoop.active() ? LocalCoop.acceptsInput() : Dungeon.hero.ready;
        if (action != GameAction.INVENTORY &&
                (!Dungeon.hero.isAlive() || !ready || Dungeon.hero.paralysed)) {
            return false;
        }
        if (action == GameAction.INVENTORY && Dungeon.hero.isAlive() && !ready) {
            return false;
        }
        for (int i = bindings.size() - 1; i >= 0; i--) {
            Binding binding = bindings.get(i);
            if (binding.action == action && binding.button.triggerAction(binding.alternate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canMove() {
        return enabled && gameplayAvailable() && Dungeon.hero.isAlive() &&
                (LocalCoop.active() ? LocalCoop.acceptsInput() : Dungeon.hero.ready)
                && !Dungeon.hero.paralysed && GameScene.canAcceptMovement();
    }

    public static boolean move(int dx, int dy) {
        if (!canMove() || (dx == 0 && dy == 0) || Math.abs(dx) > 1 || Math.abs(dy) > 1) {
            return false;
        }
        int x = Dungeon.hero.pos % Level.WIDTH + dx;
        int y = Dungeon.hero.pos / Level.WIDTH + dy;
        if (x < 0 || x >= Level.WIDTH || y < 0 || y >= Level.HEIGHT) return false;
        GameScene.handleCell(x + y * Level.WIDTH);
        return true;
    }

    private static boolean gameplayAvailable() {
        return Game.scene() instanceof GameScene && Dungeon.hero != null && Dungeon.level != null &&
                Window.topWindow() == null && GameScene.canAcceptGameplayInput();
    }

    public static Object movementContext() {
        return GameScene.canAcceptGameplayInput() ? GameScene.movementContext() : null;
    }

    public static long contextVersion() {
        Object scene = Game.scene();
        Object window = Window.topWindow();
        Object selector = movementContext();
        Object hero = LocalCoop.active() ? Dungeon.hero : null;
        boolean available = GameScene.canAcceptGameplayInput();
        if (scene != lastScene || window != lastWindow || Dungeon.level != lastLevel ||
                selector != lastSelector || hero != lastHero || available != lastAvailable) {
            lastScene = scene;
            lastWindow = window;
            lastLevel = Dungeon.level;
            lastSelector = selector;
            lastHero = hero;
            lastAvailable = available;
            contextVersion++;
        }
        return contextVersion;
    }

    private static final class Binding {
        final Button button;
        final GameAction action;
        final boolean alternate;

        Binding(Button button, GameAction action, boolean alternate) {
            this.button = button;
            this.action = action;
            this.alternate = alternate;
        }
    }
}
