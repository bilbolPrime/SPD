package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.levels.TutorialLevel;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.TutorialInstructorSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;

public class WndTutorial extends WndScrollable {
    private final TutorialLevel floor = TutorialRun.level();
    private final int lesson = TutorialRun.lesson();
    private final boolean completed = TutorialRun.completed();
    private final RedButton proceed;
    private float readingTime;

    public WndTutorial() {
        int width = boundedWidth(180);
        String key = "tutorial.lesson_" + lesson;
        TutorialInstructorSprite instructor = new TutorialInstructorSprite();
        instructor.scale.set(2);
        IconTitle title = new IconTitle(instructor,
                Messages.get(completed ? "tutorial.complete_title" : key + ".title"));
        title.setRect(0, 0, width, 0);
        HighlightedText body = new HighlightedText(8);
        String lessonText = lesson == 4 ? Messages.get(key + ".text", (Object)Talent.SMASH.title()) : Messages.get(key + ".text");
        String text = completed ? Messages.get("tutorial.complete") : lessonText
                + "\n\n_" + TutorialRun.objective() + "_";
        body.text(text, textWidth(width), false);

        proceed = new ActionButton(Messages.get(completed ? "tutorial.return"
                : TutorialRun.canAdvance() ? "tutorial.continue" : "tutorial.try")) {
            @Override protected void onClick() {
                if (!current() || readingTime < .5f) return;
                if (completed) {
                    TutorialRun.leave();
                } else {
                    if (TutorialRun.canAdvance()) TutorialRun.advance();
                    hide();
                }
            }
        };
        proceed.setSize(width, 22);
        proceed.enable(false);
        if (completed) {
            layoutContent(width, title, body, 6, proceed);
        } else {
            RedButton retry = new ActionButton(Messages.get("tutorial.retry")) {
                @Override protected void onClick() {
                    if (!current()) return;
                    hide();
                    TutorialRun.retry();
                }
            };
            RedButton leave = new ActionButton(Messages.get("tutorial.leave")) {
                @Override protected void onClick() {
                    if (!current()) return;
                    GameScene.show(new WndOptions(Messages.get("tutorial.leave"),
                            Messages.get("tutorial.leave_text"), Messages.get("tutorial.leave"),
                            Messages.get("tutorial.stay")) {
                        @Override protected void onSelect(int index) {
                            if (index == 0 && current()) TutorialRun.leave();
                        }
                    });
                }
            };
            retry.setSize((width - 6) / 2f, 20);
            leave.setSize((width - 6) / 2f, 20);
            layoutContent(width, title, body, 6, proceed, retry, leave);
        }
    }

    private boolean current() {
        return TutorialRun.active() && TutorialRun.level() == floor && TutorialRun.lesson() == lesson;
    }

    @Override public void update() {
        super.update();
        readingTime += Game.elapsed;
        proceed.enable(readingTime >= .5f);
    }

    @Override public void onBackPressed() {
        if (!completed) super.onBackPressed();
    }

    @Override public void onMenuPressed() {
        if (!completed) super.onMenuPressed();
    }
}
