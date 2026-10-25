package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import java.util.ArrayList;

public class Slider extends Component {
    private final int minimum, maximum;
    private int value, pressedValue;
    private Touch press;
    private BitmapTextMultiline label;
    private BitmapText reading;
    private ColorBlock track, edge, fill;
    private NinePatch thumb;
    private TouchArea hotArea;
    private final ArrayList<ColorBlock> ticks = new ArrayList<>();

    public Slider(String text, int minimum, int maximum, int value) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = Math.max(minimum, Math.min(maximum, value));
        label.text(text);
        if (maximum - minimum <= 10) {
            for (int i = minimum; i <= maximum; i++) {
                ColorBlock tick = new ColorBlock(1, 2, Presentation.reborn() ? 0xFF79648D : 0xFF7B8073);
                ticks.add(tick);
                addToBack(tick);
            }
        }
    }

    @Override
    protected void createChildren() {
        boolean reborn = Presentation.reborn();
        add(label = PixelScene.createMultiline(8));
        label.hardlight(reborn ? 0xECE2D4 : 0xFFFFFF);
        add(reading = PixelScene.createText(8));
        reading.hardlight(Window.titleColor());
        add(track = new ColorBlock(1, 5, reborn ? 0xFF08090E : 0xFF181A16));
        add(edge = new ColorBlock(1, 1, reborn ? 0xFF66536F : 0xFF7B8073));
        add(fill = new ColorBlock(1, 2, reborn ? 0xFF967DB2 : 0xFFB6B7A6));
        add(thumb = Chrome.get(Chrome.Type.BUTTON));
        thumb.size(12, 12);
        add(hotArea = new TouchArea(0, 0, 0, 0) {
            @Override
            protected void onTouchDown(Touch touch) {
                if (press != null || touch.longClick) return;
                press = touch;
                pressedValue = value;
                highlightThumb(true);
            }

            @Override
            protected void onDrag(Touch touch) {
                if (touch == press) moveTo(touch);
            }

            @Override
            protected void onTouchUp(Touch touch) {
                if (press == null || (touch != press && !touch.cancelled)) return;
                if (touch.cancelled) {
                    change(pressedValue);
                } else {
                    moveTo(touch);
                }
                press = null;
                highlightThumb(false);
                if (!touch.cancelled && value != pressedValue) onRelease(value);
            }
        });
    }

    public TouchArea touchArea() {
        return hotArea;
    }

    private void highlightThumb(boolean pressed) {
        if (thumb instanceof RebornChrome) ((RebornChrome)thumb).highlight(pressed);
        else thumb.brightness(pressed ? 1.2f : 1f);
    }

    public int value() {
        return value;
    }

    public float requiredHeight(float touchHeight) {
        return label.height() + 3 + Math.max(16, touchHeight);
    }

    @Override
    protected void layout() {
        if (width <= 0) return;
        reading.text(valueText(value));
        reading.measure();
        reading.x = right() - reading.width() - 2;
        reading.y = y + 1;
        label.maxWidth = Math.max(1, (int)(width - reading.width() - 10));
        label.measure();
        label.x = x + 2;
        label.y = y + 1;
        float center = y + label.height() + 3 + (height - label.height() - 3) / 2;
        float travel = Math.max(1, width - 14);
        float fraction = maximum == minimum ? 0 : (value - minimum) / (float)(maximum - minimum);
        track.x = edge.x = fill.x = x + 7;
        track.y = center - 2;
        edge.y = center - 3;
        fill.y = center - 1;
        track.size(travel, 5);
        edge.size(travel, 1);
        fill.size(Math.round(travel * fraction), 2);
        thumb.x = PixelScene.align(x + 7 + travel * fraction - thumb.width() / 2);
        thumb.y = PixelScene.align(center - thumb.height() / 2);
        for (int i = 0; i < ticks.size(); i++) {
            ColorBlock tick = ticks.get(i);
            tick.x = PixelScene.align(x + 7 + travel * i / Math.max(1, ticks.size() - 1));
            tick.y = center + 4;
        }
        hotArea.x = x;
        hotArea.y = y + label.height() + 3;
        hotArea.width = width;
        hotArea.height = Math.max(1, height - label.height() - 3);
    }

    private void moveTo(Touch touch) {
        float point = camera().screenToCamera((int)touch.current.x, (int)touch.current.y).x;
        float fraction = Math.max(0, Math.min(1, (point - x - 7) / Math.max(1, width - 14)));
        change(minimum + Math.round(fraction * (maximum - minimum)));
    }

    private void change(int next) {
        if (next == value) return;
        value = next;
        layout();
        onChange(value);
    }

    protected String valueText(int value) {
        return value + "%";
    }

    protected void onChange(int value) { }

    protected void onRelease(int value) { }
}
