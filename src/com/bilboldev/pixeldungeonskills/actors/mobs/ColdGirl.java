/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.items.armor.glyphs.Affection;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ColdGirlSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.pixeldungeonskills.windows.PersistentWndOptions;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.GameMath;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class ColdGirl extends Mob {

	{
		name = "Cold Girl";
		spriteClass = ColdGirlSprite.class;

        HP = HT = 30;
        EXP = 20;
        defenseSkill = 1;
        baseSpeed = 3;
        hostile = false;
        state = new ColdGirlAI();
        champ = 1;
	}

    public boolean isSister = false;

    private static final String TXT_SMB_MISSED	= "%s %s %s's attack";
    public static final String TXT_DEATH =  "Killed in the ice cave";

    public static final int PASSIVE = 0;
    public static final int HUNTING = 1;
    public static final int SUPER_HUNTING = 2;
    public static final int GOD_MODE = 3;
    public static final int DONE_MODE = 4;

    public static final int DISCUSSION_STEP = 10;

    public static final int DISCUSSION_DEAD = 1000;

    public int discussionProgress = 0;

    public boolean firstSwap = true;
    public boolean firstDamage = true;
    public boolean firstComplaint = true;
    public boolean firstTroll = true;
    public boolean firstFetch = true;

    public static final int FROST_DEPTH = 1000;

    public static int cameFrom = 1;
    public static int cameFromPos = 1;
    public int skillCharge = 5;

    public void turnToSis()
    {
        isSister = true;
        ((ColdGirlSprite)sprite).turnToSis();
    }

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 8, 15 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 28;
	}

	@Override
	public int dr() {
		return 10;
	}

	@Override
	public boolean act() {

		return super.act();
	}

	@Override
	public void move( int step ) {
		super.move( step );

	}

    @Override
    public int attackProc( Char enemy, int damage ) {

        if(Level.adjacent(pos, enemy.pos) == true && damage < HP)
        {
            if (firstDamage) {
                speak("I have to feel your pain too?!");
                firstDamage = false;
            }

            damage(damage, this);
        }

        if(damage < enemy.HP && Random.Int(5) < 2 && (((ColdGirlAI)ColdGirl.this.state).aiStatus == SUPER_HUNTING) && Level.adjacent(pos, enemy.pos) == true)
        {
            ArrayList<Integer> skelSpawns = new ArrayList<>();
            for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
                int ofs = Level.NEIGHBOURS8[i];
                if (pos - enemy.pos  == ofs) {
                    int maxTrollPush = 2;
                    int actualPush = ofs;
                    while(maxTrollPush > 0 && enemy.pos - actualPush > 0 && enemy.pos - actualPush < Level.passable.length && ((Level.passable[enemy.pos - actualPush] || Level.avoid[enemy.pos - actualPush]) && Actor.findChar(enemy.pos - actualPush) == null))
                    {
                        skelSpawns.add(enemy.pos - actualPush);
                        actualPush += ofs;
                        maxTrollPush--;
                    }

                    int newPos = enemy.pos - actualPush;
                    if ((Level.passable[newPos] || Level.avoid[newPos]) && Actor.findChar(newPos) == null) {

                        Actor.addDelayed( new Pushing( enemy, enemy.pos, newPos ), -1 );

                        enemy.pos = newPos;

                        if (enemy instanceof Mob) {
                            Dungeon.level.mobPress( (Mob)enemy );
                        } else {
                            Dungeon.level.press( newPos, enemy );
                        }

                        enemy.sprite.bloodBurstA( ((CharSprite)sprite).center(), enemy.HP );
                    }

                    for(int s = 0; s < skelSpawns.size(); s++)
                    {
                        ColdGirlSkel slave = new ColdGirlSkel();
                        slave.pos = skelSpawns.get(s);
                        Sample.INSTANCE.play( Assets.SND_GHOST );

                        GameScene.add( slave );
                        Actor.addDelayed( new Pushing( slave, pos, slave.pos ), -1 );
                        slave.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
                    }

                    if(skelSpawns.size() > 0)
                        speak("This is you once I am done");
                    Dungeon.observe();
                    break;
                }
            }
        }

        if(damage >= enemy.HP && enemy instanceof Hero)
        {
            if(((ColdGirlAI)state).aiStatus < GOD_MODE)
            {
                speak("Baka..");
                return super.attackProc(enemy, damage);
            }
            else
            {
                Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                enemy.sprite.bloodBurstA( sprite.center(), enemy.HP );
                speak("Are you done yet?!");
                hostile = false;
                if(Level.adjacent(pos, enemy.pos) == true)
                {
                    for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
                        int ofs = Level.NEIGHBOURS8[i];
                        if (pos - enemy.pos  == ofs) {
                            int newPos = enemy.pos - ofs;
                            if ((Level.passable[newPos] || Level.avoid[newPos]) && Actor.findChar(newPos) == null) {

                                Actor.addDelayed( new Pushing( enemy, enemy.pos, newPos ), -1 );

                                enemy.pos = newPos;

                                if (enemy instanceof Mob) {
                                    Dungeon.level.mobPress( (Mob)enemy );
                                } else {
                                    Dungeon.level.press( newPos, enemy );
                                }

                                enemy.sprite.bloodBurstA( sprite.center(), enemy.HP );
                            }
                            break;
                        }
                    }
                }
                ((ColdGirlAI)state).aiStatus = DONE_MODE;
                Buff.affect(Dungeon.hero, Frost.class, 10f);
                return -1;
            }
        }
        else
            return super.attackProc(enemy, damage);
    }

    @Override
    public void damage( int dmg, Object src ) {
        hostile = true;
        if(src instanceof Wand)
        {
            if(((ColdGirlAI)state).aiStatus == PASSIVE)
                ((ColdGirlAI)state).aiStatus = HUNTING;
            speak("Are you mocking me?");
        }
        else
            super.damage(dmg, src);

    }

    @Override
    public int defenseProc( Char enemy, int damage ) {
        hostile = true;
        if(((ColdGirlAI)state).aiStatus == PASSIVE)
            ((ColdGirlAI)state).aiStatus = HUNTING;

        if(enemy instanceof Mob && !(enemy instanceof HiredMerc))
        {

                speak("I have no time for fodder");

            firstTroll = false;
            trollMinion(enemy);
            return -1;
        }

        if(Level.adjacent(pos, enemy.pos) == false)
        {
            if(skillCharge > 0) {
                int tmpPos = pos;
                pos = enemy.pos;
                move(enemy.pos);
                sprite.place(enemy.pos);
                CellEmitter.center(enemy.pos).burst(ShadowParticle.UP, Random.IntRange(1, 2));
                enemy.move(tmpPos);
                enemy.sprite.place(tmpPos);
                CellEmitter.center(tmpPos).burst(ShadowParticle.UP, Random.IntRange(1, 2));
                Sample.INSTANCE.play(Assets.SND_GHOST);
                enemy.damage(damage, enemy);
                Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                enemy.sprite.bloodBurstA( sprite.center(), enemy.HP );
                if (firstSwap) {
                    speak("I have no time for this");
                    heroSpeak("EH?!");
                } else {

                    heroSpeak("...");
                }
                firstSwap = false;
                skillCharge--;
                return -1;
            }
            else
            {
                if(firstComplaint)
                {
                    speak("Erg...");
                    firstComplaint = false;
                }

                return super.defenseProc(enemy, damage);
            }
        }
        else if(firstFetch || ((ColdGirlAI)state).aiStatus == GOD_MODE)
        {
            if(Dungeon.hero.belongings.weapon != null) {

                int throwAt = 0;

                do{
                    throwAt = pos +  3 * Level.NEIGHBOURS8[Random.Int(Level.NEIGHBOURS8.length -1)];
                }
                while(throwAt < 0 || throwAt > Level.passable.length || Level.passable[throwAt] == false);

                final int throwAtFinal = throwAt;
                ((MissileSprite) this.sprite.parent.recycle(MissileSprite.class)).
                        reset(ColdGirl.this.pos, throwAt, Dungeon.hero.belongings.weapon, new Callback() {
                            @Override
                            public void call() {
                                Dungeon.hero.belongings.weapon.detach(Dungeon.hero.belongings.backpack).onThrowColdGirl(throwAtFinal);

                            }
                        });

                firstFetch = false;
                speak("Go Fetch");
                heroSpeak("Wha..");
                spend(1f);
            }
            return -1;
        }
        else
            return super.defenseProc(enemy, damage);
    }

    private void trollMinion(Char minion)
    {
        if(Level.adjacent(pos, minion.pos) == true)
        {
            for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
                int ofs = Level.NEIGHBOURS8[i];
                if (pos - minion.pos  == ofs) {
                    int newPos = minion.pos - ofs;
                    if ((Level.passable[newPos] || Level.avoid[newPos]) && Actor.findChar(newPos) == null) {

                        Actor.addDelayed( new Pushing( minion, minion.pos, newPos ), -1 );

                        enemy.pos = newPos;

                        if (minion instanceof Mob) {
                            Dungeon.level.mobPress( (Mob)minion );
                        } else {
                            Dungeon.level.press( newPos, minion );
                        }

                    }
                    break;
                }
            }
        }
        minion.damage(9999, this);
        Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
        minion.sprite.bloodBurstA( sprite.center(), minion.HP );
    }

	@Override
	public void die( Object cause ) {

        if(((ColdGirlAI)state).aiStatus == HUNTING)
        {
            HT = 100;
            HP = 100;
            defenseSkill = 11;
            ((ColdGirlAI)state).aiStatus = SUPER_HUNTING;
            GameScene.flash( 0x0042ff );
            Camera.main.shake( 3, 0.07f * (20) );
            int[] cells = {
                    pos-1, pos+1, pos-Level.WIDTH, pos+Level.WIDTH,
                    pos-1-Level.WIDTH,
                    pos-1+Level.WIDTH,
                    pos+1-Level.WIDTH,
                    pos+1+Level.WIDTH
            };
            for(int i = 0; i < cells.length; i++) {
                int cell = cells[i];
                Char ch = Actor.findChar(cell);
                if (ch != null && ch != this && ch != Dungeon.hero && !(ch instanceof HiredMerc) && ch.HP > 0) {
                    trollMinion(ch);
                }
            }
            Sample.INSTANCE.play(Assets.SND_BLAST);
            if(Level.adjacent(pos, enemy.pos) == true)
            {
                for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
                    int ofs = Level.NEIGHBOURS8[i];
                    if (pos - enemy.pos  == ofs) {
                        int newPos = enemy.pos - ofs;
                        if ((Level.passable[newPos] || Level.avoid[newPos]) && Actor.findChar(newPos) == null) {

                            Actor.addDelayed( new Pushing( enemy, enemy.pos, newPos ), -1 );

                            enemy.pos = newPos;

                            if (enemy instanceof Mob) {
                                Dungeon.level.mobPress( (Mob)enemy );
                            } else {
                                Dungeon.level.press( newPos, enemy );
                            }

                        }
                        break;
                    }
                }
                speak("Leave me alone!");
                spawnMinions();
                spawnMinions();
                spawnMinions();
            }
            spend(1f);
            return;
        }
        else if(((ColdGirlAI)state).aiStatus == SUPER_HUNTING)
        {
            ((ColdGirlAI)state).aiStatus = DONE_MODE;
            ((ColdGirlSprite)sprite).haloUp();

            HT = 10000;
            HP = 10000;
            defenseSkill = 1000;
            if(Level.adjacent(pos, enemy.pos) == true)
            {
                for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
                    int ofs = Level.NEIGHBOURS8[i];
                    if (pos - enemy.pos  == ofs) {
                        int newPos = enemy.pos - ofs;
                        if ((Level.passable[newPos] || Level.avoid[newPos]) && Actor.findChar(newPos) == null) {

                            Actor.addDelayed( new Pushing( enemy, enemy.pos, newPos ), -1 );

                            enemy.pos = newPos;

                            if (enemy instanceof Mob) {
                                Dungeon.level.mobPress( (Mob)enemy );
                            } else {
                                Dungeon.level.press( newPos, enemy );
                            }

                        }
                        break;
                    }
                }
            }
            discussionProgress = DISCUSSION_DEAD;
            speak("What is your malfunction?!");

            GameScene.flash( 0x0042ff );
            Camera.main.shake( 5, 0.07f * (30) );
            spend(1f);
            return;
        }

        HP = 10000;
        speak("In your dreams fool");

	}

    public void discuss() {
        DiscussionNext(0);
    }

	@Override
	public void notice() {
		super.notice();
        speak("You don't belong here");
	}

	@Override
	public String description() {
		return
			"A young girl somewhat not affected by the cold.";
	}

    private static final String AI_STATE = "aistate";
    private static final String CAME_FROM = "camefrom";
    private static final String CAME_FROM_POS = "cameformpos";
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put(AI_STATE, ((ColdGirlAI)state).aiStatus);
        bundle.put(CAME_FROM, cameFrom);
        bundle.put(CAME_FROM_POS, cameFromPos);

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        ((ColdGirlAI)state).aiStatus = bundle.getInt(AI_STATE);
        cameFrom = bundle.getInt(CAME_FROM);
        cameFromPos = bundle.getInt(CAME_FROM_POS);
    }

    public void speak(String speakText)
    {
        this.sprite.showStatus(CharSprite.NEUTRAL, speakText);
    }

    public void heroSpeak(String speakText)
    {
        Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, speakText);
    }

	private static final HashSet<Class<?>> RESISTANCES = new HashSet<Class<?>>();
	static {

	}

    @Override
    public boolean attack( Char enemy ) {
        boolean visibleFight = Dungeon.visible[pos] || Dungeon.visible[enemy.pos];

        if (hit( this, enemy, false )) {

            if (visibleFight) {
                if (enemy == Dungeon.hero) {
                    GLog.i(Char.class, "enemy_hit", Message.actor(this));
                } else {
                    GLog.i(Char.class, "hit", Message.actor(this), Message.actor(enemy));
                }
            }

            int dmg = damageRoll();

            if(enemy == Dungeon.hero)
            {
                dmg *= Dungeon.currentDifficulty.damageModifier();
                dmg *= Dungeon.hero.heroSkills.passiveA3.incomingDamageModifier();
                dmg -= Dungeon.hero.heroSkills.passiveA3.incomingDamageReduction(dmg);
            }

            int effectiveDamage = Math.max( dmg, 0 );

            effectiveDamage = attackProc( enemy, effectiveDamage );
            effectiveDamage = enemy.defenseProc( this, effectiveDamage );
            if(effectiveDamage < 0)
                return true;
            enemy.damageFrom( effectiveDamage, this, this );

            if (visibleFight) {
                Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
            }

            if (enemy == Dungeon.hero) {
                Dungeon.hero.interrupt();
                if (effectiveDamage > enemy.HT / 4) {
                    Camera.main.shake( GameMath.gate(1, effectiveDamage / (enemy.HT / 4), 5), 0.3f );
                }
            }

            enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
            enemy.sprite.flash();

            if (!enemy.isAlive() && visibleFight) {
                if (enemy == Dungeon.hero) {

                    if (Dungeon.hero.killerGlyph != null) {

                    } else {

                            Dungeon.fail( Utils.format( TXT_DEATH), this );

                        GLog.n("actors.char.kill", Message.actor(this));
                    }

                } else {
                    GLog.i("custom.generated.entry_s_defeated_s_bddf53840b", Message.actor(this), Message.actor(enemy));
                }
            }

            return true;

        } else {

            if (visibleFight) {
                String defense = enemy.defenseVerb();
                enemy.sprite.showStatus( CharSprite.NEUTRAL, defense );
                if (enemy == Dungeon.hero) {
                    GLog.i(Char.class, "enemy_missed", Message.actor(this), enemy.defenseText());
                } else {
                    GLog.i(Char.class, "mob_missed", Message.actor(enemy), enemy.defenseText(), Message.actor(this));
                }

                Sample.INSTANCE.play(Assets.SND_MISS);
            }

            return false;

        }
    }

	@Override
	public HashSet<Class<?>> resistances() {
		return RESISTANCES;
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add( Frost.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Sleep.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Burning.class );
        IMMUNITIES.add( Affection.class );
        IMMUNITIES.add( Poison.class );
	}

	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}

    private void Discussion(Message title, Message message, Message... options)
    {
        GameScene.show(new PersistentWndOptions(title, message, options) {
            @Override
            protected void onSelect(int index) {
                DiscussionNext(index);
            }
        });
    }

    private void DiscussionNext(int index)
    {
        switch (discussionProgress + index)
        {
            case 0: Discussion(Message.key("custom.generated.entry_cold_girl_200cea811f"),
                    Message.key("custom.generated.entry_my_existence_does_not_concern_you_leave_bb22e8e805"),
                    Message.key("custom.generated.entry_how_do_i_leave_ffe46b682b"),
                    Message.key("custom.generated.entry_were_you_raised_like_this_1b52068abd"));
                discussionProgress += DISCUSSION_STEP;
                break;
            case 1:
                speak("Die then");
                hostile = true;
                ((ColdGirlAI)state).aiStatus = HUNTING;
                break;
            case DISCUSSION_STEP:
                sendBack();
                break;
            case DISCUSSION_STEP + 1: Discussion(Message.key("custom.generated.entry_cold_girl_200cea811f"),
                    Message.key("custom.generated.entry_talk_about_my_mother_like_that_again_and_i_you_k_1cc504f5ce"),
                    Message.key("custom.generated.entry_ok_b0a98216a3"));
                discussionProgress += DISCUSSION_STEP;
                break;
            case 2 * DISCUSSION_STEP:
                ((ColdGirlAI)state).aiStatus = HUNTING;
                speak("The dead are always silent");
                hostile = true;
                break;
            case DISCUSSION_DEAD:
                Discussion(Message.key("custom.generated.entry_cold_girl_200cea811f"),
                        Message.key("custom.generated.entry_what_is_wrong_with_you_0bdf4cf102"),
                        Message.key("custom.generated.entry_how_are_you_this_strong_03dab51815"));
                discussionProgress += DISCUSSION_STEP;
                break;
            case DISCUSSION_DEAD + DISCUSSION_STEP:
                Discussion(Message.key("custom.generated.entry_cold_girl_200cea811f"),
                        Message.key("custom.generated.entry_the_rules_cannot_protect_you_from_me_fool_0041fbf481"),
                        Message.key("custom.generated.entry_what_rules_bbd45ef46a"));
                discussionProgress += DISCUSSION_STEP;
                break;
            case DISCUSSION_DEAD + 2 * DISCUSSION_STEP:
                Discussion(Message.key("custom.generated.entry_cold_girl_200cea811f"),
                        Message.key("custom.generated.entry_leave_697269c611"),
                        Message.key("custom.generated.entry_wai_a4a2c06838"));
                discussionProgress += DISCUSSION_STEP;
                break;
            case DISCUSSION_DEAD + 3 * DISCUSSION_STEP:
                sendBack();
                Dungeon.hero.heroSkills.unlockSkill();
                GLog.p("custom.generated.entry_fighting_that_weird_girl_inspired_you_into_learn_543b088243", Dungeon.hero.heroSkills.unlockableSkillName());
                break;
            default:
                discuss();
                discussionProgress = 0;
        }
    }

    public void spawnMinions()
    {
        ArrayList<Integer> spawnPoints = new ArrayList<Integer>();

        for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
            int p = pos + Level.NEIGHBOURS8[i];
            if (Actor.findChar( p ) == null && (Level.passable[p] || Level.avoid[p])) {
                spawnPoints.add( p );
            }
        }

        if (spawnPoints.size() > 0) {
            Slaves slave = new Slaves();
            slave.pos = Random.element( spawnPoints );
            Sample.INSTANCE.play( Assets.SND_GHOST );

            GameScene.add( slave );
            Actor.addDelayed( new Pushing( slave, pos, slave.pos ), -1 );
            slave.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
        }

    }

    private void sendBack()
    {
        InterlevelScene.mode = InterlevelScene.Mode.TELEPORT_BACK;
        Game.switchScene(InterlevelScene.class);
        Dungeon.observe();
    }

    public class ColdGirlSkel extends Skeleton
    {
        {
            HP = HT  = 10;
            defenseSkill = 1;
            EXP = 0;
            state = HUNTING;
        }

        @Override
        public boolean act()
        {
            if(((ColdGirlAI)ColdGirl.this.state).aiStatus != SUPER_HUNTING)
            {
                die(null);
                return true;
            }
            return super.act();
        }

        @Override
        public void die( Object cause ) {
            if(cause != null)
                ColdGirl.this.speak("What a waste of mana");
            super.die(cause);
        }
        @Override
        public String description() {
            return "Me in the future...";

        }
    }

    public class Slaves extends EnslavedSouls {

        {
            name = "enslaved spirit";

            HP = HT = 1;
            defenseSkill = 1;

            EXP = 0;

            state = HUNTING;
        }

        @Override
        public boolean act()
        {
            try {
                if (((ColdGirlAI) ColdGirl.this.state).aiStatus != SUPER_HUNTING) {
                    die(null);
                    return true;
                }
            }
            catch (Exception e){}
            return super.act();
        }

        @Override
        public int attackSkill( Char target ) {
            return 5;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange( 3, 8 );
        }

        @Override
        public int dr() {
            return 8;
        }

        @Override
        public void die( Object cause ) {
            if(cause != null)
                ColdGirl.this.speak("Useless!");
            super.die(cause);
        }
        @Override
        public String description() {
            return "A spirit in agony";

        }
    }

    public class ColdGirlAI implements AiState {

        public static final String TAG	= "COLD_GIRL";
        public int aiStatus = PASSIVE;
        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            if(aiStatus == PASSIVE || aiStatus == DONE_MODE) {
                enemySeen = false;
                spend(TICK);
                sprite.idle();
                target = -1;
                return true;
            }
            else if(aiStatus == HUNTING)
            {
                enemySeen = enemyInFOV;
                if (enemyInFOV && canAttack( enemy )) {

                    return doAttack( enemy );

                } else {

                    if (enemyInFOV) {
                        target = enemy.pos;
                    }

                    int oldPos = pos;
                    if (target != -1 && getCloser( target )) {

                        spend( 1 / speed() );
                        return moveSprite( oldPos,  pos );

                    } else {

                        spend( TICK );

                        sprite.idle();
                        return true;
                    }
                }
            }

            else if(aiStatus == SUPER_HUNTING)
            {
                enemySeen = enemyInFOV;
                if (enemyInFOV && canAttack( enemy )) {

                    return doAttack( enemy );

                } else {

                    if (enemyInFOV) {
                        target = enemy.pos;
                    }

                    int oldPos = pos;
                    if (target != -1 && getCloser( target )) {

                        spend( 1 / speed() );
                        return moveSprite( oldPos,  pos );

                    } else {

                        spend( TICK );

                        sprite.idle();
                        return true;
                    }
                }
            }
            else if(aiStatus == GOD_MODE)
            {
                enemySeen = enemyInFOV;
                if (enemyInFOV && canAttack( enemy )) {

                    return doAttack( enemy );

                } else {

                    if (enemyInFOV) {
                        target = enemy.pos;
                    }

                    int oldPos = pos;
                    if (target != -1 && getCloser( target )) {

                        spend( 1 / speed() );
                        return moveSprite( oldPos,  pos );

                    } else {

                        spend( TICK );

                        sprite.idle();
                        return true;
                    }
                }
            }
            spend( TICK );
            return true;
        }

        @Override
        public String status() {
            if(aiStatus == PASSIVE)
                return Utils.format("The %s seems passive.\n You can tell she is cold but she shows no physical signs of it.", name);
            else if(aiStatus == HUNTING)
                return Utils.format("The %s seems upset.\n She may be young but she looks dangerous.", name);
            else if(aiStatus == SUPER_HUNTING)
                return Utils.format("The %s seems very dangerous.\n Something is not right about her.", name);
            else
                return Utils.format("The %s seems non-human.\n Taunting her was a bad idea", name);
        }
    }
}
