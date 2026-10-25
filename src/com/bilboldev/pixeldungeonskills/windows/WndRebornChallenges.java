package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.CheckBox;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndRebornChallenges extends WndScrollable {
    public interface Selection {
        void accept(int challenges);
    }

    private int selected;
    private final ControlRow choices = new ControlRow();
    private final Selection onDone;

    public WndRebornChallenges(int challenges, Selection changed) {
        selected = challenges & RebornChallenges.ALL;
        int width = boundedWidth(240);
        RenderedTextMultiline heading = PixelScene.renderMultiline(RebornChallenges.title(), 10);
        heading.hardlight(titleColor());
        heading.maxWidth(width);
        Component content = choices;
        float top = 0;
        for (int i = 0; i < RebornChallenges.MASKS.length; i++) {
            final int mask = RebornChallenges.MASKS[i];
            if (changed == null && (selected & mask) == 0) continue;
            ItemSprite icon = new ItemSprite(RebornChallenges.icon(i), null);
            icon.x = 1;
            icon.y = top + 3;
            content.add(icon);
            if (changed != null) {
                CheckBox choice = new CheckBox(RebornChallenges.name(i)) {
                    @Override protected void createChildren() {
                        super.createChildren();
                        choices.addControl(hotArea);
                    }

                    @Override protected void onClick() {
                        super.onClick();
                        selected ^= mask;
                    }
                };
                choice.checked((selected & mask) != 0);
                choice.setRect(20, top, textWidth(width) - 20, 22);
                content.add(choice);
                top = choice.bottom();
            } else {
                RenderedTextMultiline name = PixelScene.renderMultiline("", 8);
                name.text(RebornChallenges.name(i), false);
                name.hardlight(titleColor());
                name.maxWidth(textWidth(width) - 20);
                name.setPos(20, top + 3);
                content.add(name);
                top = name.bottom();
            }
            RenderedTextMultiline description = PixelScene.renderMultiline(RebornChallenges.description(i), 6);
            description.maxWidth(textWidth(width) - 20);
            description.setPos(20, top + 3);
            content.add(description);
            top = description.bottom() + 10;
        }
        if (changed == null && selected == 0) {
            RenderedTextMultiline none = PixelScene.renderMultiline(RebornChallenges.summary(0), 6);
            none.maxWidth(textWidth(width));
            content.add(none);
            top = none.height();
        }
        content.setSize(textWidth(width), top);
        RedButton done = new RedButton(Messages.get(RebornChallenges.class, "done")) {
            @Override protected void onClick() {
                onBackPressed();
            }
        };
        done.setSize(width, 20);
        onDone = changed;
        layoutContent(width, heading, content, 6, done);
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        return choices.scrollPane(content);
    }

    @Override public void onBackPressed() {
        hide();
        if (onDone != null) onDone.accept(selected);
    }
}
