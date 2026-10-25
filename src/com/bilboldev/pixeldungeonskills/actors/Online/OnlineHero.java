package com.bilboldev.pixeldungeonskills.actors.Online;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRegeneration;
import com.bilboldev.pixeldungeonskills.actors.buffs.Regeneration;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroAction;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.PlateArmor;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.features.Chasm;
import com.bilboldev.pixeldungeonskills.online.OnlineMessageHepler;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Random;

public class OnlineHero extends Hero {

    public String Identifier;

    public Weapon weapon;
    public Armor armor;
    public PotionOfHealing potion;
    @Override
    public void live() {
        lvl = 100;
        HP = HT = 20;
        STR = 25;
        MP = MMP = 100;
        attackSkill = 5;
        defenseSkill = 5;
        Item tmp = new Longsword().identify();
        belongings.weapon = (Weapon)tmp;
        tmp = new PlateArmor().identify();
        belongings.armor = (Armor)tmp;
        weapon= null;
        armor = null;
        potion = null;
    }

    @Override
    public void move( int step ) {
        super.move( step );
    }

    @Override
    protected boolean getCloser( final int target ) {

        if(HP < 1)
        {
            sprite.die();
            sprite.showStatus(
                    CharSprite.NEGATIVE,
                    "You are dead");
            return false;
        }

        int step = -1;
        if (Level.adjacent( pos, target )) {

            if (Actor.findChar( target ) == null) {
                if (Level.pit[target] && !flying && !Chasm.jumpConfirmed) {
                    Chasm.heroJump( this );
                    interrupt();
                    return false;
                }
                if (Level.passable[target] || Level.avoid[target]) {
                    step = target;
                }
            }
            else
            {
                Char possibleEnemy = Actor.findChar( target );
                if(possibleEnemy instanceof  OnlineFoe) {
                      OnlineMessageHepler.RequestAttack(((OnlineFoe) possibleEnemy).Identifier);
                    return false;
                     }
            }

        } else {

            int len = Level.LENGTH;
            boolean[] p = Level.passable;
            boolean[] v = Dungeon.level.visited;
            boolean[] m = Dungeon.level.mapped;
            boolean[] passable = new boolean[len];
            for (int i=0; i < len; i++) {
                passable[i] = p[i] && (v[i] || m[i]);
            }

            step = Dungeon.findPath( this, pos, target, passable, Level.fieldOfView );
        }

        if(step != -1)
        {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof OnlineFoe) {
                    OnlineFoe onlineFoe = (OnlineFoe) mob;
                    if (onlineFoe.pos == step) {
                        OnlineMessageHepler.RequestAttack(( onlineFoe).Identifier);
                        return false;

                    }
                }
            }

            OnlineMessageHepler.RequestMove(step, target);
        }
        return false;
    }

    public void ResumeMove(int finalPos)
    {
        getCloser(finalPos);
    }

    public void ApprovedMove(int step)
    {
        int oldPos = pos;
        move( step );
        sprite.move( oldPos, pos );
    }

    @Override
    protected boolean actAttack( HeroAction.Attack action ) {
        enemy = action.target;
        if(enemy instanceof  OnlineFoe) {
            OnlineMessageHepler.RequestAttack(((OnlineFoe) enemy).Identifier);
        }
        curAction = null;
        spend(0.01f);
        next();
        return false;
    }

    @Override
    public void onAttackComplete()
    {
        curAction = null;
        spend(0.01f);
        next();
    }

    @Override
    public boolean attack( Char enemy )
    {
        return true;
    }

    public void AttackApproved(Char Target)
    {
        this.sprite.attack(Target.pos);
    }

    @Override
    public void die(Object cause)
    {
        destroy();
        sprite.die();
    }
}
