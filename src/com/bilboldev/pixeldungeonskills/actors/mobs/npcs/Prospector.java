package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Journal;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.Torch;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ProspectorSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndQuest;
import com.bilboldev.utils.Bundle;

public class Prospector extends NPC {
    public static final int TORCHES = 0, ICECLAWS = 1;
    public int job;
    private int killed;
    private boolean given;
    private boolean completed;

    {
        name = "stranded prospector";
        spriteClass = ProspectorSprite.class;
    }

    @Override
    protected boolean act() {
        throwItem();
        return super.act();
    }

    @Override
    public void damage(int damage, Object source) { }

    @Override
    public void add(Buff buff) { }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public String description() {
        return Messages.get(Prospector.class, "desc");
    }

    @Override
    public void interact() {
        sprite.turnTo(pos, Dungeon.hero.pos);
        if (completed) {
            tell("thanks");
        } else if (!given) {
            Message offer = Message.paragraphs(Message.key(Prospector.class, job == TORCHES ? "torches" : "iceclaws"),
                    Message.key("library.quest.prospector.reward"));
            GameScene.show(new WndQuest(this, offer,
                    Message.key(Prospector.class, "accept"), Message.key(Prospector.class, "later")) {
                @Override
                protected void onSelect(int index) {
                    if (index != 0 || given) return;
                    given = true;
                    Journal.add(Journal.Feature.PROSPECTOR);
                    updateLore();
                }
            });
        } else if (ready(Dungeon.hero)) {
            Hero hero = Dungeon.hero;
            GameScene.show(new WndOptions(sprite(), Message.capitalize(Message.actor(this)),
                    Message.key(Prospector.class, "reward"),
                    new Message[]{Message.key(ScrollOfUpgrade.class, "name"), Message.key(Prospector.class, "rations")},
                    new int[]{new ScrollOfUpgrade().image(), ItemSpriteSheet.RATION}, new int[]{1, 2}) {
                @Override
                protected void onSelect(int index) {
                    takeReward(hero, index);
                }
            });
        } else if (job == TORCHES) {
            tell("need_torches");
        } else {
            GameScene.show(new WndQuest(this, Message.key(Prospector.class, "claws_left", 3 - killed)));
        }
    }

    private void tell(String key) {
        GameScene.show(new WndQuest(this, Message.key(Prospector.class, key)));
    }

    public void iceclawSlain() {
        if (job == ICECLAWS && killed < 3) killed++;
    }

    private boolean ready(Hero hero) {
        if (job == ICECLAWS) return killed == 3;
        Torch torches = hero.belongings.getItem(Torch.class);
        return torches != null && torches.quantity() >= 3;
    }

    private void takeReward(Hero hero, int choice) {
        if (!given || completed || choice < 0 || choice > 1) return;
        if (!ready(hero)) {
            tell("need_torches");
            return;
        }
        if (job == TORCHES) {
            Torch torches = hero.belongings.getItem(Torch.class);
            for (int i = 0; i < 3; i++) torches.detach(hero.belongings.backpack);
        }
        completed = true;
        Journal.remove(Journal.Feature.PROSPECTOR);
        updateLore();
        Item reward = choice == 0 ? new ScrollOfUpgrade() : new Food();
        if (choice == 1) reward.quantity(2);
        reward.identify();
        Heap heap = Dungeon.level.drop(reward, hero.pos);
        if (heap.sprite != null) heap.sprite.drop(pos);
        tell("thanks");
    }

    public void updateLore() {
        if (given) Lore.quest(job == TORCHES ? "prospector_torches" : "prospector_iceclaws", completed);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("prospector_job", job);
        bundle.put("prospector_given", given);
        bundle.put("prospector_completed", completed);
        bundle.put("prospector_killed", killed);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        job = bundle.getInt("prospector_job");
        given = bundle.getBoolean("prospector_given");
        completed = bundle.getBoolean("prospector_completed");
        killed = bundle.getInt("prospector_killed");
    }
}
