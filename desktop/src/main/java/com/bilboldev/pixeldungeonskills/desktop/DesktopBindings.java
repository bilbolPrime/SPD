package com.bilboldev.pixeldungeonskills.desktop;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.pixeldungeonskills.messages.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DesktopBindings implements ControlSettings.Backend {

    public static final int DPAD_KEY_OFFSET = 1000;
    public static final int DPAD_UP = Input.Keys.UP + DPAD_KEY_OFFSET;
    public static final int DPAD_DOWN = Input.Keys.DOWN + DPAD_KEY_OFFSET;
    public static final int DPAD_LEFT = Input.Keys.LEFT + DPAD_KEY_OFFSET;
    public static final int DPAD_RIGHT = Input.Keys.RIGHT + DPAD_KEY_OFFSET;

    private static final String SENSITIVITY = "pointerSensitivity";
    private static final String LOOK_SENSITIVITY = "mouseLookSensitivity";
    private static final String CONTROLLER_LOOK_SENSITIVITY = "controllerLookSensitivity";
    private static final String KEYBOARD_POINTER_DEFAULTS_VERSION = "keyboard.pointerDefaultsVersion";
    private final Preferences preferences;
    private final LinkedHashMap<Integer, GameAction> keyboard;
    private final LinkedHashMap<Integer, GameAction> controller;
    private GameAction capturing;
    private boolean capturingController;
    private String status = "";
    private int sensitivity;
    private int lookSensitivity;
    private int controllerLookSensitivity;

    public DesktopBindings(Preferences preferences) {
        this.preferences = preferences;
        keyboard = load(false);
        controller = load(true);
        try {
            sensitivity = clampSensitivity(preferences.getInteger(SENSITIVITY, 5));
        } catch (NumberFormatException | ClassCastException malformed) {
            sensitivity = 5;
        }
        try {
            lookSensitivity = clampSensitivity(preferences.getInteger(LOOK_SENSITIVITY, 5));
        } catch (NumberFormatException | ClassCastException malformed) {
            lookSensitivity = 5;
        }
        try {
            controllerLookSensitivity = clampSensitivity(preferences.getInteger(CONTROLLER_LOOK_SENSITIVITY, 5));
        } catch (NumberFormatException | ClassCastException malformed) {
            controllerLookSensitivity = 5;
        }
        migrateKeyboardPointerDefaults();
    }

    public GameAction actionFor(int code, boolean controllerInput) {
        return bindings(controllerInput).get(code);
    }

    @Override
    public String bindingName(GameAction action, boolean controllerInput) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, GameAction> binding : bindings(controllerInput).entrySet()) {
            if (binding.getValue() == action) {
                if (result.length() > 0) result.append(", ");
                result.append(inputName(binding.getKey(), controllerInput));
            }
        }
        return result.length() == 0 ? msg("unbound", "Unbound") : result.toString();
    }

    @Override
    public String keyboardKeyName(GameAction action) {
        for (Map.Entry<Integer, GameAction> binding : keyboard.entrySet()) {
            if (binding.getValue() != action) continue;
            int key = binding.getKey();
            if (key == Input.Keys.UP) return "^";
            if (key == Input.Keys.DOWN) return "v";
            if (key == Input.Keys.LEFT) return "<";
            if (key == Input.Keys.RIGHT) return ">";
            if (key == Input.Keys.ESCAPE) return "Esc";
            if (key == Input.Keys.SHIFT_LEFT) return "LShift";
            if (key == Input.Keys.SHIFT_RIGHT) return "RShift";
            if (key == Input.Keys.CONTROL_LEFT) return "LCtrl";
            if (key == Input.Keys.CONTROL_RIGHT) return "RCtrl";
            if (key == Input.Keys.PAGE_UP) return "PgUp";
            if (key == Input.Keys.PAGE_DOWN) return "PgDn";
            if (key == Input.Keys.BACKSPACE) return "Bksp";
            if (key == Input.Keys.FORWARD_DEL) return "Del";
            if (key == Input.Keys.NUMPAD_ENTER) return "NEnter";
            if (key >= Input.Keys.NUMPAD_0 && key <= Input.Keys.NUMPAD_9) return "N" + (key - Input.Keys.NUMPAD_0);
            return inputName(key, false);
        }
        return "";
    }

    @Override
    public void beginRebind(GameAction action, boolean controllerInput) {
        if (action == null) return;
        capturing = action;
        capturingController = controllerInput;
        status = controllerInput ? msg("capture_button", "Press a controller button for %s. Escape or Start / Options cancels.", actionLabel(action))
                : msg("capture_key", "Press a key for %s. Escape cancels.", actionLabel(action));
    }

    public boolean capture(int code, boolean controllerInput) {
        if (capturing == null) return false;
        if ((!controllerInput && code == Input.Keys.ESCAPE)
                || (controllerInput && code == Input.Keys.BUTTON_START)) {
            cancelRebind();
            return true;
        }
        if (controllerInput != capturingController || !validCode(code, controllerInput)) return true;

        LinkedHashMap<Integer, GameAction> bindings = bindings(controllerInput);
        GameAction previous = bindings.get(code);
        if (previous != null && previous != capturing && essential(previous, controllerInput)
                && count(bindings, previous) <= 1) {
            status = msg("capture_protected", "Keep a binding for %s. Choose another key or button. Escape or Start / Options cancels.", actionLabel(previous));
            return true;
        }

        GameAction assigned = capturing;
        removeAction(bindings, assigned);
        bindings.put(code, assigned);

        if (!controllerInput) bindings.put(Input.Keys.ESCAPE, GameAction.BACK);
        save(controllerInput);
        capturing = null;
        status = msg("capture_assigned", "%1$s assigned to %2$s.", inputName(code, controllerInput), actionLabel(assigned));
        if (previous != null && previous != assigned) {
            status += " " + msg("capture_removed", "Removed from %s.", actionLabel(previous));
        }
        return true;
    }

    @Override
    public boolean isCapturing() {
        return capturing != null;
    }

    @Override
    public String captureStatus() {
        return status;
    }

    @Override
    public void cancelRebind() {
        capturing = null;
        status = msg("capture_cancelled", "Binding unchanged.");
    }

    @Override
    public void resetBindings(boolean controllerInput) {
        cancelRebind();
        LinkedHashMap<Integer, GameAction> bindings = bindings(controllerInput);
        bindings.clear();
        bindings.putAll(defaults(controllerInput));
        save(controllerInput);
        status = controllerInput ? msg("reset_controller", "Controller defaults restored.")
                : msg("reset_keyboard", "Keyboard defaults restored.");
    }

    @Override
    public int pointerSensitivity() {
        return sensitivity;
    }

    @Override
    public void pointerSensitivity(int value) {
        sensitivity = clampSensitivity(value);
        preferences.putInteger(SENSITIVITY, sensitivity).flush();
    }

    @Override
    public int mouseLookSensitivity() {
        return lookSensitivity;
    }

    @Override
    public void mouseLookSensitivity(int value) {
        lookSensitivity = clampSensitivity(value);
        preferences.putInteger(LOOK_SENSITIVITY, lookSensitivity).flush();
    }

    @Override
    public int controllerLookSensitivity() {
        return controllerLookSensitivity;
    }

    @Override
    public void controllerLookSensitivity(int value) {
        controllerLookSensitivity = clampSensitivity(value);
        preferences.putInteger(CONTROLLER_LOOK_SENSITIVITY, controllerLookSensitivity).flush();
    }

    private LinkedHashMap<Integer, GameAction> bindings(boolean controllerInput) {
        return controllerInput ? controller : keyboard;
    }

    private LinkedHashMap<Integer, GameAction> load(boolean controllerInput) {
        LinkedHashMap<Integer, GameAction> defaults = defaults(controllerInput);
        LinkedHashMap<Integer, GameAction> result = new LinkedHashMap<>();
        ArrayList<GameAction> newShortcuts = new ArrayList<>();
        for (GameAction action : GameAction.values()) {
            String key = preferenceKey(action, controllerInput);
            String value;
            try {
                value = preferences.getString(key, null);
            } catch (ClassCastException malformed) {
                value = null;
            }
            if (value == null) {
                if (!controllerInput && (action == GameAction.VIEW_MODE || action == GameAction.RAT_KING
                        || action.name().startsWith("QUICKSLOT_"))) {
                    newShortcuts.add(action);
                    continue;
                }
                for (Map.Entry<Integer, GameAction> entry : defaults.entrySet()) {
                    if (entry.getValue() == action
                            && (controllerInput || !isPointerAction(action) || !result.containsKey(entry.getKey()))) {
                        result.put(entry.getKey(), action);
                    }
                }
            } else {
                for (String token : value.split(",")) {
                    try {
                        int code = Integer.parseInt(token.trim());
                        if (validCode(code, controllerInput)) result.put(code, action);
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
        }

        for (GameAction action : newShortcuts) {
            if (action.name().startsWith("QUICKSLOT_")) {
                for (Map.Entry<Integer, GameAction> entry : defaults.entrySet()) {
                    if (entry.getValue() == action && !result.containsKey(entry.getKey())) {
                        result.put(entry.getKey(), action);
                    }
                }
                continue;
            }
            int[] candidates = action == GameAction.VIEW_MODE
                    ? new int[]{Input.Keys.V, Input.Keys.B, Input.Keys.N, Input.Keys.F6}
                    : new int[]{Input.Keys.T, Input.Keys.G, Input.Keys.Y, Input.Keys.F7};
            for (int candidate : candidates) if (!result.containsKey(candidate)) {
                result.put(candidate, action);
                break;
            }
        }
        if (!controllerInput) {
            result.put(Input.Keys.ESCAPE, GameAction.BACK);
        } else {

            if (count(result, GameAction.BACK) == 0) result.put(Input.Keys.BUTTON_B, GameAction.BACK);
            if (count(result, GameAction.POINTER_PRIMARY) == 0) {
                result.put(Input.Keys.BUTTON_R2, GameAction.POINTER_PRIMARY);
            }
            if (count(result, GameAction.BACK) == 0) result.put(Input.Keys.BUTTON_START, GameAction.BACK);
            if (count(result, GameAction.POINTER_SECONDARY) == 0) {

                int[] candidates = {Input.Keys.BUTTON_L2, Input.Keys.BUTTON_THUMBR, Input.Keys.BUTTON_R2,
                        Input.Keys.BUTTON_X, Input.Keys.BUTTON_A, Input.Keys.BUTTON_Y,
                        Input.Keys.BUTTON_L1, Input.Keys.BUTTON_R1, Input.Keys.BUTTON_THUMBL,
                        Input.Keys.BUTTON_SELECT, DPAD_LEFT, DPAD_DOWN, DPAD_RIGHT, DPAD_UP, Input.Keys.BUTTON_B};
                for (int candidate : candidates) {
                    if (!result.containsKey(candidate)) {
                        result.put(candidate, GameAction.POINTER_SECONDARY);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void migrateKeyboardPointerDefaults() {
        int version;
        try {
            version = preferences.getInteger(KEYBOARD_POINTER_DEFAULTS_VERSION, 0);
        } catch (NumberFormatException | ClassCastException malformed) {
            version = 0;
        }
        if (version >= 1) return;

        boolean changed = false;
        LinkedHashMap<Integer, GameAction> defaults = defaults(false);
        for (GameAction action : new GameAction[]{GameAction.POINTER_PRIMARY, GameAction.POINTER_SECONDARY}) {
            if (count(keyboard, action) != 0) continue;
            for (Map.Entry<Integer, GameAction> entry : defaults.entrySet()) {
                if (entry.getValue() == action && !keyboard.containsKey(entry.getKey())) {
                    keyboard.put(entry.getKey(), action);
                    changed = true;
                }
            }
        }
        preferences.putInteger(KEYBOARD_POINTER_DEFAULTS_VERSION, 1);
        if (changed) save(false);
        else preferences.flush();
    }

    private static boolean isPointerAction(GameAction action) {
        return action == GameAction.POINTER_PRIMARY || action == GameAction.POINTER_SECONDARY;
    }

    private void save(boolean controllerInput) {
        for (GameAction action : GameAction.values()) {
            StringBuilder codes = new StringBuilder();
            for (Map.Entry<Integer, GameAction> entry : bindings(controllerInput).entrySet()) {
                if (entry.getValue() == action) {
                    if (codes.length() > 0) codes.append(',');
                    codes.append(entry.getKey());
                }
            }
            preferences.putString(preferenceKey(action, controllerInput), codes.toString());
        }
        preferences.flush();
    }

    private static String preferenceKey(GameAction action, boolean controllerInput) {
        return (controllerInput ? "controller." : "keyboard.") + action.name();
    }

    private static void removeAction(Map<Integer, GameAction> bindings, GameAction action) {
        Iterator<Map.Entry<Integer, GameAction>> iterator = bindings.entrySet().iterator();
        while (iterator.hasNext()) if (iterator.next().getValue() == action) iterator.remove();
    }

    private static int count(Map<Integer, GameAction> bindings, GameAction action) {
        int count = 0;
        for (GameAction value : bindings.values()) if (value == action) count++;
        return count;
    }

    private static boolean essential(GameAction action, boolean controllerInput) {
        return action == GameAction.BACK || (controllerInput && isPointerAction(action));
    }

    private static boolean validCode(int code, boolean controllerInput) {

        if (!controllerInput) return code > Input.Keys.UNKNOWN && code <= Input.Keys.MAX_KEYCODE
                && code != Input.Keys.ALT_LEFT && code != Input.Keys.ALT_RIGHT;
        if (code == DPAD_UP || code == DPAD_DOWN || code == DPAD_LEFT || code == DPAD_RIGHT) return true;
        switch (code) {
            case Input.Keys.BUTTON_A:
            case Input.Keys.BUTTON_B:
            case Input.Keys.BUTTON_X:
            case Input.Keys.BUTTON_Y:
            case Input.Keys.BUTTON_L1:
            case Input.Keys.BUTTON_R1:
            case Input.Keys.BUTTON_L2:
            case Input.Keys.BUTTON_R2:
            case Input.Keys.BUTTON_THUMBL:
            case Input.Keys.BUTTON_THUMBR:
            case Input.Keys.BUTTON_START:
            case Input.Keys.BUTTON_SELECT:
                return true;
            default:
                return false;
        }
    }

    private static int clampSensitivity(int value) {
        return Math.max(1, Math.min(10, value));
    }

    private static String actionLabel(GameAction action) {
        return msg("action_" + action.name().toLowerCase(Locale.ENGLISH), action.label);
    }

    private static String msg(String key, String fallback, Object... args) {
        String value = Messages.get("windows.wndcontrols." + key, args);
        return Messages.NO_TEXT_FOUND.equals(value) ? String.format(Locale.ROOT, fallback, args) : value;
    }

    private static LinkedHashMap<Integer, GameAction> defaults(boolean controllerInput) {
        LinkedHashMap<Integer, GameAction> result = new LinkedHashMap<>();
        if (controllerInput) {
            put(result, GameAction.ATTACK, Input.Keys.BUTTON_A);
            put(result, GameAction.BACK, Input.Keys.BUTTON_B, Input.Keys.BUTTON_START);
            put(result, GameAction.QUICKSLOT_1, Input.Keys.BUTTON_X);
            put(result, GameAction.INVENTORY, Input.Keys.BUTTON_Y);
            put(result, GameAction.ZOOM_OUT, Input.Keys.BUTTON_L1);
            put(result, GameAction.ZOOM_IN, Input.Keys.BUTTON_R1);
            put(result, GameAction.WAIT, Input.Keys.BUTTON_THUMBL);
            put(result, GameAction.POINTER_PRIMARY, Input.Keys.BUTTON_THUMBR, Input.Keys.BUTTON_R2);
            put(result, GameAction.POINTER_SECONDARY, Input.Keys.BUTTON_L2);
            put(result, GameAction.SKILLS, DPAD_UP);
            put(result, GameAction.LAST_SKILL, DPAD_RIGHT);
            put(result, GameAction.QUICKSLOT_2, DPAD_DOWN);
            put(result, GameAction.EXAMINE, DPAD_LEFT);
            put(result, GameAction.HERO_INFO, Input.Keys.BUTTON_SELECT);
        } else {
            put(result, GameAction.MOVE_N, Input.Keys.W, Input.Keys.UP, Input.Keys.NUMPAD_8);
            put(result, GameAction.MOVE_NE, Input.Keys.NUMPAD_9);
            put(result, GameAction.MOVE_E, Input.Keys.D, Input.Keys.RIGHT, Input.Keys.NUMPAD_6);
            put(result, GameAction.MOVE_SE, Input.Keys.NUMPAD_3);
            put(result, GameAction.MOVE_S, Input.Keys.S, Input.Keys.DOWN, Input.Keys.NUMPAD_2);
            put(result, GameAction.MOVE_SW, Input.Keys.NUMPAD_1);
            put(result, GameAction.MOVE_W, Input.Keys.A, Input.Keys.LEFT, Input.Keys.NUMPAD_4);
            put(result, GameAction.MOVE_NW, Input.Keys.NUMPAD_7);
            put(result, GameAction.WAIT, Input.Keys.SPACE, Input.Keys.NUMPAD_5);
            put(result, GameAction.REST, Input.Keys.Z);
            put(result, GameAction.ATTACK, Input.Keys.Q);
            put(result, GameAction.INVENTORY, Input.Keys.I, Input.Keys.F);
            put(result, GameAction.QUICKSLOT_1, Input.Keys.NUM_1);
            put(result, GameAction.QUICKSLOT_2, Input.Keys.NUM_2);
            put(result, GameAction.QUICKSLOT_3, Input.Keys.NUM_3);
            put(result, GameAction.QUICKSLOT_4, Input.Keys.NUM_4);
            put(result, GameAction.QUICKSLOT_5, Input.Keys.NUM_5);
            put(result, GameAction.QUICKSLOT_6, Input.Keys.NUM_6);
            put(result, GameAction.QUICKSLOT_7, Input.Keys.NUM_7);
            put(result, GameAction.QUICKSLOT_8, Input.Keys.NUM_8);
            put(result, GameAction.QUICKSLOT_9, Input.Keys.NUM_9);
            put(result, GameAction.QUICKSLOT_10, Input.Keys.NUM_0);
            put(result, GameAction.EXAMINE, Input.Keys.E);
            put(result, GameAction.SKILLS, Input.Keys.K);
            put(result, GameAction.LAST_SKILL, Input.Keys.R);
            put(result, GameAction.MERCENARIES, Input.Keys.M);
            put(result, GameAction.HERO_INFO, Input.Keys.H);
            put(result, GameAction.JOURNAL, Input.Keys.J);
            put(result, GameAction.BACK, Input.Keys.ESCAPE, Input.Keys.BACKSPACE);
            put(result, GameAction.ZOOM_IN, Input.Keys.PLUS, Input.Keys.EQUALS, Input.Keys.NUMPAD_ADD);
            put(result, GameAction.ZOOM_OUT, Input.Keys.MINUS, Input.Keys.NUMPAD_SUBTRACT);
            put(result, GameAction.POINTER_PRIMARY, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER);
            put(result, GameAction.POINTER_SECONDARY, Input.Keys.C);
            put(result, GameAction.VIEW_MODE, Input.Keys.V);
            put(result, GameAction.RAT_KING, Input.Keys.T);
        }
        return result;
    }

    private static void put(Map<Integer, GameAction> bindings, GameAction action, int... codes) {
        for (int code : codes) bindings.put(code, action);
    }

    private static String inputName(int code, boolean controllerInput) {
        if (controllerInput) {
            if (code == DPAD_UP) return msg("button_dpad_up", "D-pad up");
            if (code == DPAD_DOWN) return msg("button_dpad_down", "D-pad down");
            if (code == DPAD_LEFT) return msg("button_dpad_left", "D-pad left");
            if (code == DPAD_RIGHT) return msg("button_dpad_right", "D-pad right");
            switch (code) {
                case Input.Keys.BUTTON_A: return "A / Cross";
                case Input.Keys.BUTTON_B: return "B / Circle";
                case Input.Keys.BUTTON_X: return "X / Square";
                case Input.Keys.BUTTON_Y: return "Y / Triangle";
                case Input.Keys.BUTTON_L1: return "LB / L1";
                case Input.Keys.BUTTON_R1: return "RB / R1";
                case Input.Keys.BUTTON_L2: return "LT / L2";
                case Input.Keys.BUTTON_R2: return "RT / R2";
                case Input.Keys.BUTTON_THUMBL: return msg("button_stick_left", "Left stick click");
                case Input.Keys.BUTTON_THUMBR: return msg("button_stick_right", "Right stick click");
                case Input.Keys.BUTTON_START: return "Start / Options";
                case Input.Keys.BUTTON_SELECT: return "Back / Select";
                default: break;
            }
        }
        String name = Input.Keys.toString(code);
        return name == null ? "Key " + code : name;
    }
}
