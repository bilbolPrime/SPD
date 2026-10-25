package com.bilboldev.pixeldungeonskills.network;

import java.util.ArrayList;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndPartyMenu;
import com.bilboldev.utils.Bundle;

public final class PartyWindows {
    private static final DetachedWindows detached = new DetachedWindows();
    private static final ArrayList<Window> windows = new ArrayList<>();
    private static final ArrayList<MenuModel.Choice> choices = new ArrayList<>();
    private static Window shown;
    private static WndPartyMenu clientWindow;
    private static Bundle pendingMenu;
    private static int token;
    private static long awaitingToken;
    private static boolean changing;

    static {
        detached.active = detached.visible = false;
    }

    private PartyWindows() { }

    private static class DetachedWindows extends Group {
        @Override
        public Gizmo add(Gizmo child) {
            if (child instanceof Window && capture((Window)child)) return child;
            return super.add(child);
        }

        void attach(Window window) {
            super.add(window);
        }
    }

    public static boolean capture(Window window) {
        if (!PartySession.remoteContext() || PartySession.client()) return false;
        window.detachForParty();
        detached.attach(window);
        windows.add(window);
        if (!changing) publish();
        return true;
    }

    public static void receive(Bundle message) {
        if (!PartySession.client()) return;
        pendingMenu = message;
        showReceived();
    }

    private static void showReceived() {
        if (pendingMenu == null || !(Game.scene() instanceof GameScene) || PartySession.applyingSnapshot()) return;
        Bundle message = pendingMenu;
        pendingMenu = null;
        long next;
        try {
            next = message.getInt("token");
        } catch (NumberFormatException invalid) {
            return;
        }
        if (awaitingToken != 0 && next <= awaitingToken) return;
        awaitingToken = 0;
        if (clientWindow != null) {
            clientWindow.hide();
            clientWindow = null;
        }
        if (message.getBoolean("closed")) return;
        String[] labels = new String[message.getInt("choices")];
        for (int i = 0; i < labels.length; i++) labels[i] = PartyText.render(message.getBundle("choice" + i));
        clientWindow = new WndPartyMenu(next, PartyText.render(message.getBundle("title")),
                PartyText.render(message.getBundle("text")), labels);
        GameScene.show(clientWindow);
    }

    public static void choose(long receivedToken, int index) {
        Window window = top();
        if (PartySession.client() || receivedToken != token || window == null || window != shown) return;
        if (index < 0 || index >= choices.size()) throw new IllegalArgumentException("Unknown party menu choice");
        MenuModel.Choice choice = choices.get(index);
        changing = true;
        token++;
        try {
            PartySession.withRemote(() -> {
                if (choice.button == null) ((WndOptions)window).selectForParty(choice.option);
                else if (belongsTo(choice.button, window)) choice.button.triggerPartyAction();
            });
        } finally {
            changing = false;
            publish();
        }
    }

    public static void close(long receivedToken) {
        Window window = top();
        if (PartySession.client() || receivedToken != token || window == null || window != shown) return;
        changing = true;
        token++;
        try {
            PartySession.withRemote(window::onBackPressed);
        } finally {
            changing = false;
            publish();
        }
    }

    public static void update() {
        if (!PartySession.network() || changing) return;
        if (PartySession.client()) {
            showReceived();
            return;
        }
        if (top() != shown) publish();
    }

    public static boolean busy() {
        if (!PartySession.client()) return top() != null;
        return awaitingToken != 0
                || pendingMenu != null && !pendingMenu.getBoolean("closed")
                || clientWindow != null && clientWindow.parent != null;
    }

    public static void awaitResponse(long token) {
        if (PartySession.client()) awaitingToken = token;
    }

    public static void clear() {
        if (clientWindow != null) clientWindow.hide();
        clientWindow = null;
        pendingMenu = null;
        awaitingToken = 0;
        for (Window window : windows) {
            if (window.parent == detached) {
                detached.remove(window);
                window.destroy();
            }
        }
        windows.clear();
        choices.clear();
        shown = null;
        token++;
        changing = false;
    }

    private static Window top() {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.parent == detached && window.exists) return window;
            windows.remove(i);
        }
        return null;
    }

    private static void publish() {
        try {
            publishMenu();
        } catch (IllegalArgumentException failure) {
            PartySession.textFailure(failure);
        }
    }

    private static void publishMenu() {
        shown = top();
        choices.clear();
        Bundle message = new Bundle();
        if (token == Integer.MAX_VALUE) throw new IllegalArgumentException("Party menu sequence exhausted");
        message.put("token", ++token);
        if (shown == null) {
            message.put("closed", true);
        } else {
            MenuModel menu = shown.menuModel();
            if (menu == null) throw new IllegalArgumentException("Missing party menu: " + shown.getClass().getName());
            message.put("title", PartyText.encode(menu.title));
            message.put("text", PartyText.encode(menu.description));
            for (MenuModel.Choice choice : menu.choices) {
                if (choice.button == null || belongsTo(choice.button, shown)) choices.add(choice);
            }
        }
        message.put("choices", choices.size());
        for (int i = 0; i < choices.size(); i++) message.put("choice" + i, PartyText.encode(choices.get(i).label));
        PartySession.sendUi(message);
    }

    private static boolean belongsTo(Button button, Window window) {
        for (Gizmo item = button; item != null; item = item.parent) {
            if (!item.exists || !item.active || !item.visible) return false;
            if (item == window) return true;
        }
        return false;
    }

}
