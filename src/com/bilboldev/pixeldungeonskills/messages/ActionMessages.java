package com.bilboldev.pixeldungeonskills.messages;

public final class ActionMessages {
    private ActionMessages() { }
    public static Message skill(String command) {
        switch (command) {
            case "Activate": return Message.key("items.artifacts.timekeepershourglass.ac_activate");
            case "Advance": return Message.key("custom.generated.entry_advance_67e70c3c1a");
            case "Archer": return Message.key("custom.generated.entry_archer_f75aedc160");
            case "Archer-Maiden": return Message.key("custom.generated.entry_archer_maiden_50c2d68f1e");
            case "Brute": return Message.key("custom.generated.entry_brute_fd383641d0");
            case "Cast": return Message.key("items.artifacts.etherealchains.ac_cast");
            case "Deactivate": return Message.key("custom.generated.entry_deactivate_d65ded9428");
            case "Summon": return Message.key("items.artifacts.driedrose.ac_summon");
            case "Thief": return Message.key("custom.generated.entry_thief_8ac2de7b44");
            case "Wizard": return Message.key("custom.generated.entry_wizard_127a661b8e");
            default: throw new IllegalArgumentException("Unknown skill action");
        }
    }
}
