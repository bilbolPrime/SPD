package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyConnection;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.utils.PointF;
import java.util.ArrayList;

public class WndPartyInput extends WndScrollable {
    public interface Join {
        void connect(String address, int port, String code);
    }

    private final ArrayList<Field> fields = new ArrayList<>();
    private final Field address, port, code;
    private final RenderedTextMultiline error;
    private final Join join;
    private Field focused;
    private int availableHeight = -1;

    public WndPartyInput(boolean direct, Join join) {
        this.join = join;
        boolean landscape = !isDesktopRuntime() && Game.width > Game.height;
        int preferredWidth = isDesktopRuntime() ? 300 : 190;
        if (landscape) preferredWidth = direct ? 480 : 300;
        int width = boundedWidth(preferredWidth);
        RenderedTextMultiline heading = label(text(direct ? "join_title" : "enter_code"), width, 0, 10);
        heading.hardlight(titleColor());
        RenderedTextMultiline version = label(WndPartyLobby.versionLabel(), width, heading.bottom() + 4, 6);
        version.hardlight(0xA99CB8);
        String hint = direct ? text("address_hint") + "\n" + text("port_hint", PartyConnection.DEFAULT_PORT) : text("code_hint");
        RenderedTextMultiline description = label(hint, width, version.bottom() + 6, 8);
        float top = description.bottom() + 8;
        float codeLeft = 0;
        int codeWidth = width;
        if (direct) {
            int portWidth = landscape ? width / 6 : width;
            int addressWidth = landscape ? width * 2 / 5 : width;
            address = field(PartyPrompts.Kind.ADDRESS, "ip_address", "", 15, addressWidth, 0, top);
            port = field(PartyPrompts.Kind.PORT, "port", String.valueOf(PartyConnection.DEFAULT_PORT), 5,
                    portWidth, landscape ? addressWidth + 8 : 0, landscape ? top : address.bottom() + 6);
            if (landscape) {
                codeLeft = addressWidth + portWidth + 16;
                codeWidth = width - (int)codeLeft;
            } else {
                top = port.bottom() + 6;
            }
        } else {
            address = port = null;
        }
        code = field(PartyPrompts.Kind.CODE, "enter_code", "", 32, codeWidth, codeLeft, top);
        float bottom = code.bottom();
        if (direct) bottom = Math.max(bottom, Math.max(address.bottom(), port.bottom()));
        error = label("", width, bottom + 5, 8);
        error.hardlight(0xEE9B91);

        top = error.top() + 24;
        RedButton paste = new ActionButton(text("paste")) {
            protected void onClick() { PartyPrompts.paste(); }
        };
        RedButton cancel = new ActionButton(text("cancel")) {
            protected void onClick() { onBackPressed(); }
        };
        RedButton connect = new ActionButton(text("join")) {
            protected void onClick() { submit(); }
        };
        float buttonWidth = (width - 8) / 3f;
        RedButton[] buttons = {paste, cancel, connect};
        float buttonHeight = 20;
        for (RedButton button : buttons) {
            button.setSize(buttonWidth, buttonHeight);
            buttonHeight = Math.max(buttonHeight, button.reqHeight());
            add(button);
        }
        for (int i = 0; i < buttons.length; i++) buttons[i].setRect(i * (buttonWidth + 4), top, buttonWidth, buttonHeight);
        resize(width, (int)Math.ceil(top + buttonHeight));
    }

    private RenderedTextMultiline label(String text, int width, float top, int size) {
        RenderedTextMultiline label = PixelScene.renderMultiline(text, size);
        label.maxWidth(width);
        label.setPos(0, top);
        add(label);
        return label;
    }

    private Field field(PartyPrompts.Kind kind, String title, String value, int limit, int width, float left, float top) {
        RenderedTextMultiline label = label(text(title), width, top, 8);
        label.setPos(left, top);
        label.hardlight(0xC7B3DF);
        Field field = new Field(kind, value, limit);
        field.setRect(left, label.bottom() + 3, width, 22);
        fields.add(field);
        add(field);
        return field;
    }

    private void focus(Field field) {
        if (focused != field) {
            focused = field;
            field.input.selectAll();
        }
        PartyPrompts.edit(field.input);
    }

    private void submit() {
        int selectedPort = PartyConnection.DEFAULT_PORT;
        if (address != null) {
            if (!PartyConnection.validAddress(address.input.value())) {
                invalid(address, "invalid_address");
                return;
            }
            try {
                if (!port.input.value().matches("[0-9]{1,5}")) throw new NumberFormatException();
                selectedPort = Integer.parseInt(port.input.value());
                if (selectedPort < 1 || selectedPort > 65535) throw new NumberFormatException();
            } catch (NumberFormatException invalid) {
                invalid(port, "invalid_port");
                return;
            }
        }
        if (!PartyConnection.validCode(code.input.value())) {
            invalid(code, "invalid_code");
            return;
        }
        String host = address == null ? "" : address.input.value().trim();
        String pairing = code.input.value().trim();
        hide();
        join.connect(host, selectedPort, pairing);
    }

    private void invalid(Field field, String message) {
        focus(field);
        error.text(text(message));
    }

    @Override
    public void update() {
        super.update();
        if (topWindow() != this) return;
        if (focused == null) focus(fields.get(0));
        int available = PartyPrompts.keyboardTop > 0 ? Math.min(Game.height, PartyPrompts.keyboardTop) : Game.height;
        if (available != availableHeight) {
            availableHeight = available;
            camera.zoom = Math.min(PixelScene.defaultZoom, (available - 8f) / (height + chrome.marginVer()));
            resize(width, height);
            camera.y = Math.max(0, (int)(available - camera.screenHeight()) / 2);
            shadow.boxRect(camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height());
        }
    }

    @Override
    public void destroy() {
        if (focused != null) PartyPrompts.endEdit(focused.input);
        super.destroy();
    }

    private static String text(String key, Object... args) {
        return Messages.get(WndPartyLobby.class, key, args);
    }

    private class Field extends Button {
        final PartyPrompts.Input input;
        private NinePatch frame;
        private BitmapText value, measure;
        private ColorBlock selection, caret, underline;
        private float blink;
        private float textScale;

        Field(PartyPrompts.Kind kind, String initial, int limit) {
            input = new PartyPrompts.Input(kind, initial, limit) {
                protected void changed() {
                    blink = 0;
                    if (error != null) error.text("");
                    layout();
                }

                public void next(boolean backwards) {
                    int index = fields.indexOf(Field.this);
                    focus(fields.get((index + fields.size() + (backwards ? -1 : 1)) % fields.size()));
                }

                public void accept() {
                    if (Field.this == code) submit();
                    else next(false);
                }

                public void cancel() { onBackPressed(); }
            };
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            remove(hotArea);
            hotArea.destroy();
            add(hotArea = new TouchArea(0, 0, 0, 0) {
                protected void onClick(Touch touch) {
                    boolean wasFocused = focused == Field.this;
                    focus(Field.this);
                    if (wasFocused) {
                        PointF point = camera().screenToCamera((int)touch.current.x, (int)touch.current.y);
                        int position = 0;
                        float distance = Float.MAX_VALUE;
                        for (int i = 0; i <= input.value().length(); i++) {
                            float next = Math.abs(point.x - value.x - prefixWidth(i));
                            if (next < distance) {
                                distance = next;
                                position = i;
                            }
                        }
                        input.move(position, false);
                        PartyPrompts.edit(input);
                    }
                }
            });
            add(frame = Chrome.get(Chrome.Type.BUTTON));
            add(selection = new ColorBlock(1, 1, 0x806F5199));
            add(value = PixelScene.createText(9));
            measure = PixelScene.createText(9);
            textScale = value.scale.x;
            value.hardlight(0xF0E7F5);
            add(caret = new ColorBlock(1, 10, 0xFFE7CBA5));
            add(underline = new ColorBlock(1, 1, 0xFFAC83D4));
        }

        private float prefixWidth(int count) {
            measure.text(input.value().substring(0, count), false);
            measure.scale.set(value.scale);
            measure.measure();
            return measure.width();
        }

        @Override
        protected void layout() {
            super.layout();
            frame.x = x;
            frame.y = y;
            frame.size(width, height);
            if (input == null) return;
            value.text(input.value(), false);
            value.scale.set(textScale);
            value.measure();
            if (value.width() > width - 12) value.scale.set(textScale * (width - 12) / value.width());
            value.x = x + 6;
            value.y = y + (height - value.baseLine()) / 2;
            float first = prefixWidth(Math.min(input.cursor(), input.anchor()));
            float last = prefixWidth(Math.max(input.cursor(), input.anchor()));
            selection.x = value.x + first;
            selection.y = y + 5;
            selection.size(last - first, height - 10);
            caret.x = value.x + prefixWidth(input.cursor());
            caret.y = y + 5;
            caret.size(1, height - 10);
            underline.x = x + 3;
            underline.y = bottom() - 3;
            underline.size(width - 6, 1);
        }

        @Override
        public void update() {
            super.update();
            blink += Game.elapsed;
            boolean editing = focused == this;
            selection.visible = editing && input.cursor() != input.anchor();
            caret.visible = editing && !selection.visible && blink % 1 < .6f;
            underline.visible = editing;
        }

        @Override
        public void destroy() {
            measure.destroy();
            super.destroy();
        }
    }
}
