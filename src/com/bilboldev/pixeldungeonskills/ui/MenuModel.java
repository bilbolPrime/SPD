package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import java.util.ArrayList;
import java.util.List;

public final class MenuModel {
    public enum View { OPTIONS, BAG, ITEM, SKILLS, SKILL, OLD_SKILLS, OLD_SKILL,
        QUICK_SLOT, QUICK_BIND, QUICK_SKILLS, STORAGE }

    public View view = View.OPTIONS;
    public Item item;
    public Skill skill;
    public int mode;
    public boolean selection;
    public Message prompt;
    public final Message title;
    public final Message description;
    public int image = -1;
    public final List<Choice> choices = new ArrayList<>();

    public static final class Choice {
        public final Button button;
        public final int option;
        public final Message label;
        public final int image;
        public final int quantity;

        Choice(Button button, int option, Message label) {
            this(button, option, label, -1, 1);
        }

        Choice(Button button, int option, Message label, int image, int quantity) {
            this.button = button;
            this.option = option;
            this.label = label;
            this.image = image;
            this.quantity = quantity;
        }
    }

    public MenuModel(Message title, Message description) {
        this.title = title;
        this.description = description;
    }

    public void add(Button button, Message label) {
        choices.add(new Choice(button, -1, label));
    }

    public void add(int option, Message label) {
        choices.add(new Choice(null, option, label));
    }

    public void add(int option, Message label, int image, int quantity) {
        choices.add(new Choice(null, option, label, image, quantity));
    }
}
