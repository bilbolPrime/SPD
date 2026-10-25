package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.ui.Button;
import java.util.ArrayList;
import java.util.List;

public final class MenuModel {
    public final Message title;
    public final Message description;
    public final List<Choice> choices = new ArrayList<>();

    public static final class Choice {
        public final Button button;
        public final int option;
        public final Message label;

        Choice(Button button, int option, Message label) {
            this.button = button;
            this.option = option;
            this.label = label;
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
}
