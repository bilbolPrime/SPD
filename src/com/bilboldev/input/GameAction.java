package com.bilboldev.input;

public enum GameAction {

    MOVE_N("Move north", 0, -1),
    MOVE_NE("Move north-east", 1, -1),
    MOVE_E("Move east", 1, 0),
    MOVE_SE("Move south-east", 1, 1),
    MOVE_S("Move south", 0, 1),
    MOVE_SW("Move south-west", -1, 1),
    MOVE_W("Move west", -1, 0),
    MOVE_NW("Move north-west", -1, -1),
    WAIT("Wait one turn"),
    REST("Rest until healthy"),
    ATTACK("Attack nearby enemy"),
    INVENTORY("Inventory / equipment"),
    QUICKSLOT_1("Quickslot 1"),
    QUICKSLOT_2("Quickslot 2"),
    EXAMINE("Examine / search"),
    SKILLS("Skills"),
    LAST_SKILL("Last used skill"),
    MERCENARIES("Mercenaries"),
    HERO_INFO("Hero information"),
    JOURNAL("Journal"),
    BACK("Back / cancel / menu"),
    ZOOM_IN("Zoom in"),
    ZOOM_OUT("Zoom out"),
    POINTER_PRIMARY("Select / left click"),
    POINTER_SECONDARY("Alternate / right click"),
    VIEW_MODE("Change view mode"),
    RAT_KING("Rat King"),
    QUICKSLOT_3("Quickslot 3"),
    QUICKSLOT_4("Quickslot 4"),
    QUICKSLOT_5("Quickslot 5"),
    QUICKSLOT_6("Quickslot 6"),
    QUICKSLOT_7("Quickslot 7"),
    QUICKSLOT_8("Quickslot 8"),
    QUICKSLOT_9("Quickslot 9"),
    QUICKSLOT_10("Quickslot 10");

    public final String label;
    public final int dx;
    public final int dy;

    GameAction(String label) {
        this(label, 0, 0);
    }

    GameAction(String label, int dx, int dy) {
        this.label = label;
        this.dx = dx;
        this.dy = dy;
    }

    public boolean isMovement() {
        return dx != 0 || dy != 0;
    }
}
