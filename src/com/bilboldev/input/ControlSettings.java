package com.bilboldev.input;

public final class ControlSettings {

    private ControlSettings() {}

    public static Backend backend;

    public interface Backend {
        String bindingName(GameAction action, boolean controller);

        String keyboardKeyName(GameAction action);
        void beginRebind(GameAction action, boolean controller);
        boolean isCapturing();
        String captureStatus();
        void cancelRebind();
        void resetBindings(boolean controller);

        int pointerSensitivity();
        void pointerSensitivity(int value);
        int mouseLookSensitivity();
        void mouseLookSensitivity(int value);
        int controllerLookSensitivity();
        void controllerLookSensitivity(int value);
    }
}
