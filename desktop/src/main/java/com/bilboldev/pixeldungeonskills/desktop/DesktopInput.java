package com.bilboldev.pixeldungeonskills.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.desktop.support.CompositeControllerListener;
import com.badlogic.gdx.graphics.Cursor;
import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.input.Keys;
import com.bilboldev.input.PartyControls;
import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Window;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import org.lwjgl.glfw.GLFW;

public final class DesktopInput extends InputAdapter implements ControllerListener {

    static IntPredicate debugKeys = key -> false;
    private static final int PRIMARY_POINTER = 19;
    private static final int SECONDARY_POINTER = 18;
    private static final int STICK_SOURCE = -1;
    private static final int CONTROLLER_SOURCE = 10000;

    private static final int LEFT_TRIGGER_AXIS = 4;
    private static final int RIGHT_TRIGGER_AXIS = 5;

    private final DesktopBindings bindings;
    private final MovementRepeater movement = new MovementRepeater();
    private final Set<Integer> keysDown = new HashSet<Integer>();
    private final Set<Integer> buttonsDown = new HashSet<Integer>();
    private final Map<Integer, GameAction> activeKeys = new HashMap<Integer, GameAction>();
    private final Map<Integer, GameAction> activeButtons = new HashMap<Integer, GameAction>();
    private Controller controller;
    private CompositeControllerListener controllerListeners;
    private boolean focused = true;
    private boolean minimized, windowAvailable = true;
    private boolean captureWasActive;
    private float leftX, leftY, rightX, rightY;
    private boolean leftTrigger, rightTrigger;
    private boolean requireLeftNeutral, requireRightNeutral;
    private enum ControllerMode {
        NONE, LOOK, CURSOR
    }
    private ControllerMode controllerMode = ControllerMode.NONE;
    private boolean controllerActive;

    private final Set<Integer> cursorButtons = new HashSet<Integer>();
    private boolean virtualPointer;
    private float pointerX, pointerY;
    private Image cursorImage;
    private Window windowContext;
    private Object selectorContext;
    private long contextVersion = -1;
    private float scrollDelay;
    private Object perspectiveContext;
    private boolean thirdPersonContext;
    private boolean mouseLook, mouseLookFailed, ignoreMouseMotion, discardFocusClick;
    private boolean awaitingLookClick;
    private boolean freeAimCursor;
    private final double[] windowMouseX = new double[1], windowMouseY = new double[1];
    private final int[] windowWidth = new int[1], windowHeight = new int[1];
    private BooleanSupplier windowContainsPointer = this::nativeWindowContainsPointer;
    private BooleanSupplier windowForeground = this::nativeWindowForeground;
    private int lastLookX, lastLookY, freeMouseX, freeMouseY;
    private final Set<Integer> mouseButtonsDown = new HashSet<Integer>();
    private final Set<Integer> ignoredMouseButtons = new HashSet<Integer>();

    public DesktopInput() {
        bindings = new DesktopBindings(Gdx.app.getPreferences("the-spd-3d-controls"));
        ControlSettings.backend = bindings;
        GameActions.enabled = true;
        try {
            Controllers.addListener(this);

            for (ControllerListener listener : Controllers.getListeners()) {
                if (listener instanceof CompositeControllerListener) {
                    controllerListeners = (CompositeControllerListener) listener;
                    break;
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            Gdx.app.error("DesktopInput", "Controller initialization failed; mouse and keyboard remain available.", failure);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        syncContext();
        if (inputAvailable() && PartyPrompts.editing() != null) {
            keysDown.add(keycode);
            editKey(keycode);
            return true;
        }
        if (!inputAvailable() || !keysDown.add(keycode)) return true;
        if (bindings.capture(keycode, false)) return true;
        if (debugKeys.test(keycode)) {
            movement.clear();
            return true;
        }
        if (isAlt(keycode) && GameScene.isPerspectiveView()) {
            movement.clear();
            syncContext();
            return true;
        }
        GameAction action = bindings.actionFor(keycode, false);
        if (action != null) {
            if (!PartyControls.keyboard()) return true;
            syncContext();
            activeKeys.put(keycode, action);
            pressAction(action, keycode);
        } else {
            Keys.processKeyEvent(keycode, true);
        }
        syncContext();
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keysDown.remove(keycode);
        if (PartyPrompts.editing() != null) return true;
        GameAction action = activeKeys.remove(keycode);
        if (action != null) releaseAction(action, keycode);
        else Keys.processKeyEvent(keycode, false);
        syncContext();
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        PartyPrompts.Input input = PartyPrompts.editing();
        if (!inputAvailable() || input == null) return false;
        if (!controlDown() && character >= ' ' && character <= '~') input.insert(String.valueOf(character));
        return true;
    }

    private boolean controlDown() {
        return keysDown.contains(Input.Keys.CONTROL_LEFT) || keysDown.contains(Input.Keys.CONTROL_RIGHT)
                || keysDown.contains(Input.Keys.SYM);
    }

    private void editKey(int keycode) {
        PartyPrompts.Input input = PartyPrompts.editing();
        boolean shift = keysDown.contains(Input.Keys.SHIFT_LEFT) || keysDown.contains(Input.Keys.SHIFT_RIGHT);
        if (controlDown()) {
            switch (keycode) {
                case Input.Keys.A:
                    input.selectAll();
                    break;
                case Input.Keys.C:
                    PartyPrompts.copy(input.selection());
                    break;
                case Input.Keys.X:
                    PartyPrompts.copy(input.selection());
                    input.insert("");
                    break;
                case Input.Keys.V:
                    PartyPrompts.paste();
                    break;
            }
            return;
        }
        switch (keycode) {
            case Input.Keys.DEL:
                input.erase(true);
                break;
            case Input.Keys.FORWARD_DEL:
                input.erase(false);
                break;
            case Input.Keys.LEFT:
                input.move(input.cursor() - 1, shift);
                break;
            case Input.Keys.RIGHT:
                input.move(input.cursor() + 1, shift);
                break;
            case Input.Keys.HOME:
                input.move(0, shift);
                break;
            case Input.Keys.END:
                input.move(input.value().length(), shift);
                break;
            case Input.Keys.TAB:
                input.next(shift);
                break;
            case Input.Keys.ENTER:
            case Input.Keys.NUMPAD_ENTER:
                input.accept();
                break;
            case Input.Keys.ESCAPE:
                input.cancel();
                break;
        }
    }

    private void pressAction(GameAction action, int source) {
        if (action.isMovement()) {
            if (PartyControls.keyboardCursor()) {
                showPointer();
                return;
            }

            if (Window.topWindow() == null && GameActions.movementContext() != null) movement.press(source, action);
        } else if (action == GameAction.POINTER_PRIMARY || action == GameAction.POINTER_SECONDARY) {
            if (source >= 0 && source < CONTROLLER_SOURCE && controllerMode != ControllerMode.NONE) {
                useMouse();
                syncContext();
            }
            boolean centreAim = (source >= CONTROLLER_SOURCE && controllerMode == ControllerMode.LOOK)
                    || (mouseLook && source >= 0 && source < CONTROLLER_SOURCE && GameScene.isPerspectiveTargeting());
            if (!centreAim) showPointer();
            int id = action == GameAction.POINTER_PRIMARY ? PRIMARY_POINTER : SECONDARY_POINTER;
            if (!Touchscreen.pointers.containsKey(id)) {
                Touchscreen.processTouchDown(id, centreAim ? Game.width / 2f : pointerX,
                        centreAim ? Game.height / 2f : pointerY, action == GameAction.POINTER_SECONDARY);
            }
        } else if (action == GameAction.ZOOM_IN || action == GameAction.ZOOM_OUT) {
            scroll(action == GameAction.ZOOM_IN ? -1 : 1);
            scrollDelay = 0.3f;
        } else {
            movement.clear();
            requireLeftNeutral = Math.abs(leftX) >= 0.25f || Math.abs(leftY) >= 0.25f;
            GameActions.perform(action);
        }
    }

    private void releaseAction(GameAction action, int source) {
        if (action.isMovement()) movement.release(source);
        else if (!activeKeys.containsValue(action) && !activeButtons.containsValue(action)) {
            if (action == GameAction.POINTER_PRIMARY) Touchscreen.processTouchUp(PRIMARY_POINTER);
            else if (action == GameAction.POINTER_SECONDARY) Touchscreen.processTouchUp(SECONDARY_POINTER);
        }
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        boolean wasLooking = mouseLook;
        syncContext();
        if (!inputAvailable()) return true;
        if (!PartyControls.pointer()) {
            ignoredMouseButtons.add(button);
            return true;
        }
        syncContext();
        useMouse();
        if (awaitingLookClick && canCaptureLook() && inside(x, y, Game.width, Game.height)) {

            awaitingLookClick = false;
            movement.clear();
            setMouseLook(true);
            mouseButtonsDown.add(button);
            ignoredMouseButtons.add(button);
            return true;
        }
        syncMouseLook();
        mouseButtonsDown.add(button);
        if (discardFocusClick || (!wasLooking && mouseLook)) {
            ignoredMouseButtons.add(button);
            return true;
        }
        movement.clear();
        if (mouseLook) {
            x = Game.width / 2;
            y = Game.height / 2;
        }
        Touchscreen.processTouchDown(pointer, x, y, button == Input.Buttons.RIGHT);
        syncContext();
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        mouseMotion(x, y);
        if (inputAvailable() && !mouseLook) Touchscreen.processTouchMove(pointer, x, y);
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        mouseButtonsDown.remove(button);
        syncWindowActivity();
        if (ignoredMouseButtons.remove(button) || !inputAvailable()) {
            Touchscreen.cancelTouch(pointer);
            syncContext();
            return true;
        }
        useMouse();
        Touchscreen.processTouchUp(pointer);
        syncContext();
        return true;
    }

    @Override
    public boolean touchCancelled(int x, int y, int pointer, int button) {
        mouseButtonsDown.remove(button);
        ignoredMouseButtons.remove(button);
        Touchscreen.cancelTouch(pointer);
        syncContext();
        return true;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return mouseMotion(x, y);
    }

    private boolean mouseMotion(int x, int y) {
        syncContext();
        if (!inputAvailable()) return false;
        if (PartyControls.keyboardCursor()) return true;

        if (ignoreMouseMotion) {
            ignoreMouseMotion = false;
            lastLookX = x;
            lastLookY = y;
            return mouseLook;
        }
        useMouse();
        syncContext();
        if (!ignoreMouseMotion && mouseLook) {
            double sensitivity = 0.0007 * bindings.mouseLookSensitivity();
            GameScene.lookAround(((double)x - lastLookX) * sensitivity, ((double)y - lastLookY) * sensitivity);
        }
        ignoreMouseMotion = false;
        lastLookX = x;
        lastLookY = y;
        return mouseLook;
    }

    private static boolean isAlt(int key) {
        return key == Input.Keys.ALT_LEFT || key == Input.Keys.ALT_RIGHT;
    }

    private boolean canCaptureLook() {
        return inputAvailable() && !mouseLookFailed && GameScene.canLookAround() && !bindings.isCapturing()
                && !controllerActive
                && !virtualPointer && !keysDown.contains(Input.Keys.ALT_LEFT) && !keysDown.contains(Input.Keys.ALT_RIGHT);
    }

    private void syncMouseLook() {
        boolean allowed = canCaptureLook();

        if (allowed && !mouseLook && !awaitingLookClick) {
            try {
                if (!windowContainsPointer.getAsBoolean()) awaitingLookClick = true;
            } catch (RuntimeException | LinkageError failure) {
                mouseLookFailed = true;
                allowed = false;
                Gdx.app.error("DesktopInput", "Mouse position unavailable; perspective views use drag-to-look.", failure);
            }
        }
        boolean wanted = allowed && !awaitingLookClick && (mouseLook || mouseButtonsDown.isEmpty());
        setMouseLook(wanted);
        syncAimCursor();
        GameScene.perspectiveControllerState(controllerMode != ControllerMode.NONE, controllerMode == ControllerMode.LOOK);
        GameScene.perspectiveMouseState(mouseLook, !mouseLookFailed, awaitingLookClick);
    }

    private void syncAimCursor() {
        boolean wanted = inputAvailable() && controllerMode == ControllerMode.NONE
                && !mouseLook && !virtualPointer && GameScene.isPerspectiveTargeting();
        if (wanted == freeAimCursor) return;
        freeAimCursor = wanted;
        if (!virtualPointer) Gdx.graphics.setSystemCursor(wanted ? Cursor.SystemCursor.None : Cursor.SystemCursor.Arrow);
    }

    private void restoreAimCursor() {
        if (freeAimCursor) {
            freeAimCursor = false;
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }

    private static boolean inside(double x, double y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private boolean nativeWindowContainsPointer() {
        if (!(Gdx.graphics instanceof Lwjgl3Graphics)) return inside(Gdx.input.getX(), Gdx.input.getY(), Game.width, Game.height);
        long handle = ((Lwjgl3Graphics)Gdx.graphics).getWindow().getWindowHandle();
        if (handle == 0) return false;
        GLFW.glfwGetCursorPos(handle, windowMouseX, windowMouseY);
        GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);
        return inside(windowMouseX[0], windowMouseY[0], windowWidth[0], windowHeight[0]);
    }

    private boolean nativeWindowForeground() {
        if (!(Gdx.graphics instanceof Lwjgl3Graphics)) return focused && !minimized;
        long handle = ((Lwjgl3Graphics)Gdx.graphics).getWindow().getWindowHandle();
        return handle != 0 && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE
                && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_FALSE
                && GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
    }

    private boolean inputAvailable() {
        return focused && !minimized && windowAvailable;
    }

    private void syncWindowActivity() {

        boolean available = true;
        if (GameScene.isPerspectiveView()) {
            available = focused && !minimized;
            if (available && !mouseLookFailed) try {
                available = windowForeground.getAsBoolean();
            } catch (RuntimeException | LinkageError failure) {
                mouseLookFailed = true;
                Gdx.app.error("DesktopInput", "Window state unavailable; perspective views use drag-to-look.", failure);
            }
        }
        if (windowAvailable != available) {
            windowAvailable = available;
            if (!available) {
                awaitingLookClick = true;
                resetState();
                bindings.cancelRebind();
            } else {

                reconcileReleasedKeys();
                reconcileController();
            }
        }
    }

    private void setMouseLook(boolean value) {
        try {

            boolean nativeCaptured = Gdx.input.isCursorCatched();
            if (value == mouseLook && (value ? nativeCaptured
                    : !nativeCaptured || (perspectiveContext == null && !GameScene.isPerspectiveView()))) return;
            Touchscreen.cancelAllTouches();
            ignoreMouseMotion = true;
            if (value && !mouseLook) {

                restoreAimCursor();
                freeMouseX = (int)clamp(Gdx.input.getX(), 0, Math.max(0, Game.width - 1));
                freeMouseY = (int)clamp(Gdx.input.getY(), 0, Math.max(0, Game.height - 1));
            }
            if (!value) restoreAimCursor();
            Gdx.input.setCursorCatched(value);
            if (value && !Gdx.input.isCursorCatched()) throw new IllegalStateException("Native mouse capture was not acquired");
            mouseLook = value;
            if (!value) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                if (inputAvailable()) Gdx.input.setCursorPosition(
                        (int)clamp(freeMouseX, 0, Math.max(0, Game.width - 1)),
                        (int)clamp(freeMouseY, 0, Math.max(0, Game.height - 1)));
            }
        } catch (RuntimeException | LinkageError failure) {
            mouseLook = false;
            mouseLookFailed = true;
            freeAimCursor = false;
            try {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                Gdx.input.setCursorCatched(false);
            }
            catch (RuntimeException | LinkageError ignored) {   }
            Gdx.app.error("DesktopInput", "Mouse capture unavailable; perspective views use drag-to-look.", failure);
        }
        GameScene.perspectiveMouseState(mouseLook, !mouseLookFailed, awaitingLookClick);
    }

    private void useMouse() {
        if (controllerMode != ControllerMode.NONE) {
            for (Map.Entry<Integer, GameAction> held : activeButtons.entrySet()) {
                if (held.getValue().isMovement()) movement.release(CONTROLLER_SOURCE + held.getKey());
            }
            activeButtons.clear();
        }
        controllerActive = false;
        cursorButtons.clear();
        syncControllerMode();
        if (virtualPointer) hidePointer();
        cancelControllerTouches();
        if (Math.abs(rightX) > 0.2f || Math.abs(rightY) > 0.2f) requireRightNeutral = true;
        if (Math.abs(leftX) > 0.2f || Math.abs(leftY) > 0.2f) {
            requireLeftNeutral = true;
            movement.cancel(STICK_SOURCE);
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        syncContext();
        if (!inputAvailable()) return false;
        if (!PartyControls.pointer()) return true;
        syncContext();
        useMouse();
        return scroll(amountY);
    }

    private boolean scroll(float amountY) {
        int x = virtualPointer ? (int)pointerX : Gdx.input.getX();
        int y = virtualPointer ? (int)pointerY : Gdx.input.getY();
        if (ScrollPane.processMouseWheel(x, y, amountY)) return true;
        return amountY != 0 && GameScene.zoomByAmount(-Math.signum(amountY));
    }

    public void update(float elapsed) {
        syncContext();
        if (!inputAvailable()) return;
        discardFocusClick = false;
        boolean capturing = bindings.isCapturing();
        if (capturing != captureWasActive) {
            clearActions();
            captureWasActive = capturing;
        }
        float dt = Math.min(0.1f, Math.max(0, elapsed));
        if (Math.abs(rightX) < 0.2f && Math.abs(rightY) < 0.2f) requireRightNeutral = false;
        if (!requireRightNeutral && (Math.abs(rightX) >= 0.2f || Math.abs(rightY) >= 0.2f)) {
            if (controllerMode == ControllerMode.LOOK) {
                double speed = 0.5 * bindings.controllerLookSensitivity();
                GameScene.lookAround(lookAxis(rightX) * speed * dt, lookAxis(rightY) * speed * dt * 0.65);
            } else {
                showPointer();
                float speed = 100f * bindings.pointerSensitivity() * Math.max(1, PixelScene.defaultZoom) / 3f;
                pointerX = clamp(pointerX + curved(rightX) * speed * dt, 0, Game.width - 1);
                pointerY = clamp(pointerY + curved(rightY) * speed * dt, 0, Game.height - 1);
                if (Touchscreen.pointers.containsKey(PRIMARY_POINTER)) Touchscreen.processTouchMove(PRIMARY_POINTER, pointerX, pointerY);
                if (Touchscreen.pointers.containsKey(SECONDARY_POINTER)) Touchscreen.processTouchMove(SECONDARY_POINTER, pointerX, pointerY);
            }
        }
        if (capturing) return;
        if (PartyControls.keyboardCursor()) {
            int dx = 0, dy = 0;
            for (GameAction action : activeKeys.values()) {
                if (action.isMovement()) {
                    dx += action.dx;
                    dy += action.dy;
                }
            }
            if (dx != 0 || dy != 0) {
                showPointer();
                float speed = 90f * Math.max(1, PixelScene.defaultZoom);
                pointerX = clamp(pointerX + Integer.signum(dx) * speed * dt, 0, Game.width - 1);
                pointerY = clamp(pointerY + Integer.signum(dy) * speed * dt, 0, Game.height - 1);
                if (Touchscreen.pointers.containsKey(PRIMARY_POINTER)) Touchscreen.processTouchMove(PRIMARY_POINTER, pointerX, pointerY);
                if (Touchscreen.pointers.containsKey(SECONDARY_POINTER)) Touchscreen.processTouchMove(SECONDARY_POINTER, pointerX, pointerY);
            }
        }
        if (Math.abs(leftX) < 0.25f && Math.abs(leftY) < 0.25f) requireLeftNeutral = false;
        GameAction direction = requireLeftNeutral ? null : MovementRepeater.stickDirection(leftX, leftY);
        if (direction == null) movement.release(STICK_SOURCE);
        else if (windowContext == null && selectorContext != null) movement.press(STICK_SOURCE, direction);
        int[] step = movement.poll(dt, GameActions.canMove());
        if (step != null) {
            int[] world = GameScene.perspectiveStep(step[0], step[1]);
            GameActions.move(world[0], world[1]);
        }
        scrollDelay -= dt;
        if (scrollDelay <= 0) {
            int amount = heldScroll(activeKeys) + heldScroll(activeButtons);
            if (amount != 0 && Window.topWindow() != null) scroll(Math.signum(amount));
            scrollDelay = 0.12f;
        }
        syncContext();
    }

    private static int heldScroll(Map<Integer, GameAction> actions) {
        int result = 0;
        for (GameAction action : actions.values()) {
            if (action == GameAction.ZOOM_IN) result--;
            else if (action == GameAction.ZOOM_OUT) result++;
        }
        return Integer.signum(result);
    }

    private void syncContext() {
        syncWindowActivity();
        Window window = Window.topWindow();
        Object selector = GameActions.movementContext();
        long version = GameActions.contextVersion();
        Object view = GameScene.perspectiveContext();
        boolean third = GameScene.isThirdPerson();
        boolean viewChanged = perspectiveContext != view || thirdPersonContext != third;
        if (view == null) awaitingLookClick = false;
        if (viewChanged) {
            setMouseLook(false);
            Touchscreen.cancelAllTouches();
            if (perspectiveContext != view) cursorButtons.clear();
            requireRightNeutral = Math.abs(rightX) >= 0.2f || Math.abs(rightY) >= 0.2f;
            perspectiveContext = view;
            thirdPersonContext = third;
        }
        if (contextVersion != version || viewChanged) {

            if (view != null && selectorContext != selector) Touchscreen.cancelAllTouches();
            clearActions();
            contextVersion = version;
            windowContext = window;
            selectorContext = selector;
        }
        syncControllerMode();
        syncMouseLook();
    }

    private void syncControllerMode() {
        ControllerMode wanted = !controllerActive || !inputAvailable() || !GameScene.isPerspectiveView()
                ? ControllerMode.NONE : GameActions.enabled && GameScene.canLookAround()
                && !bindings.isCapturing() && cursorButtons.isEmpty() ? ControllerMode.LOOK : ControllerMode.CURSOR;
        if (wanted == controllerMode) return;
        ControllerMode previous = controllerMode;
        controllerMode = wanted;

        cancelControllerTouches();
        activeButtons.values().removeIf(action -> action == GameAction.POINTER_PRIMARY || action == GameAction.POINTER_SECONDARY);
        if (previous == ControllerMode.NONE && wanted != ControllerMode.NONE) {
            activeKeys.values().removeIf(action -> action == GameAction.POINTER_PRIMARY || action == GameAction.POINTER_SECONDARY);
            Touchscreen.cancelAllTouches();
            ignoredMouseButtons.addAll(mouseButtonsDown);
        }
        if (previous != ControllerMode.NONE && wanted != ControllerMode.NONE) {
            movement.clear();
            requireLeftNeutral = Math.abs(leftX) >= 0.25f || Math.abs(leftY) >= 0.25f;
            requireRightNeutral = Math.abs(rightX) >= 0.2f || Math.abs(rightY) >= 0.2f;
        }
        if (wanted == ControllerMode.LOOK) {
            if (previous == ControllerMode.NONE && !virtualPointer) {
                pointerX = Game.width / 2f;
                pointerY = Game.height / 2f;
            }
            setMouseLook(false);
            hidePointer();
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        } else if (wanted == ControllerMode.CURSOR) {
            showPointer(previous == ControllerMode.LOOK);
        } else {
            hidePointer();
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            if (controllerActive && inputAvailable() && !GameScene.isPerspectiveView()) showPointer(true);
        }
    }

    private void clearActions() {
        movement.clear();
        activeKeys.clear();
        activeButtons.clear();
        cancelControllerTouches();
        requireLeftNeutral = Math.abs(leftX) >= 0.25f || Math.abs(leftY) >= 0.25f;
    }

    public void resetState() {
        restoreAimCursor();
        setMouseLook(false);
        clearActions();
        for (Integer key : keysDown) Keys.processKeyEvent(key, false);
        Touchscreen.cancelAllTouches();
        cursorButtons.clear();
        controllerMode = ControllerMode.NONE;
        hidePointer();
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        mouseButtonsDown.clear();
        ignoredMouseButtons.clear();
        requireRightNeutral = Math.abs(rightX) >= 0.2f || Math.abs(rightY) >= 0.2f;
    }

    public void setFocused(boolean value) {
        if (GameScene.isPerspectiveView()) awaitingLookClick = true;
        focused = value;
        resetState();
        if (!value) bindings.cancelRebind();
        else {
            discardFocusClick = true;
            reconcileReleasedKeys();
            reconcileController();
        }
    }

    public void setMinimized(boolean value) {
        minimized = value;
        if (value) {
            if (GameScene.isPerspectiveView()) awaitingLookClick = true;
            resetState();
            bindings.cancelRebind();
        }
    }

    public void windowChanged() {
        if (GameScene.isPerspectiveView()) awaitingLookClick = true;
        resetState();
    }

    private void reconcileReleasedKeys() {

        for (Integer key : new HashSet<Integer>(keysDown)) {
            if (!Gdx.input.isKeyPressed(key)) keysDown.remove(key);
        }
    }

    private void reconcileController() {
        buttonsDown.clear();
        if (controller == null) return;
        try {
            ControllerMapping m = controller.getMapping();
            leftX = controller.getAxis(m.axisLeftX);
            leftY = controller.getAxis(m.axisLeftY);
            rightX = controller.getAxis(m.axisRightX);
            rightY = controller.getAxis(m.axisRightY);
            requireLeftNeutral = Math.abs(leftX) >= 0.25f || Math.abs(leftY) >= 0.25f;
            requireRightNeutral = Math.abs(rightX) >= 0.2f || Math.abs(rightY) >= 0.2f;
            int[] buttons = {m.buttonA, m.buttonB, m.buttonX, m.buttonY, m.buttonStart, m.buttonBack,
                    m.buttonL1, m.buttonR1, m.buttonL2, m.buttonR2, m.buttonLeftStick, m.buttonRightStick,
                    m.buttonDpadUp, m.buttonDpadDown, m.buttonDpadLeft, m.buttonDpadRight};
            for (int button : buttons) {
                if (button >= 0 && controller.getButton(button)) buttonsDown.add(buttonToKey(m, button));
            }
            leftTrigger = controller.getAxis(LEFT_TRIGGER_AXIS) >= 0.35f;
            rightTrigger = controller.getAxis(RIGHT_TRIGGER_AXIS) >= 0.35f;
            if (leftTrigger) buttonsDown.add(Input.Keys.BUTTON_L2);
            if (rightTrigger) buttonsDown.add(Input.Keys.BUTTON_R2);
        } catch (RuntimeException disconnected) {
            buttonsDown.clear();
            cursorButtons.clear();
            controller = null;
            controllerActive = false;
            leftX = leftY = rightX = rightY = 0;
            leftTrigger = rightTrigger = false;
        }
    }

    private void showPointer() {
        showPointer(false);
    }

    private void showPointer(boolean preservePosition) {
        if (virtualPointer) return;
        setMouseLook(false);
        pointerX = clamp(preservePosition ? pointerX : Gdx.input.getX(), 0, Game.width - 1);
        pointerY = clamp(preservePosition ? pointerY : Gdx.input.getY(), 0, Game.height - 1);

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        virtualPointer = true;
    }

    private void hidePointer() {
        if (virtualPointer) {
            virtualPointer = false;
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }

    private void cancelControllerTouches() {
        Touchscreen.cancelTouch(PRIMARY_POINTER);
        Touchscreen.cancelTouch(SECONDARY_POINTER);
    }

    public void drawCursor() {

        syncContext();
        if (Game.scene() instanceof TitleScene) {
            ((TitleScene)Game.scene()).pointerMoved(
                    !inputAvailable() ? -1 : virtualPointer ? pointerX : Gdx.input.getX(),
                    !inputAvailable() ? -1 : virtualPointer ? pointerY : Gdx.input.getY());
        }
        if (freeAimCursor && PixelScene.uiCamera != null) {
            Gdx.gl.glScissor(0, 0, Game.width, Game.height);
            GameScene.drawPerspectiveTargetCursor(Gdx.input.getX(), Gdx.input.getY());
            if (!GameScene.isPerspectiveView()) syncContext();
            return;
        }
        if (!virtualPointer || !inputAvailable() || PixelScene.uiCamera == null) return;
        if (GameScene.isPerspectiveTargeting()) {
            Gdx.gl.glScissor(0, 0, Game.width, Game.height);
            GameScene.drawPerspectiveTargetCursor((int)pointerX, (int)pointerY);
            if (!GameScene.isPerspectiveView()) syncContext();
            return;
        }
        if (cursorImage == null) cursorImage = new Image("input/controller_cursor.png");
        Camera camera = PixelScene.uiCamera;
        cursorImage.camera = camera;
        cursorImage.x = (pointerX - camera.x) / camera.zoom + camera.scroll.x - cursorImage.width() / 2;
        cursorImage.y = (pointerY - camera.y) / camera.zoom + camera.scroll.y - cursorImage.height() / 2;
        Gdx.gl.glScissor(0, 0, Game.width, Game.height);
        cursorImage.draw();
    }

    @Override
    public void connected(Controller connected) {

    }

    @Override
    public void disconnected(Controller disconnected) {
        if (controller != disconnected) return;
        resetState();
        buttonsDown.clear();
        leftX = leftY = rightX = rightY = 0;
        leftTrigger = rightTrigger = false;
        controller = null;
        controllerActive = false;
        if (GameScene.isPerspectiveView()) awaitingLookClick = true;
    }

    private void useController(Controller next) {
        if (controller == next) return;
        clearActions();
        cursorButtons.clear();
        buttonsDown.clear();
        leftX = leftY = rightX = rightY = 0;
        leftTrigger = rightTrigger = false;
        requireLeftNeutral = requireRightNeutral = false;
        controller = next;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        syncContext();
        if (!inputAvailable()) return false;
        useController(controller);
        return controllerButton(buttonToKey(controller.getMapping(), buttonCode), true);
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (this.controller != controller) return false;
        return controllerButton(buttonToKey(controller.getMapping(), buttonCode), false);
    }

    private boolean controllerButton(int code, boolean down) {
        if (code == Input.Keys.UNKNOWN) return false;
        syncContext();
        if (down) {
            if (!buttonsDown.add(code)) return true;
            controllerActive = true;
            syncControllerMode();
            if (bindings.capture(code, true)) return true;
            GameAction action = bindings.actionFor(code, true);
            if (action == GameAction.POINTER_SECONDARY && GameScene.canLookAround()) {
                cursorButtons.add(code);
                syncContext();
                return true;
            }
            if (action != null) {
                if (!PartyControls.keyboard()) return true;
                syncContext();
                activeButtons.put(code, action);
                pressAction(action, CONTROLLER_SOURCE + code);
            }
        } else {
            buttonsDown.remove(code);
            cursorButtons.remove(code);
            GameAction action = activeButtons.remove(code);
            if (action != null) releaseAction(action, CONTROLLER_SOURCE + code);
        }
        syncContext();
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        syncWindowActivity();
        if (!inputAvailable() || Float.isNaN(value)) return false;
        ControllerMapping map = controller.getMapping();
        boolean trigger = axisCode == LEFT_TRIGGER_AXIS || axisCode == RIGHT_TRIGGER_AXIS;
        if (!trigger && axisCode != map.axisLeftX && axisCode != map.axisLeftY
                && axisCode != map.axisRightX && axisCode != map.axisRightY) return false;
        float activation = trigger ? 0.55f : (axisCode == map.axisLeftX || axisCode == map.axisLeftY ? 0.55f : 0.2f);
        if (this.controller != controller) {
            if (Math.abs(value) < activation) return false;
            useController(controller);
        }
        if (Math.abs(value) >= activation) {
            if (!PartyControls.keyboard()) return true;
            syncContext();
        }
        value = clamp(value, -1, 1);
        if (axisCode == map.axisLeftX) leftX = value;
        else if (axisCode == map.axisLeftY) leftY = value;
        else if (axisCode == map.axisRightX) rightX = value;
        else if (axisCode == map.axisRightY) rightY = value;
        else if (axisCode == LEFT_TRIGGER_AXIS) {
            boolean down = triggerPressed(value, leftTrigger);
            if (down != leftTrigger) {
                leftTrigger = down;
                controllerButton(Input.Keys.BUTTON_L2, down);
            }
        } else if (axisCode == RIGHT_TRIGGER_AXIS) {
            boolean down = triggerPressed(value, rightTrigger);
            if (down != rightTrigger) {
                rightTrigger = down;
                controllerButton(Input.Keys.BUTTON_R2, down);
            }
        }
        if (!trigger && Math.abs(value) >= activation
                && !(axisCode == map.axisRightX || axisCode == map.axisRightY ? requireRightNeutral : requireLeftNeutral)) {
            controllerActive = true;
            syncContext();
        }
        return true;
    }

    static boolean triggerPressed(float value, boolean wasPressed) {
        return wasPressed ? value > 0.35f : value >= 0.55f;
    }

    static int buttonToKey(ControllerMapping m, int button) {
        if (button < 0) return Input.Keys.UNKNOWN;
        if (button == m.buttonA) return Input.Keys.BUTTON_A;
        if (button == m.buttonB) return Input.Keys.BUTTON_B;
        if (button == m.buttonX) return Input.Keys.BUTTON_X;
        if (button == m.buttonY) return Input.Keys.BUTTON_Y;
        if (button == m.buttonStart) return Input.Keys.BUTTON_START;
        if (button == m.buttonBack) return Input.Keys.BUTTON_SELECT;
        if (button == m.buttonL1) return Input.Keys.BUTTON_L1;
        if (button == m.buttonR1) return Input.Keys.BUTTON_R1;
        if (button == m.buttonL2) return Input.Keys.BUTTON_L2;
        if (button == m.buttonR2) return Input.Keys.BUTTON_R2;
        if (button == m.buttonLeftStick) return Input.Keys.BUTTON_THUMBL;
        if (button == m.buttonRightStick) return Input.Keys.BUTTON_THUMBR;
        if (button == m.buttonDpadUp) return Input.Keys.DPAD_UP + 1000;
        if (button == m.buttonDpadDown) return Input.Keys.DPAD_DOWN + 1000;
        if (button == m.buttonDpadLeft) return Input.Keys.DPAD_LEFT + 1000;
        if (button == m.buttonDpadRight) return Input.Keys.DPAD_RIGHT + 1000;
        return Input.Keys.UNKNOWN;
    }

    private static float curved(float value) {
        return Math.abs(value) < 0.2f ? 0 : Math.signum(value) * (float)Math.pow(Math.abs(value), 1.5);
    }

    private static double lookAxis(float value) {

        return Math.abs(value) <= 0.2f ? 0 : Math.copySign(Math.pow((Math.abs(value) - 0.2) / 0.8, 1.5), value);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void dispose() {
        resetState();

        if (controllerListeners != null) {
            controllerListeners.removeListener(this);
            controllerListeners = null;
        }
        if (cursorImage != null) cursorImage.destroy();
        ControlSettings.backend = null;
        GameActions.enabled = false;
    }
}
