package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.input.GameActions;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.Toolbar;
import com.bilboldev.pixeldungeonskills.ui.Window;

final class DesktopKeyHints {
    private static final GameAction[] ACTIONS = {
            GameAction.WAIT, GameAction.REST, GameAction.SKILLS, GameAction.LAST_SKILL, GameAction.MERCENARIES,
            GameAction.EXAMINE, GameAction.INVENTORY, GameAction.QUICKSLOT_1,
            GameAction.QUICKSLOT_2, GameAction.QUICKSLOT_3, GameAction.QUICKSLOT_4,
            GameAction.QUICKSLOT_5, GameAction.QUICKSLOT_6, GameAction.QUICKSLOT_7,
            GameAction.QUICKSLOT_8, GameAction.QUICKSLOT_9, GameAction.QUICKSLOT_10,
            GameAction.ATTACK, GameAction.VIEW_MODE, GameAction.RAT_KING
    };
    private final Camera ui;
    private final BitmapText[] labels = new BitmapText[ACTIONS.length];
    private final BitmapText hold, wait;
    private final ColorBlock background = new ColorBlock(1, 1, 0xFF161C20);
    private final float scale;

    DesktopKeyHints(Camera ui) {
        this.ui = ui;
        background.camera = ui;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = PixelScene.createText("", 5);
            labels[i].camera = ui;
            labels[i].hardlight(0xE4D5AC);
        }
        scale = labels[0].scale.x;
        hold = PixelScene.createText(Messages.get("ui.toolbar.hold"), 5);
        hold.measure();
        hold.camera = ui;
        hold.hardlight(0xE4D5AC);
        wait = PixelScene.createText("", 5);
        wait.camera = ui;
        wait.hardlight(0xE4D5AC);
    }

    void draw() {
        for (BitmapText label : labels) label.visible = false;
        hold.visible = wait.visible = false;
        if (!GameActions.enabled || !ControlSettings.keyboardHints
                || ControlSettings.backend == null || Window.topWindow() != null
                || !GameScene.canAcceptGameplayInput()) return;
        for (int i = 0; i < ACTIONS.length; i++) {
            GameAction action = ACTIONS[i];
            Button button = Toolbar.keyHintButton(action);
            if (button == null || button.camera() != ui) continue;
            if (action == GameAction.REST && !ControlSettings.backend.keyboardKeyName(GameAction.WAIT).isEmpty()) continue;

            if (action == GameAction.ATTACK && !button.acceptsAction()) continue;
            String key = ControlSettings.backend.keyboardKeyName(action);
            if (key.isEmpty()) continue;
            BitmapText label = labels[i];
            drawLabel(label, button, key, button.bottom());
            if (action == GameAction.WAIT) {
                Button sleep = GameActions.visibleButton(GameAction.REST);
                if (sleep != null && sleep != button && sleep.camera() == ui) {
                    drawLabel(wait, sleep, key, sleep.bottom());
                    drawLabel(hold, sleep, hold.text(), wait.y + 1);
                }
            }
        }
    }

    private void drawLabel(BitmapText label, Button button, String text, float bottom) {
        label.visible = true;
        if (!text.equals(label.text())) {
            label.text(text, false);
            label.measure();
        }
        label.scale.set(scale);
        float available = Math.max(1, button.width() - 4);
        if (label.width() > available) label.scale.set(scale * available / label.width());
        label.x = PixelScene.align(ui, button.centerX() - label.width() / 2);
        label.y = PixelScene.align(ui, bottom - label.height() - 2);
        boolean enabled = button.acceptsAction();
        label.alpha(enabled ? 1 : 0.5f);
        background.alpha(enabled ? 0.85f : 0.65f);
        background.x = label.x - 1;
        background.y = label.y - 1;
        background.size(label.width() + 2, label.height() + 2);
        background.draw();
        label.draw();
    }

    void destroy() {
        for (BitmapText label : labels) label.destroy();
        hold.destroy();
        wait.destroy();
        background.destroy();
    }
}
