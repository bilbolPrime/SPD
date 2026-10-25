package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.input.ControlSettings;
import com.bilboldev.input.GameAction;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

import java.util.ArrayList;
import java.util.Locale;

public class WndControls extends WndScrollable {

    private static final int WIDTH = 190;
    private static final int GAP = 2;
    private static final int BUTTON_HEIGHT = 18;
    private static boolean lastControllerTab;

    private final ControlSettings.Backend backend;
    private final boolean controller;
    private final ArrayList<BindingRow> rows = new ArrayList<>();
    private Component content;
    private ScrollPane list;
    private boolean disposed;
    private final int columns;
    private final int windowWidth;

    public WndControls() {
        this(lastControllerTab);
    }

    private WndControls(boolean controller) {
        this.backend = ControlSettings.backend;
        this.controller = controller;
        lastControllerTab = controller;
        int availableWidth = boundedWidth(780);
        columns = landscapeLayout() ? Math.max(1, Math.min(5, availableWidth / 140)) : 1;
        windowWidth = columns == 1 ? boundedWidth(WIDTH) : availableWidth;
        Component heading = new Component();

        RenderedTextMultiline title = text(controller ? "controller_title" : "keyboard_title", 9, windowWidth);
        title.hardlight(titleColor());
        heading.add(title);

        RedButton keyboardTab = new RedButton(msg("keyboard")) {
            @Override
            protected void onClick() {
                switchTab(false);
            }
        };
        RedButton controllerTab = new RedButton(msg("controller")) {
            @Override
            protected void onClick() {
                switchTab(true);
            }
        };
        float tabWidth = (windowWidth - GAP) / 2f;
        keyboardTab.setRect(0, title.bottom() + GAP, tabWidth, BUTTON_HEIGHT);
        controllerTab.setRect(tabWidth + GAP, keyboardTab.top(), tabWidth, BUTTON_HEIGHT);
        keyboardTab.textColor(controller ? 0xFFFFFF : titleColor());
        controllerTab.textColor(controller ? titleColor() : 0xFFFFFF);
        heading.add(keyboardTab);
        heading.add(controllerTab);
        String hintText = controller && GameScene.isPerspectiveView() && backend != null
                ? msg("controller_perspective_hint", backend.bindingName(GameAction.POINTER_SECONDARY, true))
                : msg(controller ? "controller_hint" : GameScene.isPerspectiveView() ? "perspective_hint" : "keyboard_hint");
        RenderedTextMultiline hint = PixelScene.renderMultiline(hintText, 7);
        hint.maxWidth(windowWidth);
        hint.setPos(0, keyboardTab.bottom() + GAP);
        heading.add(hint);
        heading.setSize(windowWidth, hint.bottom());

        content = new Component();
        if (backend != null) {
            for (GameAction action : GameAction.values()) {
                BindingRow row = new BindingRow(action);
                row.setSize((textWidth(windowWidth) - GAP * (columns - 1)) / (float)columns, 28);
                content.add(row);
                rows.add(row);
            }
        }
        refreshBindings();

        RedButton reset = new ActionButton(msg("reset")) {
            @Override
            protected void onClick() {
                if (backend != null) {
                    backend.resetBindings(WndControls.this.controller);
                    refreshBindings();
                }
            }
        };
        RedButton close = new ActionButton(msg("close")) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        float footerWidth = (windowWidth - GAP) / 2f;
        reset.setSize(footerWidth, BUTTON_HEIGHT);
        close.setSize(footerWidth, BUTTON_HEIGHT);

        layoutContent(windowWidth, heading, content,
                backend != null && (controller || GameScene.isPerspectiveView()) ? sensitivityControls(windowWidth) : null,
                GAP, reset, close);
    }

    private Component sensitivityControls(final int windowWidth) {
        if (!controller || !GameScene.isPerspectiveView()) return sensitivityRow(windowWidth, !controller);
        Component rows = new Component() {
            @Override
            protected void layout() {
                ((Component)members.get(0)).setPos(left(), top());
                ((Component)members.get(1)).setPos(left(), top() + BUTTON_HEIGHT + GAP);
            }
        };
        rows.add(sensitivityRow(windowWidth, false));
        rows.add(sensitivityRow(windowWidth, true));
        rows.setSize(windowWidth, BUTTON_HEIGHT * 2 + GAP);
        return rows;
    }

    private Component sensitivityRow(final int windowWidth, final boolean look) {
        final String key = look ? (controller ? "controller_look_sensitivity" : "look_sensitivity") : "sensitivity";
        final Component controls = new Component() {
            @Override
            protected void layout() {
                if (members.size() == 0) return;
                Component minus = (Component)members.get(0);
                Component plus = (Component)members.get(1);
                RenderedTextMultiline label = (RenderedTextMultiline)members.get(2);
                minus.setRect(left(), top(), BUTTON_HEIGHT, BUTTON_HEIGHT);
                plus.setRect(left() + windowWidth - BUTTON_HEIGHT, top(), BUTTON_HEIGHT, BUTTON_HEIGHT);
                label.setPos(left() + (windowWidth - label.width()) / 2f,
                        top() + (BUTTON_HEIGHT - label.height()) / 2f);
            }
        };
        final RenderedTextMultiline label = PixelScene.renderMultiline(
                msg(key, sensitivity(look)), 7);
        label.maxWidth(windowWidth - BUTTON_HEIGHT * 2 - GAP * 2);
        RedButton minus = new RedButton("-") {
            @Override
            protected void onClick() {
                sensitivity(look, Math.max(1, sensitivity(look) - 1));
                label.text(msg(key, sensitivity(look)));
                controls.setSize(windowWidth, BUTTON_HEIGHT);
            }
        };
        RedButton plus = new RedButton("+") {
            @Override
            protected void onClick() {
                sensitivity(look, Math.min(10, sensitivity(look) + 1));
                label.text(msg(key, sensitivity(look)));
                controls.setSize(windowWidth, BUTTON_HEIGHT);
            }
        };
        controls.add(minus);
        controls.add(plus);
        controls.add(label);
        controls.setSize(windowWidth, BUTTON_HEIGHT);
        return controls;
    }

    private void switchTab(boolean controller) {
        if (this.controller == controller) return;
        Group container = parent;
        hide();
        container.add(new WndControls(controller));
    }

    private int sensitivity(boolean look) {
        if (!look) return backend.pointerSensitivity();
        return controller ? backend.controllerLookSensitivity() : backend.mouseLookSensitivity();
    }

    private void sensitivity(boolean look, int value) {
        if (!look) backend.pointerSensitivity(value);
        else if (controller) backend.controllerLookSensitivity(value);
        else backend.mouseLookSensitivity(value);
    }

    private void refreshBindings() {
        if (disposed) return;
        float rowHeight = 0;
        for (BindingRow row : rows) {
            row.refresh();
            rowHeight = Math.max(rowHeight, row.height());
        }
        int rowsPerColumn = (rows.size() + columns - 1) / columns;
        for (int i = 0; i < rows.size(); i++) {
            BindingRow row = rows.get(i);
            row.setPos((i / rowsPerColumn) * (row.width() + GAP),
                    (i % rowsPerColumn) * (rowHeight + GAP));
        }
        content.setSize(textWidth(windowWidth), Math.max(1, rowsPerColumn * (rowHeight + GAP) - GAP));
        if (list != null) {
            list.setRect(list.left(), list.top(), list.width(), list.height());
        }
    }

    @Override
    public void destroy() {
        disposed = true;
        super.destroy();
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        list = new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (BindingRow row : rows) {
                    if (row.inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        WndControls.this.parent.add(new CaptureWindow(row.action));
                        return;
                    }
                }
            }
        };
        return list;
    }

    private class BindingRow extends Component {
        private final GameAction action;
        private NinePatch background;
        private RenderedTextMultiline label;
        private RenderedTextMultiline binding;

        BindingRow(GameAction action) {
            this.action = action;
            label.text(actionName(action));
            label.hardlight(titleColor());
        }

        @Override
        protected void createChildren() {
            background = Chrome.get(Chrome.Type.BUTTON);
            add(background);
            label = PixelScene.renderMultiline("", 8);
            add(label);
            binding = PixelScene.renderMultiline("", 7);
            add(binding);
        }

        void refresh() {
            String names = backend.bindingName(action, controller);
            binding.text(controller && action.isMovement() ? msg("stick_buttons", names) : names, false);
            layout();
        }

        @Override
        protected void layout() {
            label.maxWidth(Math.max(1, (int)width - 6));
            label.setPos(x + 3, y + 3);
            binding.maxWidth(Math.max(1, (int)width - 6));
            binding.setPos(x + 3, label.bottom() + 2);
            height = Math.max(28, binding.bottom() - y + 3);
            background.x = x;
            background.y = y;
            background.size(width, height);
        }
    }

    private class CaptureWindow extends WndScrollable {
        private final RenderedTextMultiline status;
        private final RedButton close;
        private final Component body;
        private ScrollPane statusPane;
        private String previousStatus;
        private boolean wasCapturing = true;

        CaptureWindow(GameAction action) {
            int windowWidth = boundedWidth(WIDTH);
            RenderedTextMultiline title = PixelScene.renderMultiline(
                    msg("rebind", actionName(action)), 9);
            title.hardlight(titleColor());
            title.maxWidth(windowWidth);

            backend.beginRebind(action, controller);
            previousStatus = backend.captureStatus();
            status = PixelScene.renderMultiline(previousStatus, 8);
            status.maxWidth(textWidth(windowWidth));
            body = new Component();
            body.add(status);
            body.setSize(textWidth(windowWidth), Math.max(64, status.height()));
            close = new ActionButton(msg(controller ? "cancel_controller" : "cancel")) {
                @Override
                protected void onClick() {
                    CaptureWindow.this.hide();
                }
            };
            close.setSize(windowWidth, BUTTON_HEIGHT);
            layoutContent(windowWidth, title, body, GAP, close);
        }

        @Override
        protected ScrollPane createScrollPane(Component content) {
            statusPane = new ScrollPane(content);
            return statusPane;
        }

        @Override
        public void update() {
            super.update();
            String nextStatus = backend.captureStatus();
            if (nextStatus != null && !nextStatus.equals(previousStatus)) {
                status.text(nextStatus);
                body.setSize(body.width(), Math.max(64, status.height()));
                statusPane.setRect(statusPane.left(), statusPane.top(), statusPane.width(), statusPane.height());
                statusPane.scrollTo(0, 0);
                previousStatus = nextStatus;
            }
            boolean capturing = backend.isCapturing();
            if (capturing != wasCapturing) {
                close.text(msg(capturing ? (controller ? "cancel_controller" : "cancel") : "done"));
                wasCapturing = capturing;
                refreshBindings();
            }
        }

        @Override
        public void destroy() {
            if (backend.isCapturing()) backend.cancelRebind();
            refreshBindings();
            super.destroy();
        }
    }

    private static RenderedTextMultiline text(String key, int size, int width) {
        RenderedTextMultiline text = PixelScene.renderMultiline(msg(key), size);
        text.maxWidth(width);
        return text;
    }

    private String actionName(GameAction action) {
        if (controller && GameScene.isPerspectiveView() && action == GameAction.POINTER_SECONDARY) {
            return msg("controller_perspective_cursor");
        }
        return msg((GameScene.isPerspectiveView() && action.isMovement() ? "perspective_" : "action_")
                + action.name().toLowerCase(Locale.ENGLISH));
    }

    private static String msg(String key, Object... args) {
        return Messages.get(WndControls.class, key, args);
    }
}
