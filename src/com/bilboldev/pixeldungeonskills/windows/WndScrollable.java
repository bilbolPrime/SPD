package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.Visual;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Slider;
import com.bilboldev.pixeldungeonskills.ui.Window;
import java.util.ArrayList;

public abstract class WndScrollable extends Window {

    private static final int SCREEN_MARGIN = 4;
    protected static final int SCROLL_GUTTER = 4;

    protected WndScrollable() {
        super();
    }

    protected WndScrollable(int width, int height, NinePatch chrome) {
        super(width, height, chrome);
    }

    protected int frameHeight() {
        return chrome.marginVer();
    }

    protected final int boundedWidth(int preferredWidth) {
        return Math.max(1, Math.min(preferredWidth,
                (int)(Game.width / camera.zoom) - chrome.marginHor() - SCREEN_MARGIN * 2));
    }

    protected final int textWidth(int windowWidth) {
        return Math.max(1, windowWidth - SCROLL_GUTTER);
    }

    protected final void layoutContent(int windowWidth, Component heading, Component content,
                                       float gap, RedButton... buttons) {
        layoutContent(windowWidth, heading, content, null, gap, buttons);
    }

    protected final void layoutContent(int windowWidth, Component heading, Component content,
                                       Component controls, float gap, RedButton... buttons) {
        float top = 0;
        if (heading != null) {
            add(heading);
            top = (float)Math.ceil(heading.bottom()) + gap;
        }

        float x = 0;
        float y = 0;
        float rowHeight = 0;
        for (RedButton button : buttons) {
            float buttonWidth = Math.min(windowWidth, Math.max(button.width(), button.reqWidth()));
            button.setSize(buttonWidth, Math.max(16, button.height()));
            button.setSize(buttonWidth, Math.max(button.height(), (float)Math.ceil(button.reqHeight())));
            if (x > 0 && x + button.width() > windowWidth) {
                x = 0;
                y += rowHeight + gap;
                rowHeight = 0;
            }
            button.setPos(x, y);
            add(button);
            rowHeight = Math.max(rowHeight, button.height());
            x += button.width() + gap;
        }
        float controlsHeight = controls == null ? 0 : (float)Math.ceil(controls.height()) + gap;
        float footerHeight = controlsHeight + (buttons.length == 0 ? 0 : y + rowHeight + gap);
        int fixedHeight = (int)Math.ceil(top + footerHeight);

        float minimumHeight = fixedHeight + 16 + frameHeight() + SCREEN_MARGIN * 2;
        camera.zoom = Math.min(camera.zoom, Game.height / minimumHeight);
        int available = Math.max(1, (int)(Game.height / camera.zoom)
                - frameHeight() - SCREEN_MARGIN * 2 - fixedHeight);
        int bodyHeight = Math.min((int)Math.ceil(content.height()), available);

        ScrollPane body = createScrollPane(content);
        add(body);

        resize(windowWidth, fixedHeight + bodyHeight);
        body.setRect(0, top, windowWidth, bodyHeight);
        if (controls != null) {
            add(controls);
            controls.setPos(0, top + bodyHeight + gap);
        }
        for (RedButton button : buttons) {
            button.setPos(button.left(), top + bodyHeight + gap + controlsHeight + button.top());
            bringToFront(button);
        }
    }

    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content);
    }

    protected static class ControlRow extends Component {
        private float previousX;
        private float previousY;
        private final ArrayList<TouchArea> touchAreas = new ArrayList<>();
        private final ArrayList<TouchArea> sliderAreas = new ArrayList<>();

        @Override
        public Gizmo add(Gizmo child) {
            if (child instanceof TouchArea) addControl((TouchArea)child);
            return super.add(child);
        }

        public void addControl(TouchArea area) {

            Touchscreen.event.remove(area);
            touchAreas.add(area);
        }

        public void addSlider(Slider slider) {
            add(slider);
            addControl(slider.touchArea());
            sliderAreas.add(slider.touchArea());
        }

        public ScrollPane scrollPane(Component content) {
            return new ScrollPane(content) {
                @Override
                protected TouchController createTouchController() {
                    return new TouchController() {
                        private TouchArea pressedControl;
                        private Touch controlPress;
                        private boolean sliding;

                        @Override
                        protected void onTouchDown(Touch touch) {
                            if (this.touch != touch) return;
                            cancelControl();
                            super.onTouchDown(touch);
                            if (isDragging()) return;
                            for (TouchArea area : touchAreas) {
                                if (area.isActive() && area.target.overlapsScreenPoint(
                                        (int)touch.start.x, (int)touch.start.y)) {
                                    pressedControl = area;
                                    controlPress = touch;
                                    area.onSignal(touch);
                                    break;
                                }
                            }
                        }

                        @Override
                        protected void onDrag(Touch touch) {
                            if (pressedControl != null && sliderAreas.contains(pressedControl)) {
                                float dx = Math.abs(touch.current.x - touch.start.x);
                                float dy = Math.abs(touch.current.y - touch.start.y);
                                if (sliding || (dx > camera().zoom * 4 && dx > dy)) {
                                    sliding = true;
                                    pressedControl.onSignal(null);
                                    return;
                                }
                                if (dy <= camera().zoom * 4) return;
                            }
                            super.onDrag(touch);
                            if (isDragging()) cancelControl();
                        }

                        @Override
                        protected void onTouchUp(Touch touch) {
                            if (touch != controlPress) return;
                            if (isDragging() || touch.cancelled || (!sliding
                                    && (!overlapsScreenPoint((int)touch.current.x, (int)touch.current.y)
                                    || !pressedControl.target.overlapsScreenPoint((int)touch.current.x, (int)touch.current.y)))) {
                                cancelControl();
                            } else {
                                TouchArea area = pressedControl;
                                pressedControl = null;
                                controlPress = null;
                                sliding = false;
                                area.onSignal(touch);
                            }
                        }

                        private void cancelControl() {
                            if (pressedControl == null) return;
                            TouchArea area = pressedControl;
                            Touch cancelled = new Touch(controlPress.start.x, controlPress.start.y);
                            pressedControl = null;
                            controlPress = null;
                            sliding = false;
                            area.onSignal(cancelled.cancel());
                            area.reset();
                        }

                        @Override
                        public void destroy() {
                            cancelControl();
                            super.destroy();
                        }
                    };
                }
            };
        }

        @Override
        protected void layout() {
            float dx = x - previousX;
            float dy = y - previousY;
            for (Gizmo child : members) {
                if (child instanceof Component) {
                    Component component = (Component)child;
                    component.setPos(component.left() + dx, component.top() + dy);
                } else if (child instanceof Visual) {
                    ((Visual)child).x += dx;
                    ((Visual)child).y += dy;
                }
            }
            previousX = x;
            previousY = y;
        }
    }

    protected final Component textContent(final BitmapTextMultiline text) {
        Component content = new Component() {
            @Override
            protected void layout() {
                text.x = left();
                text.y = top();
            }
        };
        content.add(text);
        content.setSize(text.width(), text.height());
        return content;
    }

    protected static class ActionButton extends RedButton {
        public ActionButton(String label) {
            super(label);
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            remove(text);
            text.destroy();
            text = PixelScene.createMultiline(9);
            text.scale.set(text.scale.x * textScale(), text.scale.y * textScale());
            add(text);
        }

        @Override
        protected void layout() {
            ((BitmapTextMultiline)text).maxWidth = Math.max(1, (int)width - 4);
            text.measure();
            super.layout();
            text.y = y + (height - text.height()) / 2;
        }

        @Override
        public float reqHeight() {
            return text.height() + 4;
        }
    }
}
