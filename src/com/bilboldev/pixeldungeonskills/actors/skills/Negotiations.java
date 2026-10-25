package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class Negotiations extends BranchSkill{

    public static final String TXT_HIRE_BRUTE = "Brute";
    public static final String TXT_HIRE_THIEF = "Thief";
    public static final String TXT_HIRE_WIZARD = "Wizard";
    public static final String TXT_HIRE_ARCHER = "Archer";
    public static final String TXT_HIRE_ARCHER_MAIDEN = "Archer-Maiden";

    {
        name = "Hire A Mercenary";
        image = 96;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (Dungeon.rules().allowsOnDemandHiring() && hero.hiredMerc == null)
        {
            if (hero.heroClass != HeroClass.WARRIOR && hero.heroClass != HeroClass.MERCENARY)
                actions.add(TXT_HIRE_BRUTE);
            if(hero.heroClass != HeroClass.ROGUE)
                actions.add(TXT_HIRE_THIEF);
            if (hero.heroClass != HeroClass.MAGE && hero.heroClass != HeroClass.NECROMANCER)
                actions.add(TXT_HIRE_WIZARD);
            if(hero.heroClass != HeroClass.HUNTRESS)
                actions.add(TXT_HIRE_ARCHER);

            if(HiredMerc.archerMaidenUnlocked)
                actions.add(TXT_HIRE_ARCHER_MAIDEN);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (!Dungeon.rules().allowsOnDemandHiring()) return;
        if(action == TXT_HIRE_BRUTE || action == TXT_HIRE_THIEF || action == TXT_HIRE_WIZARD || action == TXT_HIRE_ARCHER  || action == TXT_HIRE_ARCHER_MAIDEN)
        {
            if(Dungeon.gold < getGoldCost())
            {
                GLog.n("custom.generated.entry_you_cannot_afford_a_merc_f3fd4a7a04");
                return;
            }

            boolean spawned = false;
            for (int nu = 0; nu < 1 ; nu++) {
                int newPos = hero.pos;
                if (Actor.findChar(newPos) != null) {
                    ArrayList<Integer> candidates = new ArrayList<Integer>();
                    boolean[] passable = Level.passable;

                    for (int n : Level.NEIGHBOURS4) {
                        int c = hero.pos + n;
                        if (passable[c] && Actor.findChar(c) == null) {
                            candidates.add(c);
                        }
                    }
                    newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
                    if (newPos != -1) {
                        if(action == TXT_HIRE_ARCHER_MAIDEN)
                            hero.hiredMerc = new HiredMerc(HiredMerc.MERC_TYPES.ArcherMaiden);
                        else if(action == TXT_HIRE_ARCHER)
                            hero.hiredMerc = new HiredMerc(HiredMerc.MERC_TYPES.Archer);
                        else
                            hero.hiredMerc = action == TXT_HIRE_BRUTE ? new HiredMerc(HiredMerc.MERC_TYPES.Brute): (action == TXT_HIRE_THIEF ? new HiredMerc(HiredMerc.MERC_TYPES.Thief) : new HiredMerc(HiredMerc.MERC_TYPES.Wizard));
                        hero.hiredMerc.spawn(Dungeon.hero.lvl);
                        hero.hiredMerc.pos = newPos;
                        GameScene.add(hero.hiredMerc);
                        Actor.addDelayed(new Pushing(hero.hiredMerc, hero.pos, newPos), -1);
                        hero.hiredMerc.sprite.alpha(0);
                        hero.hiredMerc.sprite.parent.add(new AlphaTweener(hero.hiredMerc.sprite, 1, 0.15f));
                        ((MercSprite) hero.hiredMerc.sprite).updateArmor();
                    }
                }
            }

            if(spawned == true) {
                Dungeon.gold -= getGoldCost();
                GLog.p("custom.generated.entry_s_hired_for_s_gold_f3f49e6cd1", hireName(action), getGoldCost());
            }
        }
    }

    public static Message hireName(String action) {
        if (TXT_HIRE_BRUTE.equals(action)) return Message.key("custom.generated.entry_brute_fd383641d0");
        if (TXT_HIRE_THIEF.equals(action)) return Message.key("custom.generated.entry_thief_8ac2de7b44");
        if (TXT_HIRE_WIZARD.equals(action)) return Message.key("custom.generated.entry_wizard_127a661b8e");
        if (TXT_HIRE_ARCHER.equals(action)) return Message.key("custom.generated.entry_archer_f75aedc160");
        if (TXT_HIRE_ARCHER_MAIDEN.equals(action)) return Message.key("custom.generated.entry_archer_maiden_50c2d68f1e");
        throw new IllegalArgumentException("Unknown mercenary choice");
    }

    public void restoreMerc(Hero hero)
    {
        if(hero.hiredMerc != null)
        {
            int newPos = hero.pos;

                if (newPos != -1) {
                    HiredMerc tmp = new HiredMerc(hero.hiredMerc.mercType);
                    tmp.spawn(Dungeon.hero.lvl);
                    tmp.pos = newPos;
                    GameScene.add(tmp);
                    Actor.addDelayed(new Pushing(tmp, hero.pos, newPos), -1);
                    tmp.sprite.alpha(0);
                    tmp.sprite.parent.add(new AlphaTweener(tmp.sprite, 1, 0.15f));
                    tmp.weapon = hero.hiredMerc.weapon;
                    tmp.armor = hero.hiredMerc.armor;
                    ((MercSprite) tmp.sprite).updateArmor();

                    hero.hiredMerc = tmp;

            }
        }
    }

    public int getGoldCost()
    {
        return Dungeon.hero.lvl * 0;
    }

    public String getHireText()
    {
        return "\nHiring a level " + Dungeon.hero.lvl + " merc costs " + getGoldCost() + " gold.";
    }

    @Override
    public String info()
    {
        return HiredMerc.MERC_TYPES.Brute.getDescription()  + "\n" + HiredMerc.MERC_TYPES.Thief.getDescription()  + "\n" + HiredMerc.MERC_TYPES.Wizard.getDescription() + (Dungeon.hero.hiredMerc == null ? getHireText() : "");
    }
}
