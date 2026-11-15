package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.TutorialInstructorSprite;
import com.bilboldev.pixeldungeonskills.windows.WndTutorial;

public class TutorialInstructor extends NPC {
    private int station = -1;
    {
        spriteClass = TutorialInstructorSprite.class;
        name = Messages.get("tutorial.instructor.name");
    }

    @Override protected boolean act() {
        if (TutorialRun.acceptsInput() && station >= 0 && pos != station) {
            if (!Dungeon.visible[pos] && !Dungeon.visible[station] && Actor.findChar(station) == null) {
                Actor.freeCell(pos);
                pos = station;
                if (sprite != null) sprite.place(pos);
                Actor.occupyCell(this);
            } else {
                Dungeon.level.updateFieldOfView(this);
                int from = pos;
                if (getCloser(station)) {
                    spend(TICK);
                    return moveSprite(from, pos);
                }
            }
        }
        spend(TICK);
        return true;
    }

    public void station(int cell) { station = cell; }

    @Override public void damage(int damage, Object source) { }
    @Override public void add(Buff buff) { }
    @Override public Message nameMessage() { return Message.key("tutorial.instructor.name"); }
    @Override public String displayName() { return Messages.get("tutorial.instructor.name"); }
    @Override public String description() { return Messages.get("tutorial.instructor.desc"); }
    @Override public String displayDescription() { return description(); }

    @Override public void interact() {
        if (TutorialRun.acceptsInput()) GameScene.show(new WndTutorial());
    }
}
