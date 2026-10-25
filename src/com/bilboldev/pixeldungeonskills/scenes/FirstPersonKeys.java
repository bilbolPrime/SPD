package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.ui.Window;

final class FirstPersonKeys {
    private static final GameAction[] ACTIONS = {
            GameAction.WAIT, GameAction.SKILLS, GameAction.LAST_SKILL, GameAction.MERCENARIES,
            GameAction.EXAMINE, GameAction.INVENTORY, GameAction.QUICKSLOT_1,
            GameAction.QUICKSLOT_2, GameAction.QUICKSLOT_3, GameAction.QUICKSLOT_4,
            GameAction.QUICKSLOT_5, GameAction.QUICKSLOT_6, GameAction.QUICKSLOT_7,
            GameAction.QUICKSLOT_8, GameAction.QUICKSLOT_9, GameAction.QUICKSLOT_10,
            GameAction.ATTACK, GameAction.VIEW_MODE, GameAction.RAT_KING
    };
    private final Camera ui;
    private final BitmapText[] labels = new BitmapText[ACTIONS.length];
    private final ColorBlock background = new ColorBlock(1, 1, 0xFF161C20);
    private final float scale;

    FirstPersonKeys(Camera ui) {
        this.ui = ui;
        background.camera = ui;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = PixelScene.createText("", 5);
            labels[i].camera = ui;
            labels[i].hardlight(0xE4D5AC);
        }
        scale = labels[0].scale.x;
    }

    void draw() {
        for (BitmapText label : labels) label.visible = false;
        if (!GameActions.enabled || ControlSettings.backend == null || Window.topWindow() != null) return;
        for (int i = 0; i < ACTIONS.length; i++) {
            GameAction action = ACTIONS[i];
            Button button = GameActions.visibleButton(action);
            if (button == null || button.camera() != ui) continue;

            if (action == GameAction.ATTACK && !button.acceptsAction()) continue;
            String key = ControlSettings.backend.keyboardKeyName(action);
            if (key.isEmpty()) continue;
            BitmapText label = labels[i];
            label.visible = true;
            if (!key.equals(label.text())) {
                label.text(key, false);
                label.measure();
            }
            label.scale.set(scale);
            float available = Math.max(1, button.width() - 4);
            if (label.width() > available) label.scale.set(scale * available / label.width());

            label.x = PixelScene.align(ui, button.centerX() - label.width() / 2);
            label.y = PixelScene.align(ui, button.bottom() - label.height() - 2);
            boolean enabled = button.acceptsAction();
            label.alpha(enabled ? 1 : 0.5f);
            background.alpha(enabled ? 0.85f : 0.65f);
            background.x = label.x - 1;
            background.y = label.y - 1;
            background.size(label.width() + 2, label.height() + 2);
            background.draw();
            label.draw();
        }
    }

    void destroy() {
        for (BitmapText label : labels) label.destroy();
        background.destroy();
    }
}
