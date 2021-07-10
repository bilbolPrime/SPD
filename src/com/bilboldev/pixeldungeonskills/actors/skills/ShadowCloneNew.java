package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.sprites.MirrorSprite;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class ShadowCloneNew extends ShadowClone {

    {
        castText = "Shadow clone";
        mana = 20;
        useDelay = 30f;
    }


    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_CAST) {
            chooseGameMode();
        }
        else{
            super.execute(hero, action);
        }
    }

    @Override
    public String info()
    {
        return "Creates one strong clone or three weak ones to fight for you.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  Total of " + (int)(5 + i * 2) + "  attack and " + (int)(7 + 7 * i) + " health.";
            if(i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Free Runner";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Freerunner.class);
        return toReturn;
    }

    public void chooseGameMode()
    {


        GameScene.show(new WndOptions( "Shadow Clone", "Please choose one", "One Strong Clone","Three Weak Clones" ) {
            @Override
            protected void onSelect( int index ) {
                if(index == 0)
                {
                    summonStrong();
                }
                else
                {
                    summonWeak();
                }
            }
        } );


    }

    private void summonWeak(){
        ArrayList<Integer> respawnPoints = new ArrayList<Integer>();
        Hero hero = Dungeon.hero;
        for (int i = 0; i < Level.NEIGHBOURS8.length; i++) {
            int p = hero.pos + Level.NEIGHBOURS8[i];
            if(p < 0 || p >= Level.passable.length)
                continue;
            if (Actor.findChar(p) == null && (Level.passable[p] || Level.avoid[p])) {
                respawnPoints.add(p);
            }
        }

        int nImages = 3;
        if(respawnPoints.size() > 0){
            SummonedPet.killClones();
        }
        while (nImages > 0 && respawnPoints.size() > 0) {
            int index = Random.index(respawnPoints);

            SummonedPet minion = new SummonedPet(MirrorSprite.class);
            minion.name = "Weak Shadow Clone";
            minion.screams = false;
            minion.HT = 7 + 7 * level;
            minion.HP = 7 + 7 * level;
            minion.HT = 1 + minion.HT / 3;
            minion.HP =  minion.HT;
            minion.defenseSkill = 5 + level * 2;
            minion.defenseSkill = 1 + minion.defenseSkill / 3;
            GameScene.add(minion);
            WandOfBlink.appear(minion, respawnPoints.get(index));
            minion.level = Math.min(5, level / 3);
            //((MirrorSprite)minion.sprite).updateArmor(Math.min(5, level / 3));
            minion.sprite.alpha(0);
            minion.sprite.parent.add(new AlphaTweener(minion.sprite, 1, 0.15f));
            CellEmitter.get(minion.pos).burst(ElmoParticle.FACTORY, 4);

            nImages--;
        }

        hero.MP -= getManaCost();
        StatusPane.manaDropping += getManaCost();
        castTextYell();
        Dungeon.hero.heroSkills.lastUsed = this;
        hero.spend( TIME_TO_USE );
        hero.busy();
        hero.sprite.operate( hero.pos );
    }

    private void summonStrong(){
        ArrayList<Integer> respawnPoints = new ArrayList<Integer>();
Hero hero = Dungeon.hero;
        for (int i = 0; i < Level.NEIGHBOURS8.length; i++) {
            int p = hero.pos + Level.NEIGHBOURS8[i];
            if(p < 0 || p >= Level.passable.length)
                continue;
            if (Actor.findChar(p) == null && (Level.passable[p] || Level.avoid[p])) {
                respawnPoints.add(p);
            }
        }

        int nImages = 1;
        if(respawnPoints.size() > 0){
            SummonedPet.killClones();
        }
        while (nImages > 0 && respawnPoints.size() > 0) {
            int index = Random.index(respawnPoints);

            SummonedPet minion = new SummonedPet(MirrorSprite.class);
            minion.name = "Shadow Clone";
            minion.screams = false;
            minion.HT = 7 + 7 * level;
            minion.HP = 7 + 7 * level;
            minion.defenseSkill = 5 + level * 2;
            GameScene.add(minion);
            WandOfBlink.appear(minion, respawnPoints.get(index));
            minion.level = Math.min(6, 1 + level / 2);
            //((MirrorSprite)minion.sprite).updateArmor(Math.min(5, level / 2));
            minion.sprite.alpha(0);
            minion.sprite.parent.add(new AlphaTweener(minion.sprite, 1, 0.15f));
            CellEmitter.get(minion.pos).burst(ElmoParticle.FACTORY, 4);

            nImages--;
        }

        hero.MP -= getManaCost();
        StatusPane.manaDropping += getManaCost();
        castTextYell();
        Dungeon.hero.heroSkills.lastUsed = this;
        hero.spend( TIME_TO_USE );
        hero.busy();
        hero.sprite.operate( hero.pos );
    }
}
